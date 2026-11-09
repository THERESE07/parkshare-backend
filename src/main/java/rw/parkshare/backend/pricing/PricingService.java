package rw.parkshare.backend.pricing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.Rwf;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.parking.entity.PricingRule;
import rw.parkshare.backend.parking.entity.SpecialRate;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.PricingRuleRepository;
import rw.parkshare.backend.parking.repository.SpecialRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Calculates parking prices on the server so clients can never choose
 * their own amount.
 *
 * How a price is built:
 * 1. If the operator has a FLAT rule matching the stay's duration, that is the price.
 * 2. Otherwise time is billed in blocks (default 30 min). Each block uses the
 *    special rate active at that moment in Kigali time (event, holiday,
 *    weekend, peak, off-peak), or else the lot's HOURLY rule, or else the
 *    platform default (300 RWF/h).
 * 3. Each 24-hour period is capped at the DAILY rule (or the 3,000 RWF default).
 * 4. A minimum charge applies (200 RWF), and the total is rounded to 10 RWF.
 * 5. VAT (18%) is included only for operators verified as VAT-registered ({@link VatPolicy}).
 * 6. ParkShare's commission is a % of the amount before VAT; the mobile-money
 *    fee is a % of the total. Both come out of the operator's share; the driver
 *    pays only the total.
 */
@Service
@Transactional(readOnly = true)
public class PricingService {

    private static final List<String> SPECIAL_RATE_PRIORITY =
            List.of("EVENT", "HOLIDAY", "WEEKEND", "PEAK", "OFF_PEAK");
    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);

    private final ParkingLotRepository parkingLotRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final SpecialRateRepository specialRateRepository;
    private final VatPolicy vatPolicy;
    private final ParkShareProperties properties;

    public PricingService(
            ParkingLotRepository parkingLotRepository,
            PricingRuleRepository pricingRuleRepository,
            SpecialRateRepository specialRateRepository,
            VatPolicy vatPolicy,
            ParkShareProperties properties
    ) {
        this.parkingLotRepository = parkingLotRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.specialRateRepository = specialRateRepository;
        this.vatPolicy = vatPolicy;
        this.properties = properties;
    }

    /** Price for a reservation (full breakdown; show drivers only {@link PriceQuote#toDriverQuote()}). */
    public PriceQuote quote(Long parkingLotId, OffsetDateTime start, OffsetDateTime end) {
        return calculate(parkingLotId, start, end, true, null, null);
    }

    /** Extra charge for staying past the reservation end (no minimum charge). */
    public PriceQuote quoteOverstay(Long parkingLotId, OffsetDateTime reservationEnd, OffsetDateTime checkedOutAt) {
        return calculate(parkingLotId, reservationEnd, checkedOutAt, false, null, null);
    }

    /** Mobile-money fee for an actual payment (0 for cash). */
    public BigDecimal mobileMoneyFee(BigDecimal amount, String method) {
        return Rwf.whole(amount.multiply(properties.getPayments().feeRate(method)));
    }

    /**
     * What the operator earns for typical stays, using either the lot's current
     * rules or proposed rates (nothing is saved).
     */
    public OperatorPreview operatorPreview(Long parkingLotId, BigDecimal proposedHourly,
                                           BigDecimal proposedDailyCap, String paymentMethod) {
        if (!parkingLotRepository.existsById(parkingLotId)) {
            throw ApiException.notFound("Parking lot not found: " + parkingLotId);
        }
        RateOverride override = null;
        if (proposedHourly != null || proposedDailyCap != null) {
            validateOperatorRate("HOURLY", proposedHourly);
            validateOperatorRate("DAILY", proposedDailyCap);
            override = new RateOverride(proposedHourly, proposedDailyCap);
        }

        // A normal weekday morning, so examples are not skewed by weekend or peak rates.
        ZonedDateTime base = ZonedDateTime.now(properties.zoneId())
                .with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime start = base.toOffsetDateTime();

        List<OperatorPreview.Example> examples = new ArrayList<>();
        Map<String, Integer> stays = new LinkedHashMap<>();
        stays.put("1 hour", 60);
        stays.put("2 hours", 120);
        stays.put("4 hours", 240);
        stays.put("8 hours", 480);
        stays.put("Full day (24h)", 1440);
        for (Map.Entry<String, Integer> stay : stays.entrySet()) {
            PriceQuote quote = calculate(parkingLotId, start, start.plusMinutes(stay.getValue()), true, override, paymentMethod);
            examples.add(OperatorPreview.Example.from(stay.getKey(), quote));
        }

        boolean vat = vatPolicy.appliesTo(parkingLotId);
        ParkShareProperties.Pricing cfg = properties.getPricing();
        List<String> notes = new ArrayList<>();
        notes.add("The driver pays only 'driverPays'. Commission and mobile-money fees come out of your share.");
        notes.add(vat
                ? "You are verified as VAT-registered: 18% VAT is inside the price. Declare 'vatToDeclareToRra' to RRA."
                : "No VAT is charged because you are not verified as VAT-registered. If you are registered with RRA, "
                  + "add your TIN and VAT status to your operator verification.");
        notes.add("Commission is " + percent(cfg.getPlatformCommissionRate()) + " of the price before VAT.");
        notes.add(paymentMethod == null
                ? "Mobile-money fee shown at the higher of the MTN/Airtel rates; cash payments have no fee."
                : "Mobile-money fee shown for " + paymentMethod + ".");
        notes.add("Minimum charge " + cfg.getMinimumCharge().toBigInteger() + " RWF; amounts are rounded to "
                + cfg.getRoundingStep().toBigInteger() + " RWF.");

        BigDecimal feeRate = properties.getPayments().feeRate(paymentMethod);
        return new OperatorPreview(parkingLotId, override == null ? "Your current prices" : "Proposed prices (not saved)",
                vat, vat ? "VAT-registered (verified)" : "Not VAT-registered",
                cfg.getPlatformCommissionRate(), paymentMethod == null ? "MTN_MOMO or AIRTEL_MONEY" : paymentMethod,
                feeRate, examples, notes, Rwf.CURRENCY);
    }

    /** Keeps operator prices within the platform's affordability ceilings. */
    public void validateOperatorRate(String pricingType, BigDecimal amount) {
        if (amount == null) {
            return;
        }
        if (amount.signum() < 0) {
            throw ApiException.badRequest("Rates cannot be negative");
        }
        ParkShareProperties.Pricing pricing = properties.getPricing();
        if ("HOURLY".equalsIgnoreCase(pricingType) && amount.compareTo(pricing.getMaxHourlyRate()) > 0) {
            throw ApiException.badRequest("Hourly rate cannot exceed "
                    + pricing.getMaxHourlyRate().toPlainString() + " RWF");
        }
        if ("DAILY".equalsIgnoreCase(pricingType) && amount.compareTo(pricing.getMaxDailyRate()) > 0) {
            throw ApiException.badRequest("Daily rate cannot exceed "
                    + pricing.getMaxDailyRate().toPlainString() + " RWF");
        }
    }

    /** Proposed rates used by the operator preview instead of the saved rules. */
    record RateOverride(BigDecimal hourly, BigDecimal dailyCap) {
    }

    PriceQuote calculate(Long parkingLotId, OffsetDateTime start, OffsetDateTime end, boolean applyMinimum,
                         RateOverride override, String paymentMethod) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw ApiException.badRequest("End time must be after start time");
        }
        if (!parkingLotRepository.existsById(parkingLotId)) {
            throw ApiException.notFound("Parking lot not found: " + parkingLotId);
        }

        ParkShareProperties.Pricing cfg = properties.getPricing();
        long durationMinutes = (long) Math.ceil(Duration.between(start, end).getSeconds() / 60.0);
        boolean vat = vatPolicy.appliesTo(parkingLotId);

        List<PricingRule> rules = override != null ? List.of()
                : pricingRuleRepository.findByParkingLot_IdAndActiveTrue(parkingLotId)
                .stream()
                .filter(rule -> isEffective(rule, start))
                .filter(rule -> fitsDuration(rule, durationMinutes))
                .toList();

        Optional<PricingRule> flat = rules.stream()
                .filter(rule -> "FLAT".equalsIgnoreCase(rule.getPricingType()))
                .min(Comparator.comparing(PricingRule::getAmount));

        if (flat.isPresent()) {
            BigDecimal amount = flat.get().getAmount();
            List<PriceQuote.Line> lines = List.of(new PriceQuote.Line(
                    "Flat rate: " + flat.get().getName(), durationMinutes, null, amount));
            return finish(parkingLotId, start, end, durationMinutes, durationMinutes,
                    "FLAT", lines, BigDecimal.ZERO, amount, applyMinimum, vat, paymentMethod);
        }

        BigDecimal hourly = override != null && override.hourly() != null ? override.hourly()
                : latest(rules, "HOURLY").map(PricingRule::getAmount).orElse(cfg.getDefaultHourlyRate());
        BigDecimal dailyCap = override != null && override.dailyCap() != null ? override.dailyCap()
                : latest(rules, "DAILY").map(PricingRule::getAmount).orElse(cfg.getDefaultDailyCap());

        List<SpecialRate> specials = override != null ? List.of()
                : specialRateRepository.findByParkingLot_IdAndActiveTrue(parkingLotId);
        ZoneId zone = properties.zoneId();
        int increment = Math.max(1, cfg.getBillingIncrementMinutes());
        long blocks = (durationMinutes + increment - 1) / increment;
        BigDecimal blockFraction = BigDecimal.valueOf(increment).divide(SIXTY, 10, RoundingMode.HALF_UP);

        Map<String, PriceQuote.Line> lines = new LinkedHashMap<>();
        Map<Long, BigDecimal> perDay = new LinkedHashMap<>();

        for (long i = 0; i < blocks; i++) {
            OffsetDateTime blockStart = start.plusMinutes(i * increment);
            Optional<SpecialRate> special = applicableSpecialRate(specials, blockStart, zone);
            BigDecimal rate = special.map(SpecialRate::getAmount).orElse(hourly);
            String label = special
                    .map(s -> label(s.getRateType()) + ": " + s.getName())
                    .orElse(hourlyLabel(rules));
            BigDecimal blockPrice = rate.multiply(blockFraction);

            lines.merge(label + "|" + rate.toPlainString(),
                    new PriceQuote.Line(label, increment, rate, blockPrice),
                    (a, b) -> new PriceQuote.Line(a.label(), a.minutes() + b.minutes(),
                            a.hourlyRate(), a.amount().add(b.amount())));

            long dayIndex = (i * increment) / (24 * 60);
            perDay.merge(dayIndex, blockPrice, BigDecimal::add);
        }

        BigDecimal uncapped = perDay.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal capped = perDay.values().stream()
                .map(dayTotal -> dayTotal.min(dailyCap))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal capDiscount = uncapped.subtract(capped);

        List<PriceQuote.Line> roundedLines = lines.values().stream()
                .map(line -> new PriceQuote.Line(line.label(), line.minutes(), line.hourlyRate(),
                        Rwf.whole(line.amount())))
                .toList();

        return finish(parkingLotId, start, end, durationMinutes, blocks * increment,
                "TIME_BASED", roundedLines, Rwf.whole(capDiscount), capped, applyMinimum, vat, paymentMethod);
    }

    private PriceQuote finish(
            Long parkingLotId, OffsetDateTime start, OffsetDateTime end,
            long durationMinutes, long billedMinutes, String method,
            List<PriceQuote.Line> lines, BigDecimal capDiscount,
            BigDecimal rawAmount, boolean applyMinimum, boolean vatApplies, String paymentMethod
    ) {
        ParkShareProperties.Pricing cfg = properties.getPricing();
        ParkShareProperties.Tax tax = properties.getTax();

        BigDecimal amount = applyMinimum ? rawAmount.max(cfg.getMinimumCharge()) : rawAmount;
        BigDecimal vatRate = tax.getVatRate();

        BigDecimal total;
        BigDecimal vat;
        if (!vatApplies) {
            total = Rwf.round(amount, cfg.getRoundingStep());
            vat = BigDecimal.ZERO.setScale(2);
        } else if (tax.isPricesIncludeVat()) {
            total = Rwf.round(amount, cfg.getRoundingStep());
            vat = Rwf.vatFromInclusive(total, vatRate);
        } else {
            BigDecimal net = Rwf.whole(amount);
            vat = Rwf.whole(net.multiply(vatRate));
            total = Rwf.round(net.add(vat), cfg.getRoundingStep());
        }
        BigDecimal excludingVat = total.subtract(vat);

        BigDecimal commissionRate = cfg.getPlatformCommissionRate();
        BigDecimal commission = Rwf.whole(excludingVat.multiply(commissionRate));
        BigDecimal feeRate = properties.getPayments().feeRate(paymentMethod);
        BigDecimal fee = Rwf.whole(total.multiply(feeRate));
        BigDecimal operatorReceives = total.subtract(commission).subtract(fee);

        return new PriceQuote(
                parkingLotId, start, end, durationMinutes, billedMinutes, method,
                new ArrayList<>(lines), capDiscount, total,
                vatApplies, vatRate, vat, excludingVat,
                commissionRate, commission, paymentMethod, feeRate, fee,
                operatorReceives, vat, operatorReceives.subtract(vat), Rwf.CURRENCY
        );
    }

    private static String percent(BigDecimal rate) {
        return rate.movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }

    private static boolean isEffective(PricingRule rule, OffsetDateTime at) {
        boolean started = rule.getEffectiveFrom() == null || !rule.getEffectiveFrom().isAfter(at);
        boolean notEnded = rule.getEffectiveUntil() == null || rule.getEffectiveUntil().isAfter(at);
        return started && notEnded;
    }

    private static boolean fitsDuration(PricingRule rule, long minutes) {
        boolean aboveMin = rule.getMinimumDurationMinutes() == null || minutes >= rule.getMinimumDurationMinutes();
        boolean belowMax = rule.getMaximumDurationMinutes() == null || minutes <= rule.getMaximumDurationMinutes();
        return aboveMin && belowMax;
    }

    private static Optional<PricingRule> latest(List<PricingRule> rules, String type) {
        return rules.stream()
                .filter(rule -> type.equalsIgnoreCase(rule.getPricingType()))
                .max(Comparator.comparing(
                        PricingRule::getEffectiveFrom,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    private static String hourlyLabel(List<PricingRule> rules) {
        return latest(rules, "HOURLY").map(rule -> "Standard rate: " + rule.getName())
                .orElse("Standard rate");
    }

    static Optional<SpecialRate> applicableSpecialRate(List<SpecialRate> specials, OffsetDateTime at, ZoneId zone) {
        ZonedDateTime local = at.atZoneSameInstant(zone);
        return specials.stream()
                .filter(SpecialRate::isActive)
                .filter(rate -> rate.getStartDate() == null || !rate.getStartDate().isAfter(at))
                .filter(rate -> rate.getEndDate() == null || rate.getEndDate().isAfter(at))
                .filter(rate -> !"WEEKEND".equalsIgnoreCase(rate.getRateType())
                        || local.getDayOfWeek() == DayOfWeek.SATURDAY
                        || local.getDayOfWeek() == DayOfWeek.SUNDAY)
                .filter(rate -> inTimeWindow(rate.getStartTime(), rate.getEndTime(), local.toLocalTime()))
                .min(Comparator
                        .comparingInt((SpecialRate rate) -> priority(rate.getRateType()))
                        .thenComparing(SpecialRate::getAmount));
    }

    private static boolean inTimeWindow(LocalTime from, LocalTime to, LocalTime at) {
        if (from == null || to == null) {
            return true;
        }
        if (from.isBefore(to)) {
            return !at.isBefore(from) && at.isBefore(to);
        }
        // Overnight window, e.g. 18:00 -> 06:00
        return !at.isBefore(from) || at.isBefore(to);
    }

    private static int priority(String type) {
        int index = SPECIAL_RATE_PRIORITY.indexOf(type == null ? "" : type.toUpperCase());
        return index < 0 ? SPECIAL_RATE_PRIORITY.size() : index;
    }

    private static String label(String type) {
        return switch (type == null ? "" : type.toUpperCase()) {
            case "EVENT" -> "Event rate";
            case "HOLIDAY" -> "Public holiday rate";
            case "WEEKEND" -> "Weekend rate";
            case "PEAK" -> "Peak rate";
            case "OFF_PEAK" -> "Off-peak rate";
            default -> "Special rate";
        };
    }
}

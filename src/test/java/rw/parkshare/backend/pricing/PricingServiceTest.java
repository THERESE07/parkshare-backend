package rw.parkshare.backend.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.parking.entity.PricingRule;
import rw.parkshare.backend.parking.entity.SpecialRate;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.PricingRuleRepository;
import rw.parkshare.backend.parking.repository.SpecialRateRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pricing with the default Kigali settings:
 * 300 RWF/h, billed per 30 min, 3,000 RWF daily cap, 200 RWF minimum,
 * rounded to 10 RWF, 18% VAT included, 10% platform commission.
 */
class PricingServiceTest {

    private static final long LOT = 1L;
    /** Kigali is UTC+2 all year. */
    private static final ZoneOffset KIGALI = ZoneOffset.ofHours(2);
    /** Wednesday 11 Nov 2026, 10:00 in Kigali. */
    private static final OffsetDateTime WEDNESDAY_10AM = OffsetDateTime.of(2026, 11, 11, 10, 0, 0, 0, KIGALI);
    /** Saturday 14 Nov 2026, 10:00 in Kigali. */
    private static final OffsetDateTime SATURDAY_10AM = OffsetDateTime.of(2026, 11, 14, 10, 0, 0, 0, KIGALI);

    private final List<PricingRule> rules = new ArrayList<>();
    private final List<SpecialRate> specials = new ArrayList<>();
    private PricingService pricing;

    @BeforeEach
    void setUp() {
        ParkingLotRepository lots = mock(ParkingLotRepository.class);
        PricingRuleRepository ruleRepository = mock(PricingRuleRepository.class);
        SpecialRateRepository specialRepository = mock(SpecialRateRepository.class);
        when(lots.existsById(anyLong())).thenReturn(true);
        when(ruleRepository.findByParkingLot_IdAndActiveTrue(LOT)).thenReturn(rules);
        when(specialRepository.findByParkingLot_IdAndActiveTrue(LOT)).thenReturn(specials);
        pricing = new PricingService(lots, ruleRepository, specialRepository, new ParkShareProperties());
    }

    @Test
    void twoHoursAtDefaultRateWithVatIncluded() {
        PriceQuote quote = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(2));

        assertThat(quote.total()).isEqualByComparingTo("600");
        // 600 x 18/118 = 91.53 -> 92 RWF VAT inside the price
        assertThat(quote.vatAmount()).isEqualByComparingTo("92");
        assertThat(quote.amountExcludingVat()).isEqualByComparingTo("508");
        assertThat(quote.vatInclusive()).isTrue();
        assertThat(quote.platformCommission()).isEqualByComparingTo("60");
        assertThat(quote.operatorPayout()).isEqualByComparingTo("540");
        assertThat(quote.currency()).isEqualTo("RWF");
    }

    @Test
    void eachStartedHalfHourIsBilled() {
        PriceQuote quote = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusMinutes(45));

        assertThat(quote.billedMinutes()).isEqualTo(60);
        assertThat(quote.total()).isEqualByComparingTo("300");
    }

    @Test
    void minimumChargeAppliesToShortStays() {
        PriceQuote quote = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusMinutes(20));

        assertThat(quote.total()).isEqualByComparingTo("200");
    }

    @Test
    void dailyCapLimitsLongStays() {
        PriceQuote fullDay = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(24));
        PriceQuote thirtyHours = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(30));

        assertThat(fullDay.total()).isEqualByComparingTo("3000");
        assertThat(fullDay.dailyCapDiscount()).isEqualByComparingTo("4200");
        // First 24h capped at 3,000 + 6h x 300
        assertThat(thirtyHours.total()).isEqualByComparingTo("4800");
    }

    @Test
    void operatorHourlyRuleReplacesDefault() {
        rules.add(rule("Standard", "HOURLY", "500"));

        PriceQuote quote = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(1));

        assertThat(quote.total()).isEqualByComparingTo("500");
    }

    @Test
    void flatRuleWinsWhenDurationFits() {
        PricingRule flat = rule("Event night", "FLAT", "1000");
        flat.setMinimumDurationMinutes(60);
        flat.setMaximumDurationMinutes(6 * 60);
        rules.add(flat);

        assertThat(pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(4)).total()).isEqualByComparingTo("1000");
        // Outside the flat rule's range, time-based pricing applies again
        assertThat(pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(8)).total()).isEqualByComparingTo("2400");
    }

    @Test
    void weekendRateOnlyAppliesOnSaturdayAndSunday() {
        specials.add(special("Weekend", "WEEKEND", "200", null, null));

        assertThat(pricing.quote(LOT, SATURDAY_10AM, SATURDAY_10AM.plusHours(2)).total()).isEqualByComparingTo("400");
        assertThat(pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(2)).total()).isEqualByComparingTo("600");
    }

    @Test
    void peakWindowUsesKigaliLocalTime() {
        specials.add(special("Morning rush", "PEAK", "600", LocalTime.of(7, 0), LocalTime.of(9, 0)));
        OffsetDateTime eightAm = WEDNESDAY_10AM.withHour(8);

        PriceQuote quote = pricing.quote(LOT, eightAm, eightAm.plusHours(2));

        // 08:00-09:00 peak at 600, 09:00-10:00 standard at 300
        assertThat(quote.total()).isEqualByComparingTo("900");
        assertThat(quote.lines()).hasSize(2);

        // Same instant expressed in UTC gives the same price
        PriceQuote utc = pricing.quote(LOT, eightAm.withOffsetSameInstant(ZoneOffset.UTC),
                eightAm.plusHours(2).withOffsetSameInstant(ZoneOffset.UTC));
        assertThat(utc.total()).isEqualByComparingTo("900");
    }

    @Test
    void overstayHasNoMinimumCharge() {
        PriceQuote overstay = pricing.quoteOverstay(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusMinutes(12));

        assertThat(overstay.total()).isEqualByComparingTo("150");
    }

    @Test
    void operatorRatesAreCappedForAffordability() {
        assertThatThrownBy(() -> pricing.validateOperatorRate("HOURLY", new BigDecimal("2500")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("2000");
        assertThatThrownBy(() -> pricing.validateOperatorRate("DAILY", new BigDecimal("25000")))
                .isInstanceOf(ApiException.class);
        pricing.validateOperatorRate("HOURLY", new BigDecimal("2000"));
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.minusHours(1)))
                .isInstanceOf(ApiException.class);
    }

    private static PricingRule rule(String name, String type, String amount) {
        PricingRule rule = new PricingRule();
        rule.setName(name);
        rule.setPricingType(type);
        rule.setAmount(new BigDecimal(amount));
        rule.setCurrency("RWF");
        rule.setActive(true);
        rule.setEffectiveFrom(WEDNESDAY_10AM.minusYears(1));
        return rule;
    }

    private static SpecialRate special(String name, String type, String amount, LocalTime from, LocalTime to) {
        SpecialRate rate = new SpecialRate();
        rate.setName(name);
        rate.setRateType(type);
        rate.setAmount(new BigDecimal(amount));
        rate.setCurrency("RWF");
        rate.setStartTime(from);
        rate.setEndTime(to);
        rate.setActive(true);
        return rate;
    }
}

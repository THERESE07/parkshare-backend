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
    private VatPolicy vatPolicy;

    @BeforeEach
    void setUp() {
        ParkingLotRepository lots = mock(ParkingLotRepository.class);
        PricingRuleRepository ruleRepository = mock(PricingRuleRepository.class);
        SpecialRateRepository specialRepository = mock(SpecialRateRepository.class);
        when(lots.existsById(anyLong())).thenReturn(true);
        when(ruleRepository.findByParkingLot_IdAndActiveTrue(LOT)).thenReturn(rules);
        when(specialRepository.findByParkingLot_IdAndActiveTrue(LOT)).thenReturn(specials);
        vatPolicy = mock(VatPolicy.class);
        when(vatPolicy.appliesTo(LOT)).thenReturn(true);
        pricing = new PricingService(lots, ruleRepository, specialRepository, vatPolicy, new ParkShareProperties());
    }

    @Test
    void twoHoursAtDefaultRateWithVatIncluded() {
        PriceQuote quote = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(2));

        assertThat(quote.total()).isEqualByComparingTo("600");
        // 600 x 18/118 = 91.53 -> 92 RWF VAT inside the price
        assertThat(quote.vatAmount()).isEqualByComparingTo("92");
        assertThat(quote.amountExcludingVat()).isEqualByComparingTo("508");
        assertThat(quote.vatApplied()).isTrue();
        // Commission is 10% of the price before VAT: 508 x 10% = 51
        assertThat(quote.platformCommission()).isEqualByComparingTo("51");
        // Mobile-money fee estimated at the higher rate (Airtel 1.8%): 600 x 1.8% = 11
        assertThat(quote.mobileMoneyFee()).isEqualByComparingTo("11");
        // Operator is paid 600 - 51 - 11 = 538, of which 92 is VAT owed to RRA
        assertThat(quote.operatorReceives()).isEqualByComparingTo("538");
        assertThat(quote.operatorVatToRemit()).isEqualByComparingTo("92");
        assertThat(quote.operatorNet()).isEqualByComparingTo("446");
        assertThat(quote.currency()).isEqualTo("RWF");
    }

    @Test
    void operatorsNotVerifiedForVatChargeNoVat() {
        when(vatPolicy.appliesTo(LOT)).thenReturn(false);

        PriceQuote quote = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(2));

        assertThat(quote.total()).as("driver price unchanged").isEqualByComparingTo("600");
        assertThat(quote.vatApplied()).isFalse();
        assertThat(quote.vatAmount()).isEqualByComparingTo("0");
        assertThat(quote.platformCommission()).isEqualByComparingTo("60");
        assertThat(quote.operatorReceives()).isEqualByComparingTo("529");
        assertThat(quote.operatorNet()).isEqualByComparingTo("529");
        assertThat(quote.toDriverQuote().note()).contains("No VAT");
    }

    @Test
    void feeDependsOnPaymentMethod() {
        PriceQuote mtn = pricing.calculate(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(2), true, null, "MTN_MOMO");
        PriceQuote cash = pricing.calculate(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(2), true, null, "CASH");

        assertThat(mtn.mobileMoneyFee()).isEqualByComparingTo("9"); // 600 x 1.5%
        assertThat(cash.mobileMoneyFee()).isEqualByComparingTo("0");
        assertThat(pricing.mobileMoneyFee(new BigDecimal("800"), "MTN_MOMO")).isEqualByComparingTo("12");
    }

    @Test
    void driverQuoteHidesBusinessDetails() {
        PriceQuote.DriverQuote driver = pricing.quote(LOT, WEDNESDAY_10AM, WEDNESDAY_10AM.plusHours(2)).toDriverQuote();

        assertThat(driver.total()).isEqualByComparingTo("600");
        assertThat(driver.vatAmount()).isEqualByComparingTo("92");
        assertThat(PriceQuote.DriverQuote.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("platformCommission", "mobileMoneyFee", "operatorReceives", "operatorNet");
    }

    @Test
    void operatorPreviewShowsEarningsForProposedRates() {
        OperatorPreview preview = pricing.operatorPreview(LOT, new BigDecimal("400"), null, "MTN_MOMO");

        assertThat(preview.basedOn()).contains("Proposed");
        OperatorPreview.Example twoHours = preview.examples().get(1);
        assertThat(twoHours.driverPays()).isEqualByComparingTo("800");
        assertThat(twoHours.vatInPrice()).isEqualByComparingTo("122");
        assertThat(twoHours.platformCommission()).isEqualByComparingTo("68"); // 678 x 10%
        assertThat(twoHours.mobileMoneyFee()).isEqualByComparingTo("12");     // 800 x 1.5%
        assertThat(twoHours.youReceive()).isEqualByComparingTo("720");
        assertThat(twoHours.yourNetIncome()).isEqualByComparingTo("598");
        assertThat(preview.examples().get(4).driverPays()).as("full day capped").isEqualByComparingTo("3000");

        assertThatThrownBy(() -> pricing.operatorPreview(LOT, new BigDecimal("5000"), null, null))
                .isInstanceOf(ApiException.class);
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

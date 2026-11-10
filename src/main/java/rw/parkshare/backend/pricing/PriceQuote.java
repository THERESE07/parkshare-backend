package rw.parkshare.backend.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Full internal price breakdown. Drivers only ever see {@link #toDriverQuote()};
 * commission, fees and payouts are for operators and admins.
 *
 * Money flow for one booking:
 *   total (driver pays)
 *     - platformCommission  (ParkShare, % of the amount before VAT)
 *     - mobileMoneyFee      (MTN/Airtel merchant fee, % of total)
 *     = operatorReceives    (paid out to the operator)
 *     - operatorVatToRemit  (VAT the operator declares to RRA; 0 if not VAT-registered)
 *     = operatorNet         (operator's actual income)
 */
public record PriceQuote(
        Long parkingLotId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        long durationMinutes,
        long billedMinutes,
        String pricingMethod,
        List<Line> lines,
        BigDecimal dailyCapDiscount,
        BigDecimal total,
        boolean vatApplied,
        BigDecimal vatRate,
        BigDecimal vatAmount,
        BigDecimal amountExcludingVat,
        BigDecimal platformCommissionRate,
        BigDecimal platformCommission,
        String paymentMethod,
        BigDecimal mobileMoneyFeeRate,
        BigDecimal mobileMoneyFee,
        BigDecimal operatorReceives,
        BigDecimal operatorVatToRemit,
        BigDecimal operatorNet,
        String currency
) {

    public record Line(String label, long minutes, BigDecimal hourlyRate, BigDecimal amount) {
    }

    /** What a driver is shown: the price and its VAT, nothing about ParkShare's or the operator's share. */
    public DriverQuote toDriverQuote() {
        String note = vatApplied
                ? "Price includes VAT at " + vatRate.movePointRight(2).stripTrailingZeros().toPlainString() + "%. No extra fees."
                : "No VAT charged (operator is not VAT-registered). No extra fees.";
        return new DriverQuote(parkingLotId, startTime, endTime, durationMinutes, billedMinutes, pricingMethod,
                lines, dailyCapDiscount, total, vatApplied, vatApplied ? vatRate : BigDecimal.ZERO,
                vatAmount, amountExcludingVat, currency, note);
    }

    public record DriverQuote(
            Long parkingLotId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            long durationMinutes,
            long billedMinutes,
            String pricingMethod,
            List<Line> lines,
            BigDecimal dailyCapDiscount,
            BigDecimal total,
            boolean vatIncluded,
            BigDecimal vatRate,
            BigDecimal vatAmount,
            BigDecimal amountExcludingVat,
            String currency,
            String note
    ) {
    }
}

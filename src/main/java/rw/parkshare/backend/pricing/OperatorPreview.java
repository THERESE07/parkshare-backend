package rw.parkshare.backend.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * "If I charge this, what does the driver pay and what do I earn?"
 * Shown to the lot's operator before (or after) setting prices.
 */
public record OperatorPreview(
        Long parkingLotId,
        String basedOn,
        boolean vatApplied,
        String vatStatus,
        BigDecimal platformCommissionRate,
        String paymentMethod,
        BigDecimal mobileMoneyFeeRate,
        List<Example> examples,
        List<String> notes,
        String currency
) {

    public record Example(
            String stay,
            long minutes,
            BigDecimal driverPays,
            BigDecimal vatInPrice,
            BigDecimal platformCommission,
            BigDecimal mobileMoneyFee,
            BigDecimal youReceive,
            BigDecimal vatToDeclareToRra,
            BigDecimal yourNetIncome
    ) {
        static Example from(String stay, PriceQuote quote) {
            return new Example(stay, quote.durationMinutes(), quote.total(), quote.vatAmount(),
                    quote.platformCommission(), quote.mobileMoneyFee(), quote.operatorReceives(),
                    quote.operatorVatToRemit(), quote.operatorNet());
        }
    }
}

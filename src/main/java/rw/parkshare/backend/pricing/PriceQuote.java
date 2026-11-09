package rw.parkshare.backend.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * A server-calculated price. {@code total} is what the driver pays and
 * already includes VAT when prices are VAT-inclusive (the Rwandan norm).
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
        BigDecimal amountExcludingVat,
        BigDecimal vatRate,
        BigDecimal vatAmount,
        boolean vatInclusive,
        BigDecimal platformCommission,
        BigDecimal operatorPayout,
        String currency
) {

    public record Line(String label, long minutes, BigDecimal hourlyRate, BigDecimal amount) {
    }
}

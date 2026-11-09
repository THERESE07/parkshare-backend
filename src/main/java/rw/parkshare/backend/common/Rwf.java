package rw.parkshare.backend.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Helpers for Rwandan franc amounts. RWF is used without decimals.
 */
public final class Rwf {

    public static final String CURRENCY = "RWF";

    private Rwf() {
    }

    /** Round to the nearest multiple of {@code step} (half up), never below zero. */
    public static BigDecimal round(BigDecimal amount, BigDecimal step) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (step == null || step.signum() <= 0) {
            step = BigDecimal.ONE;
        }
        BigDecimal rounded = amount.divide(step, 0, RoundingMode.HALF_UP).multiply(step);
        return rounded.max(BigDecimal.ZERO).setScale(2, RoundingMode.UNNECESSARY);
    }

    /** Round to a whole franc. */
    public static BigDecimal whole(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).setScale(2, RoundingMode.UNNECESSARY);
    }

    /** VAT portion contained in a VAT-inclusive amount: gross * rate / (1 + rate). */
    public static BigDecimal vatFromInclusive(BigDecimal gross, BigDecimal rate) {
        BigDecimal vat = gross.multiply(rate).divide(BigDecimal.ONE.add(rate), 0, RoundingMode.HALF_UP);
        return vat.setScale(2, RoundingMode.UNNECESSARY);
    }
}

package rw.parkshare.backend.payment.event;

import java.math.BigDecimal;

/** Published when a mobile-money prompt has been sent; the sandbox simulator answers it. */
public record PaymentInitiatedEvent(String transactionReference, String phone, BigDecimal amount, String method) {
}

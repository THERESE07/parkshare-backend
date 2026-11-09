package rw.parkshare.backend.payment.gateway;

import java.math.BigDecimal;

/**
 * A mobile-money provider. Real adapters (MTN MoMo Collections API,
 * Airtel Money Rwanda) implement this; payment results arrive later through
 * the signed callback at POST /api/payments/callback/{provider}.
 */
public interface PaymentGateway {

    /** Name stored on transactions and used in the callback URL, e.g. "sandbox", "mtn". */
    String name();

    /**
     * Asks the provider to send a payment prompt to the customer's phone.
     * Returns the provider's transaction id; the outcome comes via callback.
     */
    String requestPayment(String transactionReference, String phone, BigDecimal amount, String method, String description);

    /** Sends money back to the customer. Returns the provider refund id. */
    String refund(String providerTransactionId, String phone, BigDecimal amount, String reason);
}

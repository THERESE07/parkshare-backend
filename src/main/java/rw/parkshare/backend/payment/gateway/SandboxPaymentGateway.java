package rw.parkshare.backend.payment.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulates MTN MoMo / Airtel Money for development and testing. Payments stay
 * PROCESSING until completed with POST /api/payments/sandbox/{reference}/complete
 * or a signed callback, just like a real provider waiting for the customer's PIN.
 */
@Component
@ConditionalOnProperty(prefix = "parkshare.payments", name = "provider", havingValue = "sandbox", matchIfMissing = true)
public class SandboxPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SandboxPaymentGateway.class);

    @Override
    public String name() {
        return "sandbox";
    }

    @Override
    public String requestPayment(String transactionReference, String phone, BigDecimal amount, String method, String description) {
        log.info("[sandbox] {} payment prompt sent to {} for {} RWF (ref {})", method, phone, amount.toPlainString(), transactionReference);
        return "SBX-" + UUID.randomUUID();
    }

    @Override
    public String refund(String providerTransactionId, String phone, BigDecimal amount, String reason) {
        log.info("[sandbox] refund of {} RWF to {} for {}", amount.toPlainString(), phone, providerTransactionId);
        return "SBX-RF-" + UUID.randomUUID();
    }
}

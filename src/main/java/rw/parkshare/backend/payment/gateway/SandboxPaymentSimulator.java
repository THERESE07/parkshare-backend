package rw.parkshare.backend.payment.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.payment.event.PaymentInitiatedEvent;
import rw.parkshare.backend.payment.service.PaymentFlowService;

import java.time.Instant;

/**
 * Plays the customer's phone in sandbox mode, so payments behave like real
 * MoMo/Airtel payments without real money:
 *
 *  - a few seconds after the prompt is sent, the "customer" answers;
 *  - phone ending in 000  -> declined ("insufficient balance");
 *  - phone ending in 999  -> never answers (the hold expires, like a real timeout);
 *  - any other number     -> approved.
 *
 * Set PARKSHARE_PAYMENTS_SANDBOXAUTOAPPROVESECONDS=0 to answer manually with
 * POST /api/payments/sandbox/{transactionReference}/complete instead.
 */
@Component
@ConditionalOnProperty(prefix = "parkshare.payments", name = "provider", havingValue = "sandbox", matchIfMissing = true)
public class SandboxPaymentSimulator {

    private static final Logger log = LoggerFactory.getLogger(SandboxPaymentSimulator.class);

    private final PaymentFlowService paymentFlowService;
    private final TaskScheduler scheduler;
    private final ParkShareProperties properties;

    public SandboxPaymentSimulator(PaymentFlowService paymentFlowService, TaskScheduler scheduler,
                                   ParkShareProperties properties) {
        this.paymentFlowService = paymentFlowService;
        this.scheduler = scheduler;
        this.properties = properties;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentInitiated(PaymentInitiatedEvent event) {
        int delay = properties.getPayments().getSandboxAutoApproveSeconds();
        log.info("[SANDBOX PHONE {}] {} prompt: pay {} RWF to ParkShare? (ref {})",
                event.phone(), event.method(), event.amount().toBigInteger(), event.transactionReference());
        if (delay <= 0) {
            log.info("[SANDBOX] Waiting for POST /api/payments/sandbox/{}/complete", event.transactionReference());
            return;
        }
        if (event.phone().endsWith("999")) {
            log.info("[SANDBOX PHONE {}] customer does not answer; payment will time out", event.phone());
            return;
        }
        boolean approve = !event.phone().endsWith("000");
        scheduler.schedule(() -> answer(event, approve), Instant.now().plusSeconds(delay));
    }

    private void answer(PaymentInitiatedEvent event, boolean approve) {
        try {
            log.info("[SANDBOX PHONE {}] customer {} the payment", event.phone(), approve ? "APPROVED" : "DECLINED");
            paymentFlowService.applyProviderResult(event.transactionReference(), approve,
                    "SBX-" + event.transactionReference(), approve ? null : "Insufficient balance");
        } catch (RuntimeException exception) {
            log.warn("[SANDBOX] could not apply result for {}: {}", event.transactionReference(), exception.getMessage());
        }
    }
}

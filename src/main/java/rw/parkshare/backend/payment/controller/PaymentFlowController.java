package rw.parkshare.backend.payment.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.payment.dto.PaymentFlowDtos.InitiatePaymentRequest;
import rw.parkshare.backend.payment.dto.PaymentFlowDtos.PaymentView;
import rw.parkshare.backend.payment.dto.PaymentFlowDtos.Receipt;
import rw.parkshare.backend.payment.repository.PaymentTransactionRepository;
import rw.parkshare.backend.payment.service.PaymentCallbackService;
import rw.parkshare.backend.payment.service.PaymentFlowService;
import rw.parkshare.backend.security.AccessGuard;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentFlowController {

    private final PaymentFlowService paymentFlowService;
    private final PaymentCallbackService callbackService;
    private final PaymentTransactionRepository transactionRepository;
    private final AccessGuard guard;
    private final ParkShareProperties properties;

    public PaymentFlowController(
            PaymentFlowService paymentFlowService,
            PaymentCallbackService callbackService,
            PaymentTransactionRepository transactionRepository,
            AccessGuard guard,
            ParkShareProperties properties
    ) {
        this.paymentFlowService = paymentFlowService;
        this.callbackService = callbackService;
        this.transactionRepository = transactionRepository;
        this.guard = guard;
        this.properties = properties;
    }

    /** Driver pays with MTN MoMo or Airtel Money; a prompt goes to their phone. */
    @PostMapping("/reservation/{reservationId}/initiate")
    public ResponseEntity<PaymentView> initiate(
            @PathVariable Long reservationId,
            @Valid @RequestBody InitiatePaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentFlowService.initiate(reservationId, request));
    }

    /** Attendant records cash received at the gate. */
    @PostMapping("/reservation/{reservationId}/cash")
    public ResponseEntity<PaymentView> cash(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentFlowService.recordCash(reservationId));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<Receipt> receipt(@PathVariable Long id) {
        return ResponseEntity.ok(paymentFlowService.receipt(id));
    }

    /** Provider callback (MTN, Airtel, sandbox). Authenticated by HMAC signature, not JWT. */
    @PostMapping("/callback/{provider}")
    public ResponseEntity<Map<String, Object>> callback(
            @PathVariable String provider,
            @RequestHeader(value = "X-ParkShare-Signature", required = false) String signature,
            @RequestBody String rawBody
    ) {
        boolean duplicate = callbackService.handle(provider, signature, rawBody);
        return ResponseEntity.ok(Map.of("received", true, "duplicate", duplicate));
    }

    /**
     * Development helper: acts as the customer approving (or rejecting) the
     * mobile-money prompt. Disabled unless the sandbox gateway is active.
     */
    @PostMapping("/sandbox/{transactionReference}/complete")
    public ResponseEntity<Map<String, Object>> sandboxComplete(
            @PathVariable String transactionReference,
            @RequestParam(defaultValue = "true") boolean success
    ) {
        ParkShareProperties.Payments payments = properties.getPayments();
        if (!payments.isSandboxEndpointsEnabled() || !"sandbox".equals(payments.getProvider())) {
            throw ApiException.notFound("Sandbox payments are disabled");
        }
        transactionRepository.findByTransactionReference(transactionReference)
                .ifPresent(tx -> guard.requireReservationAccess(tx.getPayment().getReservation()));
        paymentFlowService.applyProviderResult(transactionReference, success, null,
                success ? null : "Customer rejected the prompt");
        return ResponseEntity.ok(Map.of("transactionReference", transactionReference, "success", success));
    }
}

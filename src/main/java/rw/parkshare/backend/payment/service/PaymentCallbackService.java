package rw.parkshare.backend.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.payment.dto.PaymentFlowDtos.ProviderCallback;
import rw.parkshare.backend.payment.entity.PaymentWebhook;
import rw.parkshare.backend.payment.repository.PaymentWebhookRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Set;

/**
 * Verifies, stores and applies payment provider callbacks.
 *
 * Every callback must carry X-ParkShare-Signature: hex(HMAC-SHA256(secret, body)).
 * Real MTN/Airtel adapters should translate the provider's own format and
 * signature scheme into this before calling {@link #handle}.
 * Each (provider, eventId) is processed once; retries are acknowledged.
 */
@Service
@Transactional
public class PaymentCallbackService {

    private static final Set<String> SUCCESS = Set.of("SUCCESS", "SUCCESSFUL", "PAID", "COMPLETED");
    private static final Set<String> FAILURE = Set.of("FAILED", "REJECTED", "CANCELLED", "EXPIRED", "TIMEOUT");

    private final PaymentWebhookRepository webhookRepository;
    private final PaymentFlowService paymentFlowService;
    private final ParkShareProperties properties;
    private final JsonMapper jsonMapper;

    public PaymentCallbackService(
            PaymentWebhookRepository webhookRepository,
            PaymentFlowService paymentFlowService,
            ParkShareProperties properties,
            JsonMapper jsonMapper
    ) {
        this.webhookRepository = webhookRepository;
        this.paymentFlowService = paymentFlowService;
        this.properties = properties;
        this.jsonMapper = jsonMapper;
    }

    /** @return true if this event had already been processed. */
    public boolean handle(String provider, String signature, String rawBody) {
        verifySignature(signature, rawBody);

        ProviderCallback callback;
        try {
            callback = jsonMapper.readValue(rawBody, ProviderCallback.class);
        } catch (JacksonException exception) {
            throw ApiException.badRequest("Callback body is not valid JSON");
        }
        if (callback.eventId() == null || callback.transactionReference() == null || callback.status() == null) {
            throw ApiException.badRequest("eventId, transactionReference and status are required");
        }
        if (webhookRepository.existsByProviderAndEventId(provider, callback.eventId())) {
            return true;
        }

        PaymentWebhook webhook = new PaymentWebhook();
        webhook.setProvider(provider);
        webhook.setEventId(callback.eventId());
        webhook.setEventType(callback.eventType());
        webhook.setPayload(rawBody);
        webhook.setReceivedAt(OffsetDateTime.now());
        webhook.setProcessed(false);
        webhook = webhookRepository.save(webhook);

        String status = callback.status().toUpperCase();
        if (SUCCESS.contains(status) || FAILURE.contains(status)) {
            paymentFlowService.applyProviderResult(
                    callback.transactionReference(),
                    SUCCESS.contains(status),
                    callback.providerTransactionId(),
                    callback.reason());
        }
        webhook.setProcessed(true);
        webhook.setProcessedAt(OffsetDateTime.now());
        webhookRepository.save(webhook);
        return false;
    }

    private void verifySignature(String signature, String rawBody) {
        String secret = properties.getPayments().getWebhookSecret();
        if (secret == null || secret.isBlank()) {
            throw ApiException.forbidden("Payment callbacks are not configured (PARKSHARE_PAYMENTS_WEBHOOKSECRET)");
        }
        if (signature == null || signature.isBlank()) {
            throw ApiException.unauthorized("Missing X-ParkShare-Signature header");
        }
        String expected = sign(secret, rawBody);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
            throw ApiException.unauthorized("Invalid callback signature");
        }
    }

    public static String sign(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }
}

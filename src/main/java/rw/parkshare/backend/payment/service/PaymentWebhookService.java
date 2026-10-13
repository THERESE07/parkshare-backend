package rw.parkshare.backend.payment.service;

import org.springframework.stereotype.Service;
import rw.parkshare.backend.common.dto.PaymentWebhookRequestDto;
import rw.parkshare.backend.common.dto.PaymentWebhookResponseDto;
import rw.parkshare.backend.payment.entity.PaymentWebhook;
import rw.parkshare.backend.payment.repository.PaymentWebhookRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PaymentWebhookService {

    private final PaymentWebhookRepository paymentWebhookRepository;

    public PaymentWebhookService(PaymentWebhookRepository paymentWebhookRepository) {
        this.paymentWebhookRepository = paymentWebhookRepository;
    }

    public PaymentWebhookResponseDto createWebhook(PaymentWebhookRequestDto request) {

        validateWebhookRequest(request);

        if (paymentWebhookRepository
                .findByProviderAndEventId(request.getProvider(), request.getEventId())
                .isPresent()) {
            throw new IllegalArgumentException(
                    "Webhook already exists for this provider and event ID"
            );
        }

        PaymentWebhook webhook = new PaymentWebhook();

        webhook.setProvider(request.getProvider().trim());
        webhook.setEventId(request.getEventId().trim());
        webhook.setEventType(
                request.getEventType() == null || request.getEventType().isBlank()
                        ? null
                        : request.getEventType().trim()
        );
        webhook.setPayload(request.getPayload());
        webhook.setProcessed(false);
        webhook.setReceivedAt(OffsetDateTime.now());

        PaymentWebhook saved = paymentWebhookRepository.save(webhook);

        return toResponseDto(saved);
    }

    public PaymentWebhookResponseDto getWebhookById(Long id) {
        return toResponseDto(getWebhookEntityById(id));
    }

    public PaymentWebhookResponseDto getWebhookByProviderAndEventId(
            String provider,
            String eventId
    ) {
        return paymentWebhookRepository
                .findByProviderAndEventId(provider, eventId)
                .map(this::toResponseDto)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment webhook not found")
                );
    }

    public List<PaymentWebhookResponseDto> getWebhooksByProvider(String provider) {
        return paymentWebhookRepository
                .findByProvider(provider)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<PaymentWebhookResponseDto> getWebhooksByEventType(String eventType) {
        return paymentWebhookRepository
                .findByEventType(eventType)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<PaymentWebhookResponseDto> getWebhooksByProcessed(boolean processed) {
        return paymentWebhookRepository
                .findByProcessed(processed)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<PaymentWebhookResponseDto> getWebhooksByProviderAndProcessed(
            String provider,
            boolean processed
    ) {
        return paymentWebhookRepository
                .findByProviderAndProcessed(provider, processed)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public boolean webhookExists(String provider, String eventId) {
        return paymentWebhookRepository
                .findByProviderAndEventId(provider, eventId)
                .isPresent();
    }

    public PaymentWebhookResponseDto markAsProcessed(Long id) {

        PaymentWebhook webhook = getWebhookEntityById(id);

        webhook.setProcessed(true);
        webhook.setProcessedAt(OffsetDateTime.now());

        PaymentWebhook saved = paymentWebhookRepository.save(webhook);

        return toResponseDto(saved);
    }

    public PaymentWebhookResponseDto markAsUnprocessed(Long id) {

        PaymentWebhook webhook = getWebhookEntityById(id);

        webhook.setProcessed(false);
        webhook.setProcessedAt(null);

        PaymentWebhook saved = paymentWebhookRepository.save(webhook);

        return toResponseDto(saved);
    }

    public PaymentWebhookResponseDto updateWebhook(
            Long id,
            PaymentWebhookRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException("Webhook request cannot be null");
        }

        if (request.getEventType() == null && request.getPayload() == null) {
            throw new IllegalArgumentException(
                    "At least eventType or payload must be provided"
            );
        }

        PaymentWebhook webhook = getWebhookEntityById(id);

        if (request.getEventType() != null) {
            webhook.setEventType(
                    request.getEventType().isBlank()
                            ? null
                            : request.getEventType().trim()
            );
        }

        if (request.getPayload() != null) {
            if (request.getPayload().isBlank()) {
                throw new IllegalArgumentException("Payload cannot be blank");
            }

            webhook.setPayload(request.getPayload());
        }

        PaymentWebhook saved = paymentWebhookRepository.save(webhook);

        return toResponseDto(saved);
    }

    public void deleteWebhook(Long id) {
        PaymentWebhook webhook = getWebhookEntityById(id);
        paymentWebhookRepository.delete(webhook);
    }

    private void validateWebhookRequest(PaymentWebhookRequestDto request) {

        if (request == null) {
            throw new IllegalArgumentException("Webhook request cannot be null");
        }

        if (request.getProvider() == null || request.getProvider().isBlank()) {
            throw new IllegalArgumentException("Provider is required");
        }

        if (request.getEventId() == null || request.getEventId().isBlank()) {
            throw new IllegalArgumentException("Event ID is required");
        }

        if (request.getPayload() == null || request.getPayload().isBlank()) {
            throw new IllegalArgumentException("Payload is required");
        }
    }

    private PaymentWebhook getWebhookEntityById(Long id) {
        return paymentWebhookRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Payment webhook not found")
                );
    }

    private PaymentWebhookResponseDto toResponseDto(PaymentWebhook webhook) {

        return new PaymentWebhookResponseDto(
                webhook.getId(),
                webhook.getProvider(),
                webhook.getEventId(),
                webhook.getEventType(),
                webhook.getPayload(),
                webhook.isProcessed(),
                webhook.getProcessedAt(),
                webhook.getReceivedAt()
        );
    }
}
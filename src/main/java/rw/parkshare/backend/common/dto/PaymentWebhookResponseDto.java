package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class PaymentWebhookResponseDto {

    private Long id;
    private String provider;
    private String eventId;
    private String eventType;
    private String payload;
    private boolean processed;
    private OffsetDateTime processedAt;
    private OffsetDateTime receivedAt;

    public PaymentWebhookResponseDto() {
    }

    public PaymentWebhookResponseDto(
            Long id,
            String provider,
            String eventId,
            String eventType,
            String payload,
            boolean processed,
            OffsetDateTime processedAt,
            OffsetDateTime receivedAt
    ) {
        this.id = id;
        this.provider = provider;
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.processed = processed;
        this.processedAt = processedAt;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class NotificationResponseDto {

    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private String channel;
    private String status;
    private String referenceType;
    private Long referenceId;
    private OffsetDateTime readAt;
    private OffsetDateTime sentAt;
    private OffsetDateTime createdAt;

    public NotificationResponseDto() {
    }

    public NotificationResponseDto(
            Long id,
            Long userId,
            String type,
            String title,
            String message,
            String channel,
            String status,
            String referenceType,
            Long referenceId,
            OffsetDateTime readAt,
            OffsetDateTime sentAt,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.channel = channel;
        this.status = status;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.readAt = readAt;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(OffsetDateTime readAt) {
        this.readAt = readAt;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
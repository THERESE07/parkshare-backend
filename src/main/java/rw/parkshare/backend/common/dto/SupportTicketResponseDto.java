package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class SupportTicketResponseDto {

    private Long id;
    private Long userId;
    private String ticketNumber;
    private String subject;
    private String description;
    private String category;
    private String status;
    private String priority;
    private Long assignedToUserId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime resolvedAt;

    public SupportTicketResponseDto() {
    }

    public SupportTicketResponseDto(
            Long id,
            Long userId,
            String ticketNumber,
            String subject,
            String description,
            String category,
            String status,
            String priority,
            Long assignedToUserId,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime resolvedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.ticketNumber = ticketNumber;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.assignedToUserId = assignedToUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedAt = resolvedAt;
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

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
package rw.parkshare.backend.common.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class DisputeResponseDto {

    private Long id;
    private Long reservationId;
    private Long raisedByUserId;
    private Long assignedToUserId;
    private String disputeType;
    private String description;
    private BigDecimal amountDisputed;
    private String currency;
    private String status;
    private String resolution;
    private OffsetDateTime resolvedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public DisputeResponseDto() {
    }

    public DisputeResponseDto(
            Long id,
            Long reservationId,
            Long raisedByUserId,
            Long assignedToUserId,
            String disputeType,
            String description,
            BigDecimal amountDisputed,
            String currency,
            String status,
            String resolution,
            OffsetDateTime resolvedAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.reservationId = reservationId;
        this.raisedByUserId = raisedByUserId;
        this.assignedToUserId = assignedToUserId;
        this.disputeType = disputeType;
        this.description = description;
        this.amountDisputed = amountDisputed;
        this.currency = currency;
        this.status = status;
        this.resolution = resolution;
        this.resolvedAt = resolvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getRaisedByUserId() {
        return raisedByUserId;
    }

    public void setRaisedByUserId(Long raisedByUserId) {
        this.raisedByUserId = raisedByUserId;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public String getDisputeType() {
        return disputeType;
    }

    public void setDisputeType(String disputeType) {
        this.disputeType = disputeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmountDisputed() {
        return amountDisputed;
    }

    public void setAmountDisputed(BigDecimal amountDisputed) {
        this.amountDisputed = amountDisputed;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
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
}

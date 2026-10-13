package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class ReservationStatusHistoryResponseDto {

    private Long id;
    private Long reservationId;
    private String oldStatus;
    private String newStatus;
    private Long changedByUserId;
    private String reason;
    private OffsetDateTime createdAt;

    public ReservationStatusHistoryResponseDto() {
    }

    public ReservationStatusHistoryResponseDto(
            Long id,
            Long reservationId,
            String oldStatus,
            String newStatus,
            Long changedByUserId,
            String reason,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.reservationId = reservationId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedByUserId = changedByUserId;
        this.reason = reason;
        this.createdAt = createdAt;
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

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

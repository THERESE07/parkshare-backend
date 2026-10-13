package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class CheckOutResponseDto {

    private Long id;
    private Long checkInId;
    private Long reservationId;
    private Long checkedOutById;
    private String checkOutMethod;
    private OffsetDateTime checkedOutAt;
    private String notes;

    public CheckOutResponseDto() {
    }

    public CheckOutResponseDto(
            Long id,
            Long checkInId,
            Long reservationId,
            Long checkedOutById,
            String checkOutMethod,
            OffsetDateTime checkedOutAt,
            String notes
    ) {
        this.id = id;
        this.checkInId = checkInId;
        this.reservationId = reservationId;
        this.checkedOutById = checkedOutById;
        this.checkOutMethod = checkOutMethod;
        this.checkedOutAt = checkedOutAt;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getCheckedOutById() {
        return checkedOutById;
    }

    public void setCheckedOutById(Long checkedOutById) {
        this.checkedOutById = checkedOutById;
    }

    public String getCheckOutMethod() {
        return checkOutMethod;
    }

    public void setCheckOutMethod(String checkOutMethod) {
        this.checkOutMethod = checkOutMethod;
    }

    public OffsetDateTime getCheckedOutAt() {
        return checkedOutAt;
    }

    public void setCheckedOutAt(OffsetDateTime checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
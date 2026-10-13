package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class CheckInRequestDto {

    private Long reservationId;
    private Long vehicleId;
    private Long parkingSpaceId;
    private Long checkedInById;
    private String checkInMethod;
    private OffsetDateTime checkedInAt;
    private String notes;

    public CheckInRequestDto() {
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getParkingSpaceId() {
        return parkingSpaceId;
    }

    public void setParkingSpaceId(Long parkingSpaceId) {
        this.parkingSpaceId = parkingSpaceId;
    }

    public Long getCheckedInById() {
        return checkedInById;
    }

    public void setCheckedInById(Long checkedInById) {
        this.checkedInById = checkedInById;
    }

    public String getCheckInMethod() {
        return checkInMethod;
    }

    public void setCheckInMethod(String checkInMethod) {
        this.checkInMethod = checkInMethod;
    }

    public OffsetDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(OffsetDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class CheckInResponseDto {

    private Long id;
    private Long reservationId;
    private Long vehicleId;
    private Long parkingSpaceId;
    private Long checkedInById;
    private String checkInMethod;
    private OffsetDateTime checkedInAt;
    private String notes;

    public CheckInResponseDto() {
    }

    public CheckInResponseDto(
            Long id,
            Long reservationId,
            Long vehicleId,
            Long parkingSpaceId,
            Long checkedInById,
            String checkInMethod,
            OffsetDateTime checkedInAt,
            String notes
    ) {
        this.id = id;
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.parkingSpaceId = parkingSpaceId;
        this.checkedInById = checkedInById;
        this.checkInMethod = checkInMethod;
        this.checkedInAt = checkedInAt;
        this.notes = notes;
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

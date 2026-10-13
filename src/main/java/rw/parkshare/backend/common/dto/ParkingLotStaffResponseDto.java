package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class ParkingLotStaffResponseDto {

    private Long id;
    private Long parkingLotId;
    private Long userId;
    private OffsetDateTime assignedAt;
    private boolean active;

    public ParkingLotStaffResponseDto() {
    }

    public ParkingLotStaffResponseDto(
            Long id,
            Long parkingLotId,
            Long userId,
            OffsetDateTime assignedAt,
            boolean active
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.userId = userId;
        this.assignedAt = assignedAt;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
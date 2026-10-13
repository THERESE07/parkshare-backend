package rw.parkshare.backend.common.dto;

public class ParkingLotStaffRequestDto {

    private Long parkingLotId;
    private Long userId;

    public ParkingLotStaffRequestDto() {
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
}

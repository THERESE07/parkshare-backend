package rw.parkshare.backend.common.dto;

public class ParkingSpaceResponseDto {

    private Long id;
    private Long parkingZoneId;
    private String spaceCode;
    private String spaceType;
    private String status;
    private String floor;

    public ParkingSpaceResponseDto() {
    }

    public ParkingSpaceResponseDto(
            Long id,
            Long parkingZoneId,
            String spaceCode,
            String spaceType,
            String status,
            String floor
    ) {
        this.id = id;
        this.parkingZoneId = parkingZoneId;
        this.spaceCode = spaceCode;
        this.spaceType = spaceType;
        this.status = status;
        this.floor = floor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParkingZoneId() {
        return parkingZoneId;
    }

    public void setParkingZoneId(Long parkingZoneId) {
        this.parkingZoneId = parkingZoneId;
    }

    public String getSpaceCode() {
        return spaceCode;
    }

    public void setSpaceCode(String spaceCode) {
        this.spaceCode = spaceCode;
    }

    public String getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(String spaceType) {
        this.spaceType = spaceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }
}

package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

public class ParkingSpaceRequestDto {

    @Size(max = 50)
    private String spaceCode;
    @Pattern(regexp = "STANDARD|DISABLED|VIP|EV|MOTORCYCLE", message = "must be one of STANDARD, DISABLED, VIP, EV, MOTORCYCLE")
    private String spaceType;
    @Pattern(regexp = "AVAILABLE|OCCUPIED|RESERVED|MAINTENANCE|BLOCKED", message = "must be one of AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE, BLOCKED")
    private String status;
    @Size(max = 100)
    private String floor;

    public ParkingSpaceRequestDto() {
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

package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

public class ParkingZoneRequestDto {

    @Size(max = 100)
    private String name;
    private String description;
    @PositiveOrZero
    @Max(100000)
    private Integer capacity;
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "must be one of ACTIVE, INACTIVE")
    private String status;

    public ParkingZoneRequestDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

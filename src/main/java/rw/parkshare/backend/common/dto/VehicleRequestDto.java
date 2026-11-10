package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

public class VehicleRequestDto {

    @Pattern(regexp = "^[A-Za-z0-9 -]{2,30}$", message = "must be a valid plate number, e.g. RAD 123 A")
    private String plateNumber;
    @Pattern(regexp = "CAR|MOTORCYCLE|TRUCK|BUS|OTHER", message = "must be one of CAR, MOTORCYCLE, TRUCK, BUS, OTHER")
    private String vehicleType;
    @Size(max = 100)
    private String make;
    @Size(max = 100)
    private String model;
    @Size(max = 50)
    private String color;
    private Boolean isDefault;
    private Boolean isActive;

    public VehicleRequestDto() {
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
package rw.parkshare.backend.common.dto;

public class VehicleResponseDto {

    private Long id;
    private Long userId;
    private String plateNumber;
    private String vehicleType;
    private String make;
    private String model;
    private String color;
    private Boolean isDefault;
    private Boolean isActive;

    public VehicleResponseDto() {
    }

    public VehicleResponseDto(
            Long id,
            Long userId,
            String plateNumber,
            String vehicleType,
            String make,
            String model,
            String color,
            Boolean isDefault,
            Boolean isActive
    ) {
        this.id = id;
        this.userId = userId;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.make = make;
        this.model = model;
        this.color = color;
        this.isDefault = isDefault;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
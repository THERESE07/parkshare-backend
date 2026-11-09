package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ParkingLotRequestDto {

    @Size(max = 150)
    private String name;
    private String description;
    @Size(max = 255)
    private String address;
    @Pattern(regexp = "(?i)(Gasabo|Kicukiro|Nyarugenge|Burera|Gakenke|Gicumbi|Musanze|Rulindo|Gisagara|Huye|Kamonyi|Muhanga|Nyamagabe|Nyanza|Nyaruguru|Ruhango|Bugesera|Gatsibo|Kayonza|Kirehe|Ngoma|Nyagatare|Rwamagana|Karongi|Ngororero|Nyabihu|Nyamasheke|Rubavu|Rusizi|Rutsiro)", message = "must be one of Rwanda's 30 districts, e.g. Gasabo, Kicukiro, Nyarugenge")
    private String district;
    @Size(max = 100)
    private String city;
    @DecimalMin(value = "-3.0", message = "must be inside Rwanda")
    @DecimalMax(value = "-1.0", message = "must be inside Rwanda")
    private BigDecimal latitude;
    @DecimalMin(value = "28.8", message = "must be inside Rwanda")
    @DecimalMax(value = "31.0", message = "must be inside Rwanda")
    private BigDecimal longitude;
    @Size(max = 30)
    private String contactPhone;
    @PositiveOrZero
    @Max(100000)
    private Integer totalCapacity;
    private String status;

    public ParkingLotRequestDto() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

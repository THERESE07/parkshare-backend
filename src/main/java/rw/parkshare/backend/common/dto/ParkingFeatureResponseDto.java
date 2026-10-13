package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class ParkingFeatureResponseDto {

    private Long id;
    private Long parkingLotId;
    private String featureCode;
    private OffsetDateTime createdAt;

    public ParkingFeatureResponseDto() {
    }

    public ParkingFeatureResponseDto(
            Long id,
            Long parkingLotId,
            String featureCode,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.featureCode = featureCode;
        this.createdAt = createdAt;
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

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(String featureCode) {
        this.featureCode = featureCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
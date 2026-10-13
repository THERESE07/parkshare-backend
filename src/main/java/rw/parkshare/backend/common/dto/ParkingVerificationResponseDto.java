package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class ParkingVerificationResponseDto {

    private Long id;
    private Long parkingLotId;
    private Long verifiedById;
    private String verificationStatus;
    private String verificationMethod;
    private String notes;
    private OffsetDateTime verifiedAt;
    private OffsetDateTime createdAt;

    public ParkingVerificationResponseDto() {
    }

    public ParkingVerificationResponseDto(
            Long id,
            Long parkingLotId,
            Long verifiedById,
            String verificationStatus,
            String verificationMethod,
            String notes,
            OffsetDateTime verifiedAt,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.verifiedById = verifiedById;
        this.verificationStatus = verificationStatus;
        this.verificationMethod = verificationMethod;
        this.notes = notes;
        this.verifiedAt = verifiedAt;
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

    public Long getVerifiedById() {
        return verifiedById;
    }

    public void setVerifiedById(Long verifiedById) {
        this.verifiedById = verifiedById;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(OffsetDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
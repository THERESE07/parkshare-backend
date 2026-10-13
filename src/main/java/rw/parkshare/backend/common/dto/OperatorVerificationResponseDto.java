package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class OperatorVerificationResponseDto {

    private Long id;
    private Long userId;
    private String businessName;
    private String businessRegistrationNumber;
    private String verificationStatus;
    private OffsetDateTime submittedAt;
    private Long reviewedById;
    private OffsetDateTime reviewedAt;
    private String rejectionReason;
    private String notes;

    public OperatorVerificationResponseDto() {
    }

    public OperatorVerificationResponseDto(
            Long id,
            Long userId,
            String businessName,
            String businessRegistrationNumber,
            String verificationStatus,
            OffsetDateTime submittedAt,
            Long reviewedById,
            OffsetDateTime reviewedAt,
            String rejectionReason,
            String notes
    ) {
        this.id = id;
        this.userId = userId;
        this.businessName = businessName;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.verificationStatus = verificationStatus;
        this.submittedAt = submittedAt;
        this.reviewedById = reviewedById;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
        this.notes = notes;
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessRegistrationNumber() {
        return businessRegistrationNumber;
    }

    public void setBusinessRegistrationNumber(
            String businessRegistrationNumber
    ) {
        this.businessRegistrationNumber =
                businessRegistrationNumber;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(
            String verificationStatus
    ) {
        this.verificationStatus = verificationStatus;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(
            OffsetDateTime submittedAt
    ) {
        this.submittedAt = submittedAt;
    }

    public Long getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Long reviewedById) {
        this.reviewedById = reviewedById;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(
            OffsetDateTime reviewedAt
    ) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(
            String rejectionReason
    ) {
        this.rejectionReason = rejectionReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
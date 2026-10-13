package rw.parkshare.backend.common.dto;

public class OperatorVerificationRequestDto {

    private String businessName;
    private String businessRegistrationNumber;
    private String verificationStatus;
    private String rejectionReason;
    private String notes;

    public OperatorVerificationRequestDto() {
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

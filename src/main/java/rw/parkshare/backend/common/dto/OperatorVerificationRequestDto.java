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

    /** RRA TIN: exactly 9 digits. */
    @jakarta.validation.constraints.Pattern(regexp = "\\d{9}", message = "must be a 9-digit RRA TIN")
    private String tin;

    /** True if the business is registered for VAT with RRA (attach the certificate for review). */
    private Boolean vatRegistered;

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public Boolean getVatRegistered() {
        return vatRegistered;
    }

    public void setVatRegistered(Boolean vatRegistered) {
        this.vatRegistered = vatRegistered;
    }
}

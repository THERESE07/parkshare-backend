package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

public class IssueReportRequestDto {

    @Pattern(regexp = "PARKING_UNAVAILABLE|WRONG_AVAILABILITY|PAYMENT_PROBLEM|RESERVATION_PROBLEM|QR_PROBLEM|CHECK_IN_PROBLEM|CHECK_OUT_PROBLEM|OVERCHARGED|SAFETY|OTHER", message = "must be one of PARKING_UNAVAILABLE, WRONG_AVAILABILITY, PAYMENT_PROBLEM, RESERVATION_PROBLEM, QR_PROBLEM, CHECK_IN_PROBLEM, CHECK_OUT_PROBLEM, OVERCHARGED, SAFETY, OTHER")
    private String issueType;
    @Size(max = 200)
    private String title;
    @Size(max = 5000)
    private String description;
    private String status;
    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "must be one of LOW, MEDIUM, HIGH, URGENT")
    private String priority;
    private String resolutionNotes;

    public IssueReportRequestDto() {
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}

package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

public class SupportTicketRequestDto {

    private String ticketNumber;
    @Size(max = 200)
    private String subject;
    @Size(max = 5000)
    private String description;
    @Pattern(regexp = "GENERAL|ACCOUNT|PARKING|RESERVATION|PAYMENT|TECHNICAL|REFUND|OTHER", message = "must be one of GENERAL, ACCOUNT, PARKING, RESERVATION, PAYMENT, TECHNICAL, REFUND, OTHER")
    private String category;
    private String status;
    @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT", message = "must be one of LOW, MEDIUM, HIGH, URGENT")
    private String priority;

    public SupportTicketRequestDto() {
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}

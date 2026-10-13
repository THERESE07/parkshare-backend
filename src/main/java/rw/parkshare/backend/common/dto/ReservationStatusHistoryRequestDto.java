package rw.parkshare.backend.common.dto;

public class ReservationStatusHistoryRequestDto {

    private String oldStatus;
    private String newStatus;
    private String reason;

    public ReservationStatusHistoryRequestDto() {
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
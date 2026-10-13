package rw.parkshare.backend.common.dto;

import java.math.BigDecimal;

public class DisputeRequestDto {

    private String disputeType;
    private String description;
    private BigDecimal amountDisputed;
    private String currency;
    private String status;
    private String resolution;

    public DisputeRequestDto() {
    }

    public String getDisputeType() {
        return disputeType;
    }

    public void setDisputeType(String disputeType) {
        this.disputeType = disputeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmountDisputed() {
        return amountDisputed;
    }

    public void setAmountDisputed(BigDecimal amountDisputed) {
        this.amountDisputed = amountDisputed;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}

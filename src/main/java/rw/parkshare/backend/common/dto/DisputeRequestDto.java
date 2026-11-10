package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class DisputeRequestDto {

    @Pattern(regexp = "PAYMENT|REFUND|OVERCHARGE|RESERVATION|SERVICE|DAMAGE|OTHER", message = "must be one of PAYMENT, REFUND, OVERCHARGE, RESERVATION, SERVICE, DAMAGE, OTHER")
    private String disputeType;
    @Size(max = 5000)
    private String description;
    @PositiveOrZero
    private BigDecimal amountDisputed;
    @Pattern(regexp = "RWF", message = "must be RWF")
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

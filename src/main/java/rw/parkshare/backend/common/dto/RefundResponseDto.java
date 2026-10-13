package rw.parkshare.backend.common.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class RefundResponseDto {

    private Long id;
    private Long paymentId;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private String status;
    private String providerRefundId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime completedAt;

    public RefundResponseDto() {
    }

    public RefundResponseDto(
            Long id,
            Long paymentId,
            BigDecimal amount,
            String currency,
            String reason,
            String status,
            String providerRefundId,
            OffsetDateTime requestedAt,
            OffsetDateTime completedAt
    ) {
        this.id = id;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
        this.status = status;
        this.providerRefundId = providerRefundId;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProviderRefundId() {
        return providerRefundId;
    }

    public void setProviderRefundId(String providerRefundId) {
        this.providerRefundId = providerRefundId;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(OffsetDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
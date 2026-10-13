package rw.parkshare.backend.common.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PaymentTransactionResponseDto {

    private Long id;
    private Long paymentId;
    private String provider;
    private String providerTransactionId;
    private String transactionReference;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String failureReason;
    private OffsetDateTime initiatedAt;
    private OffsetDateTime completedAt;

    public PaymentTransactionResponseDto() {
    }

    public PaymentTransactionResponseDto(
            Long id,
            Long paymentId,
            String provider,
            String providerTransactionId,
            String transactionReference,
            BigDecimal amount,
            String currency,
            String status,
            String failureReason,
            OffsetDateTime initiatedAt,
            OffsetDateTime completedAt
    ) {
        this.id = id;
        this.paymentId = paymentId;
        this.provider = provider;
        this.providerTransactionId = providerTransactionId;
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.failureReason = failureReason;
        this.initiatedAt = initiatedAt;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public OffsetDateTime getInitiatedAt() {
        return initiatedAt;
    }

    public void setInitiatedAt(OffsetDateTime initiatedAt) {
        this.initiatedAt = initiatedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
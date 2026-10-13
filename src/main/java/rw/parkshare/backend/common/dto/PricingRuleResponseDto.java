package rw.parkshare.backend.common.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PricingRuleResponseDto {

    private Long id;
    private Long parkingLotId;
    private String name;
    private String pricingType;
    private BigDecimal amount;
    private String currency;
    private Integer minimumDurationMinutes;
    private Integer maximumDurationMinutes;
    private boolean active;
    private OffsetDateTime effectiveFrom;
    private OffsetDateTime effectiveUntil;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public PricingRuleResponseDto() {
    }

    public PricingRuleResponseDto(
            Long id,
            Long parkingLotId,
            String name,
            String pricingType,
            BigDecimal amount,
            String currency,
            Integer minimumDurationMinutes,
            Integer maximumDurationMinutes,
            boolean active,
            OffsetDateTime effectiveFrom,
            OffsetDateTime effectiveUntil,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.pricingType = pricingType;
        this.amount = amount;
        this.currency = currency;
        this.minimumDurationMinutes = minimumDurationMinutes;
        this.maximumDurationMinutes = maximumDurationMinutes;
        this.active = active;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPricingType() {
        return pricingType;
    }

    public void setPricingType(String pricingType) {
        this.pricingType = pricingType;
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

    public Integer getMinimumDurationMinutes() {
        return minimumDurationMinutes;
    }

    public void setMinimumDurationMinutes(Integer minimumDurationMinutes) {
        this.minimumDurationMinutes = minimumDurationMinutes;
    }

    public Integer getMaximumDurationMinutes() {
        return maximumDurationMinutes;
    }

    public void setMaximumDurationMinutes(Integer maximumDurationMinutes) {
        this.maximumDurationMinutes = maximumDurationMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(OffsetDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public OffsetDateTime getEffectiveUntil() {
        return effectiveUntil;
    }

    public void setEffectiveUntil(OffsetDateTime effectiveUntil) {
        this.effectiveUntil = effectiveUntil;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
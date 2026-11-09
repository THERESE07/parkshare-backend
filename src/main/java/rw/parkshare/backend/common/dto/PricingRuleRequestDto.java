package rw.parkshare.backend.common.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PricingRuleRequestDto {

    @Size(max = 100)
    private String name;
    @Pattern(regexp = "HOURLY|DAILY|FLAT", message = "must be one of HOURLY, DAILY, FLAT")
    private String pricingType;
    @PositiveOrZero
    private BigDecimal amount;
    @Pattern(regexp = "RWF", message = "must be RWF")
    private String currency;
    @Positive
    private Integer minimumDurationMinutes;
    @Positive
    private Integer maximumDurationMinutes;
    private boolean active;
    private OffsetDateTime effectiveFrom;
    private OffsetDateTime effectiveUntil;

    public PricingRuleRequestDto() {
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
}

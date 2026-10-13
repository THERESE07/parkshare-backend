package rw.parkshare.backend.common.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class SpecialRateResponseDto {

    private Long id;
    private Long parkingLotId;
    private String name;
    private String rateType;
    private BigDecimal amount;
    private String currency;
    private LocalTime startTime;
    private LocalTime endTime;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public SpecialRateResponseDto() {
    }

    public SpecialRateResponseDto(
            Long id,
            Long parkingLotId,
            String name,
            String rateType,
            BigDecimal amount,
            String currency,
            LocalTime startTime,
            LocalTime endTime,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.rateType = rateType;
        this.amount = amount;
        this.currency = currency;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
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

    public String getRateType() {
        return rateType;
    }

    public void setRateType(String rateType) {
        this.rateType = rateType;
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public OffsetDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

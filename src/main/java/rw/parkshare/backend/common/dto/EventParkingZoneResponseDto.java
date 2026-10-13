package rw.parkshare.backend.common.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class EventParkingZoneResponseDto {

    private Long id;
    private Long eventId;
    private Long parkingLotId;
    private Integer capacity;
    private Integer reservedCount;
    private Integer occupiedCount;
    private Integer availableCount;
    private BigDecimal price;
    private String currency;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public EventParkingZoneResponseDto() {
    }

    public EventParkingZoneResponseDto(
            Long id,
            Long eventId,
            Long parkingLotId,
            Integer capacity,
            Integer reservedCount,
            Integer occupiedCount,
            Integer availableCount,
            BigDecimal price,
            String currency,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.parkingLotId = parkingLotId;
        this.capacity = capacity;
        this.reservedCount = reservedCount;
        this.occupiedCount = occupiedCount;
        this.availableCount = availableCount;
        this.price = price;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getParkingLotId() {
        return parkingLotId;
    }

    public void setParkingLotId(Long parkingLotId) {
        this.parkingLotId = parkingLotId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getReservedCount() {
        return reservedCount;
    }

    public void setReservedCount(Integer reservedCount) {
        this.reservedCount = reservedCount;
    }

    public Integer getOccupiedCount() {
        return occupiedCount;
    }

    public void setOccupiedCount(Integer occupiedCount) {
        this.occupiedCount = occupiedCount;
    }

    public Integer getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

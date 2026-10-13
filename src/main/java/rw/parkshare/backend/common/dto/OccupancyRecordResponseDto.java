package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class OccupancyRecordResponseDto {

    private Long id;
    private Long parkingLotId;
    private Long parkingZoneId;
    private Integer totalCapacity;
    private Integer occupiedSpaces;
    private Integer reservedSpaces;
    private Integer availableSpaces;
    private String source;
    private OffsetDateTime recordedAt;

    public OccupancyRecordResponseDto() {
    }

    public OccupancyRecordResponseDto(
            Long id,
            Long parkingLotId,
            Long parkingZoneId,
            Integer totalCapacity,
            Integer occupiedSpaces,
            Integer reservedSpaces,
            Integer availableSpaces,
            String source,
            OffsetDateTime recordedAt
    ) {
        this.id = id;
        this.parkingLotId = parkingLotId;
        this.parkingZoneId = parkingZoneId;
        this.totalCapacity = totalCapacity;
        this.occupiedSpaces = occupiedSpaces;
        this.reservedSpaces = reservedSpaces;
        this.availableSpaces = availableSpaces;
        this.source = source;
        this.recordedAt = recordedAt;
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

    public Long getParkingZoneId() {
        return parkingZoneId;
    }

    public void setParkingZoneId(Long parkingZoneId) {
        this.parkingZoneId = parkingZoneId;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public Integer getOccupiedSpaces() {
        return occupiedSpaces;
    }

    public void setOccupiedSpaces(Integer occupiedSpaces) {
        this.occupiedSpaces = occupiedSpaces;
    }

    public Integer getReservedSpaces() {
        return reservedSpaces;
    }

    public void setReservedSpaces(Integer reservedSpaces) {
        this.reservedSpaces = reservedSpaces;
    }

    public Integer getAvailableSpaces() {
        return availableSpaces;
    }

    public void setAvailableSpaces(Integer availableSpaces) {
        this.availableSpaces = availableSpaces;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
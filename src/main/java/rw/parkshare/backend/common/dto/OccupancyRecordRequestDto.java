package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class OccupancyRecordRequestDto {

    private Integer totalCapacity;
    private Integer occupiedSpaces;
    private Integer reservedSpaces;
    private Integer availableSpaces;
    private String source;
    private OffsetDateTime recordedAt;

    public OccupancyRecordRequestDto() {
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
package rw.parkshare.backend.checkin.entity;

import jakarta.persistence.*;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingZone;

import java.time.OffsetDateTime;

@Entity
@Table(name = "occupancy_records")
public class OccupancyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_zone_id")
    private ParkingZone parkingZone;

    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;

    @Column(name = "occupied_spaces", nullable = false)
    private Integer occupiedSpaces;

    @Column(name = "reserved_spaces", nullable = false)
    private Integer reservedSpaces;

    @Column(name = "available_spaces", nullable = false)
    private Integer availableSpaces;

    @Column(nullable = false, length = 30)
    private String source;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    public OccupancyRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    public ParkingZone getParkingZone() {
        return parkingZone;
    }

    public void setParkingZone(ParkingZone parkingZone) {
        this.parkingZone = parkingZone;
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

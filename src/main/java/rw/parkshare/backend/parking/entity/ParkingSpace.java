package rw.parkshare.backend.parking.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_spaces")
public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_zone_id", nullable = false)
    private ParkingZone parkingZone;

    @Column(nullable = false, length = 50)
    private String spaceCode;

    @Column(nullable = false, length = 30)
    private String spaceType = "STANDARD";

    @Column(nullable = false, length = 30)
    private String status = "AVAILABLE";

    @Column(length = 100)
    private String floor;

    public ParkingSpace() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ParkingZone getParkingZone() {
        return parkingZone;
    }

    public void setParkingZone(ParkingZone parkingZone) {
        this.parkingZone = parkingZone;
    }

    public String getSpaceCode() {
        return spaceCode;
    }

    public void setSpaceCode(String spaceCode) {
        this.spaceCode = spaceCode;
    }

    public String getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(String spaceType) {
        this.spaceType = spaceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }
}

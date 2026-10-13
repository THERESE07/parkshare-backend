package rw.parkshare.backend.parking.entity;

import jakarta.persistence.*;
import rw.parkshare.backend.user.entity.User;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "parking_lot_staff",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_parking_lot_staff",
                        columnNames = {"parking_lot_id", "user_id"}
                )
        }
)
public class ParkingLotStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(nullable = false)
    private boolean active;

    public ParkingLotStaff() {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }
    }
}

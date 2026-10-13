package rw.parkshare.backend.checkin.entity;

import jakarta.persistence.*;
import rw.parkshare.backend.parking.entity.ParkingSpace;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.Vehicle;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "check_ins",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_check_in_reservation",
                        columnNames = "reservation_id"
                )
        }
)
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_space_id")
    private ParkingSpace parkingSpace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_in_by")
    private User checkedInBy;

    @Column(name = "check_in_method", nullable = false, length = 30)
    private String checkInMethod;

    @Column(name = "checked_in_at", nullable = false)
    private OffsetDateTime checkedInAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public CheckIn() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ParkingSpace getParkingSpace() {
        return parkingSpace;
    }

    public void setParkingSpace(ParkingSpace parkingSpace) {
        this.parkingSpace = parkingSpace;
    }

    public User getCheckedInBy() {
        return checkedInBy;
    }

    public void setCheckedInBy(User checkedInBy) {
        this.checkedInBy = checkedInBy;
    }

    public String getCheckInMethod() {
        return checkInMethod;
    }

    public void setCheckInMethod(String checkInMethod) {
        this.checkInMethod = checkInMethod;
    }

    public OffsetDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(OffsetDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

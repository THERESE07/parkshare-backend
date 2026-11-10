package rw.parkshare.backend.checkin.entity;

import jakarta.persistence.*;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.user.entity.User;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "check_outs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_check_out_check_in",
                        columnNames = "check_in_id"
                )
        }
)
public class CheckOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false, unique = true)
    private CheckIn checkIn;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_out_by")
    private User checkedOutBy;

    @Column(name = "check_out_method", nullable = false, length = 30)
    private String checkOutMethod;

    @Column(name = "checked_out_at", nullable = false)
    private OffsetDateTime checkedOutAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public CheckOut() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CheckIn getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(CheckIn checkIn) {
        this.checkIn = checkIn;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public User getCheckedOutBy() {
        return checkedOutBy;
    }

    public void setCheckedOutBy(User checkedOutBy) {
        this.checkedOutBy = checkedOutBy;
    }

    public String getCheckOutMethod() {
        return checkOutMethod;
    }

    public void setCheckOutMethod(String checkOutMethod) {
        this.checkOutMethod = checkOutMethod;
    }

    public OffsetDateTime getCheckedOutAt() {
        return checkedOutAt;
    }

    public void setCheckedOutAt(OffsetDateTime checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Column(name = "overstay_minutes", nullable = false)
    private Integer overstayMinutes = 0;

    @Column(name = "overstay_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal overstayAmount = BigDecimal.ZERO;

    public Integer getOverstayMinutes() {
        return overstayMinutes;
    }

    public void setOverstayMinutes(Integer overstayMinutes) {
        this.overstayMinutes = overstayMinutes;
    }

    public BigDecimal getOverstayAmount() {
        return overstayAmount;
    }

    public void setOverstayAmount(BigDecimal overstayAmount) {
        this.overstayAmount = overstayAmount;
    }
}

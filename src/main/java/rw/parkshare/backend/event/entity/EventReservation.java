package rw.parkshare.backend.event.entity;

import jakarta.persistence.*;
import rw.parkshare.backend.reservation.entity.Reservation;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "event_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_event_reservation",
                        columnNames = {"event_id", "reservation_id"}
                )
        }
)
public class EventReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_parking_zone_id", nullable = false)
    private EventParkingZone eventParkingZone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public EventReservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public EventParkingZone getEventParkingZone() {
        return eventParkingZone;
    }

    public void setEventParkingZone(EventParkingZone eventParkingZone) {
        this.eventParkingZone = eventParkingZone;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
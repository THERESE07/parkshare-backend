package rw.parkshare.backend.common.dto;

import java.time.OffsetDateTime;

public class EventReservationResponseDto {

    private Long id;
    private Long eventId;
    private Long eventParkingZoneId;
    private Long reservationId;
    private OffsetDateTime createdAt;

    public EventReservationResponseDto() {
    }

    public EventReservationResponseDto(
            Long id,
            Long eventId,
            Long eventParkingZoneId,
            Long reservationId,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.eventParkingZoneId = eventParkingZoneId;
        this.reservationId = reservationId;
        this.createdAt = createdAt;
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

    public Long getEventParkingZoneId() {
        return eventParkingZoneId;
    }

    public void setEventParkingZoneId(Long eventParkingZoneId) {
        this.eventParkingZoneId = eventParkingZoneId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

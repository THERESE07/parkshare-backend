package rw.parkshare.backend.common.dto;

public class EventReservationRequestDto {

    private Long eventId;
    private Long eventParkingZoneId;
    private Long reservationId;

    public EventReservationRequestDto() {
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
}

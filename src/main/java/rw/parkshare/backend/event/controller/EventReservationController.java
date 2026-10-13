package rw.parkshare.backend.event.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.EventReservationResponseDto;
import rw.parkshare.backend.event.service.EventReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/event-reservations")
public class EventReservationController {

    private final EventReservationService eventReservationService;

    public EventReservationController(
            EventReservationService eventReservationService
    ) {
        this.eventReservationService = eventReservationService;
    }

    @PostMapping
    public ResponseEntity<EventReservationResponseDto> createEventReservation(
            @RequestParam Long eventId,
            @RequestParam Long eventParkingZoneId,
            @RequestParam Long reservationId
    ) {
        EventReservationResponseDto created =
                eventReservationService.createEventReservation(
                        eventId,
                        eventParkingZoneId,
                        reservationId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventReservationResponseDto> getEventReservationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                eventReservationService.getEventReservationById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<EventReservationResponseDto>>
    getAllEventReservations() {
        return ResponseEntity.ok(
                eventReservationService.getAllEventReservations()
        );
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventReservationResponseDto>>
    getEventReservationsByEventId(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                eventReservationService
                        .getEventReservationsByEventId(eventId)
        );
    }

    @GetMapping("/parking-zone/{eventParkingZoneId}")
    public ResponseEntity<List<EventReservationResponseDto>>
    getEventReservationsByParkingZoneId(
            @PathVariable Long eventParkingZoneId
    ) {
        return ResponseEntity.ok(
                eventReservationService
                        .getEventReservationsByParkingZoneId(
                                eventParkingZoneId
                        )
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<EventReservationResponseDto>
    getEventReservationByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                eventReservationService
                        .getEventReservationByReservationId(
                                reservationId
                        )
        );
    }

    @GetMapping("/event/{eventId}/reservation/{reservationId}")
    public ResponseEntity<EventReservationResponseDto>
    getEventReservationByEventAndReservation(
            @PathVariable Long eventId,
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                eventReservationService
                        .getEventReservationByEventAndReservation(
                                eventId,
                                reservationId
                        )
        );
    }

    @GetMapping(
            "/parking-zone/{eventParkingZoneId}/reservation/{reservationId}"
    )
    public ResponseEntity<EventReservationResponseDto>
    getEventReservationByParkingZoneAndReservation(
            @PathVariable Long eventParkingZoneId,
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                eventReservationService
                        .getEventReservationByParkingZoneAndReservation(
                                eventParkingZoneId,
                                reservationId
                        )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByEventAndReservation(
            @RequestParam Long eventId,
            @RequestParam Long reservationId
    ) {
        return ResponseEntity.ok(
                eventReservationService
                        .existsByEventAndReservation(
                                eventId,
                                reservationId
                        )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventReservation(
            @PathVariable Long id
    ) {
        eventReservationService.deleteEventReservation(id);

        return ResponseEntity.noContent().build();
    }
}
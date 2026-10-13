package rw.parkshare.backend.event.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.EventRequestDto;
import rw.parkshare.backend.common.dto.EventResponseDto;
import rw.parkshare.backend.event.service.EventService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(
            EventService eventService
    ) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(
            @RequestParam Long organizerId,
            @RequestBody EventRequestDto request
    ) {
        EventResponseDto createdEvent =
                eventService.createEvent(
                        organizerId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdEvent);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                eventService.getEventById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllEvents() {
        return ResponseEntity.ok(
                eventService.getAllEvents()
        );
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<EventResponseDto>>
    getEventsByOrganizerId(
            @PathVariable Long organizerId
    ) {
        return ResponseEntity.ok(
                eventService.getEventsByOrganizerId(
                        organizerId
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventResponseDto>> getEventsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                eventService.getEventsByStatus(status)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponseDto>> searchEventsByName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                eventService.searchEventsByName(name)
        );
    }

    @GetMapping("/venue")
    public ResponseEntity<List<EventResponseDto>>
    getEventsByVenueName(
            @RequestParam String venueName
    ) {
        return ResponseEntity.ok(
                eventService.getEventsByVenueName(
                        venueName
                )
        );
    }

    @GetMapping("/organizer/{organizerId}/status/{status}")
    public ResponseEntity<List<EventResponseDto>>
    getEventsByOrganizerAndStatus(
            @PathVariable Long organizerId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                eventService.getEventsByOrganizerAndStatus(
                        organizerId,
                        status
                )
        );
    }

    @GetMapping("/starting-between")
    public ResponseEntity<List<EventResponseDto>>
    getEventsStartingBetween(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                eventService.getEventsStartingBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/ending-between")
    public ResponseEntity<List<EventResponseDto>>
    getEventsEndingBetween(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                eventService.getEventsEndingBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/starting-after")
    public ResponseEntity<List<EventResponseDto>>
    getEventsStartingAfter(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startTime
    ) {
        return ResponseEntity.ok(
                eventService.getEventsStartingAfter(
                        startTime
                )
        );
    }

    @GetMapping("/ending-before")
    public ResponseEntity<List<EventResponseDto>>
    getEventsEndingBefore(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                eventService.getEventsEndingBefore(
                        endTime
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(
            @PathVariable Long id,
            @RequestBody EventRequestDto request
    ) {
        return ResponseEntity.ok(
                eventService.updateEvent(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponseDto> updateEventStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                eventService.updateEventStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id
    ) {
        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }
}
package rw.parkshare.backend.event.controller;

import rw.parkshare.backend.common.dto.EventParkingZoneRequestDto;
import rw.parkshare.backend.common.dto.EventParkingZoneResponseDto;
import rw.parkshare.backend.event.service.EventParkingZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/event-parking-zones")
public class EventParkingZoneController {

    private final EventParkingZoneService eventParkingZoneService;

    public EventParkingZoneController(
            EventParkingZoneService eventParkingZoneService
    ) {
        this.eventParkingZoneService = eventParkingZoneService;
    }

    @PostMapping
    public ResponseEntity<EventParkingZoneResponseDto> createEventParkingZone(
            @RequestParam Long eventId,
            @RequestParam Long parkingLotId,
            @RequestBody EventParkingZoneRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                eventParkingZoneService.createEventParkingZone(
                        eventId,
                        parkingLotId,
                        request
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventParkingZoneResponseDto> getEventParkingZoneById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getEventParkingZoneById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<EventParkingZoneResponseDto>> getAllEventParkingZones() {
        return ResponseEntity.ok(
                eventParkingZoneService.getAllEventParkingZones()
        );
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventParkingZoneResponseDto>> getEventParkingZonesByEventId(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getEventParkingZonesByEventId(eventId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<EventParkingZoneResponseDto>> getEventParkingZonesByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getEventParkingZonesByParkingLotId(
                        parkingLotId
                )
        );
    }

    @GetMapping("/event/{eventId}/parking-lot/{parkingLotId}")
    public ResponseEntity<EventParkingZoneResponseDto> getByEventAndParkingLot(
            @PathVariable Long eventId,
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getByEventAndParkingLot(
                        eventId,
                        parkingLotId
                )
        );
    }

    @GetMapping("/event/{eventId}/status/{status}")
    public ResponseEntity<List<EventParkingZoneResponseDto>> getEventParkingZonesByEventAndStatus(
            @PathVariable Long eventId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getEventParkingZonesByEventAndStatus(
                        eventId,
                        status
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/status/{status}")
    public ResponseEntity<List<EventParkingZoneResponseDto>> getEventParkingZonesByParkingLotAndStatus(
            @PathVariable Long parkingLotId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getEventParkingZonesByParkingLotAndStatus(
                        parkingLotId,
                        status
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EventParkingZoneResponseDto>> getEventParkingZonesByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getEventParkingZonesByStatus(status)
        );
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<EventParkingZoneResponseDto>> getEventParkingZonesByCurrency(
            @PathVariable String currency
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.getEventParkingZonesByCurrency(currency)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventParkingZoneResponseDto> updateEventParkingZone(
            @PathVariable Long id,
            @RequestBody EventParkingZoneRequestDto request
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.updateEventParkingZone(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/counts")
    public ResponseEntity<EventParkingZoneResponseDto> updateCounts(
            @PathVariable Long id,
            @RequestParam Integer reservedCount,
            @RequestParam Integer occupiedCount,
            @RequestParam Integer availableCount
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.updateCounts(
                        id,
                        reservedCount,
                        occupiedCount,
                        availableCount
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventParkingZoneResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                eventParkingZoneService.updateStatus(
                        id,
                        status
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEventParkingZone(
            @PathVariable Long id
    ) {
        eventParkingZoneService.deleteEventParkingZone(id);
        return ResponseEntity.noContent().build();
    }
}
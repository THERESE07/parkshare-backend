package rw.parkshare.backend.reservation.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ReservationStatusHistoryRequestDto;
import rw.parkshare.backend.common.dto.ReservationStatusHistoryResponseDto;
import rw.parkshare.backend.reservation.service.ReservationStatusHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/reservation-status-history")
public class ReservationStatusHistoryController {

    private final ReservationStatusHistoryService historyService;

    public ReservationStatusHistoryController(
            ReservationStatusHistoryService historyService
    ) {
        this.historyService = historyService;
    }

    @PostMapping
    public ResponseEntity<ReservationStatusHistoryResponseDto> createHistory(
            @RequestParam Long reservationId,
            @RequestParam(required = false) Long changedByUserId,
            @Valid @RequestBody ReservationStatusHistoryRequestDto request
    ) {
        ReservationStatusHistoryResponseDto history =
                historyService.createHistory(
                        reservationId,
                        changedByUserId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(history);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationStatusHistoryResponseDto>
    getHistoryById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                historyService.getHistoryById(id)
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<ReservationStatusHistoryResponseDto>>
    getHistoryByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                historyService.getHistoryByReservationId(
                        reservationId
                )
        );
    }

    @GetMapping("/changed-by/{userId}")
    public ResponseEntity<List<ReservationStatusHistoryResponseDto>>
    getHistoryByChangedByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                historyService.getHistoryByChangedByUserId(
                        userId
                )
        );
    }

    @GetMapping("/new-status/{newStatus}")
    public ResponseEntity<List<ReservationStatusHistoryResponseDto>>
    getHistoryByNewStatus(
            @PathVariable String newStatus
    ) {
        return ResponseEntity.ok(
                historyService.getHistoryByNewStatus(
                        newStatus
                )
        );
    }

    @GetMapping("/old-status/{oldStatus}")
    public ResponseEntity<List<ReservationStatusHistoryResponseDto>>
    getHistoryByOldStatus(
            @PathVariable String oldStatus
    ) {
        return ResponseEntity.ok(
                historyService.getHistoryByOldStatus(
                        oldStatus
                )
        );
    }

    @GetMapping(
            "/reservation/{reservationId}/new-status/{newStatus}"
    )
    public ResponseEntity<List<ReservationStatusHistoryResponseDto>>
    getHistoryByReservationAndNewStatus(
            @PathVariable Long reservationId,
            @PathVariable String newStatus
    ) {
        return ResponseEntity.ok(
                historyService.getHistoryByReservationAndNewStatus(
                        reservationId,
                        newStatus
                )
        );
    }

    @PostMapping("/record-status-change")
    public ResponseEntity<ReservationStatusHistoryResponseDto>
    recordStatusChange(
            @RequestParam Long reservationId,
            @RequestParam(required = false) Long changedByUserId,
            @Valid @RequestBody ReservationStatusHistoryRequestDto request
    ) {
        ReservationStatusHistoryResponseDto history =
                historyService.recordStatusChange(
                        reservationId,
                        changedByUserId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(history);
    }

    @PostMapping("/record-status-change/system")
    public ResponseEntity<ReservationStatusHistoryResponseDto>
    recordSystemStatusChange(
            @RequestParam Long reservationId,
            @Valid @RequestBody ReservationStatusHistoryRequestDto request
    ) {
        ReservationStatusHistoryResponseDto history =
                historyService.recordStatusChange(
                        reservationId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(history);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationStatusHistoryResponseDto>
    updateHistory(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusHistoryRequestDto request
    ) {
        return ResponseEntity.ok(
                historyService.updateHistory(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(
            @PathVariable Long id
    ) {
        historyService.deleteHistory(id);

        return ResponseEntity.noContent().build();
    }
}
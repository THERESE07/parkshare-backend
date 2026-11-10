package rw.parkshare.backend.reservation.controller;

import rw.parkshare.backend.common.dto.PageResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ReservationRequestDto;
import rw.parkshare.backend.common.dto.ReservationResponseDto;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import rw.parkshare.backend.reservation.service.ReservationService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    /**
     * Books parking for the logged-in driver. userId is only honoured for
     * admins booking on someone's behalf. Price and code are set by the server.
     */
    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestParam(required = false) Long userId,
            @RequestParam Long vehicleId,
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) Long parkingSpaceId,
            @Valid @RequestBody ReservationRequestDto request
    ) {
        ReservationResponseDto createdReservation =
                reservationService.createReservation(
                        userId,
                        vehicleId,
                        parkingLotId,
                        parkingSpaceId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReservation);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> getReservationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationById(id)
        );
    }

    @GetMapping("/code/{reservationCode}")
    public ResponseEntity<ReservationResponseDto> getReservationByCode(
            @PathVariable String reservationCode
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationByCode(
                        reservationCode
                )
        );
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReservationResponseDto>>
    getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                reservationService.getAllReservations(page, size)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<ReservationResponseDto>>
    getReservationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByUserId(userId, page, size)
        );
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsByVehicleId(
            @PathVariable Long vehicleId
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByVehicleId(vehicleId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByParkingLotId(
                        parkingLotId
                )
        );
    }

    @GetMapping("/parking-space/{parkingSpaceId}")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsByParkingSpaceId(
            @PathVariable Long parkingSpaceId
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByParkingSpaceId(
                        parkingSpaceId
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsByStatus(
            @PathVariable ReservationStatus status
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByStatus(status)
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable ReservationStatus status
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByUserAndStatus(
                        userId,
                        status
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/status/{status}")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsByParkingLotAndStatus(
            @PathVariable Long parkingLotId,
            @PathVariable ReservationStatus status
    ) {
        return ResponseEntity.ok(
                reservationService
                        .getReservationsByParkingLotAndStatus(
                                parkingLotId,
                                status
                        )
        );
    }

    @GetMapping("/between")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/between")
    public ResponseEntity<List<ReservationResponseDto>>
    getParkingLotReservationsBetween(
            @PathVariable Long parkingLotId,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                reservationService.getParkingLotReservationsBetween(
                        parkingLotId,
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/code/exists")
    public ResponseEntity<Boolean> reservationCodeExists(
            @RequestParam String reservationCode
    ) {
        return ResponseEntity.ok(
                reservationService.reservationCodeExists(
                        reservationCode
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDto> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequestDto request
    ) {
        return ResponseEntity.ok(
                reservationService.updateReservation(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponseDto>
    updateReservationStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status
    ) {
        return ResponseEntity.ok(
                reservationService.updateReservationStatus(
                        id,
                        status
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationService.CancellationResult> cancelReservation(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                reservationService.cancelReservation(id)
        );
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ReservationResponseDto> updateNotes(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body
    ) {
        return ResponseEntity.ok(reservationService.updateNotes(id, body.get("notes")));
    }

    /** QR token for the driver's app to render; scanned at the entrance. */
    @GetMapping("/{id}/qr")
    public ResponseEntity<rw.parkshare.backend.reservation.service.QrTokenService.CurrentQr> getQrCode(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(reservationService.getQrCode(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}
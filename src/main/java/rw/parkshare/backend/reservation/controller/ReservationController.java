package rw.parkshare.backend.reservation.controller;

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

    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestParam Long userId,
            @RequestParam Long vehicleId,
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) Long parkingSpaceId,
            @RequestBody ReservationRequestDto request
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
    public ResponseEntity<List<ReservationResponseDto>>
    getAllReservations() {
        return ResponseEntity.ok(
                reservationService.getAllReservations()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationResponseDto>>
    getReservationsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByUserId(userId)
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
            @RequestBody ReservationRequestDto request
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
    public ResponseEntity<ReservationResponseDto> cancelReservation(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                reservationService.cancelReservation(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }
}
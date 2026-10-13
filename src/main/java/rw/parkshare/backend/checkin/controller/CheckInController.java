package rw.parkshare.backend.checkin.controller;

import rw.parkshare.backend.checkin.service.CheckInService;
import rw.parkshare.backend.common.dto.CheckInResponseDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public ResponseEntity<CheckInResponseDto> createCheckIn(
            @RequestParam Long reservationId,
            @RequestParam Long vehicleId,
            @RequestParam(required = false) Long parkingSpaceId,
            @RequestParam(required = false) Long checkedInById,
            @RequestParam String checkInMethod,
            @RequestParam(required = false) OffsetDateTime checkedInAt,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                checkInService.createCheckIn(
                        reservationId,
                        vehicleId,
                        parkingSpaceId,
                        checkedInById,
                        checkInMethod,
                        checkedInAt,
                        notes
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckInResponseDto> getCheckInById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                checkInService.getCheckInById(id)
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<CheckInResponseDto> getCheckInByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                checkInService.getCheckInByReservationId(reservationId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CheckInResponseDto>> getAllCheckIns() {
        return ResponseEntity.ok(
                checkInService.getAllCheckIns()
        );
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<CheckInResponseDto>> getCheckInsByVehicleId(
            @PathVariable Long vehicleId
    ) {
        return ResponseEntity.ok(
                checkInService.getCheckInsByVehicleId(vehicleId)
        );
    }

    @GetMapping("/parking-space/{parkingSpaceId}")
    public ResponseEntity<List<CheckInResponseDto>> getCheckInsByParkingSpaceId(
            @PathVariable Long parkingSpaceId
    ) {
        return ResponseEntity.ok(
                checkInService.getCheckInsByParkingSpaceId(parkingSpaceId)
        );
    }

    @GetMapping("/checked-in-by/{userId}")
    public ResponseEntity<List<CheckInResponseDto>> getCheckInsByCheckedInById(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                checkInService.getCheckInsByCheckedInById(userId)
        );
    }

    @GetMapping("/method/{checkInMethod}")
    public ResponseEntity<List<CheckInResponseDto>> getCheckInsByMethod(
            @PathVariable String checkInMethod
    ) {
        return ResponseEntity.ok(
                checkInService.getCheckInsByMethod(checkInMethod)
        );
    }

    @GetMapping("/between")
    public ResponseEntity<List<CheckInResponseDto>> getCheckInsBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                checkInService.getCheckInsBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> hasCheckInForReservation(
            @RequestParam Long reservationId
    ) {
        return ResponseEntity.ok(
                checkInService.hasCheckInForReservation(reservationId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CheckInResponseDto> updateCheckIn(
            @PathVariable Long id,
            @RequestParam(required = false) Long parkingSpaceId,
            @RequestParam(required = false) Long checkedInById,
            @RequestParam(required = false) String checkInMethod,
            @RequestParam(required = false) OffsetDateTime checkedInAt,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.ok(
                checkInService.updateCheckIn(
                        id,
                        parkingSpaceId,
                        checkedInById,
                        checkInMethod,
                        checkedInAt,
                        notes
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckIn(
            @PathVariable Long id
    ) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.noContent().build();
    }
}
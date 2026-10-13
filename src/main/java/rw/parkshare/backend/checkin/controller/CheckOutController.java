package rw.parkshare.backend.checkin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.checkin.service.CheckOutService;
import rw.parkshare.backend.common.dto.CheckOutResponseDto;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/check-outs")
public class CheckOutController {

    private final CheckOutService checkOutService;

    public CheckOutController(CheckOutService checkOutService) {
        this.checkOutService = checkOutService;
    }

    @PostMapping
    public ResponseEntity<CheckOutResponseDto> createCheckOut(
            @RequestParam Long checkInId,
            @RequestParam Long reservationId,
            @RequestParam(required = false) Long checkedOutById,
            @RequestParam String checkOutMethod,
            @RequestParam(required = false) OffsetDateTime checkedOutAt,
            @RequestParam(required = false) String notes
    ) {
        CheckOutResponseDto created = checkOutService.createCheckOut(
                checkInId,
                reservationId,
                checkedOutById,
                checkOutMethod,
                checkedOutAt,
                notes
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckOutResponseDto> getCheckOutById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                checkOutService.getCheckOutById(id)
        );
    }

    @GetMapping("/check-in/{checkInId}")
    public ResponseEntity<CheckOutResponseDto> getCheckOutByCheckInId(
            @PathVariable Long checkInId
    ) {
        return ResponseEntity.ok(
                checkOutService.getCheckOutByCheckInId(checkInId)
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<CheckOutResponseDto> getCheckOutByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                checkOutService.getCheckOutByReservationId(reservationId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CheckOutResponseDto>> getAllCheckOuts() {
        return ResponseEntity.ok(
                checkOutService.getAllCheckOuts()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CheckOutResponseDto>> getCheckOutsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                checkOutService.getCheckOutsByUserId(userId)
        );
    }

    @GetMapping("/method/{checkOutMethod}")
    public ResponseEntity<List<CheckOutResponseDto>> getCheckOutsByMethod(
            @PathVariable String checkOutMethod
    ) {
        return ResponseEntity.ok(
                checkOutService.getCheckOutsByMethod(checkOutMethod)
        );
    }

    @GetMapping("/between")
    public ResponseEntity<List<CheckOutResponseDto>> getCheckOutsBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                checkOutService.getCheckOutsBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/exists/check-in")
    public ResponseEntity<Boolean> hasCheckOutForCheckIn(
            @RequestParam Long checkInId
    ) {
        return ResponseEntity.ok(
                checkOutService.hasCheckOutForCheckIn(checkInId)
        );
    }

    @GetMapping("/exists/reservation")
    public ResponseEntity<Boolean> hasCheckOutForReservation(
            @RequestParam Long reservationId
    ) {
        return ResponseEntity.ok(
                checkOutService.hasCheckOutForReservation(reservationId)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CheckOutResponseDto> updateCheckOut(
            @PathVariable Long id,
            @RequestParam(required = false) Long checkedOutById,
            @RequestParam(required = false) String checkOutMethod,
            @RequestParam(required = false) OffsetDateTime checkedOutAt,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.ok(
                checkOutService.updateCheckOut(
                        id,
                        checkedOutById,
                        checkOutMethod,
                        checkedOutAt,
                        notes
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckOut(
            @PathVariable Long id
    ) {
        checkOutService.deleteCheckOut(id);

        return ResponseEntity.noContent().build();
    }
}
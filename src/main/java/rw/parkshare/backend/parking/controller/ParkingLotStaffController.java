package rw.parkshare.backend.parking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ParkingLotStaffResponseDto;
import rw.parkshare.backend.parking.service.ParkingLotStaffService;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lot-staff")
public class ParkingLotStaffController {

    private final ParkingLotStaffService parkingLotStaffService;

    public ParkingLotStaffController(
            ParkingLotStaffService parkingLotStaffService
    ) {
        this.parkingLotStaffService = parkingLotStaffService;
    }

    @PostMapping("/parking-lot/{parkingLotId}/user/{userId}")
    public ResponseEntity<ParkingLotStaffResponseDto> assignStaff(
            @PathVariable Long parkingLotId,
            @PathVariable Long userId
    ) {
        ParkingLotStaffResponseDto staff =
                parkingLotStaffService.assignStaff(
                        parkingLotId,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(staff);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLotStaffResponseDto> getStaffById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService.getStaffById(id)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ParkingLotStaffResponseDto>>
    getStaffByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService
                        .getStaffByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ParkingLotStaffResponseDto>>
    getStaffByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService.getStaffByUserId(userId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/active")
    public ResponseEntity<List<ParkingLotStaffResponseDto>>
    getActiveStaffByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService
                        .getActiveStaffByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<ParkingLotStaffResponseDto>>
    getActiveStaffByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService
                        .getActiveStaffByUserId(userId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/user/{userId}")
    public ResponseEntity<ParkingLotStaffResponseDto>
    getStaffAssignment(
            @PathVariable Long parkingLotId,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService.getStaffAssignment(
                        parkingLotId,
                        userId
                )
        );
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isActiveStaff(
            @RequestParam Long parkingLotId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService.isActiveStaff(
                        parkingLotId,
                        userId
                )
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ParkingLotStaffResponseDto> deactivateStaff(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService.deactivateStaff(id)
        );
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ParkingLotStaffResponseDto> reactivateStaff(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingLotStaffService.reactivateStaff(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(
            @PathVariable Long id
    ) {
        parkingLotStaffService.deleteStaff(id);

        return ResponseEntity.noContent().build();
    }
}
package rw.parkshare.backend.parking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ParkingOperatingHoursRequestDto;
import rw.parkshare.backend.common.dto.ParkingOperatingHoursResponseDto;
import rw.parkshare.backend.parking.service.ParkingOperatingHoursService;

import java.util.List;

@RestController
@RequestMapping("/api/parking-operating-hours")
public class ParkingOperatingHoursController {

    private final ParkingOperatingHoursService operatingHoursService;

    public ParkingOperatingHoursController(
            ParkingOperatingHoursService operatingHoursService
    ) {
        this.operatingHoursService = operatingHoursService;
    }

    @PostMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<ParkingOperatingHoursResponseDto> createOperatingHours(
            @PathVariable Long parkingLotId,
            @RequestBody ParkingOperatingHoursRequestDto request
    ) {
        ParkingOperatingHoursResponseDto createdHours =
                operatingHoursService.createOperatingHours(
                        parkingLotId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdHours);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingOperatingHoursResponseDto> getOperatingHoursById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                operatingHoursService.getOperatingHoursById(id)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ParkingOperatingHoursResponseDto>>
    getOperatingHoursByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                operatingHoursService
                        .getOperatingHoursByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/day")
    public ResponseEntity<ParkingOperatingHoursResponseDto> getOperatingHoursByDay(
            @PathVariable Long parkingLotId,
            @RequestParam String dayOfWeek
    ) {
        return ResponseEntity.ok(
                operatingHoursService.getOperatingHoursByDay(
                        parkingLotId,
                        dayOfWeek
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/closed")
    public ResponseEntity<List<ParkingOperatingHoursResponseDto>>
    getOperatingHoursByClosedStatus(
            @PathVariable Long parkingLotId,
            @RequestParam boolean closed
    ) {
        return ResponseEntity.ok(
                operatingHoursService
                        .getOperatingHoursByClosedStatus(
                                parkingLotId,
                                closed
                        )
        );
    }

    @GetMapping("/day")
    public ResponseEntity<List<ParkingOperatingHoursResponseDto>>
    getOperatingHoursByDay(
            @RequestParam String dayOfWeek
    ) {
        return ResponseEntity.ok(
                operatingHoursService.getOperatingHoursByDay(
                        dayOfWeek
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> operatingHoursExist(
            @RequestParam Long parkingLotId,
            @RequestParam String dayOfWeek
    ) {
        return ResponseEntity.ok(
                operatingHoursService.operatingHoursExist(
                        parkingLotId,
                        dayOfWeek
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingOperatingHoursResponseDto> updateOperatingHours(
            @PathVariable Long id,
            @RequestBody ParkingOperatingHoursRequestDto request
    ) {
        return ResponseEntity.ok(
                operatingHoursService.updateOperatingHours(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/parking-lot/{parkingLotId}/day/{dayOfWeek}/close")
    public ResponseEntity<ParkingOperatingHoursResponseDto> closeDay(
            @PathVariable Long parkingLotId,
            @PathVariable String dayOfWeek
    ) {
        return ResponseEntity.ok(
                operatingHoursService.closeDay(
                        parkingLotId,
                        dayOfWeek
                )
        );
    }

    @PatchMapping("/parking-lot/{parkingLotId}/day/{dayOfWeek}/open")
    public ResponseEntity<ParkingOperatingHoursResponseDto> openDay(
            @PathVariable Long parkingLotId,
            @PathVariable String dayOfWeek
    ) {
        return ResponseEntity.ok(
                operatingHoursService.openDay(
                        parkingLotId,
                        dayOfWeek
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperatingHours(
            @PathVariable Long id
    ) {
        operatingHoursService.deleteOperatingHours(id);

        return ResponseEntity.noContent().build();
    }
}
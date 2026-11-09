package rw.parkshare.backend.parking.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ParkingLotRequestDto;
import rw.parkshare.backend.common.dto.ParkingLotResponseDto;
import rw.parkshare.backend.parking.service.ParkingLotService;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    public ParkingLotController(
            ParkingLotService parkingLotService
    ) {
        this.parkingLotService = parkingLotService;
    }

    @PostMapping("/operator/{operatorId}")
    public ResponseEntity<ParkingLotResponseDto> createParkingLot(
            @PathVariable Long operatorId,
            @Valid @RequestBody ParkingLotRequestDto request
    ) {
        ParkingLotResponseDto createdParkingLot =
                parkingLotService.createParkingLot(
                        operatorId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdParkingLot);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingLotResponseDto> getParkingLotById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingLotService.getParkingLotById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<ParkingLotResponseDto>>
    getAllParkingLots() {
        return ResponseEntity.ok(
                parkingLotService.getAllParkingLots()
        );
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<ParkingLotResponseDto>>
    getParkingLotsByOperatorId(
            @PathVariable Long operatorId
    ) {
        return ResponseEntity.ok(
                parkingLotService.getParkingLotsByOperatorId(
                        operatorId
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParkingLotResponseDto>>
    getParkingLotsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                parkingLotService.getParkingLotsByStatus(
                        status
                )
        );
    }

    @GetMapping("/city")
    public ResponseEntity<List<ParkingLotResponseDto>>
    getParkingLotsByCity(
            @RequestParam String city
    ) {
        return ResponseEntity.ok(
                parkingLotService.getParkingLotsByCity(city)
        );
    }

    @GetMapping("/operator/{operatorId}/status/{status}")
    public ResponseEntity<List<ParkingLotResponseDto>>
    getParkingLotsByOperatorAndStatus(
            @PathVariable Long operatorId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                parkingLotService
                        .getParkingLotsByOperatorAndStatus(
                                operatorId,
                                status
                        )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<ParkingLotResponseDto>>
    searchParkingLotsByName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                parkingLotService.searchParkingLotsByName(name)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingLotResponseDto> updateParkingLot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingLotRequestDto request
    ) {
        return ResponseEntity.ok(
                parkingLotService.updateParkingLot(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParkingLotResponseDto>
    updateParkingLotStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                parkingLotService.updateParkingLotStatus(
                        id,
                        status
                )
        );
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> parkingLotExists(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingLotService.parkingLotExists(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParkingLot(
            @PathVariable Long id
    ) {
        parkingLotService.deleteParkingLot(id);

        return ResponseEntity.noContent().build();
    }
}
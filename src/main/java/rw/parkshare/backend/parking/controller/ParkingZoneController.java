package rw.parkshare.backend.parking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ParkingZoneRequestDto;
import rw.parkshare.backend.common.dto.ParkingZoneResponseDto;
import rw.parkshare.backend.parking.service.ParkingZoneService;

import java.util.List;

@RestController
@RequestMapping("/api/parking-zones")
public class ParkingZoneController {

    private final ParkingZoneService parkingZoneService;

    public ParkingZoneController(
            ParkingZoneService parkingZoneService
    ) {
        this.parkingZoneService = parkingZoneService;
    }

    @PostMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<ParkingZoneResponseDto> createZone(
            @PathVariable Long parkingLotId,
            @RequestBody ParkingZoneRequestDto request
    ) {
        ParkingZoneResponseDto createdZone =
                parkingZoneService.createZone(
                        parkingLotId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdZone);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingZoneResponseDto> getZoneById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingZoneService.getZoneById(id)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ParkingZoneResponseDto>>
    getZonesByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                parkingZoneService
                        .getZonesByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/status/{status}")
    public ResponseEntity<List<ParkingZoneResponseDto>>
    getZonesByParkingLotAndStatus(
            @PathVariable Long parkingLotId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                parkingZoneService
                        .getZonesByParkingLotAndStatus(
                                parkingLotId,
                                status
                        )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParkingZoneResponseDto>>
    getZonesByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                parkingZoneService.getZonesByStatus(status)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/name")
    public ResponseEntity<ParkingZoneResponseDto> getZoneByName(
            @PathVariable Long parkingLotId,
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                parkingZoneService.getZoneByName(
                        parkingLotId,
                        name
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<ParkingZoneResponseDto>>
    searchZonesByName(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                parkingZoneService.searchZonesByName(name)
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> zoneExists(
            @RequestParam Long parkingLotId,
            @RequestParam String name
    ) {
        return ResponseEntity.ok(
                parkingZoneService.zoneExists(
                        parkingLotId,
                        name
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingZoneResponseDto> updateZone(
            @PathVariable Long id,
            @RequestBody ParkingZoneRequestDto request
    ) {
        return ResponseEntity.ok(
                parkingZoneService.updateZone(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParkingZoneResponseDto> updateZoneStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                parkingZoneService.updateZoneStatus(
                        id,
                        status
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ParkingZoneResponseDto> activateZone(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingZoneService.activateZone(id)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ParkingZoneResponseDto> deactivateZone(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingZoneService.deactivateZone(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(
            @PathVariable Long id
    ) {
        parkingZoneService.deleteZone(id);

        return ResponseEntity.noContent().build();
    }
}
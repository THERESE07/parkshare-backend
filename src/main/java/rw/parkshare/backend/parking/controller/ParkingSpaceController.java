package rw.parkshare.backend.parking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ParkingSpaceRequestDto;
import rw.parkshare.backend.common.dto.ParkingSpaceResponseDto;
import rw.parkshare.backend.parking.service.ParkingSpaceService;

import java.util.List;

@RestController
@RequestMapping("/api/parking-spaces")
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    public ParkingSpaceController(
            ParkingSpaceService parkingSpaceService
    ) {
        this.parkingSpaceService = parkingSpaceService;
    }

    @PostMapping("/zone/{parkingZoneId}")
    public ResponseEntity<ParkingSpaceResponseDto> createSpace(
            @PathVariable Long parkingZoneId,
            @RequestBody ParkingSpaceRequestDto request
    ) {
        ParkingSpaceResponseDto createdSpace =
                parkingSpaceService.createSpace(
                        parkingZoneId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdSpace);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpaceResponseDto> getSpaceById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.getSpaceById(id)
        );
    }

    @GetMapping("/zone/{parkingZoneId}")
    public ResponseEntity<List<ParkingSpaceResponseDto>>
    getSpacesByParkingZoneId(
            @PathVariable Long parkingZoneId
    ) {
        return ResponseEntity.ok(
                parkingSpaceService
                        .getSpacesByParkingZoneId(parkingZoneId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParkingSpaceResponseDto>> getSpacesByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.getSpacesByStatus(status)
        );
    }

    @GetMapping("/zone/{parkingZoneId}/status/{status}")
    public ResponseEntity<List<ParkingSpaceResponseDto>>
    getSpacesByZoneAndStatus(
            @PathVariable Long parkingZoneId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.getSpacesByZoneAndStatus(
                        parkingZoneId,
                        status
                )
        );
    }

    @GetMapping("/code")
    public ResponseEntity<ParkingSpaceResponseDto> getSpaceByCode(
            @RequestParam String spaceCode
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.getSpaceByCode(spaceCode)
        );
    }

    @GetMapping("/type/{spaceType}")
    public ResponseEntity<List<ParkingSpaceResponseDto>> getSpacesByType(
            @PathVariable String spaceType
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.getSpacesByType(spaceType)
        );
    }

    @GetMapping("/zone/{parkingZoneId}/type/{spaceType}")
    public ResponseEntity<List<ParkingSpaceResponseDto>>
    getSpacesByZoneAndType(
            @PathVariable Long parkingZoneId,
            @PathVariable String spaceType
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.getSpacesByZoneAndType(
                        parkingZoneId,
                        spaceType
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> spaceExists(
            @RequestParam Long parkingZoneId,
            @RequestParam String spaceCode
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.spaceExists(
                        parkingZoneId,
                        spaceCode
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingSpaceResponseDto> updateSpace(
            @PathVariable Long id,
            @RequestBody ParkingSpaceRequestDto request
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.updateSpace(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParkingSpaceResponseDto> updateSpaceStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.updateSpaceStatus(
                        id,
                        status
                )
        );
    }

    @PatchMapping("/{id}/available")
    public ResponseEntity<ParkingSpaceResponseDto> markSpaceAvailable(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.markSpaceAvailable(id)
        );
    }

    @PatchMapping("/{id}/occupied")
    public ResponseEntity<ParkingSpaceResponseDto> markSpaceOccupied(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.markSpaceOccupied(id)
        );
    }

    @PatchMapping("/{id}/reserved")
    public ResponseEntity<ParkingSpaceResponseDto> markSpaceReserved(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.markSpaceReserved(id)
        );
    }

    @PatchMapping("/{id}/maintenance")
    public ResponseEntity<ParkingSpaceResponseDto> markSpaceMaintenance(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingSpaceService.markSpaceMaintenance(id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(
            @PathVariable Long id
    ) {
        parkingSpaceService.deleteSpace(id);

        return ResponseEntity.noContent().build();
    }
}
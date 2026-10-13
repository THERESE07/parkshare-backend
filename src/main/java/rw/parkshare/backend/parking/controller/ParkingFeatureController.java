package rw.parkshare.backend.parking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ParkingFeatureRequestDto;
import rw.parkshare.backend.common.dto.ParkingFeatureResponseDto;
import rw.parkshare.backend.parking.service.ParkingFeatureService;

import java.util.List;

@RestController
@RequestMapping("/api/parking-features")
public class ParkingFeatureController {

    private final ParkingFeatureService parkingFeatureService;

    public ParkingFeatureController(
            ParkingFeatureService parkingFeatureService
    ) {
        this.parkingFeatureService = parkingFeatureService;
    }

    @PostMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<ParkingFeatureResponseDto> createFeature(
            @PathVariable Long parkingLotId,
            @RequestBody ParkingFeatureRequestDto request
    ) {
        ParkingFeatureResponseDto createdFeature =
                parkingFeatureService.createFeature(
                        parkingLotId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdFeature);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingFeatureResponseDto> getFeatureById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                parkingFeatureService.getFeatureById(id)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ParkingFeatureResponseDto>>
    getFeaturesByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                parkingFeatureService
                        .getFeaturesByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/code")
    public ResponseEntity<ParkingFeatureResponseDto>
    getFeatureByParkingLotAndCode(
            @PathVariable Long parkingLotId,
            @RequestParam String featureCode
    ) {
        return ResponseEntity.ok(
                parkingFeatureService
                        .getFeatureByParkingLotAndCode(
                                parkingLotId,
                                featureCode
                        )
        );
    }

    @GetMapping("/code/{featureCode}")
    public ResponseEntity<List<ParkingFeatureResponseDto>>
    getFeaturesByCode(
            @PathVariable String featureCode
    ) {
        return ResponseEntity.ok(
                parkingFeatureService.getFeaturesByCode(
                        featureCode
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> featureExists(
            @RequestParam Long parkingLotId,
            @RequestParam String featureCode
    ) {
        return ResponseEntity.ok(
                parkingFeatureService.featureExists(
                        parkingLotId,
                        featureCode
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingFeatureResponseDto> updateFeature(
            @PathVariable Long id,
            @RequestBody ParkingFeatureRequestDto request
    ) {
        return ResponseEntity.ok(
                parkingFeatureService.updateFeature(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeature(
            @PathVariable Long id
    ) {
        parkingFeatureService.deleteFeature(id);

        return ResponseEntity.noContent().build();
    }
}
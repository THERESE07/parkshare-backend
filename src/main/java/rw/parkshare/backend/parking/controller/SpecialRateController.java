package rw.parkshare.backend.parking.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.SpecialRateRequestDto;
import rw.parkshare.backend.common.dto.SpecialRateResponseDto;
import rw.parkshare.backend.parking.service.SpecialRateService;

import java.util.List;

@RestController
@RequestMapping("/api/special-rates")
public class SpecialRateController {

    private final SpecialRateService specialRateService;

    public SpecialRateController(SpecialRateService specialRateService) {
        this.specialRateService = specialRateService;
    }

    @PostMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<SpecialRateResponseDto> createSpecialRate(
            @PathVariable Long parkingLotId,
            @Valid @RequestBody SpecialRateRequestDto request
    ) {
        SpecialRateResponseDto createdRate =
                specialRateService.createSpecialRate(
                        parkingLotId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRate);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialRateResponseDto> getSpecialRateById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                specialRateService.getSpecialRateById(id)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<SpecialRateResponseDto>> getSpecialRatesByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                specialRateService.getSpecialRatesByParkingLotId(
                        parkingLotId
                )
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/active")
    public ResponseEntity<List<SpecialRateResponseDto>> getActiveSpecialRatesByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                specialRateService.getActiveSpecialRatesByParkingLotId(
                        parkingLotId
                )
        );
    }

    @GetMapping("/type/{rateType}")
    public ResponseEntity<List<SpecialRateResponseDto>> getSpecialRatesByType(
            @PathVariable String rateType
    ) {
        return ResponseEntity.ok(
                specialRateService.getSpecialRatesByType(rateType)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<SpecialRateResponseDto>> getAllActiveSpecialRates() {
        return ResponseEntity.ok(
                specialRateService.getAllActiveSpecialRates()
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/type/{rateType}")
    public ResponseEntity<List<SpecialRateResponseDto>> getSpecialRatesByParkingLotAndType(
            @PathVariable Long parkingLotId,
            @PathVariable String rateType
    ) {
        return ResponseEntity.ok(
                specialRateService.getSpecialRatesByParkingLotAndType(
                        parkingLotId,
                        rateType
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecialRateResponseDto> updateSpecialRate(
            @PathVariable Long id,
            @Valid @RequestBody SpecialRateRequestDto request
    ) {
        return ResponseEntity.ok(
                specialRateService.updateSpecialRate(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<SpecialRateResponseDto> activateSpecialRate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                specialRateService.activateSpecialRate(id)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SpecialRateResponseDto> deactivateSpecialRate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                specialRateService.deactivateSpecialRate(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SpecialRateResponseDto> updateSpecialRateStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(
                specialRateService.updateSpecialRateStatus(
                        id,
                        active
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialRate(
            @PathVariable Long id
    ) {
        specialRateService.deleteSpecialRate(id);

        return ResponseEntity.noContent().build();
    }
}
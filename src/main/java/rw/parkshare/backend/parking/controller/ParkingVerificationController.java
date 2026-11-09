package rw.parkshare.backend.parking.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ParkingVerificationRequestDto;
import rw.parkshare.backend.common.dto.ParkingVerificationResponseDto;
import rw.parkshare.backend.parking.service.ParkingVerificationService;

import java.util.List;

@RestController
@RequestMapping("/api/parking-verifications")
public class ParkingVerificationController {

    private final ParkingVerificationService verificationService;

    public ParkingVerificationController(
            ParkingVerificationService verificationService
    ) {
        this.verificationService = verificationService;
    }

    @PostMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<ParkingVerificationResponseDto> createVerification(
            @PathVariable Long parkingLotId,
            @Valid @RequestBody ParkingVerificationRequestDto request
    ) {
        ParkingVerificationResponseDto createdVerification =
                verificationService.createVerification(
                        parkingLotId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdVerification);
    }

    @PostMapping("/parking-lot/{parkingLotId}/verifier/{verifiedById}")
    public ResponseEntity<ParkingVerificationResponseDto>
    createVerificationWithVerifier(
            @PathVariable Long parkingLotId,
            @PathVariable Long verifiedById,
            @Valid @RequestBody ParkingVerificationRequestDto request
    ) {
        ParkingVerificationResponseDto createdVerification =
                verificationService.createVerification(
                        parkingLotId,
                        verifiedById,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdVerification);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingVerificationResponseDto>
    getVerificationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                verificationService.getVerificationById(id)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<List<ParkingVerificationResponseDto>>
    getVerificationsByParkingLotId(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                verificationService
                        .getVerificationsByParkingLotId(parkingLotId)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/status/{verificationStatus}")
    public ResponseEntity<List<ParkingVerificationResponseDto>>
    getVerificationsByParkingLotAndStatus(
            @PathVariable Long parkingLotId,
            @PathVariable String verificationStatus
    ) {
        return ResponseEntity.ok(
                verificationService
                        .getVerificationsByParkingLotAndStatus(
                                parkingLotId,
                                verificationStatus
                        )
        );
    }

    @GetMapping("/status/{verificationStatus}")
    public ResponseEntity<List<ParkingVerificationResponseDto>>
    getVerificationsByStatus(
            @PathVariable String verificationStatus
    ) {
        return ResponseEntity.ok(
                verificationService
                        .getVerificationsByStatus(
                                verificationStatus
                        )
        );
    }

    @GetMapping("/verifier/{verifiedById}")
    public ResponseEntity<List<ParkingVerificationResponseDto>>
    getVerificationsByVerifier(
            @PathVariable Long verifiedById
    ) {
        return ResponseEntity.ok(
                verificationService
                        .getVerificationsByVerifier(verifiedById)
        );
    }

    @GetMapping("/parking-lot/{parkingLotId}/latest")
    public ResponseEntity<ParkingVerificationResponseDto>
    getLatestVerification(
            @PathVariable Long parkingLotId
    ) {
        return ResponseEntity.ok(
                verificationService.getLatestVerification(
                        parkingLotId
                )
        );
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ParkingVerificationResponseDto>
    approveVerification(
            @PathVariable Long id,
            @RequestParam Long verifiedById,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.ok(
                verificationService.approveVerification(
                        id,
                        verifiedById,
                        notes
                )
        );
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ParkingVerificationResponseDto>
    rejectVerification(
            @PathVariable Long id,
            @RequestParam Long verifiedById,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.ok(
                verificationService.rejectVerification(
                        id,
                        verifiedById,
                        notes
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ParkingVerificationResponseDto>
    updateVerificationStatus(
            @PathVariable Long id,
            @RequestParam String verificationStatus
    ) {
        return ResponseEntity.ok(
                verificationService.updateVerificationStatus(
                        id,
                        verificationStatus
                )
        );
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ParkingVerificationResponseDto>
    addNotes(
            @PathVariable Long id,
            @RequestParam(required = false) String notes
    ) {
        return ResponseEntity.ok(
                verificationService.addNotes(
                        id,
                        notes
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVerification(
            @PathVariable Long id
    ) {
        verificationService.deleteVerification(id);

        return ResponseEntity.noContent().build();
    }
}
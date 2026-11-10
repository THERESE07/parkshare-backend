package rw.parkshare.backend.admin.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.admin.service.OperatorVerificationService;
import rw.parkshare.backend.common.dto.OperatorVerificationRequestDto;
import rw.parkshare.backend.common.dto.OperatorVerificationResponseDto;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/operator-verifications")
public class OperatorVerificationController {

    private final OperatorVerificationService
            operatorVerificationService;

    public OperatorVerificationController(
            OperatorVerificationService operatorVerificationService
    ) {
        this.operatorVerificationService =
                operatorVerificationService;
    }

    @PostMapping
    public ResponseEntity<OperatorVerificationResponseDto>
    createOperatorVerification(
            @RequestParam Long userId,
            @Valid @RequestBody OperatorVerificationRequestDto request
    ) {
        OperatorVerificationResponseDto created =
                operatorVerificationService
                        .createOperatorVerification(
                                userId,
                                request
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperatorVerificationResponseDto>
    getOperatorVerificationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getOperatorVerificationById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<OperatorVerificationResponseDto>
    getOperatorVerificationByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getOperatorVerificationByUserId(userId)
        );
    }

    @GetMapping
    public ResponseEntity<List<OperatorVerificationResponseDto>>
    getAllOperatorVerifications() {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getAllOperatorVerifications()
        );
    }

    @GetMapping("/reviewer/{userId}")
    public ResponseEntity<List<OperatorVerificationResponseDto>>
    getOperatorVerificationsByReviewer(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getOperatorVerificationsByReviewer(userId)
        );
    }

    @GetMapping("/status/{verificationStatus}")
    public ResponseEntity<List<OperatorVerificationResponseDto>>
    getOperatorVerificationsByStatus(
            @PathVariable String verificationStatus
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getOperatorVerificationsByStatus(
                                verificationStatus
                        )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<OperatorVerificationResponseDto>>
    searchByBusinessName(
            @RequestParam String businessName
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .searchByBusinessName(businessName)
        );
    }

    @GetMapping("/registration-number")
    public ResponseEntity<OperatorVerificationResponseDto>
    getByBusinessRegistrationNumber(
            @RequestParam String businessRegistrationNumber
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getByBusinessRegistrationNumber(
                                businessRegistrationNumber
                        )
        );
    }

    @GetMapping("/submitted-between")
    public ResponseEntity<List<OperatorVerificationResponseDto>>
    getOperatorVerificationsSubmittedBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getOperatorVerificationsSubmittedBetween(
                                startTime,
                                endTime
                        )
        );
    }

    @GetMapping("/reviewed-between")
    public ResponseEntity<List<OperatorVerificationResponseDto>>
    getOperatorVerificationsReviewedBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getOperatorVerificationsReviewedBetween(
                                startTime,
                                endTime
                        )
        );
    }

    @GetMapping("/status/{verificationStatus}/submitted-between")
    public ResponseEntity<List<OperatorVerificationResponseDto>>
    getOperatorVerificationsByStatusAndSubmittedBetween(
            @PathVariable String verificationStatus,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .getOperatorVerificationsByStatusAndSubmittedBetween(
                                verificationStatus,
                                startTime,
                                endTime
                        )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperatorVerificationResponseDto>
    updateOperatorVerification(
            @PathVariable Long id,
            @Valid @RequestBody OperatorVerificationRequestDto request
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .updateOperatorVerification(
                                id,
                                request
                        )
        );
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<OperatorVerificationResponseDto>
    reviewOperatorVerification(
            @PathVariable Long id,
            @RequestParam Long reviewerId,
            @Valid @RequestBody OperatorVerificationRequestDto request
    ) {
        return ResponseEntity.ok(
                operatorVerificationService
                        .reviewOperatorVerification(
                                id,
                                reviewerId,
                                request
                        )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperatorVerification(
            @PathVariable Long id
    ) {
        operatorVerificationService
                .deleteOperatorVerification(id);

        return ResponseEntity.noContent().build();
    }
}
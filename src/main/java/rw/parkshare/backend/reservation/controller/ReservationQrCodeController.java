package rw.parkshare.backend.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.ReservationQrCodeRequestDto;
import rw.parkshare.backend.common.dto.ReservationQrCodeResponseDto;
import rw.parkshare.backend.reservation.service.ReservationQrCodeService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservation-qr-codes")
public class ReservationQrCodeController {

    private final ReservationQrCodeService qrCodeService;

    public ReservationQrCodeController(
            ReservationQrCodeService qrCodeService
    ) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/reservation/{reservationId}")
    public ResponseEntity<ReservationQrCodeResponseDto> createQrCode(
            @PathVariable Long reservationId,
            @RequestBody ReservationQrCodeRequestDto request
    ) {
        ReservationQrCodeResponseDto createdQrCode =
                qrCodeService.createQrCode(
                        reservationId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdQrCode);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationQrCodeResponseDto> getQrCodeById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                qrCodeService.getQrCodeById(id)
        );
    }

    @GetMapping("/token/{tokenHash}")
    public ResponseEntity<ReservationQrCodeResponseDto>
    getQrCodeByTokenHash(
            @PathVariable String tokenHash
    ) {
        return ResponseEntity.ok(
                qrCodeService.getQrCodeByTokenHash(tokenHash)
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<ReservationQrCodeResponseDto>>
    getQrCodesByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                qrCodeService.getQrCodesByReservationId(
                        reservationId
                )
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<ReservationQrCodeResponseDto>>
    getActiveQrCodes() {
        return ResponseEntity.ok(
                qrCodeService.getActiveQrCodes()
        );
    }

    @GetMapping("/reservation/{reservationId}/active")
    public ResponseEntity<List<ReservationQrCodeResponseDto>>
    getActiveQrCodesByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                qrCodeService.getActiveQrCodesByReservationId(
                        reservationId
                )
        );
    }

    @GetMapping("/expired")
    public ResponseEntity<List<ReservationQrCodeResponseDto>>
    getExpiredQrCodes() {
        return ResponseEntity.ok(
                qrCodeService.getExpiredQrCodes()
        );
    }

    @GetMapping("/token/exists")
    public ResponseEntity<Boolean> tokenHashExists(
            @RequestParam String tokenHash
    ) {
        return ResponseEntity.ok(
                qrCodeService.tokenHashExists(tokenHash)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ReservationQrCodeResponseDto>
    deactivateQrCode(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                qrCodeService.deactivateQrCode(id)
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ReservationQrCodeResponseDto>
    activateQrCode(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                qrCodeService.activateQrCode(id)
        );
    }

    @PatchMapping("/{id}/use")
    public ResponseEntity<ReservationQrCodeResponseDto> markAsUsed(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                qrCodeService.markAsUsed(id)
        );
    }

    @PatchMapping("/deactivate-expired")
    public ResponseEntity<Integer> deactivateExpiredQrCodes() {
        return ResponseEntity.ok(
                qrCodeService.deactivateExpiredQrCodes()
        );
    }

    @PatchMapping("/{id}/expiration")
    public ResponseEntity<ReservationQrCodeResponseDto>
    updateExpiration(
            @PathVariable Long id,
            @RequestParam OffsetDateTime expiresAt
    ) {
        return ResponseEntity.ok(
                qrCodeService.updateExpiration(
                        id,
                        expiresAt
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQrCode(
            @PathVariable Long id
    ) {
        qrCodeService.deleteQrCode(id);

        return ResponseEntity.noContent().build();
    }
}
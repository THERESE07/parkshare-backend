package rw.parkshare.backend.support.controller;

import jakarta.validation.Valid;

import rw.parkshare.backend.common.dto.DisputeRequestDto;
import rw.parkshare.backend.common.dto.DisputeResponseDto;
import rw.parkshare.backend.support.service.DisputeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    @PostMapping("/reservation/{reservationId}/raised-by/{userId}")
    public ResponseEntity<DisputeResponseDto> createDispute(
            @PathVariable Long reservationId,
            @PathVariable Long userId,
            @Valid @RequestBody DisputeRequestDto request
    ) {
        return ResponseEntity.ok(
                disputeService.createDispute(
                        reservationId,
                        userId,
                        request
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisputeResponseDto> getDisputeById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                disputeService.getDisputeById(id)
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<DisputeResponseDto>> getDisputesByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                disputeService.getDisputesByReservationId(reservationId)
        );
    }

    @GetMapping("/raised-by/{userId}")
    public ResponseEntity<List<DisputeResponseDto>> getDisputesRaisedByUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                disputeService.getDisputesRaisedByUser(userId)
        );
    }

    @GetMapping("/assigned-to/{userId}")
    public ResponseEntity<List<DisputeResponseDto>> getDisputesAssignedToUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                disputeService.getDisputesAssignedToUser(userId)
        );
    }

    @GetMapping("/type/{disputeType}")
    public ResponseEntity<List<DisputeResponseDto>> getDisputesByType(
            @PathVariable String disputeType
    ) {
        return ResponseEntity.ok(
                disputeService.getDisputesByType(disputeType)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DisputeResponseDto>> getDisputesByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                disputeService.getDisputesByStatus(status)
        );
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<DisputeResponseDto>> getDisputesByCurrency(
            @PathVariable String currency
    ) {
        return ResponseEntity.ok(
                disputeService.getDisputesByCurrency(currency)
        );
    }

    @GetMapping("/assigned-to/{userId}/status/{status}")
    public ResponseEntity<List<DisputeResponseDto>> getAssignedDisputesByStatus(
            @PathVariable Long userId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                disputeService.getAssignedDisputesByStatus(
                        userId,
                        status
                )
        );
    }

    @GetMapping("/raised-by/{userId}/status/{status}")
    public ResponseEntity<List<DisputeResponseDto>> getRaisedDisputesByStatus(
            @PathVariable Long userId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                disputeService.getRaisedDisputesByStatus(
                        userId,
                        status
                )
        );
    }

    @PatchMapping("/{disputeId}/assign")
    public ResponseEntity<DisputeResponseDto> assignDispute(
            @PathVariable Long disputeId,
            @RequestParam Long assignedToUserId
    ) {
        return ResponseEntity.ok(
                disputeService.assignDispute(
                        disputeId,
                        assignedToUserId
                )
        );
    }

    @PatchMapping("/{disputeId}/status")
    public ResponseEntity<DisputeResponseDto> updateStatus(
            @PathVariable Long disputeId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                disputeService.updateStatus(
                        disputeId,
                        status
                )
        );
    }

    @PatchMapping("/{disputeId}/resolve")
    public ResponseEntity<DisputeResponseDto> resolveDispute(
            @PathVariable Long disputeId,
            @RequestParam String resolution
    ) {
        return ResponseEntity.ok(
                disputeService.resolveDispute(
                        disputeId,
                        resolution
                )
        );
    }

    @PutMapping("/{disputeId}")
    public ResponseEntity<DisputeResponseDto> updateDispute(
            @PathVariable Long disputeId,
            @Valid @RequestBody DisputeRequestDto request
    ) {
        return ResponseEntity.ok(
                disputeService.updateDispute(
                        disputeId,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDispute(
            @PathVariable Long id
    ) {
        disputeService.deleteDispute(id);
        return ResponseEntity.noContent().build();
    }
}
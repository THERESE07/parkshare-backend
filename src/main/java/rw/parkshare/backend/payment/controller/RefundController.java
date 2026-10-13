package rw.parkshare.backend.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.RefundRequestDto;
import rw.parkshare.backend.common.dto.RefundResponseDto;
import rw.parkshare.backend.payment.service.RefundService;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(
            RefundService refundService
    ) {
        this.refundService = refundService;
    }

    @PostMapping("/payment/{paymentId}")
    public ResponseEntity<RefundResponseDto> createRefund(
            @PathVariable Long paymentId,
            @RequestBody RefundRequestDto request
    ) {
        RefundResponseDto createdRefund =
                refundService.createRefund(
                        paymentId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdRefund);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundResponseDto> getRefundById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                refundService.getRefundById(id)
        );
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByPaymentId(
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(
                refundService.getRefundsByPaymentId(paymentId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                refundService.getRefundsByStatus(status)
        );
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByCurrency(
            @PathVariable String currency
    ) {
        return ResponseEntity.ok(
                refundService.getRefundsByCurrency(currency)
        );
    }

    @GetMapping("/provider-refund/{providerRefundId}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByProviderRefundId(
            @PathVariable String providerRefundId
    ) {
        return ResponseEntity.ok(
                refundService.getRefundsByProviderRefundId(
                        providerRefundId
                )
        );
    }

    @GetMapping("/payment/{paymentId}/status/{status}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByPaymentAndStatus(
            @PathVariable Long paymentId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                refundService.getRefundsByPaymentAndStatus(
                        paymentId,
                        status
                )
        );
    }

    @GetMapping("/provider-refund/exists")
    public ResponseEntity<Boolean> providerRefundIdExists(
            @RequestParam String providerRefundId
    ) {
        return ResponseEntity.ok(
                refundService.providerRefundIdExists(
                        providerRefundId
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RefundResponseDto> updateRefundStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                refundService.updateRefundStatus(
                        id,
                        status
                )
        );
    }

    @PatchMapping("/{id}/provider-refund")
    public ResponseEntity<RefundResponseDto> setProviderRefundId(
            @PathVariable Long id,
            @RequestParam String providerRefundId
    ) {
        return ResponseEntity.ok(
                refundService.setProviderRefundId(
                        id,
                        providerRefundId
                )
        );
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<RefundResponseDto> completeRefund(
            @PathVariable Long id,
            @RequestParam(required = false) String providerRefundId
    ) {
        return ResponseEntity.ok(
                refundService.completeRefund(
                        id,
                        providerRefundId
                )
        );
    }

    @PatchMapping("/{id}/fail")
    public ResponseEntity<RefundResponseDto> failRefund(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(
                refundService.failRefund(
                        id,
                        reason
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RefundResponseDto> updateRefund(
            @PathVariable Long id,
            @RequestBody RefundRequestDto request
    ) {
        return ResponseEntity.ok(
                refundService.updateRefund(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRefund(
            @PathVariable Long id
    ) {
        refundService.deleteRefund(id);

        return ResponseEntity.noContent().build();
    }
}
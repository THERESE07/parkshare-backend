package rw.parkshare.backend.payment.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.PaymentRequestDto;
import rw.parkshare.backend.common.dto.PaymentResponseDto;
import rw.parkshare.backend.payment.entity.PaymentStatus;
import rw.parkshare.backend.payment.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    @PostMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentResponseDto> createPayment(
            @PathVariable Long reservationId,
            @Valid @RequestBody PaymentRequestDto request
    ) {
        PaymentResponseDto createdPayment =
                paymentService.createPayment(
                        reservationId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdPayment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentById(id)
        );
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByReservationId(
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentByReservationId(
                        reservationId
                )
        );
    }

    @GetMapping("/reference/{paymentReference}")
    public ResponseEntity<PaymentResponseDto> getPaymentByReference(
            @PathVariable String paymentReference
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentByReference(
                        paymentReference
                )
        );
    }

    @GetMapping("/transaction/{transactionReference}")
    public ResponseEntity<PaymentResponseDto>
    getPaymentByTransactionReference(
            @PathVariable String transactionReference
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentByTransactionReference(
                        transactionReference
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStatus(
            @PathVariable PaymentStatus status
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(status)
        );
    }

    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByMethod(
            @PathVariable String paymentMethod
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentsByMethod(
                        paymentMethod
                )
        );
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByCurrency(
            @PathVariable String currency
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentsByCurrency(currency)
        );
    }

    @GetMapping("/status/{status}/method/{paymentMethod}")
    public ResponseEntity<List<PaymentResponseDto>>
    getPaymentsByStatusAndMethod(
            @PathVariable PaymentStatus status,
            @PathVariable String paymentMethod
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentsByStatusAndMethod(
                        status,
                        paymentMethod
                )
        );
    }

    @GetMapping("/reference/exists")
    public ResponseEntity<Boolean> paymentReferenceExists(
            @RequestParam String paymentReference
    ) {
        return ResponseEntity.ok(
                paymentService.paymentReferenceExists(
                        paymentReference
                )
        );
    }

    @GetMapping("/transaction/exists")
    public ResponseEntity<Boolean> transactionReferenceExists(
            @RequestParam String transactionReference
    ) {
        return ResponseEntity.ok(
                paymentService.transactionReferenceExists(
                        transactionReference
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponseDto> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status
    ) {
        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(
                        id,
                        status
                )
        );
    }

    @PatchMapping("/{id}/transaction-reference")
    public ResponseEntity<PaymentResponseDto> updateTransactionReference(
            @PathVariable Long id,
            @RequestParam String transactionReference
    ) {
        return ResponseEntity.ok(
                paymentService.updateTransactionReference(
                        id,
                        transactionReference
                )
        );
    }

    @PatchMapping("/{id}/paid")
    public ResponseEntity<PaymentResponseDto> markAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) String transactionReference
    ) {
        return ResponseEntity.ok(
                paymentService.markAsPaid(
                        id,
                        transactionReference
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequestDto request
    ) {
        return ResponseEntity.ok(
                paymentService.updatePayment(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id
    ) {
        paymentService.deletePayment(id);

        return ResponseEntity.noContent().build();
    }
}
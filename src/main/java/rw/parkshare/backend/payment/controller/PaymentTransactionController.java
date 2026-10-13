package rw.parkshare.backend.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.PaymentTransactionRequestDto;
import rw.parkshare.backend.common.dto.PaymentTransactionResponseDto;
import rw.parkshare.backend.payment.service.PaymentTransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/payment-transactions")
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    public PaymentTransactionController(
            PaymentTransactionService paymentTransactionService
    ) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @PostMapping("/payment/{paymentId}")
    public ResponseEntity<PaymentTransactionResponseDto> createTransaction(
            @PathVariable Long paymentId,
            @RequestBody PaymentTransactionRequestDto request
    ) {
        PaymentTransactionResponseDto createdTransaction =
                paymentTransactionService.createTransaction(
                        paymentId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTransaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransactionResponseDto> getTransactionById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.getTransactionById(id)
        );
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<PaymentTransactionResponseDto>>
    getTransactionsByPaymentId(
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.getTransactionsByPaymentId(
                        paymentId
                )
        );
    }

    @GetMapping("/reference/{transactionReference}")
    public ResponseEntity<PaymentTransactionResponseDto>
    getTransactionByReference(
            @PathVariable String transactionReference
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.getTransactionByReference(
                        transactionReference
                )
        );
    }

    @GetMapping("/provider-transaction/{providerTransactionId}")
    public ResponseEntity<PaymentTransactionResponseDto>
    getTransactionByProviderTransactionId(
            @PathVariable String providerTransactionId
    ) {
        return ResponseEntity.ok(
                paymentTransactionService
                        .getTransactionByProviderTransactionId(
                                providerTransactionId
                        )
        );
    }

    @GetMapping("/provider/{provider}")
    public ResponseEntity<List<PaymentTransactionResponseDto>>
    getTransactionsByProvider(
            @PathVariable String provider
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.getTransactionsByProvider(
                        provider
                )
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentTransactionResponseDto>>
    getTransactionsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.getTransactionsByStatus(
                        status
                )
        );
    }

    @GetMapping("/currency/{currency}")
    public ResponseEntity<List<PaymentTransactionResponseDto>>
    getTransactionsByCurrency(
            @PathVariable String currency
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.getTransactionsByCurrency(
                        currency
                )
        );
    }

    @GetMapping("/payment/{paymentId}/status/{status}")
    public ResponseEntity<List<PaymentTransactionResponseDto>>
    getTransactionsByPaymentAndStatus(
            @PathVariable Long paymentId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                paymentTransactionService
                        .getTransactionsByPaymentAndStatus(
                                paymentId,
                                status
                        )
        );
    }

    @GetMapping("/reference/exists")
    public ResponseEntity<Boolean> transactionReferenceExists(
            @RequestParam String transactionReference
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.transactionReferenceExists(
                        transactionReference
                )
        );
    }

    @GetMapping("/provider-transaction/exists")
    public ResponseEntity<Boolean> providerTransactionIdExists(
            @RequestParam String providerTransactionId
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.providerTransactionIdExists(
                        providerTransactionId
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentTransactionResponseDto>
    updateTransactionStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.updateTransactionStatus(
                        id,
                        status
                )
        );
    }

    @PatchMapping("/{id}/provider-transaction")
    public ResponseEntity<PaymentTransactionResponseDto>
    updateProviderTransactionId(
            @PathVariable Long id,
            @RequestParam String providerTransactionId
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.updateProviderTransactionId(
                        id,
                        providerTransactionId
                )
        );
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<PaymentTransactionResponseDto>
    completeTransaction(
            @PathVariable Long id,
            @RequestParam(required = false) String providerTransactionId
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.completeTransaction(
                        id,
                        providerTransactionId
                )
        );
    }

    @PatchMapping("/{id}/fail")
    public ResponseEntity<PaymentTransactionResponseDto>
    failTransaction(
            @PathVariable Long id,
            @RequestParam(required = false) String failureReason
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.failTransaction(
                        id,
                        failureReason
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentTransactionResponseDto>
    updateTransaction(
            @PathVariable Long id,
            @RequestBody PaymentTransactionRequestDto request
    ) {
        return ResponseEntity.ok(
                paymentTransactionService.updateTransaction(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id
    ) {
        paymentTransactionService.deleteTransaction(id);

        return ResponseEntity.noContent().build();
    }
}
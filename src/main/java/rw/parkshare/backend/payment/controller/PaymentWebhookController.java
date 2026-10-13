package rw.parkshare.backend.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.PaymentWebhookRequestDto;
import rw.parkshare.backend.common.dto.PaymentWebhookResponseDto;
import rw.parkshare.backend.payment.service.PaymentWebhookService;

import java.util.List;

@RestController
@RequestMapping("/api/payment-webhooks")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping
    public ResponseEntity<PaymentWebhookResponseDto> createWebhook(
            @RequestBody PaymentWebhookRequestDto request
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.createWebhook(request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentWebhookResponseDto> getWebhookById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.getWebhookById(id)
        );
    }

    @GetMapping("/provider/{provider}/event/{eventId}")
    public ResponseEntity<PaymentWebhookResponseDto> getWebhookByProviderAndEventId(
            @PathVariable String provider,
            @PathVariable String eventId
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.getWebhookByProviderAndEventId(
                        provider,
                        eventId
                )
        );
    }

    @GetMapping("/provider/{provider}")
    public ResponseEntity<List<PaymentWebhookResponseDto>> getWebhooksByProvider(
            @PathVariable String provider
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.getWebhooksByProvider(provider)
        );
    }

    @GetMapping("/event-type/{eventType}")
    public ResponseEntity<List<PaymentWebhookResponseDto>> getWebhooksByEventType(
            @PathVariable String eventType
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.getWebhooksByEventType(eventType)
        );
    }

    @GetMapping("/processed")
    public ResponseEntity<List<PaymentWebhookResponseDto>> getWebhooksByProcessed(
            @RequestParam boolean processed
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.getWebhooksByProcessed(processed)
        );
    }

    @GetMapping("/provider/{provider}/processed")
    public ResponseEntity<List<PaymentWebhookResponseDto>> getWebhooksByProviderAndProcessed(
            @PathVariable String provider,
            @RequestParam boolean processed
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.getWebhooksByProviderAndProcessed(
                        provider,
                        processed
                )
        );
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> webhookExists(
            @RequestParam String provider,
            @RequestParam String eventId
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.webhookExists(provider, eventId)
        );
    }

    @PatchMapping("/{id}/processed")
    public ResponseEntity<PaymentWebhookResponseDto> markAsProcessed(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.markAsProcessed(id)
        );
    }

    @PatchMapping("/{id}/unprocessed")
    public ResponseEntity<PaymentWebhookResponseDto> markAsUnprocessed(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.markAsUnprocessed(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentWebhookResponseDto> updateWebhook(
            @PathVariable Long id,
            @RequestBody PaymentWebhookRequestDto request
    ) {
        return ResponseEntity.ok(
                paymentWebhookService.updateWebhook(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(
            @PathVariable Long id
    ) {
        paymentWebhookService.deleteWebhook(id);
        return ResponseEntity.noContent().build();
    }
}
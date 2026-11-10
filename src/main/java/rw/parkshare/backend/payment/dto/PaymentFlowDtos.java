package rw.parkshare.backend.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public final class PaymentFlowDtos {

    private PaymentFlowDtos() {
    }

    /** Driver starts paying for a reservation by mobile money. */
    public record InitiatePaymentRequest(
            @NotBlank
            @Pattern(regexp = "MTN_MOMO|AIRTEL_MONEY", message = "must be MTN_MOMO or AIRTEL_MONEY")
            String method,
            /** Optional: defaults to the phone on the driver's account. */
            String phone
    ) {
    }

    public record PaymentView(
            Long id,
            Long reservationId,
            String paymentReference,
            String transactionReference,
            BigDecimal amount,
            BigDecimal vatAmount,
            String currency,
            String method,
            String status,
            String payerPhone,
            OffsetDateTime paidAt,
            String instructions
    ) {
    }

    /** Body of a provider callback, signed with HMAC-SHA256 in X-ParkShare-Signature. */
    public record ProviderCallback(
            String eventId,
            String eventType,
            String transactionReference,
            String providerTransactionId,
            String status,
            String reason
    ) {
    }

    public record Receipt(
            String receiptNumber,
            String status,
            LocalDateTime issuedAtKigali,
            String parkingLotName,
            String parkingLotAddress,
            String operatorBusinessName,
            String operatorRegistrationNumber,
            String customerName,
            String vehiclePlate,
            String reservationCode,
            LocalDateTime startKigali,
            LocalDateTime endKigali,
            BigDecimal amountExcludingVat,
            BigDecimal vatRate,
            BigDecimal vatAmount,
            BigDecimal total,
            BigDecimal refunded,
            String currency,
            String paymentMethod,
            String payerPhone,
            String note
    ) {
    }
}

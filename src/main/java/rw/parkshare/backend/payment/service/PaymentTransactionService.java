package rw.parkshare.backend.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.PaymentTransactionRequestDto;
import rw.parkshare.backend.common.dto.PaymentTransactionResponseDto;
import rw.parkshare.backend.payment.entity.Payment;
import rw.parkshare.backend.payment.entity.PaymentTransaction;
import rw.parkshare.backend.payment.repository.PaymentRepository;
import rw.parkshare.backend.payment.repository.PaymentTransactionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentRepository paymentRepository;

    public PaymentTransactionService(
            PaymentTransactionRepository paymentTransactionRepository,
            PaymentRepository paymentRepository
    ) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentRepository = paymentRepository;
    }

    public PaymentTransactionResponseDto createTransaction(
            Long paymentId,
            PaymentTransactionRequestDto request
    ) {
        validateTransactionRequest(request);

        if (paymentTransactionRepository.existsByTransactionReference(
                request.getTransactionReference()
        )) {
            throw new IllegalArgumentException(
                    "A transaction with this reference already exists."
            );
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found with ID: " + paymentId
                        )
                );

        PaymentTransaction transaction = new PaymentTransaction();

        transaction.setPayment(payment);
        transaction.setProvider(request.getProvider());
        transaction.setProviderTransactionId(
                request.getProviderTransactionId()
        );
        transaction.setTransactionReference(
                request.getTransactionReference()
        );
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());

        if (request.getStatus() == null
                || request.getStatus().isBlank()) {
            transaction.setStatus("PENDING");
        } else {
            transaction.setStatus(request.getStatus());
        }

        transaction.setFailureReason(
                request.getFailureReason()
        );

        transaction.setInitiatedAt(OffsetDateTime.now());

        PaymentTransaction savedTransaction =
                paymentTransactionRepository.save(transaction);

        return toResponseDto(savedTransaction);
    }

    @Transactional(readOnly = true)
    public PaymentTransactionResponseDto getTransactionById(Long id) {
        return toResponseDto(
                getTransactionEntityById(id)
        );
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto>
    getTransactionsByPaymentId(Long paymentId) {

        return paymentTransactionRepository
                .findByPayment_Id(paymentId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentTransactionResponseDto getTransactionByReference(
            String transactionReference
    ) {
        PaymentTransaction transaction =
                paymentTransactionRepository
                        .findByTransactionReference(transactionReference)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment transaction not found with reference: "
                                                + transactionReference
                                )
                        );

        return toResponseDto(transaction);
    }

    @Transactional(readOnly = true)
    public PaymentTransactionResponseDto
    getTransactionByProviderTransactionId(
            String providerTransactionId
    ) {
        PaymentTransaction transaction =
                paymentTransactionRepository
                        .findByProviderTransactionId(
                                providerTransactionId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment transaction not found with provider transaction ID: "
                                                + providerTransactionId
                                )
                        );

        return toResponseDto(transaction);
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto>
    getTransactionsByProvider(String provider) {

        return paymentTransactionRepository
                .findByProvider(provider)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto>
    getTransactionsByStatus(String status) {

        return paymentTransactionRepository
                .findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto>
    getTransactionsByCurrency(String currency) {

        return paymentTransactionRepository
                .findByCurrency(currency)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransactionResponseDto>
    getTransactionsByPaymentAndStatus(
            Long paymentId,
            String status
    ) {

        return paymentTransactionRepository
                .findByPayment_IdAndStatus(
                        paymentId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean transactionReferenceExists(
            String transactionReference
    ) {
        return paymentTransactionRepository
                .existsByTransactionReference(
                        transactionReference
                );
    }

    @Transactional(readOnly = true)
    public boolean providerTransactionIdExists(
            String providerTransactionId
    ) {
        return paymentTransactionRepository
                .existsByProviderTransactionId(
                        providerTransactionId
                );
    }

    public PaymentTransactionResponseDto updateTransactionStatus(
            Long id,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction status is required."
            );
        }

        PaymentTransaction transaction =
                getTransactionEntityById(id);

        transaction.setStatus(status);

        if ("COMPLETED".equalsIgnoreCase(status)
                && transaction.getCompletedAt() == null) {

            transaction.setCompletedAt(
                    OffsetDateTime.now()
            );
        }

        return toResponseDto(
                paymentTransactionRepository.save(transaction)
        );
    }

    public PaymentTransactionResponseDto updateProviderTransactionId(
            Long id,
            String providerTransactionId
    ) {
        validateProviderTransactionId(providerTransactionId);

        PaymentTransaction transaction =
                getTransactionEntityById(id);

        checkProviderTransactionIdUniqueness(
                id,
                providerTransactionId
        );

        transaction.setProviderTransactionId(
                providerTransactionId
        );

        return toResponseDto(
                paymentTransactionRepository.save(transaction)
        );
    }

    public PaymentTransactionResponseDto completeTransaction(
            Long id,
            String providerTransactionId
    ) {
        PaymentTransaction transaction =
                getTransactionEntityById(id);

        if (providerTransactionId != null
                && !providerTransactionId.isBlank()) {

            checkProviderTransactionIdUniqueness(
                    id,
                    providerTransactionId
            );

            transaction.setProviderTransactionId(
                    providerTransactionId
            );
        }

        transaction.setStatus("COMPLETED");
        transaction.setCompletedAt(
                OffsetDateTime.now()
        );

        return toResponseDto(
                paymentTransactionRepository.save(transaction)
        );
    }

    public PaymentTransactionResponseDto failTransaction(
            Long id,
            String failureReason
    ) {
        PaymentTransaction transaction =
                getTransactionEntityById(id);

        transaction.setStatus("FAILED");
        transaction.setFailureReason(failureReason);
        transaction.setCompletedAt(
                OffsetDateTime.now()
        );

        return toResponseDto(
                paymentTransactionRepository.save(transaction)
        );
    }

    public PaymentTransactionResponseDto updateTransaction(
            Long id,
            PaymentTransactionRequestDto request
    ) {
        PaymentTransaction existingTransaction =
                getTransactionEntityById(id);

        if (request.getProvider() != null
                && !request.getProvider().isBlank()) {

            existingTransaction.setProvider(
                    request.getProvider()
            );
        }

        if (request.getProviderTransactionId() != null
                && !request.getProviderTransactionId().isBlank()) {

            checkProviderTransactionIdUniqueness(
                    id,
                    request.getProviderTransactionId()
            );

            existingTransaction.setProviderTransactionId(
                    request.getProviderTransactionId()
            );
        }

        if (request.getAmount() != null) {

            if (request.getAmount()
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "Transaction amount cannot be negative."
                );
            }

            existingTransaction.setAmount(
                    request.getAmount()
            );
        }

        if (request.getCurrency() != null
                && !request.getCurrency().isBlank()) {

            existingTransaction.setCurrency(
                    request.getCurrency()
            );
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            existingTransaction.setStatus(
                    request.getStatus()
            );
        }

        if (request.getFailureReason() != null) {
            existingTransaction.setFailureReason(
                    request.getFailureReason()
            );
        }

        return toResponseDto(
                paymentTransactionRepository.save(
                        existingTransaction
                )
        );
    }

    public void deleteTransaction(Long id) {

        PaymentTransaction transaction =
                getTransactionEntityById(id);

        paymentTransactionRepository.delete(transaction);
    }

    private void validateTransactionRequest(
            PaymentTransactionRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Transaction request is required."
            );
        }

        if (request.getProvider() == null
                || request.getProvider().isBlank()) {

            throw new IllegalArgumentException(
                    "Payment provider is required."
            );
        }

        if (request.getTransactionReference() == null
                || request.getTransactionReference().isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction reference is required."
            );
        }

        if (request.getAmount() == null
                || request.getAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Transaction amount must be zero or greater."
            );
        }

        if (request.getCurrency() == null
                || request.getCurrency().isBlank()) {

            throw new IllegalArgumentException(
                    "Transaction currency is required."
            );
        }
    }

    private void validateProviderTransactionId(
            String providerTransactionId
    ) {
        if (providerTransactionId == null
                || providerTransactionId.isBlank()) {

            throw new IllegalArgumentException(
                    "Provider transaction ID is required."
            );
        }
    }

    private void checkProviderTransactionIdUniqueness(
            Long currentTransactionId,
            String providerTransactionId
    ) {
        if (paymentTransactionRepository
                .existsByProviderTransactionId(
                        providerTransactionId
                )) {

            PaymentTransaction existingTransaction =
                    paymentTransactionRepository
                            .findByProviderTransactionId(
                                    providerTransactionId
                            )
                            .orElse(null);

            if (existingTransaction != null
                    && !existingTransaction
                    .getId()
                    .equals(currentTransactionId)) {

                throw new IllegalArgumentException(
                        "A transaction with this provider transaction ID already exists."
                );
            }
        }
    }

    private PaymentTransaction getTransactionEntityById(
            Long id
    ) {
        return paymentTransactionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment transaction not found with ID: "
                                        + id
                        )
                );
    }

    private PaymentTransactionResponseDto toResponseDto(
            PaymentTransaction transaction
    ) {
        Long paymentId = transaction.getPayment() != null
                ? transaction.getPayment().getId()
                : null;

        return new PaymentTransactionResponseDto(
                transaction.getId(),
                paymentId,
                transaction.getProvider(),
                transaction.getProviderTransactionId(),
                transaction.getTransactionReference(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getFailureReason(),
                transaction.getInitiatedAt(),
                transaction.getCompletedAt()
        );
    }
}
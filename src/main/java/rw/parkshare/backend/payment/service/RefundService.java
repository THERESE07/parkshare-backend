package rw.parkshare.backend.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.RefundRequestDto;
import rw.parkshare.backend.common.dto.RefundResponseDto;
import rw.parkshare.backend.payment.entity.Payment;
import rw.parkshare.backend.payment.entity.Refund;
import rw.parkshare.backend.payment.repository.PaymentRepository;
import rw.parkshare.backend.payment.repository.RefundRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    public RefundService(
            RefundRepository refundRepository,
            PaymentRepository paymentRepository
    ) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
    }

    public RefundResponseDto createRefund(
            Long paymentId,
            RefundRequestDto request
    ) {
        validateRefundRequest(request);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found with ID: " + paymentId
                        )
                );

        Refund refund = new Refund();

        refund.setPayment(payment);
        refund.setAmount(request.getAmount());
        refund.setCurrency(request.getCurrency().trim());
        refund.setReason(request.getReason());
        refund.setProviderRefundId(request.getProviderRefundId());
        refund.setStatus(
                request.getStatus() == null || request.getStatus().isBlank()
                        ? "PENDING"
                        : request.getStatus().trim()
        );
        refund.setRequestedAt(OffsetDateTime.now());

        Refund savedRefund = refundRepository.save(refund);

        return toResponseDto(savedRefund);
    }

    @Transactional(readOnly = true)
    public RefundResponseDto getRefundById(Long id) {
        return toResponseDto(getRefundEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByPaymentId(
            Long paymentId
    ) {
        return refundRepository.findByPayment_Id(paymentId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByStatus(
            String status
    ) {
        return refundRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByCurrency(
            String currency
    ) {
        return refundRepository.findByCurrency(currency)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByProviderRefundId(
            String providerRefundId
    ) {
        return refundRepository.findByProviderRefundId(providerRefundId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundResponseDto> getRefundsByPaymentAndStatus(
            Long paymentId,
            String status
    ) {
        return refundRepository.findByPayment_IdAndStatus(
                        paymentId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean providerRefundIdExists(
            String providerRefundId
    ) {
        return refundRepository.existsByProviderRefundId(
                providerRefundId
        );
    }

    public RefundResponseDto updateRefundStatus(
            Long id,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Refund status is required."
            );
        }

        Refund refund = getRefundEntityById(id);

        refund.setStatus(status.trim());

        if ("COMPLETED".equalsIgnoreCase(status)
                && refund.getCompletedAt() == null) {
            refund.setCompletedAt(OffsetDateTime.now());
        }

        Refund savedRefund = refundRepository.save(refund);

        return toResponseDto(savedRefund);
    }

    public RefundResponseDto setProviderRefundId(
            Long id,
            String providerRefundId
    ) {
        validateProviderRefundId(providerRefundId);

        Refund refund = getRefundEntityById(id);

        checkProviderRefundIdUniqueness(
                id,
                providerRefundId
        );

        refund.setProviderRefundId(providerRefundId.trim());

        Refund savedRefund = refundRepository.save(refund);

        return toResponseDto(savedRefund);
    }

    public RefundResponseDto completeRefund(
            Long id,
            String providerRefundId
    ) {
        Refund refund = getRefundEntityById(id);

        if (providerRefundId != null
                && !providerRefundId.isBlank()) {

            checkProviderRefundIdUniqueness(
                    id,
                    providerRefundId
            );

            refund.setProviderRefundId(
                    providerRefundId.trim()
            );
        }

        refund.setStatus("COMPLETED");
        refund.setCompletedAt(OffsetDateTime.now());

        Refund savedRefund = refundRepository.save(refund);

        return toResponseDto(savedRefund);
    }

    public RefundResponseDto failRefund(
            Long id,
            String reason
    ) {
        Refund refund = getRefundEntityById(id);

        refund.setStatus("FAILED");
        refund.setReason(reason);
        refund.setCompletedAt(OffsetDateTime.now());

        Refund savedRefund = refundRepository.save(refund);

        return toResponseDto(savedRefund);
    }

    public RefundResponseDto updateRefund(
            Long id,
            RefundRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Refund request cannot be null."
            );
        }

        Refund existingRefund = getRefundEntityById(id);

        if (request.getAmount() != null) {
            if (request.getAmount()
                    .compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Refund amount must be greater than zero."
                );
            }

            existingRefund.setAmount(
                    request.getAmount()
            );
        }

        if (request.getCurrency() != null
                && !request.getCurrency().isBlank()) {
            existingRefund.setCurrency(
                    request.getCurrency().trim()
            );
        }

        if (request.getReason() != null) {
            existingRefund.setReason(
                    request.getReason()
            );
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {
            existingRefund.setStatus(
                    request.getStatus().trim()
            );
        }

        if (request.getProviderRefundId() != null
                && !request.getProviderRefundId().isBlank()) {

            checkProviderRefundIdUniqueness(
                    id,
                    request.getProviderRefundId()
            );

            existingRefund.setProviderRefundId(
                    request.getProviderRefundId().trim()
            );
        }

        Refund savedRefund = refundRepository.save(existingRefund);

        return toResponseDto(savedRefund);
    }

    public void deleteRefund(Long id) {
        Refund refund = getRefundEntityById(id);

        refundRepository.delete(refund);
    }

    private void validateRefundRequest(
            RefundRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Refund request cannot be null."
            );
        }

        if (request.getAmount() == null
                || request.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Refund amount must be greater than zero."
            );
        }

        if (request.getCurrency() == null
                || request.getCurrency().isBlank()) {
            throw new IllegalArgumentException(
                    "Refund currency is required."
            );
        }
    }

    private void validateProviderRefundId(
            String providerRefundId
    ) {
        if (providerRefundId == null
                || providerRefundId.isBlank()) {
            throw new IllegalArgumentException(
                    "Provider refund ID is required."
            );
        }
    }

    private void checkProviderRefundIdUniqueness(
            Long currentRefundId,
            String providerRefundId
    ) {
        List<Refund> existingRefunds =
                refundRepository.findByProviderRefundId(
                        providerRefundId
                );

        for (Refund existingRefund : existingRefunds) {
            if (!existingRefund.getId().equals(currentRefundId)) {
                throw new IllegalArgumentException(
                        "A refund with this provider refund ID already exists."
                );
            }
        }
    }

    private Refund getRefundEntityById(Long id) {
        return refundRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Refund not found with ID: " + id
                        )
                );
    }

    private RefundResponseDto toResponseDto(
            Refund refund
    ) {
        return new RefundResponseDto(
                refund.getId(),
                refund.getPayment().getId(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getReason(),
                refund.getStatus(),
                refund.getProviderRefundId(),
                refund.getRequestedAt(),
                refund.getCompletedAt()
        );
    }
}
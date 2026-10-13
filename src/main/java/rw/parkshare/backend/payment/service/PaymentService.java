package rw.parkshare.backend.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.PaymentRequestDto;
import rw.parkshare.backend.common.dto.PaymentResponseDto;
import rw.parkshare.backend.payment.entity.Payment;
import rw.parkshare.backend.payment.entity.PaymentStatus;
import rw.parkshare.backend.payment.repository.PaymentRepository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.repository.ReservationRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    public PaymentResponseDto createPayment(
            Long reservationId,
            PaymentRequestDto request
    ) {
        validatePaymentRequest(request);

        if (paymentRepository.findByReservation_Id(reservationId).isPresent()) {
            throw new IllegalArgumentException(
                    "A payment already exists for this reservation."
            );
        }

        if (paymentRepository.existsByPaymentReference(
                request.getPaymentReference()
        )) {
            throw new IllegalArgumentException(
                    "A payment with this reference already exists."
            );
        }

        Reservation reservation = reservationRepository.findById(
                reservationId
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Reservation not found with ID: "
                                + reservationId
                )
        );

        Payment payment = new Payment();

        payment.setReservation(reservation);
        payment.setPaymentReference(request.getPaymentReference());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());

        if (request.getCurrency() == null
                || request.getCurrency().isBlank()) {
            payment.setCurrency("RWF");
        } else {
            payment.setCurrency(request.getCurrency());
        }

        if (request.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        } else {
            payment.setStatus(request.getStatus());
        }

        payment.setTransactionReference(
                request.getTransactionReference()
        );

        Payment savedPayment = paymentRepository.save(payment);

        return toResponseDto(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(Long id) {

        return toResponseDto(getPaymentEntityById(id));
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByReservationId(
            Long reservationId
    ) {
        Payment payment = paymentRepository
                .findByReservation_Id(reservationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found for reservation with ID: "
                                        + reservationId
                        )
                );

        return toResponseDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByReference(
            String paymentReference
    ) {
        Payment payment = paymentRepository
                .findByPaymentReference(paymentReference)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found with reference: "
                                        + paymentReference
                        )
                );

        return toResponseDto(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByTransactionReference(
            String transactionReference
    ) {
        Payment payment = paymentRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found with transaction reference: "
                                        + transactionReference
                        )
                );

        return toResponseDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByStatus(
            PaymentStatus status
    ) {
        return paymentRepository
                .findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByMethod(
            String paymentMethod
    ) {
        return paymentRepository
                .findByPaymentMethod(paymentMethod)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByCurrency(
            String currency
    ) {
        return paymentRepository
                .findByCurrency(currency)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByStatusAndMethod(
            PaymentStatus status,
            String paymentMethod
    ) {
        return paymentRepository
                .findByStatusAndPaymentMethod(
                        status,
                        paymentMethod
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean paymentReferenceExists(
            String paymentReference
    ) {
        return paymentRepository
                .existsByPaymentReference(paymentReference);
    }

    @Transactional(readOnly = true)
    public boolean transactionReferenceExists(
            String transactionReference
    ) {
        return paymentRepository
                .existsByTransactionReference(transactionReference);
    }

    public PaymentResponseDto updatePaymentStatus(
            Long id,
            PaymentStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Payment status is required."
            );
        }

        Payment payment = getPaymentEntityById(id);

        payment.setStatus(status);

        Payment savedPayment = paymentRepository.save(payment);

        return toResponseDto(savedPayment);
    }

    public PaymentResponseDto updateTransactionReference(
            Long id,
            String transactionReference
    ) {
        if (transactionReference == null
                || transactionReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction reference is required."
            );
        }

        if (paymentRepository.existsByTransactionReference(
                transactionReference
        )) {
            Payment existingPayment =
                    paymentRepository
                            .findByTransactionReference(
                                    transactionReference
                            )
                            .orElse(null);

            if (existingPayment != null
                    && !existingPayment.getId().equals(id)) {
                throw new IllegalArgumentException(
                        "A payment with this transaction reference already exists."
                );
            }
        }

        Payment payment = getPaymentEntityById(id);

        payment.setTransactionReference(transactionReference);

        Payment savedPayment = paymentRepository.save(payment);

        return toResponseDto(savedPayment);
    }

    public PaymentResponseDto markAsPaid(
            Long id,
            String transactionReference
    ) {
        Payment payment = getPaymentEntityById(id);

        if (transactionReference != null
                && !transactionReference.isBlank()) {

            if (paymentRepository.existsByTransactionReference(
                    transactionReference
            )) {
                Payment existingPayment =
                        paymentRepository
                                .findByTransactionReference(
                                        transactionReference
                                )
                                .orElse(null);

                if (existingPayment != null
                        && !existingPayment.getId().equals(id)) {
                    throw new IllegalArgumentException(
                            "A payment with this transaction reference already exists."
                    );
                }
            }

            payment.setTransactionReference(
                    transactionReference
            );
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(OffsetDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        return toResponseDto(savedPayment);
    }

    public PaymentResponseDto updatePayment(
            Long id,
            PaymentRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Payment request is required."
            );
        }

        Payment existingPayment = getPaymentEntityById(id);

        if (request.getAmount() != null) {
            if (request.getAmount()
                    .compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Payment amount cannot be negative."
                );
            }

            existingPayment.setAmount(
                    request.getAmount()
            );
        }

        if (request.getCurrency() != null
                && !request.getCurrency().isBlank()) {
            existingPayment.setCurrency(
                    request.getCurrency()
            );
        }

        if (request.getPaymentMethod() != null
                && !request.getPaymentMethod().isBlank()) {
            existingPayment.setPaymentMethod(
                    request.getPaymentMethod()
            );
        }

        if (request.getStatus() != null) {
            existingPayment.setStatus(
                    request.getStatus()
            );
        }

        if (request.getTransactionReference() != null
                && !request.getTransactionReference().isBlank()) {

            String transactionReference =
                    request.getTransactionReference();

            if (paymentRepository.existsByTransactionReference(
                    transactionReference
            )) {
                Payment existingTransactionPayment =
                        paymentRepository
                                .findByTransactionReference(
                                        transactionReference
                                )
                                .orElse(null);

                if (existingTransactionPayment != null
                        && !existingTransactionPayment
                        .getId()
                        .equals(id)) {

                    throw new IllegalArgumentException(
                            "A payment with this transaction reference already exists."
                    );
                }
            }

            existingPayment.setTransactionReference(
                    transactionReference
            );
        }

        Payment savedPayment =
                paymentRepository.save(existingPayment);

        return toResponseDto(savedPayment);
    }

    public void deletePayment(Long id) {
        Payment payment = getPaymentEntityById(id);

        paymentRepository.delete(payment);
    }

    private void validatePaymentRequest(
            PaymentRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Payment request is required."
            );
        }

        if (request.getPaymentReference() == null
                || request.getPaymentReference().isBlank()) {
            throw new IllegalArgumentException(
                    "Payment reference is required."
            );
        }

        if (request.getAmount() == null
                || request.getAmount()
                .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be zero or greater."
            );
        }

        if (request.getPaymentMethod() == null
                || request.getPaymentMethod().isBlank()) {
            throw new IllegalArgumentException(
                    "Payment method is required."
            );
        }
    }

    private Payment getPaymentEntityById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found with ID: " + id
                        )
                );
    }

    private PaymentResponseDto toResponseDto(
            Payment payment
    ) {
        Long reservationId = null;

        if (payment.getReservation() != null) {
            reservationId =
                    payment.getReservation().getId();
        }

        return new PaymentResponseDto(
                payment.getId(),
                reservationId,
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionReference(),
                payment.getPaidAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
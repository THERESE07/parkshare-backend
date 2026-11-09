package rw.parkshare.backend.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.admin.entity.OperatorVerification;
import rw.parkshare.backend.admin.repository.OperatorVerificationRepository;
import rw.parkshare.backend.common.RwandaPhone;
import rw.parkshare.backend.common.Rwf;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.notification.service.Notifier;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.payment.dto.PaymentFlowDtos.InitiatePaymentRequest;
import rw.parkshare.backend.payment.dto.PaymentFlowDtos.PaymentView;
import rw.parkshare.backend.payment.dto.PaymentFlowDtos.Receipt;
import rw.parkshare.backend.payment.entity.Payment;
import rw.parkshare.backend.payment.entity.PaymentStatus;
import rw.parkshare.backend.payment.entity.PaymentTransaction;
import rw.parkshare.backend.payment.entity.Refund;
import rw.parkshare.backend.payment.gateway.PaymentGateway;
import rw.parkshare.backend.payment.repository.PaymentRepository;
import rw.parkshare.backend.payment.repository.PaymentTransactionRepository;
import rw.parkshare.backend.payment.repository.RefundRepository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.reservation.service.QrTokenService;
import rw.parkshare.backend.reservation.service.ReservationLifecycle;
import rw.parkshare.backend.security.AccessGuard;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Money flows: mobile-money payment, cash at the gate, provider callbacks,
 * cancellation refunds and receipts. Amounts always come from the
 * reservation's server-calculated price, never from the client.
 */
@Service
@Transactional
public class PaymentFlowService {

    private static final Logger log = LoggerFactory.getLogger(PaymentFlowService.class);
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;
    private final OperatorVerificationRepository operatorVerificationRepository;
    private final PaymentGateway gateway;
    private final ReservationLifecycle lifecycle;
    private final QrTokenService qrTokenService;
    private final Notifier notifier;
    private final AccessGuard guard;
    private final ParkShareProperties properties;

    public PaymentFlowService(
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            PaymentTransactionRepository transactionRepository,
            RefundRepository refundRepository,
            UserRepository userRepository,
            OperatorVerificationRepository operatorVerificationRepository,
            PaymentGateway gateway,
            ReservationLifecycle lifecycle,
            QrTokenService qrTokenService,
            Notifier notifier,
            AccessGuard guard,
            ParkShareProperties properties
    ) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.refundRepository = refundRepository;
        this.userRepository = userRepository;
        this.operatorVerificationRepository = operatorVerificationRepository;
        this.gateway = gateway;
        this.lifecycle = lifecycle;
        this.qrTokenService = qrTokenService;
        this.notifier = notifier;
        this.guard = guard;
        this.properties = properties;
    }

    // ------------------------------------------------------------------
    // Mobile money
    // ------------------------------------------------------------------

    public PaymentView initiate(Long reservationId, InitiatePaymentRequest request) {
        Reservation reservation = loadReservation(reservationId);
        guard.requireSelfOrAdmin(reservation.getUser().getId());
        requirePayable(reservation);

        String method = request.method();
        String phone = RwandaPhone.requireValid(
                request.phone() != null && !request.phone().isBlank()
                        ? request.phone()
                        : reservation.getUser().getPhone());
        RwandaPhone.Network network = RwandaPhone.network(phone);
        if ("MTN_MOMO".equals(method) && network != RwandaPhone.Network.MTN) {
            throw ApiException.badRequest("MTN MoMo needs an MTN number (078 or 079)");
        }
        if ("AIRTEL_MONEY".equals(method) && network != RwandaPhone.Network.AIRTEL) {
            throw ApiException.badRequest("Airtel Money needs an Airtel number (072 or 073)");
        }

        Payment payment = paymentFor(reservation);
        // A newer prompt replaces an unanswered one.
        transactionRepository.findByPayment_IdAndStatus(payment.getId() == null ? -1L : payment.getId(), "PROCESSING")
                .forEach(old -> {
                    old.setStatus("CANCELLED");
                    old.setFailureReason("Replaced by a new payment request");
                    old.setCompletedAt(OffsetDateTime.now());
                    transactionRepository.save(old);
                });

        String txReference = newReference("TX");
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setPayerPhone(phone);
        payment.setTransactionReference(txReference);
        payment = paymentRepository.save(payment);

        PaymentTransaction tx = new PaymentTransaction();
        tx.setPayment(payment);
        tx.setProvider(gateway.name());
        tx.setTransactionReference(txReference);
        tx.setAmount(payment.getAmount());
        tx.setCurrency(Rwf.CURRENCY);
        tx.setStatus("PROCESSING");
        tx.setInitiatedAt(OffsetDateTime.now());

        String description = "ParkShare " + reservation.getReservationCode();
        String providerId = gateway.requestPayment(txReference, phone, payment.getAmount(), method, description);
        tx.setProviderTransactionId(providerId);
        transactionRepository.save(tx);

        String wallet = "MTN_MOMO".equals(method) ? "MTN MoMo" : "Airtel Money";
        return view(payment, "A " + wallet + " prompt for " + payment.getAmount().toBigInteger()
                + " RWF was sent to " + mask(phone) + ". Enter your PIN to approve it.");
    }

    /** Attendant/operator records a cash payment at the lot. */
    public PaymentView recordCash(Long reservationId) {
        Reservation reservation = loadReservation(reservationId);
        guard.requireLotManager(reservation.getParkingLot().getId());
        requirePayable(reservation);

        Payment payment = paymentFor(reservation);
        String txReference = newReference("CASH");
        payment.setPaymentMethod("CASH");
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(OffsetDateTime.now());
        payment.setTransactionReference(txReference);
        payment = paymentRepository.save(payment);

        PaymentTransaction tx = new PaymentTransaction();
        tx.setPayment(payment);
        tx.setProvider("cash");
        tx.setTransactionReference(txReference);
        tx.setAmount(payment.getAmount());
        tx.setCurrency(Rwf.CURRENCY);
        tx.setStatus("SUCCESS");
        tx.setInitiatedAt(OffsetDateTime.now());
        tx.setCompletedAt(OffsetDateTime.now());
        transactionRepository.save(tx);

        User staff = userRepository.findById(guard.current().id()).orElse(null);
        confirm(reservation, staff, "Cash payment received");
        return view(payment, "Cash payment recorded");
    }

    /**
     * Applies a provider result. Safe to call more than once for the same
     * transaction (providers retry callbacks).
     */
    public void applyProviderResult(String transactionReference, boolean success, String providerTransactionId, String reason) {
        PaymentTransaction tx = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> ApiException.notFound("Unknown transaction: " + transactionReference));
        if ("SUCCESS".equals(tx.getStatus()) || "FAILED".equals(tx.getStatus())) {
            log.info("Ignoring repeated result for transaction {}", transactionReference);
            return;
        }

        Payment payment = tx.getPayment();
        Reservation reservation = payment.getReservation();
        OffsetDateTime now = OffsetDateTime.now();

        if (!success) {
            tx.setStatus("FAILED");
            tx.setFailureReason(reason);
            tx.setCompletedAt(now);
            transactionRepository.save(tx);
            if (payment.getStatus() == PaymentStatus.PROCESSING
                    && transactionReference.equals(payment.getTransactionReference())) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                notifier.inAppAndSms(reservation.getUser(), "PAYMENT_FAILED", "Payment failed",
                        "Payment for reservation " + reservation.getReservationCode() + " failed"
                                + (reason == null ? "." : ": " + reason + ".") + " You can try again.",
                        "PAYMENT", payment.getId());
            }
            return;
        }

        tx.setStatus("SUCCESS");
        tx.setCompletedAt(now);
        if (providerTransactionId != null) {
            tx.setProviderTransactionId(providerTransactionId);
        }
        transactionRepository.save(tx);

        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.REFUNDED) {
            // The customer approved two prompts: give the duplicate back.
            sendRefund(payment, tx.getAmount(), "Duplicate payment " + transactionReference, tx.getProviderTransactionId());
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(now);
        payment.setTransactionReference(transactionReference);
        paymentRepository.save(payment);

        if (reservation.getStatus() == ReservationStatus.PENDING) {
            confirm(reservation, null, "Mobile money payment received");
        } else {
            // Paid after the hold expired or was cancelled: refund in full.
            sendRefund(payment, payment.getAmount(), "Reservation was " + reservation.getStatus() + " when payment arrived",
                    tx.getProviderTransactionId());
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            notifier.inAppAndSms(reservation.getUser(), "PAYMENT_SUCCESS", "Payment refunded",
                    "Your payment for " + reservation.getReservationCode()
                            + " arrived after the reservation ended, so it has been refunded.",
                    "PAYMENT", payment.getId());
        }
    }

    // ------------------------------------------------------------------
    // Cancellation & expiry
    // ------------------------------------------------------------------

    /**
     * Refund owed when a reservation is cancelled. Full refund when the lot
     * cancels, or the driver cancels at least {freeCancellationMinutes} before
     * start; a partial refund later; nothing after the start time.
     */
    public BigDecimal refundForCancellation(Reservation reservation, boolean cancelledByLot) {
        Optional<Payment> maybePayment = paymentRepository.findByReservation_Id(reservation.getId());
        if (maybePayment.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Payment payment = maybePayment.get();
        if (payment.getStatus() != PaymentStatus.PAID) {
            cancelOpenPayment(payment);
            return BigDecimal.ZERO;
        }

        BigDecimal refund = cancellationRefundAmount(reservation, payment.getAmount(), cancelledByLot, OffsetDateTime.now());
        if (refund.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        String providerTx = transactionRepository.findByTransactionReference(payment.getTransactionReference())
                .map(PaymentTransaction::getProviderTransactionId).orElse(null);
        sendRefund(payment, refund, "Reservation " + reservation.getReservationCode() + " cancelled", providerTx);
        if (refund.compareTo(payment.getAmount()) >= 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }
        return refund;
    }

    BigDecimal cancellationRefundAmount(Reservation reservation, BigDecimal paid, boolean cancelledByLot, OffsetDateTime now) {
        if (cancelledByLot) {
            return paid;
        }
        long minutesBefore = Duration.between(now, reservation.getStartTime()).toMinutes();
        ParkShareProperties.Reservation policy = properties.getReservation();
        if (minutesBefore >= policy.getFreeCancellationMinutes()) {
            return paid;
        }
        if (minutesBefore > 0) {
            return Rwf.round(paid.multiply(policy.getLateCancellationRefundRate()), properties.getPricing().getRoundingStep());
        }
        return BigDecimal.ZERO;
    }

    /** Called when an unpaid reservation expires. */
    public void cancelOpenPayment(Reservation reservation) {
        paymentRepository.findByReservation_Id(reservation.getId()).ifPresent(this::cancelOpenPayment);
    }

    private void cancelOpenPayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.PROCESSING) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            transactionRepository.findByPayment_IdAndStatus(payment.getId(), "PROCESSING").forEach(tx -> {
                tx.setStatus("CANCELLED");
                tx.setCompletedAt(OffsetDateTime.now());
                transactionRepository.save(tx);
            });
        }
    }

    // ------------------------------------------------------------------
    // Receipt
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Receipt receipt(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("Payment not found: " + paymentId));
        Reservation reservation = payment.getReservation();
        guard.requireReservationAccess(reservation);
        if (payment.getStatus() != PaymentStatus.PAID && payment.getStatus() != PaymentStatus.REFUNDED) {
            throw ApiException.conflict("A receipt is only available after payment");
        }

        ParkingLot lot = reservation.getParkingLot();
        Optional<OperatorVerification> business = lot.getOperator() == null
                ? Optional.empty()
                : operatorVerificationRepository.findByUser_Id(lot.getOperator().getId());
        BigDecimal refunded = refundRepository.findByPayment_Id(payment.getId()).stream()
                .filter(refund -> "SUCCESS".equals(refund.getStatus()) || "PENDING".equals(refund.getStatus()))
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal vatRate = properties.getTax().getVatRate();
        BigDecimal vat = Rwf.vatFromInclusive(payment.getAmount(), vatRate);
        ZoneId zone = properties.zoneId();

        return new Receipt(
                payment.getPaymentReference(),
                payment.getStatus().name(),
                payment.getPaidAt() == null ? null : payment.getPaidAt().atZoneSameInstant(zone).toLocalDateTime(),
                lot.getName(),
                lot.getAddress() + ", " + lot.getDistrict() + ", " + lot.getCity(),
                business.map(OperatorVerification::getBusinessName).orElse(null),
                business.map(OperatorVerification::getBusinessRegistrationNumber).orElse(null),
                reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName(),
                reservation.getVehicle().getPlateNumber(),
                reservation.getReservationCode(),
                reservation.getStartTime().atZoneSameInstant(zone).toLocalDateTime(),
                reservation.getEndTime().atZoneSameInstant(zone).toLocalDateTime(),
                payment.getAmount().subtract(vat),
                vatRate,
                vat,
                payment.getAmount(),
                refunded,
                Rwf.CURRENCY,
                payment.getPaymentMethod(),
                mask(payment.getPayerPhone()),
                "Prices include VAT at " + vatRate.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP)
                        + "%. This is a ParkShare payment receipt; the operator's RRA EBM invoice is issued separately."
        );
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void confirm(Reservation reservation, User by, String reason) {
        lifecycle.transition(reservation, ReservationStatus.CONFIRMED, by, reason);
        qrTokenService.issue(reservation);
        notifier.inAppAndSms(reservation.getUser(), "RESERVATION_CONFIRMED", "Reservation confirmed",
                "Reservation " + reservation.getReservationCode() + " at " + reservation.getParkingLot().getName()
                        + " is confirmed for " + reservation.getStartTime().atZoneSameInstant(properties.zoneId())
                        .format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))
                        + ". Show your QR code at the entrance.",
                "RESERVATION", reservation.getId());
    }

    private void requirePayable(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw ApiException.conflict("Only reservations awaiting payment can be paid (status is "
                    + reservation.getStatus() + ")");
        }
        OffsetDateTime holdEnds = reservation.getCreatedAt().plusMinutes(properties.getReservation().getPaymentHoldMinutes());
        if (holdEnds.isBefore(OffsetDateTime.now())) {
            throw ApiException.conflict("The payment window for this reservation has closed; please book again");
        }
        paymentRepository.findByReservation_Id(reservation.getId())
                .filter(payment -> payment.getStatus() == PaymentStatus.PAID)
                .ifPresent(payment -> {
                    throw ApiException.conflict("This reservation is already paid");
                });
    }

    private Payment paymentFor(Reservation reservation) {
        Payment payment = paymentRepository.findByReservation_Id(reservation.getId()).orElseGet(() -> {
            Payment created = new Payment();
            created.setReservation(reservation);
            created.setPaymentReference(newReference("PAY"));
            created.setCurrency(Rwf.CURRENCY);
            created.setStatus(PaymentStatus.PENDING);
            return created;
        });
        payment.setAmount(reservation.getTotalAmount());
        return payment;
    }

    private void sendRefund(Payment payment, BigDecimal amount, String reason, String providerTransactionId) {
        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(amount);
        refund.setCurrency(Rwf.CURRENCY);
        refund.setReason(reason);
        refund.setRequestedAt(OffsetDateTime.now());
        if ("CASH".equals(payment.getPaymentMethod())) {
            refund.setStatus("PENDING"); // operator hands the cash back
        } else {
            String providerRefundId = gateway.refund(providerTransactionId, payment.getPayerPhone(), amount, reason);
            refund.setProviderRefundId(providerRefundId);
            refund.setStatus("SUCCESS");
            refund.setCompletedAt(OffsetDateTime.now());
        }
        refundRepository.save(refund);
    }

    private Reservation loadReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found: " + reservationId));
    }

    private PaymentView view(Payment payment, String instructions) {
        return new PaymentView(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getPaymentReference(),
                payment.getTransactionReference(),
                payment.getAmount(),
                Rwf.vatFromInclusive(payment.getAmount(), properties.getTax().getVatRate()),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus().name(),
                mask(payment.getPayerPhone()),
                payment.getPaidAt(),
                instructions
        );
    }

    static String mask(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 6) + "****" + phone.substring(phone.length() - 3);
    }

    static String newReference(String prefix) {
        StringBuilder builder = new StringBuilder(prefix).append('-')
                .append(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))).append('-');
        for (int i = 0; i < 8; i++) {
            builder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}

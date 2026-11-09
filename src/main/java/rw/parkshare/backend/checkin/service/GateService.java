package rw.parkshare.backend.checkin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.checkin.entity.CheckIn;
import rw.parkshare.backend.checkin.entity.CheckOut;
import rw.parkshare.backend.checkin.repository.CheckInRepository;
import rw.parkshare.backend.checkin.repository.CheckOutRepository;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.notification.service.Notifier;
import rw.parkshare.backend.payment.entity.PaymentStatus;
import rw.parkshare.backend.payment.repository.PaymentRepository;
import rw.parkshare.backend.pricing.PriceQuote;
import rw.parkshare.backend.pricing.PricingService;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationQrCode;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.reservation.service.QrTokenService;
import rw.parkshare.backend.reservation.service.ReservationLifecycle;
import rw.parkshare.backend.security.AccessGuard;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Entrance and exit at the lot, operated by attendants/operators.
 */
@Service
@Transactional
public class GateService {

    private final ReservationRepository reservationRepository;
    private final CheckInRepository checkInRepository;
    private final CheckOutRepository checkOutRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final QrTokenService qrTokenService;
    private final ReservationLifecycle lifecycle;
    private final PricingService pricingService;
    private final Notifier notifier;
    private final AccessGuard guard;
    private final ParkShareProperties properties;

    public GateService(
            ReservationRepository reservationRepository,
            CheckInRepository checkInRepository,
            CheckOutRepository checkOutRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            QrTokenService qrTokenService,
            ReservationLifecycle lifecycle,
            PricingService pricingService,
            Notifier notifier,
            AccessGuard guard,
            ParkShareProperties properties
    ) {
        this.reservationRepository = reservationRepository;
        this.checkInRepository = checkInRepository;
        this.checkOutRepository = checkOutRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.qrTokenService = qrTokenService;
        this.lifecycle = lifecycle;
        this.pricingService = pricingService;
        this.notifier = notifier;
        this.guard = guard;
        this.properties = properties;
    }

    public GateResult checkInByQr(String token) {
        ReservationQrCode qr = qrTokenService.resolve(token);
        Reservation reservation = qr.getReservation();
        GateResult result = checkIn(reservation, "QR");
        qrTokenService.markUsed(qr);
        return result;
    }

    /** Fallback when the driver's phone cannot show the QR: attendant types the code. */
    public GateResult checkInByCode(String reservationCode) {
        Reservation reservation = reservationRepository.findByReservationCode(reservationCode == null ? "" : reservationCode.trim())
                .orElseThrow(() -> ApiException.notFound("Reservation not found: " + reservationCode));
        return checkIn(reservation, "CODE");
    }

    private GateResult checkIn(Reservation reservation, String method) {
        guard.requireLotManager(reservation.getParkingLot().getId());

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw ApiException.conflict("Reservation " + reservation.getReservationCode()
                    + " cannot check in (status " + reservation.getStatus() + ")");
        }
        boolean paid = paymentRepository.findByReservation_Id(reservation.getId())
                .map(payment -> payment.getStatus() == PaymentStatus.PAID)
                .orElse(false);
        if (!paid) {
            throw ApiException.conflict("Reservation has not been paid");
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime earliest = reservation.getStartTime()
                .minusMinutes(properties.getReservation().getEarlyCheckInMinutes());
        if (now.isBefore(earliest)) {
            throw ApiException.conflict("Too early: check-in opens at " + earliest.atZoneSameInstant(properties.zoneId())
                    .toLocalTime().withNano(0));
        }
        if (!now.isBefore(reservation.getEndTime())) {
            throw ApiException.conflict("This reservation has already ended");
        }
        if (checkInRepository.existsByReservation_Id(reservation.getId())) {
            throw ApiException.conflict("Already checked in");
        }

        User attendant = userRepository.findById(guard.current().id()).orElse(null);
        CheckIn checkIn = new CheckIn();
        checkIn.setReservation(reservation);
        checkIn.setVehicle(reservation.getVehicle());
        checkIn.setParkingSpace(reservation.getParkingSpace());
        checkIn.setCheckedInBy(attendant);
        checkIn.setCheckInMethod(method);
        checkIn.setCheckedInAt(now);
        checkInRepository.save(checkIn);

        lifecycle.transition(reservation, ReservationStatus.CHECKED_IN, attendant, "Checked in by " + method);
        notifier.inApp(reservation.getUser(), "CHECK_IN_SUCCESS", "Checked in",
                "Welcome to " + reservation.getParkingLot().getName() + ". Your space: "
                        + spaceCode(reservation) + ". Leave by "
                        + reservation.getEndTime().atZoneSameInstant(properties.zoneId()).toLocalTime().withNano(0)
                        + " to avoid overstay charges.",
                "RESERVATION", reservation.getId());

        return new GateResult(reservation.getId(), reservation.getReservationCode(),
                reservation.getVehicle().getPlateNumber(), spaceCode(reservation),
                ReservationStatus.CHECKED_IN.name(), now, 0, BigDecimal.ZERO.setScale(2), null);
    }

    /**
     * Exit. Overstay past the grace period is priced from the reservation end
     * using the lot's normal rates and must be settled at the gate.
     */
    public GateResult checkOut(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found: " + reservationId));
        guard.requireLotManager(reservation.getParkingLot().getId());
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw ApiException.conflict("Reservation is not checked in (status " + reservation.getStatus() + ")");
        }
        CheckIn checkIn = checkInRepository.findByReservation_Id(reservationId)
                .orElseThrow(() -> ApiException.conflict("No check-in found for this reservation"));

        OffsetDateTime now = OffsetDateTime.now();
        long overstayMinutes = Math.max(0, Duration.between(reservation.getEndTime(), now).toMinutes());
        BigDecimal overstayAmount = BigDecimal.ZERO.setScale(2);
        if (overstayMinutes > properties.getPricing().getOverstayGraceMinutes()) {
            PriceQuote overstay = pricingService.quoteOverstay(reservation.getParkingLot().getId(),
                    reservation.getEndTime(), now);
            overstayAmount = overstay.total();
        } else {
            overstayMinutes = 0;
        }

        User attendant = userRepository.findById(guard.current().id()).orElse(null);
        CheckOut checkOut = new CheckOut();
        checkOut.setCheckIn(checkIn);
        checkOut.setReservation(reservation);
        checkOut.setCheckedOutBy(attendant);
        checkOut.setCheckOutMethod("MANUAL");
        checkOut.setCheckedOutAt(now);
        checkOut.setOverstayMinutes((int) overstayMinutes);
        checkOut.setOverstayAmount(overstayAmount);
        checkOutRepository.save(checkOut);

        lifecycle.transition(reservation, ReservationStatus.COMPLETED, attendant, "Checked out");

        String message = overstayAmount.signum() > 0
                ? "You stayed " + overstayMinutes + " min past your booking. Overstay due: "
                + overstayAmount.toBigInteger() + " RWF (VAT incl.)."
                : "Thank you for parking with ParkShare.";
        notifier.notify(reservation.getUser(), "CHECK_OUT_SUCCESS", "Checked out", message,
                "RESERVATION", reservation.getId());

        return new GateResult(reservation.getId(), reservation.getReservationCode(),
                reservation.getVehicle().getPlateNumber(), spaceCode(reservation),
                ReservationStatus.COMPLETED.name(), now, overstayMinutes, overstayAmount,
                overstayAmount.signum() > 0 ? "Collect the overstay amount before the vehicle leaves" : null);
    }

    private static String spaceCode(Reservation reservation) {
        return reservation.getParkingSpace() == null ? null : reservation.getParkingSpace().getSpaceCode();
    }

    public record GateResult(
            Long reservationId,
            String reservationCode,
            String vehiclePlate,
            String spaceCode,
            String status,
            OffsetDateTime at,
            long overstayMinutes,
            BigDecimal overstayAmount,
            String instruction
    ) {
    }
}

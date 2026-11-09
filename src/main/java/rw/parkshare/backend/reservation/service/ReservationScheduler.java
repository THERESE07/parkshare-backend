package rw.parkshare.backend.reservation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import rw.parkshare.backend.checkin.repository.CheckInRepository;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.notification.repository.NotificationRepository;
import rw.parkshare.backend.notification.service.Notifier;
import rw.parkshare.backend.payment.service.PaymentFlowService;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import rw.parkshare.backend.reservation.repository.ReservationRepository;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Background housekeeping, once a minute:
 * - unpaid reservations past the payment hold become EXPIRED (space released);
 * - paid reservations with no check-in long after start become NO_SHOW;
 * - drivers get a reminder 30 minutes before their reservation starts.
 * Each step runs in its own transaction; call the public methods inside one.
 */
@Component
@ConditionalOnProperty(prefix = "parkshare.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReservationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationScheduler.class);
    private static final int REMINDER_MINUTES = 30;

    private final ReservationRepository reservationRepository;
    private final CheckInRepository checkInRepository;
    private final NotificationRepository notificationRepository;
    private final ReservationLifecycle lifecycle;
    private final PaymentFlowService paymentFlowService;
    private final Notifier notifier;
    private final ParkShareProperties properties;
    private final TransactionTemplate tx;

    public ReservationScheduler(
            ReservationRepository reservationRepository,
            CheckInRepository checkInRepository,
            NotificationRepository notificationRepository,
            ReservationLifecycle lifecycle,
            PaymentFlowService paymentFlowService,
            Notifier notifier,
            ParkShareProperties properties,
            PlatformTransactionManager transactionManager
    ) {
        this.tx = new TransactionTemplate(transactionManager);
        this.reservationRepository = reservationRepository;
        this.checkInRepository = checkInRepository;
        this.notificationRepository = notificationRepository;
        this.lifecycle = lifecycle;
        this.paymentFlowService = paymentFlowService;
        this.notifier = notifier;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${parkshare.scheduler.interval-ms:60000}", initialDelayString = "${parkshare.scheduler.initial-delay-ms:30000}")
    public void run() {
        try {
            tx.executeWithoutResult(status -> expireUnpaid());
            tx.executeWithoutResult(status -> markNoShows());
            tx.executeWithoutResult(status -> sendReminders());
        } catch (RuntimeException exception) {
            log.error("Reservation housekeeping failed", exception);
        }
    }

    public int expireUnpaid() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(properties.getReservation().getPaymentHoldMinutes());
        int count = 0;
        for (Reservation reservation : reservationRepository.findByStatusAndCreatedAtBefore(ReservationStatus.PENDING, cutoff)) {
            paymentFlowService.cancelOpenPayment(reservation);
            lifecycle.transition(reservation, ReservationStatus.EXPIRED, null, "Not paid within the payment window");
            notifier.inApp(reservation.getUser(), "RESERVATION_CANCELLED", "Reservation expired",
                    "Reservation " + reservation.getReservationCode() + " expired because it was not paid in time.",
                    "RESERVATION", reservation.getId());
            count++;
        }
        if (count > 0) {
            log.info("Expired {} unpaid reservation(s)", count);
        }
        return count;
    }

    public int markNoShows() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(properties.getReservation().getNoShowGraceMinutes());
        int count = 0;
        for (Reservation reservation : reservationRepository.findByStatusAndStartTimeBefore(ReservationStatus.CONFIRMED, cutoff)) {
            if (checkInRepository.existsByReservation_Id(reservation.getId())) {
                continue;
            }
            lifecycle.transition(reservation, ReservationStatus.NO_SHOW, null, "Did not arrive");
            count++;
        }
        if (count > 0) {
            log.info("Marked {} reservation(s) as no-show", count);
        }
        return count;
    }

    public int sendReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        int count = 0;
        for (Reservation reservation : reservationRepository.findByStatusAndStartTimeBetween(
                ReservationStatus.CONFIRMED, now, now.plusMinutes(REMINDER_MINUTES))) {
            boolean alreadyReminded = notificationRepository
                    .findByUser_IdAndReferenceTypeAndReferenceId(reservation.getUser().getId(), "RESERVATION", reservation.getId())
                    .stream().anyMatch(n -> "RESERVATION_REMINDER".equals(n.getType()));
            if (alreadyReminded) {
                continue;
            }
            notifier.inAppAndSms(reservation.getUser(), "RESERVATION_REMINDER", "Parking starts soon",
                    "Your parking at " + reservation.getParkingLot().getName() + " starts at "
                            + reservation.getStartTime().atZoneSameInstant(properties.zoneId())
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                            + ". Code: " + reservation.getReservationCode(),
                    "RESERVATION", reservation.getId());
            count++;
        }
        return count;
    }
}

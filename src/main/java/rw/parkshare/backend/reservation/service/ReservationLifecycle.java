package rw.parkshare.backend.reservation.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.parking.entity.ParkingSpace;
import rw.parkshare.backend.parking.repository.ParkingSpaceRepository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import rw.parkshare.backend.reservation.entity.ReservationStatusHistory;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.reservation.repository.ReservationStatusHistoryRepository;
import rw.parkshare.backend.user.entity.User;

import java.time.OffsetDateTime;

/**
 * The only place reservation status changes happen. Enforces the allowed
 * transitions, records history, and keeps the space status in step.
 */
@Component
@Transactional
public class ReservationLifecycle {

    private final ReservationRepository reservationRepository;
    private final ReservationStatusHistoryRepository historyRepository;
    private final ParkingSpaceRepository spaceRepository;
    private final QrTokenService qrTokenService;

    public ReservationLifecycle(
            ReservationRepository reservationRepository,
            ReservationStatusHistoryRepository historyRepository,
            ParkingSpaceRepository spaceRepository,
            QrTokenService qrTokenService
    ) {
        this.reservationRepository = reservationRepository;
        this.historyRepository = historyRepository;
        this.spaceRepository = spaceRepository;
        this.qrTokenService = qrTokenService;
    }

    /** Records the initial status of a newly created reservation. */
    public void recordCreated(Reservation reservation, User by) {
        saveHistory(reservation, null, reservation.getStatus(), by, "Reservation created");
    }

    public Reservation transition(Reservation reservation, ReservationStatus next, User by, String reason) {
        ReservationStatus current = reservation.getStatus();
        if (current == next) {
            return reservation;
        }
        if (!current.canTransitionTo(next)) {
            throw ApiException.conflict("Reservation cannot change from " + current + " to " + next);
        }
        reservation.setStatus(next);
        Reservation saved = reservationRepository.save(reservation);
        saveHistory(saved, current, next, by, reason);

        ParkingSpace space = saved.getParkingSpace();
        if (space != null) {
            if (next == ReservationStatus.CHECKED_IN) {
                space.setStatus("OCCUPIED");
                spaceRepository.save(space);
            } else if (current == ReservationStatus.CHECKED_IN && "OCCUPIED".equals(space.getStatus())) {
                space.setStatus("AVAILABLE");
                spaceRepository.save(space);
            }
        }
        if (!next.isActive()) {
            qrTokenService.deactivateAll(saved);
        }
        return saved;
    }

    private void saveHistory(Reservation reservation, ReservationStatus from, ReservationStatus to, User by, String reason) {
        ReservationStatusHistory history = new ReservationStatusHistory();
        history.setReservation(reservation);
        history.setOldStatus(from == null ? null : from.name());
        history.setNewStatus(to.name());
        history.setChangedBy(by);
        history.setReason(reason);
        history.setCreatedAt(OffsetDateTime.now());
        historyRepository.save(history);
    }
}

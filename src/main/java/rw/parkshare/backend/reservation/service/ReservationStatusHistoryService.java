package rw.parkshare.backend.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ReservationStatusHistoryRequestDto;
import rw.parkshare.backend.common.dto.ReservationStatusHistoryResponseDto;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatusHistory;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.reservation.repository.ReservationStatusHistoryRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationStatusHistoryService {

    private final ReservationStatusHistoryRepository historyRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ReservationStatusHistoryService(
            ReservationStatusHistoryRepository historyRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository
    ) {
        this.historyRepository = historyRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    public ReservationStatusHistoryResponseDto createHistory(
            Long reservationId,
            Long changedByUserId,
            ReservationStatusHistoryRequestDto request
    ) {
        if (request.getNewStatus() == null
                || request.getNewStatus().isBlank()) {
            throw new IllegalArgumentException(
                    "New reservation status is required."
            );
        }

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found with ID: "
                                                + reservationId
                                )
                        );

        User changedBy = null;

        if (changedByUserId != null) {
            changedBy = userRepository.findById(changedByUserId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found with ID: "
                                            + changedByUserId
                            )
                    );
        }

        ReservationStatusHistory history =
                new ReservationStatusHistory();

        history.setReservation(reservation);
        history.setOldStatus(request.getOldStatus());
        history.setNewStatus(request.getNewStatus());
        history.setChangedBy(changedBy);
        history.setReason(request.getReason());
        history.setCreatedAt(OffsetDateTime.now());

        return toResponseDto(
                historyRepository.save(history)
        );
    }

    @Transactional(readOnly = true)
    public ReservationStatusHistoryResponseDto getHistoryById(Long id) {

        return toResponseDto(
                getHistoryEntityById(id)
        );
    }

    @Transactional(readOnly = true)
    public List<ReservationStatusHistoryResponseDto>
    getHistoryByReservationId(Long reservationId) {

        return historyRepository.findByReservation_Id(
                        reservationId
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationStatusHistoryResponseDto>
    getHistoryByChangedByUserId(Long userId) {

        return historyRepository.findByChangedBy_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationStatusHistoryResponseDto>
    getHistoryByNewStatus(String newStatus) {

        return historyRepository.findByNewStatus(newStatus)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationStatusHistoryResponseDto>
    getHistoryByOldStatus(String oldStatus) {

        return historyRepository.findByOldStatus(oldStatus)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationStatusHistoryResponseDto>
    getHistoryByReservationAndNewStatus(
            Long reservationId,
            String newStatus
    ) {

        return historyRepository
                .findByReservation_IdAndNewStatus(
                        reservationId,
                        newStatus
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public ReservationStatusHistoryResponseDto recordStatusChange(
            Long reservationId,
            Long changedByUserId,
            ReservationStatusHistoryRequestDto request
    ) {
        return createHistory(
                reservationId,
                changedByUserId,
                request
        );
    }

    public ReservationStatusHistoryResponseDto recordStatusChange(
            Long reservationId,
            ReservationStatusHistoryRequestDto request
    ) {
        return createHistory(
                reservationId,
                null,
                request
        );
    }

    public ReservationStatusHistoryResponseDto updateHistory(
            Long id,
            ReservationStatusHistoryRequestDto request
    ) {
        if (request.getNewStatus() == null
                || request.getNewStatus().isBlank()) {
            throw new IllegalArgumentException(
                    "New reservation status is required."
            );
        }

        ReservationStatusHistory history =
                getHistoryEntityById(id);

        history.setNewStatus(request.getNewStatus());

        if (request.getReason() != null) {
            history.setReason(request.getReason());
        }

        return toResponseDto(
                historyRepository.save(history)
        );
    }

    public void deleteHistory(Long id) {

        ReservationStatusHistory history =
                getHistoryEntityById(id);

        historyRepository.delete(history);
    }

    private ReservationStatusHistory getHistoryEntityById(Long id) {

        return historyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation status history not found with ID: "
                                        + id
                        )
                );
    }

    private ReservationStatusHistoryResponseDto toResponseDto(
            ReservationStatusHistory history
    ) {
        return new ReservationStatusHistoryResponseDto(
                history.getId(),
                history.getReservation().getId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getChangedBy() != null
                        ? history.getChangedBy().getId()
                        : null,
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
package rw.parkshare.backend.checkin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.checkin.entity.CheckIn;
import rw.parkshare.backend.checkin.entity.CheckOut;
import rw.parkshare.backend.checkin.repository.CheckInRepository;
import rw.parkshare.backend.checkin.repository.CheckOutRepository;
import rw.parkshare.backend.common.dto.CheckOutResponseDto;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class CheckOutService {

    private final CheckOutRepository checkOutRepository;
    private final CheckInRepository checkInRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public CheckOutService(
            CheckOutRepository checkOutRepository,
            CheckInRepository checkInRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository
    ) {
        this.checkOutRepository = checkOutRepository;
        this.checkInRepository = checkInRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    public CheckOutResponseDto createCheckOut(
            Long checkInId,
            Long reservationId,
            Long checkedOutById,
            String checkOutMethod,
            OffsetDateTime checkedOutAt,
            String notes
    ) {
        if (checkOutRepository.existsByCheckIn_Id(checkInId)) {
            throw new IllegalArgumentException(
                    "A check-out already exists for this check-in."
            );
        }

        if (checkOutRepository.existsByReservation_Id(reservationId)) {
            throw new IllegalArgumentException(
                    "A check-out already exists for this reservation."
            );
        }

        if (checkOutMethod == null || checkOutMethod.isBlank()) {
            throw new IllegalArgumentException(
                    "Check-out method is required."
            );
        }

        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-in not found with ID: " + checkInId
                        )
                );

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation not found with ID: " + reservationId
                        )
                );

        if (!checkIn.getReservation().getId().equals(reservationId)) {
            throw new IllegalArgumentException(
                    "The check-in does not belong to the specified reservation."
            );
        }

        User checkedOutBy = null;

        if (checkedOutById != null) {
            checkedOutBy = userRepository.findById(checkedOutById)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Check-out user not found with ID: "
                                            + checkedOutById
                            )
                    );
        }

        if (checkedOutAt == null) {
            checkedOutAt = OffsetDateTime.now();
        }

        if (checkedOutAt.isBefore(checkIn.getCheckedInAt())) {
            throw new IllegalArgumentException(
                    "Check-out time cannot be before check-in time."
            );
        }

        CheckOut checkOut = new CheckOut();

        checkOut.setCheckIn(checkIn);
        checkOut.setReservation(reservation);
        checkOut.setCheckedOutBy(checkedOutBy);
        checkOut.setCheckOutMethod(checkOutMethod);
        checkOut.setCheckedOutAt(checkedOutAt);
        checkOut.setNotes(notes);

        return toResponseDto(checkOutRepository.save(checkOut));
    }

    @Transactional(readOnly = true)
    public CheckOutResponseDto getCheckOutById(Long id) {
        return checkOutRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-out not found with ID: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public CheckOutResponseDto getCheckOutByCheckInId(Long checkInId) {
        return checkOutRepository.findByCheckIn_Id(checkInId)
                .map(this::toResponseDto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-out not found for check-in ID: "
                                        + checkInId
                        )
                );
    }

    @Transactional(readOnly = true)
    public CheckOutResponseDto getCheckOutByReservationId(Long reservationId) {
        return checkOutRepository.findByReservation_Id(reservationId)
                .map(this::toResponseDto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-out not found for reservation ID: "
                                        + reservationId
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<CheckOutResponseDto> getAllCheckOuts() {
        return checkOutRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheckOutResponseDto> getCheckOutsByUserId(Long userId) {
        return checkOutRepository.findByCheckedOutBy_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheckOutResponseDto> getCheckOutsByMethod(
            String checkOutMethod
    ) {
        return checkOutRepository.findByCheckOutMethod(checkOutMethod)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheckOutResponseDto> getCheckOutsBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return checkOutRepository.findByCheckedOutAtBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasCheckOutForCheckIn(Long checkInId) {
        return checkOutRepository.existsByCheckIn_Id(checkInId);
    }

    @Transactional(readOnly = true)
    public boolean hasCheckOutForReservation(Long reservationId) {
        return checkOutRepository.existsByReservation_Id(reservationId);
    }

    public CheckOutResponseDto updateCheckOut(
            Long id,
            Long checkedOutById,
            String checkOutMethod,
            OffsetDateTime checkedOutAt,
            String notes
    ) {
        CheckOut checkOut = checkOutRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-out not found with ID: " + id
                        )
                );

        if (checkedOutById != null) {
            User checkedOutBy = userRepository.findById(checkedOutById)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Check-out user not found with ID: "
                                            + checkedOutById
                            )
                    );

            checkOut.setCheckedOutBy(checkedOutBy);
        }

        if (checkOutMethod != null && !checkOutMethod.isBlank()) {
            checkOut.setCheckOutMethod(checkOutMethod);
        }

        if (checkedOutAt != null) {
            if (checkedOutAt.isBefore(
                    checkOut.getCheckIn().getCheckedInAt()
            )) {
                throw new IllegalArgumentException(
                        "Check-out time cannot be before check-in time."
                );
            }

            checkOut.setCheckedOutAt(checkedOutAt);
        }

        if (notes != null) {
            checkOut.setNotes(notes);
        }

        return toResponseDto(checkOutRepository.save(checkOut));
    }

    public void deleteCheckOut(Long id) {
        CheckOut checkOut = checkOutRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-out not found with ID: " + id
                        )
                );

        checkOutRepository.delete(checkOut);
    }

    private CheckOutResponseDto toResponseDto(CheckOut checkOut) {
        return new CheckOutResponseDto(
                checkOut.getId(),
                checkOut.getCheckIn() != null
                        ? checkOut.getCheckIn().getId()
                        : null,
                checkOut.getReservation() != null
                        ? checkOut.getReservation().getId()
                        : null,
                checkOut.getCheckedOutBy() != null
                        ? checkOut.getCheckedOutBy().getId()
                        : null,
                checkOut.getCheckOutMethod(),
                checkOut.getCheckedOutAt(),
                checkOut.getNotes()
        );
    }
}
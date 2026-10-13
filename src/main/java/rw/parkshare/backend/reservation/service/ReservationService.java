package rw.parkshare.backend.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ReservationRequestDto;
import rw.parkshare.backend.common.dto.ReservationResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingSpace;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingSpaceRepository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.Vehicle;
import rw.parkshare.backend.user.repository.UserRepository;
import rw.parkshare.backend.user.repository.VehicleRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            ParkingLotRepository parkingLotRepository,
            ParkingSpaceRepository parkingSpaceRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSpaceRepository = parkingSpaceRepository;
    }

    public ReservationResponseDto createReservation(
            Long userId,
            Long vehicleId,
            Long parkingLotId,
            Long parkingSpaceId,
            ReservationRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("Reservation request is required");
        }

        if (request.getReservationCode() == null
                || request.getReservationCode().isBlank()) {
            throw new IllegalArgumentException("Reservation code is required");
        }

        if (reservationRepository.existsByReservationCode(
                request.getReservationCode())) {
            throw new IllegalArgumentException("Reservation code already exists");
        }

        if (request.getStartTime() == null
                || request.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        if (request.getTotalAmount() != null
                && request.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Total amount cannot be negative"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        ));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not found: " + vehicleId
                        ));

        if (vehicle.getUser() == null
                || !vehicle.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Vehicle does not belong to the specified user"
            );
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found: " + parkingLotId
                        ));

        ParkingSpace parkingSpace = null;

        if (parkingSpaceId != null) {
            parkingSpace = parkingSpaceRepository.findById(parkingSpaceId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Parking space not found: "
                                            + parkingSpaceId
                            ));

            if (parkingSpace.getParkingZone() == null
                    || parkingSpace.getParkingZone().getParkingLot() == null
                    || !parkingSpace.getParkingZone()
                    .getParkingLot()
                    .getId()
                    .equals(parkingLotId)) {

                throw new IllegalArgumentException(
                        "Parking space does not belong to the specified parking lot"
                );
            }

            List<Reservation> existingReservations =
                    reservationRepository.findByParkingSpace_Id(
                            parkingSpaceId
                    );

            for (Reservation existing : existingReservations) {
                if (isBlockingStatus(existing.getStatus())
                        && timesOverlap(
                        existing.getStartTime(),
                        existing.getEndTime(),
                        request.getStartTime(),
                        request.getEndTime()
                )) {
                    throw new IllegalArgumentException(
                            "Parking space is already reserved for the selected time"
                    );
                }
            }
        }

        Reservation reservation = new Reservation();

        reservation.setReservationCode(request.getReservationCode());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());

        reservation.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : ReservationStatus.PENDING
        );

        reservation.setTotalAmount(
                request.getTotalAmount() != null
                        ? request.getTotalAmount()
                        : BigDecimal.ZERO
        );

        reservation.setCurrency(
                request.getCurrency() != null
                        && !request.getCurrency().isBlank()
                        ? request.getCurrency()
                        : "RWF"
        );

        reservation.setNotes(request.getNotes());

        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setParkingLot(parkingLot);
        reservation.setParkingSpace(parkingSpace);

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return toResponseDto(savedReservation);
    }

    private boolean isBlockingStatus(ReservationStatus status) {
        return status == ReservationStatus.PENDING
                || status == ReservationStatus.CONFIRMED;
    }

    private boolean timesOverlap(
            OffsetDateTime existingStart,
            OffsetDateTime existingEnd,
            OffsetDateTime newStart,
            OffsetDateTime newEnd
    ) {
        return existingStart.isBefore(newEnd)
                && newStart.isBefore(existingEnd);
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(Long id) {
        return toResponseDto(getReservationEntityById(id));
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationByCode(
            String reservationCode
    ) {
        Reservation reservation =
                reservationRepository.findByReservationCode(reservationCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found with code: "
                                                + reservationCode
                                ));

        return toResponseDto(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByUserId(
            Long userId
    ) {
        return reservationRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByVehicleId(
            Long vehicleId
    ) {
        return reservationRepository.findByVehicle_Id(vehicleId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByParkingLotId(
            Long parkingLotId
    ) {
        return reservationRepository.findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByParkingSpaceId(
            Long parkingSpaceId
    ) {
        return reservationRepository.findByParkingSpace_Id(parkingSpaceId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByStatus(
            ReservationStatus status
    ) {
        return reservationRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByUserAndStatus(
            Long userId,
            ReservationStatus status
    ) {
        return reservationRepository.findByUser_IdAndStatus(
                        userId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByParkingLotAndStatus(
            Long parkingLotId,
            ReservationStatus status
    ) {
        return reservationRepository
                .findByParkingLot_IdAndStatus(
                        parkingLotId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return reservationRepository.findByStartTimeBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getParkingLotReservationsBetween(
            Long parkingLotId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return reservationRepository
                .findByParkingLot_IdAndStartTimeBetween(
                        parkingLotId,
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private void validateDateRange(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }
    }

    public ReservationResponseDto updateReservation(
            Long id,
            ReservationRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Reservation request is required"
            );
        }

        Reservation reservation = getReservationEntityById(id);

        if (request.getStartTime() != null) {
            reservation.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            reservation.setEndTime(request.getEndTime());
        }

        if (reservation.getStartTime() != null
                && reservation.getEndTime() != null
                && !reservation.getEndTime()
                .isAfter(reservation.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        if (request.getStatus() != null) {
            reservation.setStatus(request.getStatus());
        }

        if (request.getTotalAmount() != null) {
            if (request.getTotalAmount()
                    .compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Total amount cannot be negative"
                );
            }

            reservation.setTotalAmount(request.getTotalAmount());
        }

        if (request.getCurrency() != null
                && !request.getCurrency().isBlank()) {
            reservation.setCurrency(request.getCurrency());
        }

        if (request.getNotes() != null) {
            reservation.setNotes(request.getNotes());
        }

        return toResponseDto(
                reservationRepository.save(reservation)
        );
    }

    public ReservationResponseDto updateReservationStatus(
            Long id,
            ReservationStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        Reservation reservation = getReservationEntityById(id);
        reservation.setStatus(status);

        return toResponseDto(
                reservationRepository.save(reservation)
        );
    }

    public ReservationResponseDto cancelReservation(Long id) {
        Reservation reservation = getReservationEntityById(id);
        reservation.setStatus(ReservationStatus.CANCELLED);

        return toResponseDto(
                reservationRepository.save(reservation)
        );
    }

    @Transactional(readOnly = true)
    public boolean reservationCodeExists(String reservationCode) {
        return reservationRepository.existsByReservationCode(
                reservationCode
        );
    }

    public void deleteReservation(Long id) {
        Reservation reservation = getReservationEntityById(id);
        reservationRepository.delete(reservation);
    }

    private Reservation getReservationEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation not found: " + id
                        ));
    }

    private ReservationResponseDto toResponseDto(
            Reservation reservation
    ) {
        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getReservationCode(),
                reservation.getUser() != null
                        ? reservation.getUser().getId()
                        : null,
                reservation.getVehicle() != null
                        ? reservation.getVehicle().getId()
                        : null,
                reservation.getParkingLot() != null
                        ? reservation.getParkingLot().getId()
                        : null,
                reservation.getParkingSpace() != null
                        ? reservation.getParkingSpace().getId()
                        : null,
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getTotalAmount(),
                reservation.getCurrency(),
                reservation.getNotes()
        );
    }
}
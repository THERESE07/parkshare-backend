package rw.parkshare.backend.checkin.service;

import rw.parkshare.backend.checkin.entity.CheckIn;
import rw.parkshare.backend.checkin.repository.CheckInRepository;
import rw.parkshare.backend.common.dto.CheckInResponseDto;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.Vehicle;
import rw.parkshare.backend.user.repository.UserRepository;
import rw.parkshare.backend.user.repository.VehicleRepository;
import rw.parkshare.backend.parking.entity.ParkingSpace;
import rw.parkshare.backend.parking.repository.ParkingSpaceRepository;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final UserRepository userRepository;

    public CheckInService(
            CheckInRepository checkInRepository,
            ReservationRepository reservationRepository,
            VehicleRepository vehicleRepository,
            ParkingSpaceRepository parkingSpaceRepository,
            UserRepository userRepository
    ) {
        this.checkInRepository = checkInRepository;
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
        this.parkingSpaceRepository = parkingSpaceRepository;
        this.userRepository = userRepository;
    }

    public CheckInResponseDto createCheckIn(
            Long reservationId,
            Long vehicleId,
            Long parkingSpaceId,
            Long checkedInById,
            String checkInMethod,
            OffsetDateTime checkedInAt,
            String notes
    ) {
        if (checkInRepository.existsByReservation_Id(reservationId)) {
            throw new IllegalArgumentException(
                    "Check-in already exists for reservation ID: " + reservationId
            );
        }

        if (checkInMethod == null || checkInMethod.isBlank()) {
            throw new IllegalArgumentException("Check-in method is required");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reservation not found: " + reservationId
                        )
                );

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not found: " + vehicleId
                        )
                );

        if (!vehicle.getUser().getId().equals(reservation.getUser().getId())) {
            throw new IllegalArgumentException(
                    "Vehicle does not belong to the reservation user"
            );
        }

        ParkingSpace parkingSpace = null;

        if (parkingSpaceId != null) {
            parkingSpace = parkingSpaceRepository.findById(parkingSpaceId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Parking space not found: " + parkingSpaceId
                            )
                    );
        }

        User checkedInBy = null;

        if (checkedInById != null) {
            checkedInBy = userRepository.findById(checkedInById)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found: " + checkedInById
                            )
                    );
        }

        CheckIn checkIn = new CheckIn();

        checkIn.setReservation(reservation);
        checkIn.setVehicle(vehicle);
        checkIn.setParkingSpace(parkingSpace);
        checkIn.setCheckedInBy(checkedInBy);
        checkIn.setCheckInMethod(checkInMethod);
        checkIn.setCheckedInAt(
                checkedInAt != null ? checkedInAt : OffsetDateTime.now()
        );
        checkIn.setNotes(notes);

        return toResponseDto(checkInRepository.save(checkIn));
    }

    public CheckInResponseDto getCheckInById(Long id) {
        return checkInRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-in not found: " + id
                        )
                );
    }

    public CheckInResponseDto getCheckInByReservationId(Long reservationId) {
        return checkInRepository.findByReservation_Id(reservationId)
                .map(this::toResponseDto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-in not found for reservation: " + reservationId
                        )
                );
    }

    public List<CheckInResponseDto> getAllCheckIns() {
        return checkInRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<CheckInResponseDto> getCheckInsByVehicleId(Long vehicleId) {
        return checkInRepository.findByVehicle_Id(vehicleId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<CheckInResponseDto> getCheckInsByParkingSpaceId(Long parkingSpaceId) {
        return checkInRepository.findByParkingSpace_Id(parkingSpaceId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<CheckInResponseDto> getCheckInsByCheckedInById(Long userId) {
        return checkInRepository.findByCheckedInBy_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<CheckInResponseDto> getCheckInsByMethod(String checkInMethod) {
        return checkInRepository.findByCheckInMethod(checkInMethod)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<CheckInResponseDto> getCheckInsBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return checkInRepository.findByCheckedInAtBetween(startTime, endTime)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public boolean hasCheckInForReservation(Long reservationId) {
        return checkInRepository.existsByReservation_Id(reservationId);
    }

    public CheckInResponseDto updateCheckIn(
            Long id,
            Long parkingSpaceId,
            Long checkedInById,
            String checkInMethod,
            OffsetDateTime checkedInAt,
            String notes
    ) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Check-in not found: " + id
                        )
                );

        if (parkingSpaceId != null) {
            ParkingSpace parkingSpace = parkingSpaceRepository.findById(parkingSpaceId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Parking space not found: " + parkingSpaceId
                            )
                    );

            checkIn.setParkingSpace(parkingSpace);
        }

        if (checkedInById != null) {
            User checkedInBy = userRepository.findById(checkedInById)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found: " + checkedInById
                            )
                    );

            checkIn.setCheckedInBy(checkedInBy);
        }

        if (checkInMethod != null && !checkInMethod.isBlank()) {
            checkIn.setCheckInMethod(checkInMethod);
        }

        if (checkedInAt != null) {
            checkIn.setCheckedInAt(checkedInAt);
        }

        if (notes != null) {
            checkIn.setNotes(notes);
        }

        return toResponseDto(checkInRepository.save(checkIn));
    }

    public void deleteCheckIn(Long id) {
        if (!checkInRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Check-in not found: " + id
            );
        }

        checkInRepository.deleteById(id);
    }

    private CheckInResponseDto toResponseDto(CheckIn checkIn) {
        return new CheckInResponseDto(
                checkIn.getId(),
                checkIn.getReservation() != null
                        ? checkIn.getReservation().getId()
                        : null,
                checkIn.getVehicle() != null
                        ? checkIn.getVehicle().getId()
                        : null,
                checkIn.getParkingSpace() != null
                        ? checkIn.getParkingSpace().getId()
                        : null,
                checkIn.getCheckedInBy() != null
                        ? checkIn.getCheckedInBy().getId()
                        : null,
                checkIn.getCheckInMethod(),
                checkIn.getCheckedInAt(),
                checkIn.getNotes()
        );
    }
}
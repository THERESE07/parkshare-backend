package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ParkingLotStaffResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingLotStaff;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingLotStaffRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ParkingLotStaffService {

    private final ParkingLotStaffRepository parkingLotStaffRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;

    public ParkingLotStaffService(
            ParkingLotStaffRepository parkingLotStaffRepository,
            ParkingLotRepository parkingLotRepository,
            UserRepository userRepository
    ) {
        this.parkingLotStaffRepository = parkingLotStaffRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
    }

    /**
     * Assign a user as staff to a parking lot.
     */
    public ParkingLotStaffResponseDto assignStaff(
            Long parkingLotId,
            Long userId
    ) {
        if (parkingLotId == null) {
            throw new IllegalArgumentException(
                    "Parking lot ID is required."
            );
        }

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        if (parkingLotStaffRepository
                .existsByParkingLot_IdAndUser_IdAndActiveTrue(
                        parkingLotId,
                        userId
                )) {

            throw new IllegalArgumentException(
                    "This user is already active staff for the parking lot."
            );
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: "
                                        + parkingLotId
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + userId
                        )
                );

        ParkingLotStaff parkingLotStaff =
                parkingLotStaffRepository
                        .findByParkingLot_IdAndUser_Id(
                                parkingLotId,
                                userId
                        )
                        .orElseGet(ParkingLotStaff::new);

        parkingLotStaff.setParkingLot(parkingLot);
        parkingLotStaff.setUser(user);
        parkingLotStaff.setAssignedAt(OffsetDateTime.now());
        parkingLotStaff.setActive(true);

        ParkingLotStaff savedStaff =
                parkingLotStaffRepository.save(parkingLotStaff);

        return toResponseDto(savedStaff);
    }

    /**
     * Get staff assignment by ID.
     */
    @Transactional(readOnly = true)
    public ParkingLotStaffResponseDto getStaffById(Long id) {

        ParkingLotStaff staff =
                parkingLotStaffRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking lot staff assignment not found with ID: "
                                                + id
                                )
                        );

        return toResponseDto(staff);
    }

    /**
     * Get all staff assigned to a parking lot.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotStaffResponseDto> getStaffByParkingLotId(
            Long parkingLotId
    ) {

        return parkingLotStaffRepository
                .findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get all parking lots where a user is assigned as staff.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotStaffResponseDto> getStaffByUserId(
            Long userId
    ) {

        return parkingLotStaffRepository
                .findByUser_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get active staff for a parking lot.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotStaffResponseDto>
    getActiveStaffByParkingLotId(
            Long parkingLotId
    ) {

        return parkingLotStaffRepository
                .findByParkingLot_IdAndActiveTrue(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get active parking-lot staff assignments for a user.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotStaffResponseDto>
    getActiveStaffByUserId(
            Long userId
    ) {

        return parkingLotStaffRepository
                .findByUser_IdAndActiveTrue(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get a specific staff assignment.
     */
    @Transactional(readOnly = true)
    public ParkingLotStaffResponseDto getStaffAssignment(
            Long parkingLotId,
            Long userId
    ) {

        ParkingLotStaff staff =
                parkingLotStaffRepository
                        .findByParkingLot_IdAndUser_Id(
                                parkingLotId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Staff assignment not found."
                                )
                        );

        return toResponseDto(staff);
    }

    /**
     * Check whether a user is active staff for a parking lot.
     */
    @Transactional(readOnly = true)
    public boolean isActiveStaff(
            Long parkingLotId,
            Long userId
    ) {

        return parkingLotStaffRepository
                .existsByParkingLot_IdAndUser_IdAndActiveTrue(
                        parkingLotId,
                        userId
                );
    }

    /**
     * Deactivate a staff assignment.
     */
    public ParkingLotStaffResponseDto deactivateStaff(Long id) {

        ParkingLotStaff staff = getStaffEntityById(id);

        staff.setActive(false);

        ParkingLotStaff savedStaff =
                parkingLotStaffRepository.save(staff);

        return toResponseDto(savedStaff);
    }

    /**
     * Reactivate a staff assignment.
     */
    public ParkingLotStaffResponseDto reactivateStaff(Long id) {

        ParkingLotStaff staff = getStaffEntityById(id);

        staff.setActive(true);

        ParkingLotStaff savedStaff =
                parkingLotStaffRepository.save(staff);

        return toResponseDto(savedStaff);
    }

    /**
     * Remove a staff assignment permanently.
     */
    public void deleteStaff(Long id) {

        ParkingLotStaff staff = getStaffEntityById(id);

        parkingLotStaffRepository.delete(staff);
    }

    /**
     * Get the staff entity internally.
     */
    private ParkingLotStaff getStaffEntityById(Long id) {

        return parkingLotStaffRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot staff assignment not found with ID: "
                                        + id
                        )
                );
    }

    /**
     * Convert ParkingLotStaff entity to response DTO.
     */
    private ParkingLotStaffResponseDto toResponseDto(
            ParkingLotStaff staff
    ) {

        Long parkingLotId = null;
        Long userId = null;

        if (staff.getParkingLot() != null) {
            parkingLotId = staff.getParkingLot().getId();
        }

        if (staff.getUser() != null) {
            userId = staff.getUser().getId();
        }

        return new ParkingLotStaffResponseDto(
                staff.getId(),
                parkingLotId,
                userId,
                staff.getAssignedAt(),
                staff.isActive()
        );
    }
}
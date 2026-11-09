package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ParkingLotRequestDto;
import rw.parkshare.backend.common.dto.ParkingLotResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;

    public ParkingLotService(
            ParkingLotRepository parkingLotRepository,
            UserRepository userRepository
    ) {
        this.parkingLotRepository = parkingLotRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new parking lot.
     */
    public ParkingLotResponseDto createParkingLot(
            Long operatorId,
            ParkingLotRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Parking lot request is required."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Parking lot name is required."
            );
        }

        if (request.getAddress() == null
                || request.getAddress().isBlank()) {
            throw new IllegalArgumentException(
                    "Parking lot address is required."
            );
        }

        User operator = userRepository.findById(operatorId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Operator not found with ID: " + operatorId
                        )
                );

        ParkingLot parkingLot = new ParkingLot();

        parkingLot.setOperator(operator);
        parkingLot.setName(request.getName());
        parkingLot.setDescription(request.getDescription());
        parkingLot.setAddress(request.getAddress());
        parkingLot.setDistrict(request.getDistrict());
        parkingLot.setLatitude(request.getLatitude());
        parkingLot.setLongitude(request.getLongitude());
        parkingLot.setContactPhone(request.getContactPhone());

        if (request.getCity() == null
                || request.getCity().isBlank()) {
            parkingLot.setCity("Kigali");
        } else {
            parkingLot.setCity(request.getCity());
        }

        if (request.getTotalCapacity() == null) {
            parkingLot.setTotalCapacity(0);
        } else {
            parkingLot.setTotalCapacity(
                    request.getTotalCapacity()
            );
        }

        // New lots always start PENDING; only an admin can activate them
        // (PATCH /api/parking-lots/{id}/status) after verification.
        parkingLot.setStatus("PENDING");

        OffsetDateTime now = OffsetDateTime.now();
        parkingLot.setCreatedAt(now);
        parkingLot.setUpdatedAt(now);

        ParkingLot savedParkingLot =
                parkingLotRepository.save(parkingLot);

        return toResponseDto(savedParkingLot);
    }

    /**
     * Get a parking lot by ID.
     */
    @Transactional(readOnly = true)
    public ParkingLotResponseDto getParkingLotById(Long id) {

        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: " + id
                        )
                );

        return toResponseDto(parkingLot);
    }

    /**
     * Get all parking lots.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotResponseDto> getAllParkingLots() {

        return parkingLotRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get parking lots belonging to an operator.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotResponseDto> getParkingLotsByOperatorId(
            Long operatorId
    ) {

        return parkingLotRepository
                .findByOperator_Id(operatorId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get parking lots by status.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotResponseDto> getParkingLotsByStatus(
            String status
    ) {

        return parkingLotRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get parking lots by city.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotResponseDto> getParkingLotsByCity(
            String city
    ) {

        return parkingLotRepository
                .findByCityIgnoreCase(city)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get an operator's parking lots by status.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotResponseDto>
    getParkingLotsByOperatorAndStatus(
            Long operatorId,
            String status
    ) {

        return parkingLotRepository
                .findByOperator_IdAndStatus(
                        operatorId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Search parking lots by name.
     */
    @Transactional(readOnly = true)
    public List<ParkingLotResponseDto> searchParkingLotsByName(
            String name
    ) {

        return parkingLotRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Update a parking lot.
     */
    public ParkingLotResponseDto updateParkingLot(
            Long id,
            ParkingLotRequestDto request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Parking lot request is required."
            );
        }

        ParkingLot existingParkingLot =
                parkingLotRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking lot not found with ID: " + id
                                )
                        );

        if (request.getName() != null
                && !request.getName().isBlank()) {
            existingParkingLot.setName(
                    request.getName()
            );
        }

        if (request.getDescription() != null) {
            existingParkingLot.setDescription(
                    request.getDescription()
            );
        }

        if (request.getAddress() != null
                && !request.getAddress().isBlank()) {
            existingParkingLot.setAddress(
                    request.getAddress()
            );
        }

        if (request.getCity() != null
                && !request.getCity().isBlank()) {
            existingParkingLot.setCity(
                    request.getCity()
            );
        }

        if (request.getLatitude() != null) {
            existingParkingLot.setLatitude(
                    request.getLatitude()
            );
        }

        if (request.getLongitude() != null) {
            existingParkingLot.setLongitude(
                    request.getLongitude()
            );
        }

        if (request.getContactPhone() != null) {
            existingParkingLot.setContactPhone(
                    request.getContactPhone()
            );
        }

        if (request.getTotalCapacity() != null) {
            existingParkingLot.setTotalCapacity(
                    request.getTotalCapacity()
            );
        }

        existingParkingLot.setUpdatedAt(
                OffsetDateTime.now()
        );

        ParkingLot savedParkingLot =
                parkingLotRepository.save(existingParkingLot);

        return toResponseDto(savedParkingLot);
    }

    /**
     * Update the parking lot status.
     */
    public ParkingLotResponseDto updateParkingLotStatus(
            Long id,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Status is required."
            );
        }

        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: " + id
                        )
                );

        parkingLot.setStatus(status);
        parkingLot.setUpdatedAt(OffsetDateTime.now());

        ParkingLot savedParkingLot =
                parkingLotRepository.save(parkingLot);

        return toResponseDto(savedParkingLot);
    }

    /**
     * Delete a parking lot.
     */
    public void deleteParkingLot(Long id) {

        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: " + id
                        )
                );

        parkingLotRepository.delete(parkingLot);
    }

    /**
     * Check whether a parking lot exists.
     */
    @Transactional(readOnly = true)
    public boolean parkingLotExists(Long id) {
        return parkingLotRepository.existsById(id);
    }

    /**
     * Convert ParkingLot entity to response DTO.
     */
    private ParkingLotResponseDto toResponseDto(
            ParkingLot parkingLot
    ) {

        Long operatorId = null;

        if (parkingLot.getOperator() != null) {
            operatorId = parkingLot.getOperator().getId();
        }

        return new ParkingLotResponseDto(
                parkingLot.getId(),
                operatorId,
                parkingLot.getName(),
                parkingLot.getDescription(),
                parkingLot.getAddress(),
                parkingLot.getCity(),
                parkingLot.getLatitude(),
                parkingLot.getLongitude(),
                parkingLot.getContactPhone(),
                parkingLot.getTotalCapacity(),
                parkingLot.getStatus(),
                parkingLot.getCreatedAt(),
                parkingLot.getUpdatedAt()
        );
    }
}
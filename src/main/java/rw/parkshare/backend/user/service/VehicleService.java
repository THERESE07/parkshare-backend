package rw.parkshare.backend.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.VehicleRequestDto;
import rw.parkshare.backend.common.dto.VehicleResponseDto;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.Vehicle;
import rw.parkshare.backend.user.repository.UserRepository;
import rw.parkshare.backend.user.repository.VehicleRepository;

import java.util.List;

@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(
            VehicleRepository vehicleRepository,
            UserRepository userRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Register a vehicle for a user.
     */
    public VehicleResponseDto createVehicle(
            Long userId,
            VehicleRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Vehicle request is required."
            );
        }

        if (request.getPlateNumber() == null
                || request.getPlateNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Plate number is required."
            );
        }

        if (vehicleRepository.existsByPlateNumber(
                request.getPlateNumber()
        )) {
            throw new IllegalArgumentException(
                    "A vehicle with this plate number already exists."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + userId
                        )
                );

        Vehicle vehicle = new Vehicle();

        vehicle.setUser(user);
        vehicle.setPlateNumber(request.getPlateNumber());

        if (request.getVehicleType() == null
                || request.getVehicleType().isBlank()) {
            vehicle.setVehicleType("CAR");
        } else {
            vehicle.setVehicleType(request.getVehicleType());
        }

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());

        if (request.getIsDefault() == null) {
            vehicle.setIsDefault(false);
        } else {
            vehicle.setIsDefault(request.getIsDefault());
        }

        if (request.getIsActive() == null) {
            vehicle.setIsActive(true);
        } else {
            vehicle.setIsActive(request.getIsActive());
        }

        Vehicle savedVehicle =
                vehicleRepository.save(vehicle);

        return toResponseDto(savedVehicle);
    }

    /**
     * Get a vehicle by ID.
     */
    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleById(Long id) {

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not found with ID: " + id
                        )
                );

        return toResponseDto(vehicle);
    }

    /**
     * Get a vehicle by plate number.
     */
    @Transactional(readOnly = true)
    public VehicleResponseDto getVehicleByPlateNumber(
            String plateNumber
    ) {

        Vehicle vehicle =
                vehicleRepository.findByPlateNumber(plateNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Vehicle not found with plate number: "
                                                + plateNumber
                                )
                        );

        return toResponseDto(vehicle);
    }

    /**
     * Get all vehicles belonging to a user.
     */
    @Transactional(readOnly = true)
    public List<VehicleResponseDto> getVehiclesByUserId(
            Long userId
    ) {

        return vehicleRepository.findByUserId(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Update an existing vehicle.
     */
    public VehicleResponseDto updateVehicle(
            Long id,
            VehicleRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Vehicle request is required."
            );
        }

        if (request.getPlateNumber() == null
                || request.getPlateNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Plate number is required."
            );
        }

        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not found with ID: " + id
                        )
                );

        if (!existingVehicle.getPlateNumber()
                .equals(request.getPlateNumber())
                && vehicleRepository.existsByPlateNumber(
                request.getPlateNumber()
        )) {

            throw new IllegalArgumentException(
                    "A vehicle with this plate number already exists."
            );
        }

        existingVehicle.setPlateNumber(
                request.getPlateNumber()
        );

        if (request.getVehicleType() != null
                && !request.getVehicleType().isBlank()) {

            existingVehicle.setVehicleType(
                    request.getVehicleType()
            );
        }

        if (request.getMake() != null) {
            existingVehicle.setMake(
                    request.getMake()
            );
        }

        if (request.getModel() != null) {
            existingVehicle.setModel(
                    request.getModel()
            );
        }

        if (request.getColor() != null) {
            existingVehicle.setColor(
                    request.getColor()
            );
        }

        if (request.getIsDefault() != null) {
            existingVehicle.setIsDefault(
                    request.getIsDefault()
            );
        }

        if (request.getIsActive() != null) {
            existingVehicle.setIsActive(
                    request.getIsActive()
            );
        }

        Vehicle savedVehicle =
                vehicleRepository.save(existingVehicle);

        return toResponseDto(savedVehicle);
    }

    /**
     * Set a vehicle as the user's default vehicle.
     */
    public VehicleResponseDto setDefaultVehicle(
            Long vehicleId
    ) {

        Vehicle vehicle = getVehicleEntityById(vehicleId);

        Long userId = vehicle.getUser().getId();

        List<Vehicle> userVehicles =
                vehicleRepository.findByUserId(userId);

        for (Vehicle userVehicle : userVehicles) {
            userVehicle.setIsDefault(false);
        }

        vehicle.setIsDefault(true);

        vehicleRepository.saveAll(userVehicles);

        return toResponseDto(vehicle);
    }

    /**
     * Activate a vehicle.
     */
    public VehicleResponseDto activateVehicle(Long id) {

        Vehicle vehicle = getVehicleEntityById(id);

        vehicle.setIsActive(true);

        Vehicle savedVehicle =
                vehicleRepository.save(vehicle);

        return toResponseDto(savedVehicle);
    }

    /**
     * Deactivate a vehicle.
     */
    public VehicleResponseDto deactivateVehicle(Long id) {

        Vehicle vehicle = getVehicleEntityById(id);

        vehicle.setIsActive(false);
        vehicle.setIsDefault(false);

        Vehicle savedVehicle =
                vehicleRepository.save(vehicle);

        return toResponseDto(savedVehicle);
    }

    /**
     * Delete a vehicle.
     */
    public void deleteVehicle(Long id) {

        Vehicle vehicle = getVehicleEntityById(id);

        vehicleRepository.delete(vehicle);
    }

    /**
     * Check whether a vehicle exists.
     */
    @Transactional(readOnly = true)
    public boolean vehicleExists(Long id) {
        return vehicleRepository.existsById(id);
    }

    /**
     * Check whether a plate number is already registered.
     */
    @Transactional(readOnly = true)
    public boolean plateNumberExists(String plateNumber) {
        return vehicleRepository.existsByPlateNumber(plateNumber);
    }

    /**
     * Get the vehicle entity internally.
     */
    private Vehicle getVehicleEntityById(Long id) {

        return vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vehicle not found with ID: " + id
                        )
                );
    }

    /**
     * Convert Vehicle entity to response DTO.
     */
    private VehicleResponseDto toResponseDto(
            Vehicle vehicle
    ) {

        Long userId = null;

        if (vehicle.getUser() != null) {
            userId = vehicle.getUser().getId();
        }

        return new VehicleResponseDto(
                vehicle.getId(),
                userId,
                vehicle.getPlateNumber(),
                vehicle.getVehicleType(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getIsDefault(),
                vehicle.getIsActive()
        );
    }
}
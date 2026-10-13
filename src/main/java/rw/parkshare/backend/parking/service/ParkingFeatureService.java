package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ParkingFeatureRequestDto;
import rw.parkshare.backend.common.dto.ParkingFeatureResponseDto;
import rw.parkshare.backend.parking.entity.ParkingFeature;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.repository.ParkingFeatureRepository;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class ParkingFeatureService {

    private final ParkingFeatureRepository parkingFeatureRepository;
    private final ParkingLotRepository parkingLotRepository;

    public ParkingFeatureService(
            ParkingFeatureRepository parkingFeatureRepository,
            ParkingLotRepository parkingLotRepository
    ) {
        this.parkingFeatureRepository = parkingFeatureRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    /**
     * Add a feature to a parking lot.
     */
    public ParkingFeatureResponseDto createFeature(
            Long parkingLotId,
            ParkingFeatureRequestDto request
    ) {
        validateFeature(request);

        if (parkingFeatureRepository
                .existsByParkingLot_IdAndFeatureCode(
                        parkingLotId,
                        request.getFeatureCode()
                )) {

            throw new IllegalArgumentException(
                    "This feature already exists for the parking lot."
            );
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: "
                                        + parkingLotId
                        )
                );

        ParkingFeature parkingFeature = new ParkingFeature();

        parkingFeature.setParkingLot(parkingLot);
        parkingFeature.setFeatureCode(request.getFeatureCode());
        parkingFeature.setCreatedAt(OffsetDateTime.now());

        ParkingFeature savedFeature =
                parkingFeatureRepository.save(parkingFeature);

        return toResponseDto(savedFeature);
    }

    /**
     * Get a feature by ID.
     */
    @Transactional(readOnly = true)
    public ParkingFeatureResponseDto getFeatureById(Long id) {

        ParkingFeature parkingFeature = getFeatureEntityById(id);

        return toResponseDto(parkingFeature);
    }

    /**
     * Get all features belonging to a parking lot.
     */
    @Transactional(readOnly = true)
    public List<ParkingFeatureResponseDto> getFeaturesByParkingLotId(
            Long parkingLotId
    ) {

        return parkingFeatureRepository
                .findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Get a specific feature from a parking lot.
     */
    @Transactional(readOnly = true)
    public ParkingFeatureResponseDto getFeatureByParkingLotAndCode(
            Long parkingLotId,
            String featureCode
    ) {

        return parkingFeatureRepository
                .findByParkingLot_IdAndFeatureCode(
                        parkingLotId,
                        featureCode
                )
                .map(this::toResponseDto)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Feature not found for this parking lot."
                        )
                );
    }

    /**
     * Get all parking features with a specific feature code.
     */
    @Transactional(readOnly = true)
    public List<ParkingFeatureResponseDto> getFeaturesByCode(
            String featureCode
    ) {

        return parkingFeatureRepository
                .findByFeatureCode(featureCode)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Check whether a parking lot already has a feature.
     */
    @Transactional(readOnly = true)
    public boolean featureExists(
            Long parkingLotId,
            String featureCode
    ) {

        return parkingFeatureRepository
                .existsByParkingLot_IdAndFeatureCode(
                        parkingLotId,
                        featureCode
                );
    }

    /**
     * Update a parking feature.
     */
    public ParkingFeatureResponseDto updateFeature(
            Long id,
            ParkingFeatureRequestDto request
    ) {
        validateFeature(request);

        ParkingFeature existingFeature =
                getFeatureEntityById(id);

        Long parkingLotId =
                existingFeature.getParkingLot().getId();

        if (!existingFeature.getFeatureCode()
                .equals(request.getFeatureCode())
                && parkingFeatureRepository
                .existsByParkingLot_IdAndFeatureCode(
                        parkingLotId,
                        request.getFeatureCode()
                )) {

            throw new IllegalArgumentException(
                    "This feature already exists for the parking lot."
            );
        }

        existingFeature.setFeatureCode(
                request.getFeatureCode()
        );

        ParkingFeature savedFeature =
                parkingFeatureRepository.save(existingFeature);

        return toResponseDto(savedFeature);
    }

    /**
     * Delete a parking feature.
     */
    public void deleteFeature(Long id) {

        ParkingFeature parkingFeature =
                getFeatureEntityById(id);

        parkingFeatureRepository.delete(parkingFeature);
    }

    /**
     * Validate feature request.
     */
    private void validateFeature(
            ParkingFeatureRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Parking feature request is required."
            );
        }

        if (request.getFeatureCode() == null
                || request.getFeatureCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Feature code is required."
            );
        }
    }

    /**
     * Get the entity internally.
     */
    private ParkingFeature getFeatureEntityById(Long id) {

        return parkingFeatureRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking feature not found with ID: " + id
                        )
                );
    }

    /**
     * Convert entity to response DTO.
     */
    private ParkingFeatureResponseDto toResponseDto(
            ParkingFeature parkingFeature
    ) {

        Long parkingLotId = null;

        if (parkingFeature.getParkingLot() != null) {
            parkingLotId =
                    parkingFeature.getParkingLot().getId();
        }

        return new ParkingFeatureResponseDto(
                parkingFeature.getId(),
                parkingLotId,
                parkingFeature.getFeatureCode(),
                parkingFeature.getCreatedAt()
        );
    }
}
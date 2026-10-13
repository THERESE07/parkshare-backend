package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ParkingSpaceRequestDto;
import rw.parkshare.backend.common.dto.ParkingSpaceResponseDto;
import rw.parkshare.backend.parking.entity.ParkingSpace;
import rw.parkshare.backend.parking.entity.ParkingZone;
import rw.parkshare.backend.parking.repository.ParkingSpaceRepository;
import rw.parkshare.backend.parking.repository.ParkingZoneRepository;

import java.util.List;

@Service
@Transactional
public class ParkingSpaceService {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ParkingZoneRepository parkingZoneRepository;

    public ParkingSpaceService(
            ParkingSpaceRepository parkingSpaceRepository,
            ParkingZoneRepository parkingZoneRepository
    ) {
        this.parkingSpaceRepository = parkingSpaceRepository;
        this.parkingZoneRepository = parkingZoneRepository;
    }

    public ParkingSpaceResponseDto createSpace(
            Long parkingZoneId,
            ParkingSpaceRequestDto request
    ) {
        validateRequest(request);

        if (parkingSpaceRepository.existsByParkingZone_IdAndSpaceCode(
                parkingZoneId,
                request.getSpaceCode()
        )) {
            throw new IllegalArgumentException(
                    "A parking space with this code already exists in the zone."
            );
        }

        ParkingZone parkingZone = parkingZoneRepository.findById(parkingZoneId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking zone not found with ID: " + parkingZoneId
                        )
                );

        ParkingSpace parkingSpace = new ParkingSpace();
        parkingSpace.setParkingZone(parkingZone);
        parkingSpace.setSpaceCode(request.getSpaceCode());

        if (request.getSpaceType() == null
                || request.getSpaceType().isBlank()) {
            parkingSpace.setSpaceType("STANDARD");
        } else {
            parkingSpace.setSpaceType(request.getSpaceType());
        }

        if (request.getStatus() == null
                || request.getStatus().isBlank()) {
            parkingSpace.setStatus("AVAILABLE");
        } else {
            parkingSpace.setStatus(request.getStatus());
        }

        parkingSpace.setFloor(request.getFloor());

        ParkingSpace savedSpace = parkingSpaceRepository.save(parkingSpace);

        return toResponseDto(savedSpace);
    }

    @Transactional(readOnly = true)
    public ParkingSpaceResponseDto getSpaceById(Long id) {
        ParkingSpace parkingSpace = parkingSpaceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking space not found with ID: " + id
                        )
                );

        return toResponseDto(parkingSpace);
    }

    @Transactional(readOnly = true)
    public List<ParkingSpaceResponseDto> getSpacesByParkingZoneId(
            Long parkingZoneId
    ) {
        return parkingSpaceRepository
                .findByParkingZone_Id(parkingZoneId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingSpaceResponseDto> getSpacesByStatus(
            String status
    ) {
        return parkingSpaceRepository
                .findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingSpaceResponseDto> getSpacesByZoneAndStatus(
            Long parkingZoneId,
            String status
    ) {
        return parkingSpaceRepository
                .findByParkingZone_IdAndStatus(
                        parkingZoneId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkingSpaceResponseDto getSpaceByCode(
            String spaceCode
    ) {
        ParkingSpace parkingSpace =
                parkingSpaceRepository.findBySpaceCode(spaceCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking space not found with code: "
                                                + spaceCode
                                )
                        );

        return toResponseDto(parkingSpace);
    }

    @Transactional(readOnly = true)
    public List<ParkingSpaceResponseDto> getSpacesByType(
            String spaceType
    ) {
        return parkingSpaceRepository
                .findBySpaceType(spaceType)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingSpaceResponseDto> getSpacesByZoneAndType(
            Long parkingZoneId,
            String spaceType
    ) {
        return parkingSpaceRepository
                .findByParkingZone_IdAndSpaceType(
                        parkingZoneId,
                        spaceType
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean spaceExists(
            Long parkingZoneId,
            String spaceCode
    ) {
        return parkingSpaceRepository
                .existsByParkingZone_IdAndSpaceCode(
                        parkingZoneId,
                        spaceCode
                );
    }

    public ParkingSpaceResponseDto updateSpace(
            Long id,
            ParkingSpaceRequestDto request
    ) {
        validateRequest(request);

        ParkingSpace existingSpace = getSpaceEntityById(id);

        Long parkingZoneId =
                existingSpace.getParkingZone().getId();

        if (!existingSpace.getSpaceCode().equals(request.getSpaceCode())
                && parkingSpaceRepository
                .existsByParkingZone_IdAndSpaceCode(
                        parkingZoneId,
                        request.getSpaceCode()
                )) {

            throw new IllegalArgumentException(
                    "A parking space with this code already exists in the zone."
            );
        }

        existingSpace.setSpaceCode(request.getSpaceCode());

        if (request.getSpaceType() != null
                && !request.getSpaceType().isBlank()) {
            existingSpace.setSpaceType(request.getSpaceType());
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {
            existingSpace.setStatus(request.getStatus());
        }

        existingSpace.setFloor(request.getFloor());

        ParkingSpace savedSpace =
                parkingSpaceRepository.save(existingSpace);

        return toResponseDto(savedSpace);
    }

    public ParkingSpaceResponseDto updateSpaceStatus(
            Long id,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Space status is required."
            );
        }

        ParkingSpace parkingSpace = getSpaceEntityById(id);
        parkingSpace.setStatus(status);

        ParkingSpace savedSpace =
                parkingSpaceRepository.save(parkingSpace);

        return toResponseDto(savedSpace);
    }

    public ParkingSpaceResponseDto markSpaceAvailable(Long id) {
        return updateSpaceStatus(id, "AVAILABLE");
    }

    public ParkingSpaceResponseDto markSpaceOccupied(Long id) {
        return updateSpaceStatus(id, "OCCUPIED");
    }

    public ParkingSpaceResponseDto markSpaceReserved(Long id) {
        return updateSpaceStatus(id, "RESERVED");
    }

    public ParkingSpaceResponseDto markSpaceMaintenance(Long id) {
        return updateSpaceStatus(id, "MAINTENANCE");
    }

    public void deleteSpace(Long id) {
        ParkingSpace parkingSpace = getSpaceEntityById(id);
        parkingSpaceRepository.delete(parkingSpace);
    }

    private void validateRequest(ParkingSpaceRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Parking space request is required."
            );
        }

        if (request.getSpaceCode() == null
                || request.getSpaceCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Space code is required."
            );
        }
    }

    private ParkingSpace getSpaceEntityById(Long id) {
        return parkingSpaceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking space not found with ID: " + id
                        )
                );
    }

    private ParkingSpaceResponseDto toResponseDto(
            ParkingSpace parkingSpace
    ) {
        Long parkingZoneId = null;

        if (parkingSpace.getParkingZone() != null) {
            parkingZoneId =
                    parkingSpace.getParkingZone().getId();
        }

        return new ParkingSpaceResponseDto(
                parkingSpace.getId(),
                parkingZoneId,
                parkingSpace.getSpaceCode(),
                parkingSpace.getSpaceType(),
                parkingSpace.getStatus(),
                parkingSpace.getFloor()
        );
    }
}
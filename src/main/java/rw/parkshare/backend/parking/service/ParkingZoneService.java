package rw.parkshare.backend.parking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ParkingZoneRequestDto;
import rw.parkshare.backend.common.dto.ParkingZoneResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingZone;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingZoneRepository;

import java.util.List;

@Service
@Transactional
public class ParkingZoneService {

    private final ParkingZoneRepository parkingZoneRepository;
    private final ParkingLotRepository parkingLotRepository;

    public ParkingZoneService(
            ParkingZoneRepository parkingZoneRepository,
            ParkingLotRepository parkingLotRepository
    ) {
        this.parkingZoneRepository = parkingZoneRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    public ParkingZoneResponseDto createZone(
            Long parkingLotId,
            ParkingZoneRequestDto request
    ) {
        validateRequest(request);

        if (parkingZoneRepository
                .existsByParkingLot_IdAndName(
                        parkingLotId,
                        request.getName()
                )) {

            throw new IllegalArgumentException(
                    "A parking zone with this name already exists in the parking lot."
            );
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking lot not found with ID: "
                                        + parkingLotId
                        )
                );

        ParkingZone parkingZone = new ParkingZone();

        parkingZone.setParkingLot(parkingLot);
        parkingZone.setName(request.getName());
        parkingZone.setDescription(request.getDescription());

        if (request.getCapacity() == null) {
            parkingZone.setCapacity(0);
        } else {
            parkingZone.setCapacity(request.getCapacity());
        }

        if (request.getStatus() == null
                || request.getStatus().isBlank()) {

            parkingZone.setStatus("ACTIVE");

        } else {
            parkingZone.setStatus(request.getStatus());
        }

        ParkingZone savedZone =
                parkingZoneRepository.save(parkingZone);

        return toResponseDto(savedZone);
    }

    @Transactional(readOnly = true)
    public ParkingZoneResponseDto getZoneById(Long id) {

        ParkingZone parkingZone =
                parkingZoneRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking zone not found with ID: "
                                                + id
                                )
                        );

        return toResponseDto(parkingZone);
    }

    @Transactional(readOnly = true)
    public List<ParkingZoneResponseDto>
    getZonesByParkingLotId(Long parkingLotId) {

        return parkingZoneRepository
                .findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingZoneResponseDto>
    getZonesByParkingLotAndStatus(
            Long parkingLotId,
            String status
    ) {

        return parkingZoneRepository
                .findByParkingLot_IdAndStatus(
                        parkingLotId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ParkingZoneResponseDto>
    getZonesByStatus(String status) {

        return parkingZoneRepository
                .findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkingZoneResponseDto getZoneByName(
            Long parkingLotId,
            String name
    ) {

        ParkingZone parkingZone =
                parkingZoneRepository
                        .findByParkingLot_IdAndName(
                                parkingLotId,
                                name
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parking zone not found with name: "
                                                + name
                                )
                        );

        return toResponseDto(parkingZone);
    }

    @Transactional(readOnly = true)
    public List<ParkingZoneResponseDto>
    searchZonesByName(String name) {

        return parkingZoneRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean zoneExists(
            Long parkingLotId,
            String name
    ) {

        return parkingZoneRepository
                .existsByParkingLot_IdAndName(
                        parkingLotId,
                        name
                );
    }

    public ParkingZoneResponseDto updateZone(
            Long id,
            ParkingZoneRequestDto request
    ) {
        validateRequest(request);

        ParkingZone existingZone = getZoneEntityById(id);

        Long parkingLotId =
                existingZone.getParkingLot().getId();

        if (!existingZone.getName().equals(request.getName())
                && parkingZoneRepository
                .existsByParkingLot_IdAndName(
                        parkingLotId,
                        request.getName()
                )) {

            throw new IllegalArgumentException(
                    "A parking zone with this name already exists in the parking lot."
            );
        }

        existingZone.setName(request.getName());
        existingZone.setDescription(request.getDescription());

        if (request.getCapacity() != null) {
            existingZone.setCapacity(
                    request.getCapacity()
            );
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            existingZone.setStatus(
                    request.getStatus()
            );
        }

        ParkingZone savedZone =
                parkingZoneRepository.save(existingZone);

        return toResponseDto(savedZone);
    }

    public ParkingZoneResponseDto updateZoneStatus(
            Long id,
            String status
    ) {

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Zone status is required."
            );
        }

        ParkingZone parkingZone =
                getZoneEntityById(id);

        parkingZone.setStatus(status);

        ParkingZone savedZone =
                parkingZoneRepository.save(parkingZone);

        return toResponseDto(savedZone);
    }

    public ParkingZoneResponseDto activateZone(Long id) {
        return updateZoneStatus(id, "ACTIVE");
    }

    public ParkingZoneResponseDto deactivateZone(Long id) {
        return updateZoneStatus(id, "INACTIVE");
    }

    public void deleteZone(Long id) {

        ParkingZone parkingZone =
                getZoneEntityById(id);

        parkingZoneRepository.delete(parkingZone);
    }

    private void validateRequest(
            ParkingZoneRequestDto request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Parking zone request is required."
            );
        }

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Parking zone name is required."
            );
        }
    }

    private ParkingZone getZoneEntityById(Long id) {

        return parkingZoneRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Parking zone not found with ID: "
                                        + id
                        )
                );
    }

    private ParkingZoneResponseDto toResponseDto(
            ParkingZone parkingZone
    ) {

        Long parkingLotId = null;

        if (parkingZone.getParkingLot() != null) {
            parkingLotId =
                    parkingZone.getParkingLot().getId();
        }

        return new ParkingZoneResponseDto(
                parkingZone.getId(),
                parkingLotId,
                parkingZone.getName(),
                parkingZone.getDescription(),
                parkingZone.getCapacity(),
                parkingZone.getStatus()
        );
    }
}
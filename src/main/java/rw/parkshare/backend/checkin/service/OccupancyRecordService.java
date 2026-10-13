package rw.parkshare.backend.checkin.service;

import rw.parkshare.backend.checkin.entity.OccupancyRecord;
import rw.parkshare.backend.checkin.repository.OccupancyRecordRepository;
import rw.parkshare.backend.common.dto.OccupancyRecordRequestDto;
import rw.parkshare.backend.common.dto.OccupancyRecordResponseDto;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingZone;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingZoneRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OccupancyRecordService {

    private final OccupancyRecordRepository occupancyRecordRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingZoneRepository parkingZoneRepository;

    public OccupancyRecordService(
            OccupancyRecordRepository occupancyRecordRepository,
            ParkingLotRepository parkingLotRepository,
            ParkingZoneRepository parkingZoneRepository
    ) {
        this.occupancyRecordRepository = occupancyRecordRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.parkingZoneRepository = parkingZoneRepository;
    }

    public OccupancyRecordResponseDto createOccupancyRecord(
            Long parkingLotId,
            Long parkingZoneId,
            OccupancyRecordRequestDto request
    ) {
        validateCounts(
                request.getTotalCapacity(),
                request.getOccupiedSpaces(),
                request.getReservedSpaces(),
                request.getAvailableSpaces()
        );

        if (request.getSource() == null || request.getSource().isBlank()) {
            throw new IllegalArgumentException("Source is required");
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new IllegalArgumentException("Parking lot not found"));

        ParkingZone parkingZone = null;

        if (parkingZoneId != null) {
            parkingZone = parkingZoneRepository.findById(parkingZoneId)
                    .orElseThrow(() -> new IllegalArgumentException("Parking zone not found"));

            if (!parkingZone.getParkingLot().getId().equals(parkingLotId)) {
                throw new IllegalArgumentException(
                        "Parking zone does not belong to the specified parking lot"
                );
            }
        }

        OccupancyRecord record = new OccupancyRecord();
        record.setParkingLot(parkingLot);
        record.setParkingZone(parkingZone);
        record.setTotalCapacity(request.getTotalCapacity());
        record.setOccupiedSpaces(request.getOccupiedSpaces());
        record.setReservedSpaces(request.getReservedSpaces());
        record.setAvailableSpaces(request.getAvailableSpaces());
        record.setSource(request.getSource());
        record.setRecordedAt(
                request.getRecordedAt() != null
                        ? request.getRecordedAt()
                        : OffsetDateTime.now()
        );

        return toResponseDto(occupancyRecordRepository.save(record));
    }

    public OccupancyRecordResponseDto getOccupancyRecordById(Long id) {
        return occupancyRecordRepository.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new IllegalArgumentException("Occupancy record not found"));
    }

    public List<OccupancyRecordResponseDto> getAllOccupancyRecords() {
        return occupancyRecordRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsByParkingLotId(Long parkingLotId) {
        return occupancyRecordRepository.findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsByParkingZoneId(Long parkingZoneId) {
        return occupancyRecordRepository.findByParkingZone_Id(parkingZoneId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsByParkingLotAndZone(
            Long parkingLotId,
            Long parkingZoneId
    ) {
        return occupancyRecordRepository
                .findByParkingLot_IdAndParkingZone_Id(parkingLotId, parkingZoneId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsBySource(String source) {
        return occupancyRecordRepository.findBySource(source)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsByParkingLotAndSource(
            Long parkingLotId,
            String source
    ) {
        return occupancyRecordRepository.findByParkingLot_IdAndSource(parkingLotId, source)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return occupancyRecordRepository.findByRecordedAtBetween(startTime, endTime)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsByParkingLotBetween(
            Long parkingLotId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return occupancyRecordRepository
                .findByParkingLot_IdAndRecordedAtBetween(parkingLotId, startTime, endTime)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<OccupancyRecordResponseDto> getOccupancyRecordsByParkingZoneBetween(
            Long parkingZoneId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return occupancyRecordRepository
                .findByParkingZone_IdAndRecordedAtBetween(parkingZoneId, startTime, endTime)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public OccupancyRecordResponseDto getLatestOccupancyByParkingLot(Long parkingLotId) {
        List<OccupancyRecord> records =
                occupancyRecordRepository.findTopByParkingLot_IdOrderByRecordedAtDesc(parkingLotId);

        if (records.isEmpty()) {
            throw new IllegalArgumentException(
                    "No occupancy record found for parking lot"
            );
        }

        return toResponseDto(records.get(0));
    }

    public OccupancyRecordResponseDto getLatestOccupancyByParkingZone(Long parkingZoneId) {
        List<OccupancyRecord> records =
                occupancyRecordRepository.findTopByParkingZone_IdOrderByRecordedAtDesc(parkingZoneId);

        if (records.isEmpty()) {
            throw new IllegalArgumentException(
                    "No occupancy record found for parking zone"
            );
        }

        return toResponseDto(records.get(0));
    }

    public OccupancyRecordResponseDto updateOccupancyRecord(
            Long id,
            OccupancyRecordRequestDto request
    ) {
        OccupancyRecord record = occupancyRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Occupancy record not found"));

        Integer totalCapacity = request.getTotalCapacity() != null
                ? request.getTotalCapacity()
                : record.getTotalCapacity();

        Integer occupiedSpaces = request.getOccupiedSpaces() != null
                ? request.getOccupiedSpaces()
                : record.getOccupiedSpaces();

        Integer reservedSpaces = request.getReservedSpaces() != null
                ? request.getReservedSpaces()
                : record.getReservedSpaces();

        Integer availableSpaces = request.getAvailableSpaces() != null
                ? request.getAvailableSpaces()
                : record.getAvailableSpaces();

        validateCounts(
                totalCapacity,
                occupiedSpaces,
                reservedSpaces,
                availableSpaces
        );

        record.setTotalCapacity(totalCapacity);
        record.setOccupiedSpaces(occupiedSpaces);
        record.setReservedSpaces(reservedSpaces);
        record.setAvailableSpaces(availableSpaces);

        if (request.getSource() != null && !request.getSource().isBlank()) {
            record.setSource(request.getSource());
        }

        if (request.getRecordedAt() != null) {
            record.setRecordedAt(request.getRecordedAt());
        }

        return toResponseDto(occupancyRecordRepository.save(record));
    }

    public void deleteOccupancyRecord(Long id) {
        if (!occupancyRecordRepository.existsById(id)) {
            throw new IllegalArgumentException("Occupancy record not found");
        }

        occupancyRecordRepository.deleteById(id);
    }

    private void validateCounts(
            Integer totalCapacity,
            Integer occupiedSpaces,
            Integer reservedSpaces,
            Integer availableSpaces
    ) {
        if (totalCapacity == null
                || occupiedSpaces == null
                || reservedSpaces == null
                || availableSpaces == null) {
            throw new IllegalArgumentException("Occupancy values are required");
        }

        if (totalCapacity < 0
                || occupiedSpaces < 0
                || reservedSpaces < 0
                || availableSpaces < 0) {
            throw new IllegalArgumentException(
                    "Occupancy values cannot be negative"
            );
        }

        if (occupiedSpaces + reservedSpaces + availableSpaces != totalCapacity) {
            throw new IllegalArgumentException(
                    "Occupied, reserved, and available spaces must equal total capacity"
            );
        }
    }

    private OccupancyRecordResponseDto toResponseDto(OccupancyRecord record) {
        return new OccupancyRecordResponseDto(
                record.getId(),
                record.getParkingLot() != null
                        ? record.getParkingLot().getId()
                        : null,
                record.getParkingZone() != null
                        ? record.getParkingZone().getId()
                        : null,
                record.getTotalCapacity(),
                record.getOccupiedSpaces(),
                record.getReservedSpaces(),
                record.getAvailableSpaces(),
                record.getSource(),
                record.getRecordedAt()
        );
    }
}
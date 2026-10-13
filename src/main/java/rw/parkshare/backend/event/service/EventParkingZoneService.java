package rw.parkshare.backend.event.service;

import rw.parkshare.backend.common.dto.EventParkingZoneRequestDto;
import rw.parkshare.backend.common.dto.EventParkingZoneResponseDto;
import rw.parkshare.backend.event.entity.EventParkingZone;
import rw.parkshare.backend.event.repository.EventParkingZoneRepository;
import rw.parkshare.backend.event.repository.EventRepository;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EventParkingZoneService {

    private final EventParkingZoneRepository eventParkingZoneRepository;
    private final EventRepository eventRepository;
    private final ParkingLotRepository parkingLotRepository;

    public EventParkingZoneService(
            EventParkingZoneRepository eventParkingZoneRepository,
            EventRepository eventRepository,
            ParkingLotRepository parkingLotRepository
    ) {
        this.eventParkingZoneRepository = eventParkingZoneRepository;
        this.eventRepository = eventRepository;
        this.parkingLotRepository = parkingLotRepository;
    }

    public EventParkingZoneResponseDto createEventParkingZone(
            Long eventId,
            Long parkingLotId,
            EventParkingZoneRequestDto request
    ) {
        if (request.getCapacity() == null || request.getCapacity() < 0) {
            throw new IllegalArgumentException("Capacity must be greater than or equal to 0");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
        }

        if (eventParkingZoneRepository.existsByEvent_IdAndParkingLot_Id(eventId, parkingLotId)) {
            throw new IllegalArgumentException(
                    "This parking lot is already assigned to this event"
            );
        }

        EventParkingZone eventParkingZone = new EventParkingZone();

        eventParkingZone.setEvent(
                eventRepository.findById(eventId)
                        .orElseThrow(() -> new IllegalArgumentException("Event not found"))
        );

        eventParkingZone.setParkingLot(
                parkingLotRepository.findById(parkingLotId)
                        .orElseThrow(() -> new IllegalArgumentException("Parking lot not found"))
        );

        eventParkingZone.setCapacity(request.getCapacity());

        eventParkingZone.setReservedCount(
                request.getReservedCount() == null ? 0 : request.getReservedCount()
        );

        eventParkingZone.setOccupiedCount(
                request.getOccupiedCount() == null ? 0 : request.getOccupiedCount()
        );

        eventParkingZone.setAvailableCount(
                request.getAvailableCount() == null
                        ? request.getCapacity()
                        : request.getAvailableCount()
        );

        eventParkingZone.setPrice(request.getPrice());

        eventParkingZone.setCurrency(
                request.getCurrency() == null || request.getCurrency().isBlank()
                        ? "RWF"
                        : request.getCurrency()
        );

        eventParkingZone.setStatus(
                request.getStatus() == null || request.getStatus().isBlank()
                        ? "ACTIVE"
                        : request.getStatus()
        );

        validateCounts(eventParkingZone);

        return toResponseDto(eventParkingZoneRepository.save(eventParkingZone));
    }

    public EventParkingZoneResponseDto getEventParkingZoneById(Long id) {
        return toResponseDto(
                eventParkingZoneRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Event parking zone not found"
                        ))
        );
    }

    public List<EventParkingZoneResponseDto> getAllEventParkingZones() {
        return eventParkingZoneRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<EventParkingZoneResponseDto> getEventParkingZonesByEventId(Long eventId) {
        return eventParkingZoneRepository.findByEvent_Id(eventId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<EventParkingZoneResponseDto> getEventParkingZonesByParkingLotId(Long parkingLotId) {
        return eventParkingZoneRepository.findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public EventParkingZoneResponseDto getByEventAndParkingLot(
            Long eventId,
            Long parkingLotId
    ) {
        return toResponseDto(
                eventParkingZoneRepository
                        .findByEvent_IdAndParkingLot_Id(eventId, parkingLotId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Event parking zone not found"
                        ))
        );
    }

    public List<EventParkingZoneResponseDto> getEventParkingZonesByEventAndStatus(
            Long eventId,
            String status
    ) {
        return eventParkingZoneRepository
                .findByEvent_IdAndStatus(eventId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<EventParkingZoneResponseDto> getEventParkingZonesByParkingLotAndStatus(
            Long parkingLotId,
            String status
    ) {
        return eventParkingZoneRepository
                .findByParkingLot_IdAndStatus(parkingLotId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<EventParkingZoneResponseDto> getEventParkingZonesByStatus(String status) {
        return eventParkingZoneRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public List<EventParkingZoneResponseDto> getEventParkingZonesByCurrency(String currency) {
        return eventParkingZoneRepository.findByCurrency(currency)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public EventParkingZoneResponseDto updateEventParkingZone(
            Long id,
            EventParkingZoneRequestDto request
    ) {
        EventParkingZone eventParkingZone = eventParkingZoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Event parking zone not found"
                ));

        if (request.getCapacity() != null && request.getCapacity() < 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than or equal to 0"
            );
        }

        if (request.getPrice() != null &&
                request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than or equal to 0"
            );
        }

        if (request.getCapacity() != null) {
            eventParkingZone.setCapacity(request.getCapacity());
        }

        if (request.getReservedCount() != null) {
            eventParkingZone.setReservedCount(request.getReservedCount());
        }

        if (request.getOccupiedCount() != null) {
            eventParkingZone.setOccupiedCount(request.getOccupiedCount());
        }

        if (request.getAvailableCount() != null) {
            eventParkingZone.setAvailableCount(request.getAvailableCount());
        }

        if (request.getPrice() != null) {
            eventParkingZone.setPrice(request.getPrice());
        }

        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            eventParkingZone.setCurrency(request.getCurrency());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            eventParkingZone.setStatus(request.getStatus());
        }

        validateCounts(eventParkingZone);

        return toResponseDto(eventParkingZoneRepository.save(eventParkingZone));
    }

    public EventParkingZoneResponseDto updateCounts(
            Long id,
            Integer reservedCount,
            Integer occupiedCount,
            Integer availableCount
    ) {
        if (reservedCount == null ||
                occupiedCount == null ||
                availableCount == null) {
            throw new IllegalArgumentException(
                    "Reserved count, occupied count and available count are required"
            );
        }

        EventParkingZone eventParkingZone = eventParkingZoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Event parking zone not found"
                ));

        eventParkingZone.setReservedCount(reservedCount);
        eventParkingZone.setOccupiedCount(occupiedCount);
        eventParkingZone.setAvailableCount(availableCount);

        validateCounts(eventParkingZone);

        return toResponseDto(eventParkingZoneRepository.save(eventParkingZone));
    }

    public EventParkingZoneResponseDto updateStatus(Long id, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        EventParkingZone eventParkingZone = eventParkingZoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Event parking zone not found"
                ));

        eventParkingZone.setStatus(status);

        return toResponseDto(eventParkingZoneRepository.save(eventParkingZone));
    }

    public void deleteEventParkingZone(Long id) {
        if (!eventParkingZoneRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Event parking zone not found"
            );
        }

        eventParkingZoneRepository.deleteById(id);
    }

    private void validateCounts(EventParkingZone eventParkingZone) {

        Integer capacity = eventParkingZone.getCapacity();
        Integer reservedCount = eventParkingZone.getReservedCount();
        Integer occupiedCount = eventParkingZone.getOccupiedCount();
        Integer availableCount = eventParkingZone.getAvailableCount();

        if (capacity == null ||
                reservedCount == null ||
                occupiedCount == null ||
                availableCount == null) {
            throw new IllegalArgumentException(
                    "Capacity and all parking counts are required"
            );
        }

        if (capacity < 0 ||
                reservedCount < 0 ||
                occupiedCount < 0 ||
                availableCount < 0) {
            throw new IllegalArgumentException(
                    "Capacity and parking counts cannot be negative"
            );
        }

        if (reservedCount + occupiedCount + availableCount != capacity) {
            throw new IllegalArgumentException(
                    "Reserved, occupied and available counts must equal capacity"
            );
        }
    }

    private EventParkingZoneResponseDto toResponseDto(
            EventParkingZone eventParkingZone
    ) {
        return new EventParkingZoneResponseDto(
                eventParkingZone.getId(),
                eventParkingZone.getEvent().getId(),
                eventParkingZone.getParkingLot().getId(),
                eventParkingZone.getCapacity(),
                eventParkingZone.getReservedCount(),
                eventParkingZone.getOccupiedCount(),
                eventParkingZone.getAvailableCount(),
                eventParkingZone.getPrice(),
                eventParkingZone.getCurrency(),
                eventParkingZone.getStatus(),
                eventParkingZone.getCreatedAt(),
                eventParkingZone.getUpdatedAt()
        );
    }
}
package rw.parkshare.backend.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.EventReservationRequestDto;
import rw.parkshare.backend.common.dto.EventReservationResponseDto;
import rw.parkshare.backend.event.entity.Event;
import rw.parkshare.backend.event.entity.EventParkingZone;
import rw.parkshare.backend.event.entity.EventReservation;
import rw.parkshare.backend.event.repository.EventParkingZoneRepository;
import rw.parkshare.backend.event.repository.EventRepository;
import rw.parkshare.backend.event.repository.EventReservationRepository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.repository.ReservationRepository;

import java.util.List;

@Service
@Transactional
public class EventReservationService {

    private final EventReservationRepository eventReservationRepository;
    private final EventRepository eventRepository;
    private final EventParkingZoneRepository eventParkingZoneRepository;
    private final ReservationRepository reservationRepository;

    public EventReservationService(
            EventReservationRepository eventReservationRepository,
            EventRepository eventRepository,
            EventParkingZoneRepository eventParkingZoneRepository,
            ReservationRepository reservationRepository
    ) {
        this.eventReservationRepository = eventReservationRepository;
        this.eventRepository = eventRepository;
        this.eventParkingZoneRepository = eventParkingZoneRepository;
        this.reservationRepository = reservationRepository;
    }

    public EventReservationResponseDto createEventReservation(
            Long eventId,
            Long eventParkingZoneId,
            Long reservationId
    ) {
        if (eventReservationRepository
                .existsByEvent_IdAndReservation_Id(
                        eventId,
                        reservationId
                )) {
            throw new IllegalArgumentException(
                    "This reservation is already linked to the event."
            );
        }

        Event event =
                eventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event not found with ID: "
                                                + eventId
                                )
                        );

        EventParkingZone eventParkingZone =
                eventParkingZoneRepository.findById(
                                eventParkingZoneId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event parking zone not found with ID: "
                                                + eventParkingZoneId
                                )
                        );

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found with ID: "
                                                + reservationId
                                )
                        );

        if (!eventParkingZone.getEvent()
                .getId()
                .equals(eventId)) {
            throw new IllegalArgumentException(
                    "The event parking zone does not belong to the specified event."
            );
        }

        if (eventParkingZone.getStatus() == null
                || !"ACTIVE".equalsIgnoreCase(
                eventParkingZone.getStatus()
        )) {
            throw new IllegalArgumentException(
                    "The event parking zone is not active."
            );
        }

        if (eventParkingZone.getAvailableCount() == null
                || eventParkingZone.getAvailableCount() <= 0) {
            throw new IllegalArgumentException(
                    "No available parking capacity remains "
                            + "for this event parking zone."
            );
        }

        eventParkingZone.setReservedCount(
                eventParkingZone.getReservedCount() + 1
        );

        eventParkingZone.setAvailableCount(
                eventParkingZone.getAvailableCount() - 1
        );

        validateCounts(eventParkingZone);

        EventReservation eventReservation =
                new EventReservation();

        eventReservation.setEvent(event);
        eventReservation.setEventParkingZone(
                eventParkingZone
        );
        eventReservation.setReservation(reservation);

        eventParkingZoneRepository.save(eventParkingZone);

        return toResponseDto(
                eventReservationRepository.save(eventReservation)
        );
    }

    @Transactional(readOnly = true)
    public EventReservationResponseDto getEventReservationById(
            Long id
    ) {
        return toResponseDto(
                eventReservationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event reservation not found with ID: "
                                                + id
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<EventReservationResponseDto> getAllEventReservations() {
        return eventReservationRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventReservationResponseDto> getEventReservationsByEventId(
            Long eventId
    ) {
        return eventReservationRepository
                .findByEvent_Id(eventId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventReservationResponseDto>
    getEventReservationsByParkingZoneId(
            Long eventParkingZoneId
    ) {
        return eventReservationRepository
                .findByEventParkingZone_Id(
                        eventParkingZoneId
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventReservationResponseDto getEventReservationByReservationId(
            Long reservationId
    ) {
        return toResponseDto(
                eventReservationRepository
                        .findByReservation_Id(reservationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event reservation not found "
                                                + "for reservation ID: "
                                                + reservationId
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public EventReservationResponseDto
    getEventReservationByEventAndReservation(
            Long eventId,
            Long reservationId
    ) {
        return toResponseDto(
                eventReservationRepository
                        .findByEvent_IdAndReservation_Id(
                                eventId,
                                reservationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event reservation not found."
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public EventReservationResponseDto
    getEventReservationByParkingZoneAndReservation(
            Long eventParkingZoneId,
            Long reservationId
    ) {
        return toResponseDto(
                eventReservationRepository
                        .findByEventParkingZone_IdAndReservation_Id(
                                eventParkingZoneId,
                                reservationId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event reservation not found."
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public boolean existsByEventAndReservation(
            Long eventId,
            Long reservationId
    ) {
        return eventReservationRepository
                .existsByEvent_IdAndReservation_Id(
                        eventId,
                        reservationId
                );
    }

    public void deleteEventReservation(Long id) {
        EventReservation eventReservation =
                eventReservationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Event reservation not found with ID: "
                                                + id
                                )
                        );

        EventParkingZone eventParkingZone =
                eventReservation.getEventParkingZone();

        if (eventParkingZone != null) {

            int reservedCount =
                    eventParkingZone.getReservedCount() != null
                            ? eventParkingZone.getReservedCount()
                            : 0;

            int availableCount =
                    eventParkingZone.getAvailableCount() != null
                            ? eventParkingZone.getAvailableCount()
                            : 0;

            if (reservedCount > 0) {
                eventParkingZone.setReservedCount(
                        reservedCount - 1
                );

                eventParkingZone.setAvailableCount(
                        availableCount + 1
                );

                validateCounts(eventParkingZone);

                eventParkingZoneRepository.save(
                        eventParkingZone
                );
            }
        }

        eventReservationRepository.delete(
                eventReservation
        );
    }

    private void validateCounts(
            EventParkingZone eventParkingZone
    ) {
        Integer capacity =
                eventParkingZone.getCapacity();

        Integer reservedCount =
                eventParkingZone.getReservedCount();

        Integer occupiedCount =
                eventParkingZone.getOccupiedCount();

        Integer availableCount =
                eventParkingZone.getAvailableCount();

        if (capacity == null
                || reservedCount == null
                || occupiedCount == null
                || availableCount == null) {
            throw new IllegalArgumentException(
                    "Event parking zone counts cannot be null."
            );
        }

        if (capacity < 0
                || reservedCount < 0
                || occupiedCount < 0
                || availableCount < 0) {
            throw new IllegalArgumentException(
                    "Event parking zone counts cannot be negative."
            );
        }

        if (reservedCount + occupiedCount + availableCount
                != capacity) {
            throw new IllegalArgumentException(
                    "Reserved, occupied, and available counts "
                            + "must equal capacity."
            );
        }
    }

    private EventReservationResponseDto toResponseDto(
            EventReservation eventReservation
    ) {
        return new EventReservationResponseDto(
                eventReservation.getId(),
                eventReservation.getEvent().getId(),
                eventReservation.getEventParkingZone().getId(),
                eventReservation.getReservation().getId(),
                eventReservation.getCreatedAt()
        );
    }
}
package rw.parkshare.backend.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.EventRequestDto;
import rw.parkshare.backend.common.dto.EventResponseDto;
import rw.parkshare.backend.event.entity.Event;
import rw.parkshare.backend.event.repository.EventRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public EventResponseDto createEvent(
            Long organizerId,
            EventRequestDto request
    ) {
        if (request.getName() == null
                || request.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Event name is required."
            );
        }

        if (request.getStartTime() == null) {
            throw new IllegalArgumentException(
                    "Event start time is required."
            );
        }

        if (request.getEndTime() == null) {
            throw new IllegalArgumentException(
                    "Event end time is required."
            );
        }

        if (!request.getEndTime().isAfter(
                request.getStartTime()
        )) {
            throw new IllegalArgumentException(
                    "Event end time must be after start time."
            );
        }

        User organizer =
                userRepository.findById(organizerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Organizer not found with ID: "
                                                + organizerId
                                )
                        );

        Event event = new Event();

        event.setOrganizer(organizer);
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setVenueName(request.getVenueName());
        event.setAddress(request.getAddress());
        event.setLatitude(request.getLatitude());
        event.setLongitude(request.getLongitude());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());

        event.setStatus(
                request.getStatus() == null
                        || request.getStatus().isBlank()
                        ? "DRAFT"
                        : request.getStatus()
        );

        event.setCreatedAt(OffsetDateTime.now());
        event.setUpdatedAt(OffsetDateTime.now());

        return toResponseDto(
                eventRepository.save(event)
        );
    }

    @Transactional(readOnly = true)
    public EventResponseDto getEventById(Long id) {
        return toResponseDto(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByOrganizerId(
            Long organizerId
    ) {
        return eventRepository
                .findByOrganizer_Id(organizerId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByStatus(
            String status
    ) {
        return eventRepository
                .findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> searchEventsByName(
            String name
    ) {
        return eventRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByVenueName(
            String venueName
    ) {
        return eventRepository
                .findByVenueNameContainingIgnoreCase(venueName)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByOrganizerAndStatus(
            Long organizerId,
            String status
    ) {
        return eventRepository
                .findByOrganizer_IdAndStatus(
                        organizerId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsStartingBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return eventRepository
                .findByStartTimeBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsEndingBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return eventRepository
                .findByEndTimeBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsStartingAfter(
            OffsetDateTime startTime
    ) {
        return eventRepository
                .findByStartTimeAfter(startTime)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsEndingBefore(
            OffsetDateTime endTime
    ) {
        return eventRepository
                .findByEndTimeBefore(endTime)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public EventResponseDto updateEvent(
            Long id,
            EventRequestDto request
    ) {
        Event event = getEntityById(id);

        if (request.getName() != null
                && !request.getName().isBlank()) {
            event.setName(request.getName());
        }

        if (request.getDescription() != null) {
            event.setDescription(
                    request.getDescription()
            );
        }

        if (request.getVenueName() != null) {
            event.setVenueName(
                    request.getVenueName()
            );
        }

        if (request.getAddress() != null) {
            event.setAddress(
                    request.getAddress()
            );
        }

        if (request.getLatitude() != null) {
            event.setLatitude(
                    request.getLatitude()
            );
        }

        if (request.getLongitude() != null) {
            event.setLongitude(
                    request.getLongitude()
            );
        }

        OffsetDateTime newStartTime =
                request.getStartTime() != null
                        ? request.getStartTime()
                        : event.getStartTime();

        OffsetDateTime newEndTime =
                request.getEndTime() != null
                        ? request.getEndTime()
                        : event.getEndTime();

        if (!newEndTime.isAfter(newStartTime)) {
            throw new IllegalArgumentException(
                    "Event end time must be after start time."
            );
        }

        event.setStartTime(newStartTime);
        event.setEndTime(newEndTime);

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {
            event.setStatus(
                    request.getStatus()
            );
        }

        return toResponseDto(
                eventRepository.save(event)
        );
    }

    public EventResponseDto updateEventStatus(
            Long id,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Event status is required."
            );
        }

        Event event = getEntityById(id);

        event.setStatus(status);

        return toResponseDto(
                eventRepository.save(event)
        );
    }

    public void deleteEvent(Long id) {
        Event event = getEntityById(id);

        eventRepository.delete(event);
    }

    private Event getEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Event not found with ID: " + id
                        )
                );
    }

    private EventResponseDto toResponseDto(
            Event event
    ) {
        return new EventResponseDto(
                event.getId(),
                event.getOrganizer() != null
                        ? event.getOrganizer().getId()
                        : null,
                event.getName(),
                event.getDescription(),
                event.getVenueName(),
                event.getAddress(),
                event.getLatitude(),
                event.getLongitude(),
                event.getStartTime(),
                event.getEndTime(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
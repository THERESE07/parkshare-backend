package rw.parkshare.backend.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.event.entity.Event;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer_Id(Long organizerId);

    List<Event> findByStatus(String status);

    List<Event> findByNameContainingIgnoreCase(String name);

    List<Event> findByVenueNameContainingIgnoreCase(String venueName);

    List<Event> findByOrganizer_IdAndStatus(
            Long organizerId,
            String status
    );

    List<Event> findByStartTimeBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<Event> findByEndTimeBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<Event> findByStartTimeAfter(OffsetDateTime startTime);

    List<Event> findByEndTimeBefore(OffsetDateTime endTime);
}
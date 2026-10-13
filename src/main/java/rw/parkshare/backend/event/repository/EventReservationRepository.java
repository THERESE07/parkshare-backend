package rw.parkshare.backend.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.event.entity.EventReservation;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventReservationRepository extends JpaRepository<EventReservation, Long> {

    List<EventReservation> findByEvent_Id(Long eventId);

    List<EventReservation> findByEventParkingZone_Id(Long eventParkingZoneId);

    Optional<EventReservation> findByReservation_Id(Long reservationId);

    Optional<EventReservation> findByEvent_IdAndReservation_Id(
            Long eventId,
            Long reservationId
    );

    Optional<EventReservation> findByEventParkingZone_IdAndReservation_Id(
            Long eventParkingZoneId,
            Long reservationId
    );

    boolean existsByEvent_IdAndReservation_Id(
            Long eventId,
            Long reservationId
    );
}

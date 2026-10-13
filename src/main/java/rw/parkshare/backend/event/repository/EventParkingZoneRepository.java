package rw.parkshare.backend.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.event.entity.EventParkingZone;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventParkingZoneRepository extends JpaRepository<EventParkingZone, Long> {

    List<EventParkingZone> findByEvent_Id(Long eventId);

    List<EventParkingZone> findByParkingLot_Id(Long parkingLotId);

    Optional<EventParkingZone> findByEvent_IdAndParkingLot_Id(
            Long eventId,
            Long parkingLotId
    );

    List<EventParkingZone> findByEvent_IdAndStatus(
            Long eventId,
            String status
    );

    List<EventParkingZone> findByParkingLot_IdAndStatus(
            Long parkingLotId,
            String status
    );

    List<EventParkingZone> findByStatus(String status);

    List<EventParkingZone> findByCurrency(String currency);

    boolean existsByEvent_IdAndParkingLot_Id(
            Long eventId,
            Long parkingLotId
    );
}
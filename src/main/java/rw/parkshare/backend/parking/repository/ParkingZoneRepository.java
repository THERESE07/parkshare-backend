package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingZone;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingZoneRepository extends JpaRepository<ParkingZone, Long> {

    List<ParkingZone> findByParkingLot_Id(Long parkingLotId);

    List<ParkingZone> findByParkingLot_IdAndStatus(
            Long parkingLotId,
            String status
    );

    List<ParkingZone> findByStatus(String status);

    Optional<ParkingZone> findByParkingLot_IdAndName(
            Long parkingLotId,
            String name
    );

    List<ParkingZone> findByNameContainingIgnoreCase(String name);

    boolean existsByParkingLot_IdAndName(
            Long parkingLotId,
            String name
    );
}
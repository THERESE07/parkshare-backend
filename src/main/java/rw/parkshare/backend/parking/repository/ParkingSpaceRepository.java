package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingSpace;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {

    List<ParkingSpace> findByParkingZone_Id(Long parkingZoneId);

    List<ParkingSpace> findByParkingZone_IdAndStatus(
            Long parkingZoneId,
            String status
    );

    List<ParkingSpace> findByStatus(String status);

    Optional<ParkingSpace> findBySpaceCode(String spaceCode);

    List<ParkingSpace> findBySpaceType(String spaceType);

    List<ParkingSpace> findByParkingZone_IdAndSpaceType(
            Long parkingZoneId,
            String spaceType
    );

    boolean existsByParkingZone_IdAndSpaceCode(
            Long parkingZoneId,
            String spaceCode
    );
}

package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingOperatingHours;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingOperatingHoursRepository extends JpaRepository<ParkingOperatingHours, Long> {

    List<ParkingOperatingHours> findByParkingLot_Id(Long parkingLotId);

    Optional<ParkingOperatingHours> findByParkingLot_IdAndDayOfWeek(
            Long parkingLotId,
            String dayOfWeek
    );

    List<ParkingOperatingHours> findByParkingLot_IdAndClosed(
            Long parkingLotId,
            boolean closed
    );

    List<ParkingOperatingHours> findByDayOfWeek(String dayOfWeek);

    boolean existsByParkingLot_IdAndDayOfWeek(
            Long parkingLotId,
            String dayOfWeek
    );
}

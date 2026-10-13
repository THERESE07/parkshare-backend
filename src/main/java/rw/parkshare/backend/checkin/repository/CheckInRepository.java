package rw.parkshare.backend.checkin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.checkin.entity.CheckIn;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByReservation_Id(Long reservationId);

    List<CheckIn> findByVehicle_Id(Long vehicleId);

    List<CheckIn> findByParkingSpace_Id(Long parkingSpaceId);

    List<CheckIn> findByCheckedInBy_Id(Long userId);

    List<CheckIn> findByCheckInMethod(String checkInMethod);

    List<CheckIn> findByCheckedInAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    boolean existsByReservation_Id(Long reservationId);
}
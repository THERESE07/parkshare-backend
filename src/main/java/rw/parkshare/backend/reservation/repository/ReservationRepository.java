package rw.parkshare.backend.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(String reservationCode);

    List<Reservation> findByUser_Id(Long userId);

    List<Reservation> findByVehicle_Id(Long vehicleId);

    List<Reservation> findByParkingLot_Id(Long parkingLotId);

    List<Reservation> findByParkingSpace_Id(Long parkingSpaceId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByUser_IdAndStatus(
            Long userId,
            ReservationStatus status
    );

    List<Reservation> findByParkingLot_IdAndStatus(
            Long parkingLotId,
            ReservationStatus status
    );

    List<Reservation> findByStartTimeBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<Reservation> findByParkingLot_IdAndStartTimeBetween(
            Long parkingLotId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    boolean existsByReservationCode(String reservationCode);
}
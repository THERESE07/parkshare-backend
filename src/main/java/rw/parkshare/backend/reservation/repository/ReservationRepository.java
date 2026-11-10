package rw.parkshare.backend.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatus;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationCode(String reservationCode);

    List<Reservation> findByUser_Id(Long userId);

    org.springframework.data.domain.Page<Reservation> findByUser_Id(
            Long userId, org.springframework.data.domain.Pageable pageable);

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

    /** Pass -1 as excludeId when checking a new reservation. */
    @Query("""
            select count(r) > 0 from Reservation r
            where r.parkingSpace.id = :spaceId
              and r.status in :statuses
              and r.startTime < :endTime and r.endTime > :startTime
              and r.id <> :excludeId
            """)
    boolean existsOverlappingOnSpace(
            @Param("spaceId") Long spaceId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("excludeId") Long excludeId
    );

    @Query("""
            select count(r) from Reservation r
            where r.parkingLot.id = :lotId
              and r.status in :statuses
              and r.startTime < :endTime and r.endTime > :startTime
            """)
    long countOverlappingInLot(
            @Param("lotId") Long lotId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses
    );

    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, OffsetDateTime before);

    List<Reservation> findByStatusAndStartTimeBefore(ReservationStatus status, OffsetDateTime before);

    List<Reservation> findByStatusAndStartTimeBetween(
            ReservationStatus status, OffsetDateTime from, OffsetDateTime to);
}

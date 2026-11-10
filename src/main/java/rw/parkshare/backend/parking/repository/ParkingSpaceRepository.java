package rw.parkshare.backend.parking.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingSpace;

import java.time.OffsetDateTime;
import java.util.Collection;
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

    /** Locks the space row so two bookings for it are processed one after the other. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSpace s where s.id = :id")
    Optional<ParkingSpace> findByIdForUpdate(@Param("id") Long id);

    long countByParkingZone_ParkingLot_Id(Long parkingLotId);

    /** Bookable spaces in a lot with no active reservation overlapping the window. */
    @Query("""
            select s from ParkingSpace s
            where s.parkingZone.parkingLot.id = :lotId
              and s.parkingZone.status = 'ACTIVE'
              and s.status not in ('MAINTENANCE', 'BLOCKED')
              and not exists (
                  select r.id from Reservation r
                  where r.parkingSpace = s
                    and r.status in :statuses
                    and r.startTime < :endTime and r.endTime > :startTime
              )
            order by s.id
            """)
    List<ParkingSpace> findFreeSpaces(
            @Param("lotId") Long lotId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses
    );
}

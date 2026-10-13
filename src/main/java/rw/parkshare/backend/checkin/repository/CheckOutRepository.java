package rw.parkshare.backend.checkin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.checkin.entity.CheckOut;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckOutRepository extends JpaRepository<CheckOut, Long> {

    Optional<CheckOut> findByCheckIn_Id(Long checkInId);

    Optional<CheckOut> findByReservation_Id(Long reservationId);

    List<CheckOut> findByCheckedOutBy_Id(Long userId);

    List<CheckOut> findByCheckOutMethod(String checkOutMethod);

    List<CheckOut> findByCheckedOutAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    boolean existsByCheckIn_Id(Long checkInId);

    boolean existsByReservation_Id(Long reservationId);
}

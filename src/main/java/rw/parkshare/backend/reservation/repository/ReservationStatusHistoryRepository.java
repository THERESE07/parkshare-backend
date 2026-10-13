package rw.parkshare.backend.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.reservation.entity.ReservationStatusHistory;

import java.util.List;

@Repository
public interface ReservationStatusHistoryRepository
        extends JpaRepository<ReservationStatusHistory, Long> {

    List<ReservationStatusHistory> findByReservation_Id(Long reservationId);

    List<ReservationStatusHistory> findByChangedBy_Id(Long userId);

    List<ReservationStatusHistory> findByNewStatus(String newStatus);

    List<ReservationStatusHistory> findByOldStatus(String oldStatus);

    List<ReservationStatusHistory> findByReservation_IdAndNewStatus(
            Long reservationId,
            String newStatus
    );
}

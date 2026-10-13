package rw.parkshare.backend.support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.support.entity.Dispute;

import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByReservation_Id(Long reservationId);

    List<Dispute> findByRaisedBy_Id(Long userId);

    List<Dispute> findByAssignedTo_Id(Long userId);

    List<Dispute> findByDisputeType(String disputeType);

    List<Dispute> findByStatus(String status);

    List<Dispute> findByCurrency(String currency);

    List<Dispute> findByAssignedTo_IdAndStatus(
            Long userId,
            String status
    );

    List<Dispute> findByRaisedBy_IdAndStatus(
            Long userId,
            String status
    );
}

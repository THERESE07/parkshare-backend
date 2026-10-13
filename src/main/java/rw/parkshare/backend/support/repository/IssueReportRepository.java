package rw.parkshare.backend.support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.support.entity.IssueReport;

import java.util.List;

@Repository
public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {

    List<IssueReport> findByUser_Id(Long userId);

    List<IssueReport> findByParkingLot_Id(Long parkingLotId);

    List<IssueReport> findByReservation_Id(Long reservationId);

    List<IssueReport> findByResolvedBy_Id(Long userId);

    List<IssueReport> findByIssueType(String issueType);

    List<IssueReport> findByStatus(String status);

    List<IssueReport> findByPriority(String priority);

    List<IssueReport> findByStatusAndPriority(
            String status,
            String priority
    );

    List<IssueReport> findByUser_IdAndStatus(
            Long userId,
            String status
    );
}

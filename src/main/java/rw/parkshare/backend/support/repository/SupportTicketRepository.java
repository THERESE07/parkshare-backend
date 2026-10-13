package rw.parkshare.backend.support.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.support.entity.SupportTicket;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    List<SupportTicket> findByUser_Id(Long userId);

    List<SupportTicket> findByAssignedTo_Id(Long userId);

    List<SupportTicket> findByCategory(String category);

    List<SupportTicket> findByStatus(String status);

    List<SupportTicket> findByPriority(String priority);

    List<SupportTicket> findByUser_IdAndStatus(Long userId, String status);

    List<SupportTicket> findByAssignedTo_IdAndStatus(Long userId, String status);

    List<SupportTicket> findByStatusAndPriority(String status, String priority);

    List<SupportTicket> findByCreatedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<SupportTicket> findByResolvedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    boolean existsByTicketNumber(String ticketNumber);
}
package rw.parkshare.backend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.admin.entity.AuditLog;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUser_Id(Long userId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByEntityType(String entityType);

    List<AuditLog> findByEntityId(Long entityId);

    List<AuditLog> findByEntityTypeAndEntityId(
            String entityType,
            Long entityId
    );

    List<AuditLog> findByUser_IdAndAction(
            Long userId,
            String action
    );

    List<AuditLog> findByIpAddress(String ipAddress);

    List<AuditLog> findByCreatedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<AuditLog> findByUser_IdAndCreatedAtBetween(
            Long userId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<AuditLog> findByEntityTypeAndCreatedAtBetween(
            String entityType,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );
}

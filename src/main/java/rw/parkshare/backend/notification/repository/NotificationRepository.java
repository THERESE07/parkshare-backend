package rw.parkshare.backend.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.notification.entity.Notification;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_Id(Long userId);

    org.springframework.data.domain.Page<Notification> findByUser_Id(
            Long userId, org.springframework.data.domain.Pageable pageable);

    List<Notification> findByUser_IdAndStatus(Long userId, String status);

    List<Notification> findByType(String type);

    List<Notification> findByChannel(String channel);

    List<Notification> findByStatus(String status);

    List<Notification> findByReferenceType(String referenceType);

    List<Notification> findByReferenceId(Long referenceId);

    List<Notification> findByUser_IdAndType(
            Long userId,
            String type
    );

    List<Notification> findByUser_IdAndChannel(
            Long userId,
            String channel
    );

    List<Notification> findByUser_IdAndReferenceTypeAndReferenceId(
            Long userId,
            String referenceType,
            Long referenceId
    );

    List<Notification> findByCreatedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<Notification> findByUser_IdAndReadAtIsNull(Long userId);

    List<Notification> findByUser_IdAndSentAtIsNull(Long userId);
}

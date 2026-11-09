package rw.parkshare.backend.notification.service;

import rw.parkshare.backend.common.dto.PageResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.NotificationRequestDto;
import rw.parkshare.backend.common.dto.NotificationResponseDto;
import rw.parkshare.backend.notification.entity.Notification;
import rw.parkshare.backend.notification.repository.NotificationRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public NotificationResponseDto createNotification(
            Long userId,
            NotificationRequestDto request
    ) {
        if (request.getType() == null
                || request.getType().isBlank()) {
            throw new IllegalArgumentException(
                    "Notification type is required."
            );
        }

        if (request.getTitle() == null
                || request.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "Notification title is required."
            );
        }

        if (request.getMessage() == null
                || request.getMessage().isBlank()) {
            throw new IllegalArgumentException(
                    "Notification message is required."
            );
        }

        if (request.getChannel() == null
                || request.getChannel().isBlank()) {
            throw new IllegalArgumentException(
                    "Notification channel is required."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + userId
                        )
                );

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setChannel(request.getChannel());

        notification.setStatus(
                request.getStatus() == null
                        || request.getStatus().isBlank()
                        ? "UNREAD"
                        : request.getStatus()
        );

        notification.setReferenceType(
                request.getReferenceType()
        );

        notification.setReferenceId(
                request.getReferenceId()
        );

        notification.setCreatedAt(
                OffsetDateTime.now()
        );

        return toResponseDto(
                notificationRepository.save(notification)
        );
    }

    @Transactional(readOnly = true)
    public NotificationResponseDto getNotificationById(
            Long id
    ) {
        return toResponseDto(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDto> getNotificationsByUserId(
            Long userId, int page, int size
    ) {
        return PageResponse.of(
                notificationRepository.findByUser_Id(userId, PageResponse.request(page, size, "createdAt")),
                this::toResponseDto
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByUserAndStatus(
            Long userId,
            String status
    ) {
        return notificationRepository
                .findByUser_IdAndStatus(userId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByType(
            String type
    ) {
        return notificationRepository.findByType(type)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByChannel(
            String channel
    ) {
        return notificationRepository.findByChannel(channel)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByStatus(
            String status
    ) {
        return notificationRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByReferenceType(
            String referenceType
    ) {
        return notificationRepository
                .findByReferenceType(referenceType)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByReferenceId(
            Long referenceId
    ) {
        return notificationRepository
                .findByReferenceId(referenceId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByUserAndType(
            Long userId,
            String type
    ) {
        return notificationRepository
                .findByUser_IdAndType(userId, type)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsByUserAndChannel(
            Long userId,
            String channel
    ) {
        return notificationRepository
                .findByUser_IdAndChannel(userId, channel)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto>
    getNotificationsByUserAndReference(
            Long userId,
            String referenceType,
            Long referenceId
    ) {
        return notificationRepository
                .findByUser_IdAndReferenceTypeAndReferenceId(
                        userId,
                        referenceType,
                        referenceId
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotificationsCreatedBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return notificationRepository
                .findByCreatedAtBetween(startTime, endTime)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotifications(
            Long userId
    ) {
        return notificationRepository
                .findByUser_IdAndReadAtIsNull(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnsentNotifications(
            Long userId
    ) {
        return notificationRepository
                .findByUser_IdAndSentAtIsNull(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public NotificationResponseDto markAsRead(Long id) {
        Notification notification = getEntityById(id);

        if (notification.getReadAt() == null) {
            notification.setReadAt(
                    OffsetDateTime.now()
            );
        }

        return toResponseDto(
                notificationRepository.save(notification)
        );
    }

    public NotificationResponseDto markAsSent(Long id) {
        Notification notification = getEntityById(id);

        if (notification.getSentAt() == null) {
            notification.setSentAt(
                    OffsetDateTime.now()
            );
        }

        notification.setStatus("SENT");

        return toResponseDto(
                notificationRepository.save(notification)
        );
    }

    public NotificationResponseDto updateStatus(
            Long id,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification status is required."
            );
        }

        Notification notification = getEntityById(id);

        notification.setStatus(status);

        return toResponseDto(
                notificationRepository.save(notification)
        );
    }

    public NotificationResponseDto updateNotification(
            Long id,
            NotificationRequestDto request
    ) {
        Notification notification = getEntityById(id);

        if (request.getType() != null
                && !request.getType().isBlank()) {
            notification.setType(request.getType());
        }

        if (request.getTitle() != null
                && !request.getTitle().isBlank()) {
            notification.setTitle(request.getTitle());
        }

        if (request.getMessage() != null
                && !request.getMessage().isBlank()) {
            notification.setMessage(request.getMessage());
        }

        if (request.getChannel() != null
                && !request.getChannel().isBlank()) {
            notification.setChannel(request.getChannel());
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {
            notification.setStatus(request.getStatus());
        }

        if (request.getReferenceType() != null) {
            notification.setReferenceType(
                    request.getReferenceType()
            );
        }

        if (request.getReferenceId() != null) {
            notification.setReferenceId(
                    request.getReferenceId()
            );
        }

        return toResponseDto(
                notificationRepository.save(notification)
        );
    }

    public void deleteNotification(Long id) {
        Notification notification = getEntityById(id);

        notificationRepository.delete(notification);
    }

    private Notification getEntityById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found with ID: " + id
                        )
                );
    }

    private NotificationResponseDto toResponseDto(
            Notification notification
    ) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getUser() != null
                        ? notification.getUser().getId()
                        : null,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getReadAt(),
                notification.getSentAt(),
                notification.getCreatedAt()
        );
    }
}
package rw.parkshare.backend.notification.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.NotificationRequestDto;
import rw.parkshare.backend.common.dto.NotificationResponseDto;
import rw.parkshare.backend.notification.service.NotificationService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(
            @RequestParam Long userId,
            @RequestBody NotificationRequestDto request
    ) {
        return ResponseEntity.ok(
                notificationService.createNotification(
                        userId,
                        request
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> getNotificationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationById(id)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId)
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserAndStatus(
                        userId,
                        status
                )
        );
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByType(
            @PathVariable String type
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByType(type)
        );
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByChannel(
            @PathVariable String channel
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByChannel(channel)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByStatus(status)
        );
    }

    @GetMapping("/reference-type/{referenceType}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByReferenceType(
            @PathVariable String referenceType
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByReferenceType(
                        referenceType
                )
        );
    }

    @GetMapping("/reference-id/{referenceId}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByReferenceId(
            @PathVariable Long referenceId
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByReferenceId(
                        referenceId
                )
        );
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByUserAndType(
            @PathVariable Long userId,
            @PathVariable String type
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserAndType(
                        userId,
                        type
                )
        );
    }

    @GetMapping("/user/{userId}/channel/{channel}")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByUserAndChannel(
            @PathVariable Long userId,
            @PathVariable String channel
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserAndChannel(
                        userId,
                        channel
                )
        );
    }

    @GetMapping("/user/{userId}/reference")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsByUserAndReference(
            @PathVariable Long userId,
            @RequestParam String referenceType,
            @RequestParam Long referenceId
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserAndReference(
                        userId,
                        referenceType,
                        referenceId
                )
        );
    }

    @GetMapping("/created-between")
    public ResponseEntity<List<NotificationResponseDto>>
    getNotificationsCreatedBetween(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsCreatedBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDto>>
    getUnreadNotifications(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(userId)
        );
    }

    @GetMapping("/user/{userId}/unsent")
    public ResponseEntity<List<NotificationResponseDto>>
    getUnsentNotifications(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                notificationService.getUnsentNotifications(userId)
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id)
        );
    }

    @PatchMapping("/{id}/sent")
    public ResponseEntity<NotificationResponseDto> markAsSent(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                notificationService.markAsSent(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                notificationService.updateStatus(
                        id,
                        status
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> updateNotification(
            @PathVariable Long id,
            @RequestBody NotificationRequestDto request
    ) {
        return ResponseEntity.ok(
                notificationService.updateNotification(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id
    ) {
        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }
}
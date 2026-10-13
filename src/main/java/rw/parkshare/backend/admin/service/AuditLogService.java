package rw.parkshare.backend.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.admin.entity.AuditLog;
import rw.parkshare.backend.admin.repository.AuditLogRepository;
import rw.parkshare.backend.common.dto.AuditLogRequestDto;
import rw.parkshare.backend.common.dto.AuditLogResponseDto;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public AuditLogResponseDto createAuditLog(
            Long userId,
            AuditLogRequestDto request
    ) {
        validateRequiredFields(
                request.getAction(),
                request.getEntityType()
        );

        User user = null;

        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found with ID: " + userId
                            )
                    );
        }

        AuditLog auditLog = new AuditLog();

        auditLog.setUser(user);
        auditLog.setAction(request.getAction());
        auditLog.setEntityType(request.getEntityType());
        auditLog.setEntityId(request.getEntityId());
        auditLog.setDescription(request.getDescription());
        auditLog.setOldValues(request.getOldValues());
        auditLog.setNewValues(request.getNewValues());
        auditLog.setIpAddress(request.getIpAddress());
        auditLog.setUserAgent(request.getUserAgent());

        return toResponseDto(auditLogRepository.save(auditLog));
    }

    @Transactional(readOnly = true)
    public AuditLogResponseDto getAuditLogById(Long id) {
        return toResponseDto(
                auditLogRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Audit log not found with ID: " + id
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAllAuditLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByUserId(Long userId) {
        return auditLogRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByEntityType(
            String entityType
    ) {
        return auditLogRepository.findByEntityType(entityType)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByEntityId(Long entityId) {
        return auditLogRepository.findByEntityId(entityId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByEntity(
            String entityType,
            Long entityId
    ) {
        return auditLogRepository
                .findByEntityTypeAndEntityId(
                        entityType,
                        entityId
                )
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByUserAndAction(
            Long userId,
            String action
    ) {
        return auditLogRepository
                .findByUser_IdAndAction(
                        userId,
                        action
                )
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByIpAddress(
            String ipAddress
    ) {
        return auditLogRepository.findByIpAddress(ipAddress)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return auditLogRepository.findByCreatedAtBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByUserBetween(
            Long userId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return auditLogRepository
                .findByUser_IdAndCreatedAtBetween(
                        userId,
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAuditLogsByEntityTypeBetween(
            String entityType,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return auditLogRepository
                .findByEntityTypeAndCreatedAtBetween(
                        entityType,
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public AuditLogResponseDto updateAuditLog(
            Long id,
            Long userId,
            AuditLogRequestDto request
    ) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Audit log not found with ID: " + id
                        )
                );

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "User not found with ID: " + userId
                            )
                    );

            auditLog.setUser(user);
        }

        if (request.getAction() != null
                && !request.getAction().isBlank()) {
            auditLog.setAction(request.getAction());
        }

        if (request.getEntityType() != null
                && !request.getEntityType().isBlank()) {
            auditLog.setEntityType(request.getEntityType());
        }

        if (request.getEntityId() != null) {
            auditLog.setEntityId(request.getEntityId());
        }

        if (request.getDescription() != null) {
            auditLog.setDescription(request.getDescription());
        }

        if (request.getOldValues() != null) {
            auditLog.setOldValues(request.getOldValues());
        }

        if (request.getNewValues() != null) {
            auditLog.setNewValues(request.getNewValues());
        }

        if (request.getIpAddress() != null) {
            auditLog.setIpAddress(request.getIpAddress());
        }

        if (request.getUserAgent() != null) {
            auditLog.setUserAgent(request.getUserAgent());
        }

        return toResponseDto(auditLogRepository.save(auditLog));
    }

    public void deleteAuditLog(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Audit log not found with ID: " + id
                        )
                );

        auditLogRepository.delete(auditLog);
    }

    private void validateRequiredFields(
            String action,
            String entityType
    ) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit log action is required."
            );
        }

        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit log entity type is required."
            );
        }
    }

    private void validateDateRange(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required."
            );
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException(
                    "Start time cannot be after end time."
            );
        }
    }

    private AuditLogResponseDto toResponseDto(AuditLog auditLog) {
        return new AuditLogResponseDto(
                auditLog.getId(),
                auditLog.getUser() != null
                        ? auditLog.getUser().getId()
                        : null,
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDescription(),
                auditLog.getOldValues(),
                auditLog.getNewValues(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getCreatedAt()
        );
    }
}
package rw.parkshare.backend.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.admin.service.AuditLogService;
import rw.parkshare.backend.common.dto.AuditLogRequestDto;
import rw.parkshare.backend.common.dto.AuditLogResponseDto;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(
            AuditLogService auditLogService
    ) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<AuditLogResponseDto> createAuditLog(
            @RequestParam(required = false) Long userId,
            @RequestBody AuditLogRequestDto request
    ) {
        AuditLogResponseDto created = auditLogService.createAuditLog(
                userId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponseDto> getAuditLogById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDto>> getAllAuditLogs() {
        return ResponseEntity.ok(
                auditLogService.getAllAuditLogs()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByUserId(userId)
        );
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByAction(
            @PathVariable String action
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByAction(action)
        );
    }

    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByEntityType(
            @PathVariable String entityType
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByEntityType(entityType)
        );
    }

    @GetMapping("/entity-id/{entityId}")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByEntityId(
            @PathVariable Long entityId
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByEntityId(entityId)
        );
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByEntity(
                        entityType,
                        entityId
                )
        );
    }

    @GetMapping("/user/{userId}/action/{action}")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByUserAndAction(
            @PathVariable Long userId,
            @PathVariable String action
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByUserAndAction(
                        userId,
                        action
                )
        );
    }

    @GetMapping("/ip-address/{ipAddress}")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByIpAddress(
            @PathVariable String ipAddress
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByIpAddress(ipAddress)
        );
    }

    @GetMapping("/between")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/user/{userId}/between")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByUserBetween(
            @PathVariable Long userId,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByUserBetween(
                        userId,
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/entity-type/{entityType}/between")
    public ResponseEntity<List<AuditLogResponseDto>> getAuditLogsByEntityTypeBetween(
            @PathVariable String entityType,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                auditLogService.getAuditLogsByEntityTypeBetween(
                        entityType,
                        startTime,
                        endTime
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuditLogResponseDto> updateAuditLog(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestBody AuditLogRequestDto request
    ) {
        return ResponseEntity.ok(
                auditLogService.updateAuditLog(
                        id,
                        userId,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditLog(
            @PathVariable Long id
    ) {
        auditLogService.deleteAuditLog(id);

        return ResponseEntity.noContent().build();
    }
}
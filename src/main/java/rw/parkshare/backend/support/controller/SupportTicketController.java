package rw.parkshare.backend.support.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rw.parkshare.backend.common.dto.SupportTicketRequestDto;
import rw.parkshare.backend.common.dto.SupportTicketResponseDto;
import rw.parkshare.backend.support.service.SupportTicketService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/support-tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(
            SupportTicketService supportTicketService
    ) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponseDto> createTicket(
            @RequestParam Long userId,
            @Valid @RequestBody SupportTicketRequestDto request
    ) {
        return ResponseEntity.ok(
                supportTicketService.createTicket(userId, request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketResponseDto> getTicketById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketById(id)
        );
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<SupportTicketResponseDto> getTicketByNumber(
            @PathVariable String ticketNumber
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketByNumber(ticketNumber)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByUserId(userId)
        );
    }

    @GetMapping("/assigned-to/{userId}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByAssignedUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByAssignedUserId(userId)
        );
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByCategory(
            @PathVariable String category
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByCategory(category)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByStatus(
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByStatus(status)
        );
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByPriority(
            @PathVariable String priority
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByPriority(priority)
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByUserAndStatus(
                        userId,
                        status
                )
        );
    }

    @GetMapping("/assigned-to/{userId}/status/{status}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByAssignedUserAndStatus(
            @PathVariable Long userId,
            @PathVariable String status
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByAssignedUserAndStatus(
                        userId,
                        status
                )
        );
    }

    @GetMapping("/status/{status}/priority/{priority}")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsByStatusAndPriority(
            @PathVariable String status,
            @PathVariable String priority
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsByStatusAndPriority(
                        status,
                        priority
                )
        );
    }

    @GetMapping("/created-between")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsCreatedBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsCreatedBetween(
                        startTime,
                        endTime
                )
        );
    }

    @GetMapping("/resolved-between")
    public ResponseEntity<List<SupportTicketResponseDto>> getTicketsResolvedBetween(
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(
                supportTicketService.getTicketsResolvedBetween(
                        startTime,
                        endTime
                )
        );
    }

    @PatchMapping("/{ticketId}/assign")
    public ResponseEntity<SupportTicketResponseDto> assignTicket(
            @PathVariable Long ticketId,
            @RequestParam Long assignedToUserId
    ) {
        return ResponseEntity.ok(
                supportTicketService.assignTicket(
                        ticketId,
                        assignedToUserId
                )
        );
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<SupportTicketResponseDto> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                supportTicketService.updateStatus(
                        ticketId,
                        status
                )
        );
    }

    @PatchMapping("/{ticketId}/resolve")
    public ResponseEntity<SupportTicketResponseDto> resolveTicket(
            @PathVariable Long ticketId
    ) {
        return ResponseEntity.ok(
                supportTicketService.resolveTicket(ticketId)
        );
    }

    @PatchMapping("/{ticketId}/priority")
    public ResponseEntity<SupportTicketResponseDto> updatePriority(
            @PathVariable Long ticketId,
            @RequestParam String priority
    ) {
        return ResponseEntity.ok(
                supportTicketService.updatePriority(
                        ticketId,
                        priority
                )
        );
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<SupportTicketResponseDto> updateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody SupportTicketRequestDto request
    ) {
        return ResponseEntity.ok(
                supportTicketService.updateTicket(
                        ticketId,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id
    ) {
        supportTicketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
package rw.parkshare.backend.support.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.SupportTicketRequestDto;
import rw.parkshare.backend.common.dto.SupportTicketResponseDto;
import rw.parkshare.backend.support.entity.SupportTicket;
import rw.parkshare.backend.support.repository.SupportTicketRepository;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    public SupportTicketService(
            SupportTicketRepository supportTicketRepository,
            UserRepository userRepository
    ) {
        this.supportTicketRepository = supportTicketRepository;
        this.userRepository = userRepository;
    }

    public SupportTicketResponseDto createTicket(
            Long userId,
            SupportTicketRequestDto request
    ) {
        if (request.getTicketNumber() == null
                || request.getTicketNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket number is required."
            );
        }

        if (supportTicketRepository.existsByTicketNumber(
                request.getTicketNumber()
        )) {
            throw new IllegalArgumentException(
                    "A support ticket with this ticket number already exists."
            );
        }

        if (request.getSubject() == null
                || request.getSubject().isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket subject is required."
            );
        }

        if (request.getDescription() == null
                || request.getDescription().isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket description is required."
            );
        }

        if (request.getCategory() == null
                || request.getCategory().isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket category is required."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + userId
                        )
                );

        SupportTicket ticket = new SupportTicket();

        ticket.setUser(user);
        ticket.setTicketNumber(request.getTicketNumber());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());

        ticket.setStatus(
                request.getStatus() == null
                        || request.getStatus().isBlank()
                        ? "OPEN"
                        : request.getStatus()
        );

        ticket.setPriority(
                request.getPriority() == null
                        || request.getPriority().isBlank()
                        ? "MEDIUM"
                        : request.getPriority()
        );

        OffsetDateTime now = OffsetDateTime.now();
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);

        return toResponseDto(
                supportTicketRepository.save(ticket)
        );
    }

    @Transactional(readOnly = true)
    public SupportTicketResponseDto getTicketById(Long id) {
        return toResponseDto(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public SupportTicketResponseDto getTicketByNumber(
            String ticketNumber
    ) {
        SupportTicket ticket =
                supportTicketRepository
                        .findByTicketNumber(ticketNumber)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Support ticket not found with number: "
                                                + ticketNumber
                                )
                        );

        return toResponseDto(ticket);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByUserId(
            Long userId
    ) {
        return supportTicketRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByAssignedUserId(
            Long userId
    ) {
        return supportTicketRepository.findByAssignedTo_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByCategory(
            String category
    ) {
        return supportTicketRepository.findByCategory(category)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByStatus(
            String status
    ) {
        return supportTicketRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByPriority(
            String priority
    ) {
        return supportTicketRepository.findByPriority(priority)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByUserAndStatus(
            Long userId,
            String status
    ) {
        return supportTicketRepository
                .findByUser_IdAndStatus(userId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByAssignedUserAndStatus(
            Long userId,
            String status
    ) {
        return supportTicketRepository
                .findByAssignedTo_IdAndStatus(userId, status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsByStatusAndPriority(
            String status,
            String priority
    ) {
        return supportTicketRepository
                .findByStatusAndPriority(status, priority)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsCreatedBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return supportTicketRepository
                .findByCreatedAtBetween(startTime, endTime)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponseDto> getTicketsResolvedBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        return supportTicketRepository
                .findByResolvedAtBetween(startTime, endTime)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public SupportTicketResponseDto assignTicket(
            Long ticketId,
            Long assignedToUserId
    ) {
        SupportTicket ticket = getEntityById(ticketId);

        User assignedTo = userRepository.findById(assignedToUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Assigned user not found with ID: "
                                        + assignedToUserId
                        )
                );

        ticket.setAssignedTo(assignedTo);

        return toResponseDto(
                supportTicketRepository.save(ticket)
        );
    }

    public SupportTicketResponseDto updateStatus(
            Long ticketId,
            String status
    ) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket status is required."
            );
        }

        SupportTicket ticket = getEntityById(ticketId);
        ticket.setStatus(status);

        if ("RESOLVED".equalsIgnoreCase(status)
                && ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(OffsetDateTime.now());
        }

        return toResponseDto(
                supportTicketRepository.save(ticket)
        );
    }

    public SupportTicketResponseDto resolveTicket(
            Long ticketId
    ) {
        SupportTicket ticket = getEntityById(ticketId);

        ticket.setStatus("RESOLVED");
        ticket.setResolvedAt(OffsetDateTime.now());

        return toResponseDto(
                supportTicketRepository.save(ticket)
        );
    }

    public SupportTicketResponseDto updatePriority(
            Long ticketId,
            String priority
    ) {
        if (priority == null || priority.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket priority is required."
            );
        }

        SupportTicket ticket = getEntityById(ticketId);
        ticket.setPriority(priority);

        return toResponseDto(
                supportTicketRepository.save(ticket)
        );
    }

    public SupportTicketResponseDto updateTicket(
            Long ticketId,
            SupportTicketRequestDto request
    ) {
        SupportTicket ticket = getEntityById(ticketId);

        if (request.getSubject() != null
                && !request.getSubject().isBlank()) {
            ticket.setSubject(request.getSubject());
        }

        if (request.getDescription() != null
                && !request.getDescription().isBlank()) {
            ticket.setDescription(request.getDescription());
        }

        if (request.getCategory() != null
                && !request.getCategory().isBlank()) {
            ticket.setCategory(request.getCategory());
        }

        if (request.getPriority() != null
                && !request.getPriority().isBlank()) {
            ticket.setPriority(request.getPriority());
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {
            ticket.setStatus(request.getStatus());

            if ("RESOLVED".equalsIgnoreCase(request.getStatus())
                    && ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(OffsetDateTime.now());
            }
        }

        return toResponseDto(
                supportTicketRepository.save(ticket)
        );
    }

    public void deleteTicket(Long id) {
        SupportTicket ticket = getEntityById(id);
        supportTicketRepository.delete(ticket);
    }

    private SupportTicket getEntityById(Long id) {
        return supportTicketRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Support ticket not found with ID: " + id
                        )
                );
    }

    private SupportTicketResponseDto toResponseDto(
            SupportTicket ticket
    ) {
        return new SupportTicketResponseDto(
                ticket.getId(),
                ticket.getUser() != null
                        ? ticket.getUser().getId()
                        : null,
                ticket.getTicketNumber(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getAssignedTo() != null
                        ? ticket.getAssignedTo().getId()
                        : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt()
        );
    }
}
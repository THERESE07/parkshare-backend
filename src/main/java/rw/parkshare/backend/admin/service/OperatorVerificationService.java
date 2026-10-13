package rw.parkshare.backend.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.admin.entity.OperatorVerification;
import rw.parkshare.backend.admin.repository.OperatorVerificationRepository;
import rw.parkshare.backend.common.dto.OperatorVerificationRequestDto;
import rw.parkshare.backend.common.dto.OperatorVerificationResponseDto;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class OperatorVerificationService {

    private final OperatorVerificationRepository operatorVerificationRepository;
    private final UserRepository userRepository;

    public OperatorVerificationService(
            OperatorVerificationRepository operatorVerificationRepository,
            UserRepository userRepository
    ) {
        this.operatorVerificationRepository =
                operatorVerificationRepository;
        this.userRepository = userRepository;
    }

    public OperatorVerificationResponseDto createOperatorVerification(
            Long userId,
            OperatorVerificationRequestDto request
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }

        if (operatorVerificationRepository
                .existsByUser_Id(userId)) {
            throw new IllegalArgumentException(
                    "An operator verification already exists for "
                            + "user ID: " + userId
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + userId
                        )
                );

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required."
            );
        }

        String businessRegistrationNumber =
                request.getBusinessRegistrationNumber();

        if (businessRegistrationNumber != null
                && !businessRegistrationNumber.isBlank()
                && operatorVerificationRepository
                .existsByBusinessRegistrationNumber(
                        businessRegistrationNumber
                )) {
            throw new IllegalArgumentException(
                    "Business registration number already exists: "
                            + businessRegistrationNumber
            );
        }

        OperatorVerification verification =
                new OperatorVerification();

        verification.setUser(user);

        verification.setBusinessName(
                request.getBusinessName()
        );

        verification.setBusinessRegistrationNumber(
                businessRegistrationNumber
        );

        String verificationStatus =
                request.getVerificationStatus();

        if (verificationStatus == null
                || verificationStatus.isBlank()) {
            verificationStatus = "PENDING";
        }

        verification.setVerificationStatus(
                verificationStatus
        );

        verification.setNotes(
                request.getNotes()
        );

        return toResponseDto(
                operatorVerificationRepository.save(
                        verification
                )
        );
    }

    @Transactional(readOnly = true)
    public OperatorVerificationResponseDto
    getOperatorVerificationById(Long id) {

        return toResponseDto(
                operatorVerificationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operator verification not found "
                                                + "with ID: " + id
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public OperatorVerificationResponseDto
    getOperatorVerificationByUserId(Long userId) {

        return toResponseDto(
                operatorVerificationRepository
                        .findByUser_Id(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operator verification not found "
                                                + "for user ID: " + userId
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<OperatorVerificationResponseDto>
    getAllOperatorVerifications() {

        return operatorVerificationRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OperatorVerificationResponseDto>
    getOperatorVerificationsByReviewer(Long userId) {

        return operatorVerificationRepository
                .findByReviewedBy_Id(userId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OperatorVerificationResponseDto>
    getOperatorVerificationsByStatus(
            String verificationStatus
    ) {

        return operatorVerificationRepository
                .findByVerificationStatus(
                        verificationStatus
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OperatorVerificationResponseDto>
    searchByBusinessName(String businessName) {

        return operatorVerificationRepository
                .findByBusinessNameContainingIgnoreCase(
                        businessName
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperatorVerificationResponseDto
    getByBusinessRegistrationNumber(
            String businessRegistrationNumber
    ) {

        return toResponseDto(
                operatorVerificationRepository
                        .findByBusinessRegistrationNumber(
                                businessRegistrationNumber
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operator verification not found "
                                                + "for business registration "
                                                + "number: "
                                                + businessRegistrationNumber
                                )
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<OperatorVerificationResponseDto>
    getOperatorVerificationsSubmittedBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return operatorVerificationRepository
                .findBySubmittedAtBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OperatorVerificationResponseDto>
    getOperatorVerificationsReviewedBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return operatorVerificationRepository
                .findByReviewedAtBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OperatorVerificationResponseDto>
    getOperatorVerificationsByStatusAndSubmittedBetween(
            String verificationStatus,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return operatorVerificationRepository
                .findByVerificationStatusAndSubmittedAtBetween(
                        verificationStatus,
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    public OperatorVerificationResponseDto
    updateOperatorVerification(
            Long id,
            OperatorVerificationRequestDto request
    ) {

        OperatorVerification verification =
                operatorVerificationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operator verification not found "
                                                + "with ID: " + id
                                )
                        );

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request body is required."
            );
        }

        String businessRegistrationNumber =
                request.getBusinessRegistrationNumber();

        if (businessRegistrationNumber != null
                && !businessRegistrationNumber.isBlank()
                && !businessRegistrationNumber.equals(
                verification
                        .getBusinessRegistrationNumber()
        )
                && operatorVerificationRepository
                .existsByBusinessRegistrationNumber(
                        businessRegistrationNumber
                )) {
            throw new IllegalArgumentException(
                    "Business registration number already exists: "
                            + businessRegistrationNumber
            );
        }

        if (request.getBusinessName() != null) {
            verification.setBusinessName(
                    request.getBusinessName()
            );
        }

        if (businessRegistrationNumber != null) {
            verification.setBusinessRegistrationNumber(
                    businessRegistrationNumber
            );
        }

        if (request.getVerificationStatus() != null
                && !request.getVerificationStatus().isBlank()) {
            verification.setVerificationStatus(
                    request.getVerificationStatus()
            );
        }

        if (request.getRejectionReason() != null) {
            verification.setRejectionReason(
                    request.getRejectionReason()
            );
        }

        if (request.getNotes() != null) {
            verification.setNotes(
                    request.getNotes()
            );
        }

        return toResponseDto(
                operatorVerificationRepository.save(
                        verification
                )
        );
    }

    public OperatorVerificationResponseDto
    reviewOperatorVerification(
            Long id,
            Long reviewerId,
            OperatorVerificationRequestDto request
    ) {

        OperatorVerification verification =
                operatorVerificationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operator verification not found "
                                                + "with ID: " + id
                                )
                        );

        if (reviewerId == null) {
            throw new IllegalArgumentException(
                    "Reviewer ID is required."
            );
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reviewer not found with ID: "
                                        + reviewerId
                        )
                );

        if (request == null
                || request.getVerificationStatus() == null
                || request.getVerificationStatus().isBlank()) {
            throw new IllegalArgumentException(
                    "Verification status is required."
            );
        }

        verification.setVerificationStatus(
                request.getVerificationStatus()
        );

        verification.setReviewedBy(reviewer);

        verification.setReviewedAt(
                OffsetDateTime.now()
        );

        if (request.getRejectionReason() != null) {
            verification.setRejectionReason(
                    request.getRejectionReason()
            );
        }

        if (request.getNotes() != null) {
            verification.setNotes(
                    request.getNotes()
            );
        }

        return toResponseDto(
                operatorVerificationRepository.save(
                        verification
                )
        );
    }

    public void deleteOperatorVerification(Long id) {

        OperatorVerification verification =
                operatorVerificationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Operator verification not found "
                                                + "with ID: " + id
                                )
                        );

        operatorVerificationRepository.delete(
                verification
        );
    }

    private OperatorVerificationResponseDto
    toResponseDto(OperatorVerification verification) {

        Long userId = verification.getUser() != null
                ? verification.getUser().getId()
                : null;

        Long reviewedById = verification.getReviewedBy() != null
                ? verification.getReviewedBy().getId()
                : null;

        return new OperatorVerificationResponseDto(
                verification.getId(),
                userId,
                verification.getBusinessName(),
                verification.getBusinessRegistrationNumber(),
                verification.getVerificationStatus(),
                verification.getSubmittedAt(),
                reviewedById,
                verification.getReviewedAt(),
                verification.getRejectionReason(),
                verification.getNotes()
        );
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
}
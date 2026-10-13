package rw.parkshare.backend.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.admin.entity.OperatorVerification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperatorVerificationRepository
        extends JpaRepository<OperatorVerification, Long> {

    Optional<OperatorVerification> findByUser_Id(Long userId);

    List<OperatorVerification> findByReviewedBy_Id(Long userId);

    List<OperatorVerification> findByVerificationStatus(String verificationStatus);

    List<OperatorVerification> findByBusinessNameContainingIgnoreCase(
            String businessName
    );

    Optional<OperatorVerification> findByBusinessRegistrationNumber(
            String businessRegistrationNumber
    );

    List<OperatorVerification> findBySubmittedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<OperatorVerification> findByReviewedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<OperatorVerification> findByVerificationStatusAndSubmittedAtBetween(
            String verificationStatus,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    boolean existsByUser_Id(Long userId);

    boolean existsByBusinessRegistrationNumber(
            String businessRegistrationNumber
    );
}

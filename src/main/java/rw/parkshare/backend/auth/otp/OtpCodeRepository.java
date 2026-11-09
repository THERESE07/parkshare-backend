package rw.parkshare.backend.auth.otp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findFirstByDestinationAndPurposeOrderByCreatedAtDesc(String destination, OtpCode.Purpose purpose);

    long countByDestinationAndPurposeAndCreatedAtAfter(String destination, OtpCode.Purpose purpose, OffsetDateTime after);
}

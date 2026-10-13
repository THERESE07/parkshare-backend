package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingVerification;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingVerificationRepository extends JpaRepository<ParkingVerification, Long> {

    List<ParkingVerification> findByParkingLot_Id(Long parkingLotId);

    List<ParkingVerification> findByParkingLot_IdAndVerificationStatus(
            Long parkingLotId,
            String verificationStatus
    );

    List<ParkingVerification> findByVerificationStatus(String verificationStatus);

    List<ParkingVerification> findByVerifiedBy_Id(Long verifiedById);

    Optional<ParkingVerification> findTopByParkingLot_IdOrderByCreatedAtDesc(
            Long parkingLotId
    );
}

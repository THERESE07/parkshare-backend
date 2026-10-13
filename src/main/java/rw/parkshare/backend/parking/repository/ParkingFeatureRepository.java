package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingFeature;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingFeatureRepository extends JpaRepository<ParkingFeature, Long> {

    List<ParkingFeature> findByParkingLot_Id(Long parkingLotId);

    Optional<ParkingFeature> findByParkingLot_IdAndFeatureCode(
            Long parkingLotId,
            String featureCode
    );

    List<ParkingFeature> findByFeatureCode(String featureCode);

    boolean existsByParkingLot_IdAndFeatureCode(
            Long parkingLotId,
            String featureCode
    );
}
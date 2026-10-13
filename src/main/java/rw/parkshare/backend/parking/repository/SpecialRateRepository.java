package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.SpecialRate;

import java.util.List;

@Repository
public interface SpecialRateRepository extends JpaRepository<SpecialRate, Long> {

    List<SpecialRate> findByParkingLot_Id(Long parkingLotId);

    List<SpecialRate> findByParkingLot_IdAndActiveTrue(Long parkingLotId);

    List<SpecialRate> findByRateType(String rateType);

    List<SpecialRate> findByActiveTrue();

    List<SpecialRate> findByParkingLot_IdAndRateType(
            Long parkingLotId,
            String rateType
    );
}
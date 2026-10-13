package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.PricingRule;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    List<PricingRule> findByParkingLot_Id(Long parkingLotId);

    List<PricingRule> findByParkingLot_IdAndActiveTrue(Long parkingLotId);

    List<PricingRule> findByParkingLot_IdAndPricingType(
            Long parkingLotId,
            String pricingType
    );

    List<PricingRule> findByActiveTrue();

    Optional<PricingRule> findByParkingLot_IdAndName(
            Long parkingLotId,
            String name
    );

    List<PricingRule> findByCurrency(String currency);

    List<PricingRule> findByParkingLot_IdAndCurrency(
            Long parkingLotId,
            String currency
    );
}

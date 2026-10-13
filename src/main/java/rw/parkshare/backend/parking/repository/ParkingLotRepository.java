package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingLot;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    List<ParkingLot> findByOperator_Id(Long operatorId);

    List<ParkingLot> findByStatus(String status);

    List<ParkingLot> findByCityIgnoreCase(String city);

    List<ParkingLot> findByOperator_IdAndStatus(Long operatorId, String status);

    List<ParkingLot> findByNameContainingIgnoreCase(String name);
}
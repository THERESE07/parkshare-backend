package rw.parkshare.backend.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.parking.entity.ParkingLotStaff;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingLotStaffRepository extends JpaRepository<ParkingLotStaff, Long> {

    List<ParkingLotStaff> findByParkingLot_Id(Long parkingLotId);

    List<ParkingLotStaff> findByUser_Id(Long userId);

    List<ParkingLotStaff> findByParkingLot_IdAndActiveTrue(Long parkingLotId);

    List<ParkingLotStaff> findByUser_IdAndActiveTrue(Long userId);

    Optional<ParkingLotStaff> findByParkingLot_IdAndUser_Id(
            Long parkingLotId,
            Long userId
    );

    boolean existsByParkingLot_IdAndUser_IdAndActiveTrue(
            Long parkingLotId,
            Long userId
    );
}

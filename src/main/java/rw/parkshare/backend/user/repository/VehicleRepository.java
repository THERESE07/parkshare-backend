package rw.parkshare.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.parkshare.backend.user.entity.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);

    List<Vehicle> findByUserId(Long userId);
}
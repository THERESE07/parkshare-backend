package rw.parkshare.backend.checkin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rw.parkshare.backend.checkin.entity.OccupancyRecord;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface OccupancyRecordRepository extends JpaRepository<OccupancyRecord, Long> {

    List<OccupancyRecord> findByParkingLot_Id(Long parkingLotId);

    List<OccupancyRecord> findByParkingZone_Id(Long parkingZoneId);

    List<OccupancyRecord> findByParkingLot_IdAndParkingZone_Id(
            Long parkingLotId,
            Long parkingZoneId
    );

    List<OccupancyRecord> findBySource(String source);

    List<OccupancyRecord> findByParkingLot_IdAndSource(
            Long parkingLotId,
            String source
    );

    List<OccupancyRecord> findByRecordedAtBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<OccupancyRecord> findByParkingLot_IdAndRecordedAtBetween(
            Long parkingLotId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<OccupancyRecord> findByParkingZone_IdAndRecordedAtBetween(
            Long parkingZoneId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    List<OccupancyRecord> findTopByParkingLot_IdOrderByRecordedAtDesc(
            Long parkingLotId
    );

    List<OccupancyRecord> findTopByParkingZone_IdOrderByRecordedAtDesc(
            Long parkingZoneId
    );
}
package rw.parkshare.backend.reservation.service;

import rw.parkshare.backend.common.dto.PageResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.dto.ReservationRequestDto;
import rw.parkshare.backend.common.dto.ReservationResponseDto;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.config.ParkShareProperties;
import rw.parkshare.backend.notification.service.Notifier;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.entity.ParkingOperatingHours;
import rw.parkshare.backend.parking.entity.ParkingSpace;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingOperatingHoursRepository;
import rw.parkshare.backend.parking.repository.ParkingSpaceRepository;
import rw.parkshare.backend.payment.service.PaymentFlowService;
import rw.parkshare.backend.pricing.PriceQuote;
import rw.parkshare.backend.pricing.PricingService;
import rw.parkshare.backend.reservation.entity.Reservation;
import rw.parkshare.backend.reservation.entity.ReservationStatus;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.security.AccessGuard;
import rw.parkshare.backend.security.CurrentUser;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.entity.Vehicle;
import rw.parkshare.backend.user.repository.UserRepository;
import rw.parkshare.backend.user.repository.VehicleRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    /** Allow for small clock differences between phone and server. */
    private static final int START_CLOCK_SKEW_MINUTES = 5;

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ParkingOperatingHoursRepository operatingHoursRepository;
    private final PricingService pricingService;
    private final ReservationLifecycle lifecycle;
    private final PaymentFlowService paymentFlowService;
    private final QrTokenService qrTokenService;
    private final Notifier notifier;
    private final AccessGuard guard;
    private final ParkShareProperties properties;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            ParkingLotRepository parkingLotRepository,
            ParkingSpaceRepository parkingSpaceRepository,
            ParkingOperatingHoursRepository operatingHoursRepository,
            PricingService pricingService,
            ReservationLifecycle lifecycle,
            PaymentFlowService paymentFlowService,
            QrTokenService qrTokenService,
            Notifier notifier,
            AccessGuard guard,
            ParkShareProperties properties
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSpaceRepository = parkingSpaceRepository;
        this.operatingHoursRepository = operatingHoursRepository;
        this.pricingService = pricingService;
        this.lifecycle = lifecycle;
        this.paymentFlowService = paymentFlowService;
        this.qrTokenService = qrTokenService;
        this.notifier = notifier;
        this.guard = guard;
        this.properties = properties;
    }

    /**
     * Books a space. The driver is taken from the token (admins may book for
     * someone else); the code, price and status are decided by the server.
     * If no space is chosen, the first free suitable space is assigned.
     */
    public ReservationResponseDto createReservation(
            Long requestedUserId,
            Long vehicleId,
            Long parkingLotId,
            Long parkingSpaceId,
            ReservationRequestDto request
    ) {
        if (request == null) {
            throw ApiException.badRequest("Reservation request is required");
        }
        CurrentUser caller = guard.current();
        Long userId = caller.isAdmin() && requestedUserId != null ? requestedUserId : caller.id();

        OffsetDateTime start = request.getStartTime();
        OffsetDateTime end = request.getEndTime();
        validateWindow(start, end);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found: " + userId));
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw ApiException.forbidden("Your account is not active");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> ApiException.notFound("Vehicle not found: " + vehicleId));
        if (vehicle.getUser() == null || !vehicle.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("Vehicle does not belong to you");
        }
        if (Boolean.FALSE.equals(vehicle.getIsActive())) {
            throw ApiException.badRequest("Vehicle is not active");
        }

        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> ApiException.notFound("Parking lot not found: " + parkingLotId));
        if (!"ACTIVE".equalsIgnoreCase(parkingLot.getStatus())) {
            throw ApiException.conflict("This parking lot is not accepting reservations");
        }
        validateOperatingHours(parkingLotId, start, end);

        ParkingSpace parkingSpace = parkingSpaceId != null
                ? lockRequestedSpace(parkingSpaceId, parkingLotId, vehicle, start, end)
                : assignSpace(parkingLot, vehicle, start, end);

        PriceQuote quote = pricingService.quote(parkingLotId, start, end);

        Reservation reservation = new Reservation();
        reservation.setReservationCode(generateCode());
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setTotalAmount(quote.total());
        reservation.setVatAmount(quote.vatAmount());
        reservation.setPlatformFee(quote.platformCommission());
        reservation.setCurrency(quote.currency());
        reservation.setNotes(request.getNotes());
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setParkingLot(parkingLot);
        reservation.setParkingSpace(parkingSpace);

        Reservation saved = reservationRepository.saveAndFlush(reservation);
        User actor = caller.id().equals(userId) ? user : userRepository.findById(caller.id()).orElse(null);
        lifecycle.recordCreated(saved, actor);

        notifier.inApp(user, "RESERVATION_CREATED", "Reservation created",
                "Reservation " + saved.getReservationCode() + " at " + parkingLot.getName() + " costs "
                        + quote.total().toBigInteger() + " RWF (VAT incl.). Pay within "
                        + properties.getReservation().getPaymentHoldMinutes() + " minutes to keep your space.",
                "RESERVATION", saved.getId());

        return toResponseDto(saved);
    }

    private void validateWindow(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) {
            throw ApiException.badRequest("Start time and end time are required");
        }
        if (!end.isAfter(start)) {
            throw ApiException.badRequest("End time must be after start time");
        }
        ParkShareProperties.Reservation rules = properties.getReservation();
        OffsetDateTime now = OffsetDateTime.now();
        if (start.isBefore(now.minusMinutes(START_CLOCK_SKEW_MINUTES))) {
            throw ApiException.badRequest("Start time cannot be in the past");
        }
        if (start.isAfter(now.plusDays(rules.getMaxAdvanceBookingDays()))) {
            throw ApiException.badRequest("Reservations can be made at most "
                    + rules.getMaxAdvanceBookingDays() + " days ahead");
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes < rules.getMinDurationMinutes()) {
            throw ApiException.badRequest("Minimum reservation is " + rules.getMinDurationMinutes() + " minutes");
        }
        if (minutes > rules.getMaxDurationMinutes()) {
            throw ApiException.badRequest("Maximum reservation is " + (rules.getMaxDurationMinutes() / 60) + " hours");
        }
    }

    /**
     * If the lot has published opening hours, the stay must fit inside them.
     * A day is "open all day" when it opens at 00:00 with no closing time.
     * Lots without any hours configured are treated as open 24/7.
     */
    private void validateOperatingHours(Long lotId, OffsetDateTime start, OffsetDateTime end) {
        if (operatingHoursRepository.findByParkingLot_Id(lotId).isEmpty()) {
            return;
        }
        ZoneId zone = properties.zoneId();
        ZonedDateTime localStart = start.atZoneSameInstant(zone);
        ZonedDateTime localEnd = end.atZoneSameInstant(zone);
        // An end at exactly midnight belongs to the previous day.
        LocalDate lastDay = localEnd.toLocalTime().equals(LocalTime.MIDNIGHT)
                ? localEnd.toLocalDate().minusDays(1)
                : localEnd.toLocalDate();

        for (LocalDate day = localStart.toLocalDate(); !day.isAfter(lastDay); day = day.plusDays(1)) {
            Optional<ParkingOperatingHours> hours =
                    operatingHoursRepository.findByParkingLot_IdAndDayOfWeek(lotId, day.getDayOfWeek().name());
            if (hours.isEmpty()) {
                continue;
            }
            ParkingOperatingHours h = hours.get();
            if (h.isClosed()) {
                throw ApiException.badRequest("The lot is closed on " + day.getDayOfWeek());
            }
            LocalTime opens = h.getOpeningTime() == null ? LocalTime.MIDNIGHT : h.getOpeningTime();
            LocalTime closes = h.getClosingTime();
            LocalTime from = day.equals(localStart.toLocalDate()) ? localStart.toLocalTime() : LocalTime.MIDNIGHT;
            boolean endsToday = day.equals(lastDay);
            if (from.isBefore(opens)) {
                throw ApiException.badRequest("The lot opens at " + opens + " on " + day.getDayOfWeek());
            }
            if (closes != null) {
                LocalTime until = endsToday && !localEnd.toLocalTime().equals(LocalTime.MIDNIGHT)
                        ? localEnd.toLocalTime() : null;
                if (until == null || until.isAfter(closes)) {
                    throw ApiException.badRequest("The lot closes at " + closes + " on " + day.getDayOfWeek());
                }
            }
        }
    }

    private ParkingSpace lockRequestedSpace(Long spaceId, Long lotId, Vehicle vehicle,
                                            OffsetDateTime start, OffsetDateTime end) {
        ParkingSpace space = parkingSpaceRepository.findByIdForUpdate(spaceId)
                .orElseThrow(() -> ApiException.notFound("Parking space not found: " + spaceId));
        if (!space.getParkingZone().getParkingLot().getId().equals(lotId)) {
            throw ApiException.badRequest("Parking space does not belong to the specified parking lot");
        }
        if ("MAINTENANCE".equals(space.getStatus()) || "BLOCKED".equals(space.getStatus())) {
            throw ApiException.conflict("This parking space is not available");
        }
        if (!fits(space, vehicle)) {
            throw ApiException.badRequest("This space is not suitable for a " + vehicle.getVehicleType());
        }
        if (reservationRepository.existsOverlappingOnSpace(spaceId, start, end, ReservationStatus.ACTIVE, -1L)) {
            throw ApiException.conflict("Parking space is already reserved for the selected time");
        }
        return space;
    }

    private ParkingSpace assignSpace(ParkingLot lot, Vehicle vehicle, OffsetDateTime start, OffsetDateTime end) {
        if (parkingSpaceRepository.countByParkingZone_ParkingLot_Id(lot.getId()) == 0) {
            // Lot without individual spaces: limit by total capacity.
            long overlapping = reservationRepository.countOverlappingInLot(lot.getId(), start, end, ReservationStatus.ACTIVE);
            if (lot.getTotalCapacity() == null || overlapping >= lot.getTotalCapacity()) {
                throw ApiException.conflict("The parking lot is full for the selected time");
            }
            return null;
        }
        List<ParkingSpace> candidates = parkingSpaceRepository
                .findFreeSpaces(lot.getId(), start, end, ReservationStatus.ACTIVE).stream()
                .filter(space -> fits(space, vehicle))
                // Do not auto-assign accessible or VIP bays; drivers must pick them on purpose.
                .filter(space -> !"DISABLED".equals(space.getSpaceType()) && !"VIP".equals(space.getSpaceType()))
                .sorted(Comparator.comparingInt((ParkingSpace space) -> preference(space, vehicle))
                        .thenComparing(ParkingSpace::getId))
                .toList();
        for (ParkingSpace candidate : candidates) {
            ParkingSpace locked = parkingSpaceRepository.findByIdForUpdate(candidate.getId()).orElse(null);
            if (locked != null && !reservationRepository.existsOverlappingOnSpace(
                    locked.getId(), start, end, ReservationStatus.ACTIVE, -1L)) {
                return locked;
            }
        }
        throw ApiException.conflict("No suitable space is free for the selected time");
    }

    private static boolean fits(ParkingSpace space, Vehicle vehicle) {
        boolean motorcycle = "MOTORCYCLE".equals(vehicle.getVehicleType());
        boolean motorcycleSpace = "MOTORCYCLE".equals(space.getSpaceType());
        return motorcycle || !motorcycleSpace;
    }

    private static int preference(ParkingSpace space, Vehicle vehicle) {
        boolean motorcycle = "MOTORCYCLE".equals(vehicle.getVehicleType());
        if (motorcycle) {
            return "MOTORCYCLE".equals(space.getSpaceType()) ? 0 : 1;
        }
        return "STANDARD".equals(space.getSpaceType()) ? 0 : 1;
    }

    private String generateCode() {
        String date = OffsetDateTime.now(properties.zoneId()).format(DateTimeFormatter.ofPattern("yyMMdd"));
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder code = new StringBuilder("PS-").append(date).append('-');
            for (int i = 0; i < 6; i++) {
                code.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
            }
            if (!reservationRepository.existsByReservationCode(code.toString())) {
                return code.toString();
            }
        }
        throw new IllegalStateException("Could not generate a unique reservation code");
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationById(Long id) {
        return toResponseDto(getReservationEntityById(id));
    }

    @Transactional(readOnly = true)
    public ReservationResponseDto getReservationByCode(
            String reservationCode
    ) {
        Reservation reservation =
                reservationRepository.findByReservationCode(reservationCode)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Reservation not found with code: "
                                                + reservationCode
                                ));

        return toResponseDto(reservation);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationResponseDto> getAllReservations(int page, int size) {
        return PageResponse.of(
                reservationRepository.findAll(PageResponse.request(page, size, "createdAt")),
                this::toResponseDto
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ReservationResponseDto> getReservationsByUserId(
            Long userId, int page, int size
    ) {
        return PageResponse.of(
                reservationRepository.findByUser_Id(userId, PageResponse.request(page, size, "startTime")),
                this::toResponseDto
        );
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByVehicleId(
            Long vehicleId
    ) {
        return reservationRepository.findByVehicle_Id(vehicleId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByParkingLotId(
            Long parkingLotId
    ) {
        return reservationRepository.findByParkingLot_Id(parkingLotId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByParkingSpaceId(
            Long parkingSpaceId
    ) {
        return reservationRepository.findByParkingSpace_Id(parkingSpaceId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByStatus(
            ReservationStatus status
    ) {
        return reservationRepository.findByStatus(status)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByUserAndStatus(
            Long userId,
            ReservationStatus status
    ) {
        return reservationRepository.findByUser_IdAndStatus(
                        userId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsByParkingLotAndStatus(
            Long parkingLotId,
            ReservationStatus status
    ) {
        return reservationRepository
                .findByParkingLot_IdAndStatus(
                        parkingLotId,
                        status
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservationsBetween(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return reservationRepository.findByStartTimeBetween(
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getParkingLotReservationsBetween(
            Long parkingLotId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        validateDateRange(startTime, endTime);

        return reservationRepository
                .findByParkingLot_IdAndStartTimeBetween(
                        parkingLotId,
                        startTime,
                        endTime
                )
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private void validateDateRange(
            OffsetDateTime startTime,
            OffsetDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start time and end time are required"
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }
    }

    /**
     * Admin correction of a reservation's time window. The price is
     * recalculated; amounts and statuses in the request are ignored.
     */
    public ReservationResponseDto updateReservation(Long id, ReservationRequestDto request) {
        if (request == null) {
            throw ApiException.badRequest("Reservation request is required");
        }
        Reservation reservation = getReservationEntityById(id);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw ApiException.conflict("Only unpaid reservations can be rescheduled");
        }
        OffsetDateTime start = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
        OffsetDateTime end = request.getEndTime() != null ? request.getEndTime() : reservation.getEndTime();
        validateWindow(start, end);
        if (reservation.getParkingSpace() != null) {
            parkingSpaceRepository.findByIdForUpdate(reservation.getParkingSpace().getId());
            if (reservationRepository.existsOverlappingOnSpace(reservation.getParkingSpace().getId(),
                    start, end, ReservationStatus.ACTIVE, reservation.getId())) {
                throw ApiException.conflict("Parking space is already reserved for the selected time");
            }
        }
        PriceQuote quote = pricingService.quote(reservation.getParkingLot().getId(), start, end);
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setTotalAmount(quote.total());
        reservation.setVatAmount(quote.vatAmount());
        reservation.setPlatformFee(quote.platformCommission());
        if (request.getNotes() != null) {
            reservation.setNotes(request.getNotes());
        }
        return toResponseDto(reservationRepository.save(reservation));
    }

    public ReservationResponseDto updateNotes(Long id, String notes) {
        Reservation reservation = getReservationEntityById(id);
        guard.requireSelfOrAdmin(reservation.getUser().getId());
        reservation.setNotes(notes);
        return toResponseDto(reservationRepository.save(reservation));
    }

    /** Admin override; still limited to valid lifecycle transitions. */
    public ReservationResponseDto updateReservationStatus(Long id, ReservationStatus status) {
        if (status == null) {
            throw ApiException.badRequest("Status is required");
        }
        Reservation reservation = getReservationEntityById(id);
        lifecycle.transition(reservation, status, currentUserEntity(), "Changed by administrator");
        return toResponseDto(reservation);
    }

    /**
     * Cancels a reservation. Drivers get a full refund up to the free
     * cancellation window before start, a partial refund after that, and
     * none once the stay has started. Cancellation by the lot is always
     * refunded in full.
     */
    public CancellationResult cancelReservation(Long id) {
        Reservation reservation = getReservationEntityById(id);
        guard.requireReservationAccess(reservation);
        CurrentUser caller = guard.current();
        boolean byDriver = reservation.getUser().getId().equals(caller.id());
        boolean byLot = !byDriver;

        if (reservation.getStatus() != ReservationStatus.PENDING
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw ApiException.conflict("A " + reservation.getStatus() + " reservation cannot be cancelled");
        }

        BigDecimal refund = paymentFlowService.refundForCancellation(reservation, byLot);
        lifecycle.transition(reservation, ReservationStatus.CANCELLED, currentUserEntity(),
                byDriver ? "Cancelled by driver" : "Cancelled by parking lot");

        notifier.inAppAndSms(reservation.getUser(), "RESERVATION_CANCELLED", "Reservation cancelled",
                "Reservation " + reservation.getReservationCode() + " was cancelled."
                        + (refund.signum() > 0 ? " Refund: " + refund.toBigInteger() + " RWF." : ""),
                "RESERVATION", reservation.getId());

        return new CancellationResult(toResponseDto(reservation), refund);
    }

    /** The QR token for the app to display. Only the driver (or an admin) can fetch it. */
    @Transactional(readOnly = true)
    public QrTokenService.CurrentQr getQrCode(Long id) {
        Reservation reservation = getReservationEntityById(id);
        guard.requireSelfOrAdmin(reservation.getUser().getId());
        if (reservation.getStatus() != ReservationStatus.CONFIRMED
                && reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw ApiException.conflict("A QR code is available once the reservation is paid");
        }
        return qrTokenService.current(reservation)
                .orElseThrow(() -> ApiException.notFound("No active QR code for this reservation"));
    }

    @Transactional(readOnly = true)
    public boolean reservationCodeExists(String reservationCode) {
        return reservationRepository.existsByReservationCode(reservationCode);
    }

    /** Only reservations that never became active can be deleted; others are kept for records. */
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationEntityById(id);
        if (reservation.getStatus() != ReservationStatus.CANCELLED
                && reservation.getStatus() != ReservationStatus.EXPIRED) {
            throw ApiException.conflict("Only cancelled or expired reservations can be deleted");
        }
        reservationRepository.delete(reservation);
    }

    private User currentUserEntity() {
        return userRepository.findById(guard.current().id()).orElse(null);
    }

    private Reservation getReservationEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Reservation not found: " + id));
    }

    private ReservationResponseDto toResponseDto(Reservation reservation) {
        ReservationResponseDto dto = new ReservationResponseDto(
                reservation.getId(),
                reservation.getReservationCode(),
                reservation.getUser() != null ? reservation.getUser().getId() : null,
                reservation.getVehicle() != null ? reservation.getVehicle().getId() : null,
                reservation.getParkingLot() != null ? reservation.getParkingLot().getId() : null,
                reservation.getParkingSpace() != null ? reservation.getParkingSpace().getId() : null,
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getTotalAmount(),
                reservation.getCurrency(),
                reservation.getNotes()
        );
        dto.setVatAmount(reservation.getVatAmount());
        if (reservation.getStatus() == ReservationStatus.PENDING && reservation.getCreatedAt() != null) {
            dto.setPaymentDueBy(reservation.getCreatedAt()
                    .plusMinutes(properties.getReservation().getPaymentHoldMinutes()));
        }
        return dto;
    }

    public record CancellationResult(ReservationResponseDto reservation, BigDecimal refundAmount) {
    }
}

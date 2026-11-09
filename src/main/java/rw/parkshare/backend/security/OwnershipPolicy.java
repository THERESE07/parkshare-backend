package rw.parkshare.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.notification.repository.NotificationRepository;
import rw.parkshare.backend.parking.repository.ParkingFeatureRepository;
import rw.parkshare.backend.parking.repository.ParkingLotStaffRepository;
import rw.parkshare.backend.parking.repository.ParkingOperatingHoursRepository;
import rw.parkshare.backend.parking.repository.ParkingSpaceRepository;
import rw.parkshare.backend.parking.repository.ParkingZoneRepository;
import rw.parkshare.backend.parking.repository.PricingRuleRepository;
import rw.parkshare.backend.parking.repository.SpecialRateRepository;
import rw.parkshare.backend.payment.repository.PaymentRepository;
import rw.parkshare.backend.reservation.repository.ReservationRepository;
import rw.parkshare.backend.user.entity.Vehicle;
import rw.parkshare.backend.user.repository.VehicleRepository;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Ownership rules applied to every API request after SecurityConfig has
 * checked roles. Admins bypass these checks.
 *
 * - A user id in the path or query (userId, operatorId, organizerId) must be the caller.
 * - Reservations, their payments and their check-ins are visible to the driver who
 *   booked, the lot's operator/staff, and admins.
 * - Vehicles and notifications belong to their owner.
 * - Changing a lot or anything inside it (zones, spaces, prices, hours, features,
 *   staff) requires owning the lot; attendants may only update space status.
 */
@Component
@Transactional(readOnly = true)
public class OwnershipPolicy {

    private static final Set<String> USER_ID_PARAMS = Set.of("userId", "operatorId", "organizerId");
    private static final Set<String> LOT_RESOURCES = Set.of(
            "parking-lots", "parking-zones", "parking-spaces", "pricing-rules", "special-rates",
            "parking-operating-hours", "parking-features", "parking-lot-staff");
    private static final Pattern SPACE_STATUS_CHANGE =
            Pattern.compile("^/api/parking-spaces/\\d+/(status|available|occupied|reserved|maintenance)$");

    private final AccessGuard guard;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationRepository notificationRepository;
    private final ParkingZoneRepository zoneRepository;
    private final ParkingSpaceRepository spaceRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final SpecialRateRepository specialRateRepository;
    private final ParkingOperatingHoursRepository hoursRepository;
    private final ParkingFeatureRepository featureRepository;
    private final ParkingLotStaffRepository staffRepository;

    public OwnershipPolicy(
            AccessGuard guard,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            VehicleRepository vehicleRepository,
            NotificationRepository notificationRepository,
            ParkingZoneRepository zoneRepository,
            ParkingSpaceRepository spaceRepository,
            PricingRuleRepository pricingRuleRepository,
            SpecialRateRepository specialRateRepository,
            ParkingOperatingHoursRepository hoursRepository,
            ParkingFeatureRepository featureRepository,
            ParkingLotStaffRepository staffRepository
    ) {
        this.guard = guard;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.vehicleRepository = vehicleRepository;
        this.notificationRepository = notificationRepository;
        this.zoneRepository = zoneRepository;
        this.spaceRepository = spaceRepository;
        this.pricingRuleRepository = pricingRuleRepository;
        this.specialRateRepository = specialRateRepository;
        this.hoursRepository = hoursRepository;
        this.featureRepository = featureRepository;
        this.staffRepository = staffRepository;
    }

    public void check(HttpServletRequest request, Map<String, String> vars) {
        CurrentUser me = guard.current();
        if (me.isAdmin()) {
            return;
        }

        String path = request.getRequestURI().substring(request.getContextPath().length());
        String[] segments = path.split("/");
        if (segments.length < 3 || !"api".equals(segments[1])) {
            return;
        }
        String resource = segments[2];
        boolean read = "GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod());

        checkUserIds(request, vars, resource, me);
        checkReservationScope(request, vars, resource);
        checkPayment(vars, resource);
        checkVehicle(vars, resource, read, me);
        checkNotification(vars, resource, me);

        if (!read && LOT_RESOURCES.contains(resource)) {
            Long lotId = resolveLot(resource, vars);
            if (lotId != null) {
                if ("parking-spaces".equals(resource) && SPACE_STATUS_CHANGE.matcher(path).matches()) {
                    guard.requireLotManager(lotId);
                } else {
                    guard.requireLotOwner(lotId);
                }
            }
        }
    }

    private void checkUserIds(HttpServletRequest request, Map<String, String> vars, String resource, CurrentUser me) {
        // An operator assigning staff to their own lot names another user's id.
        if ("parking-lot-staff".equals(resource) && vars.containsKey("parkingLotId")) {
            return;
        }
        for (String name : USER_ID_PARAMS) {
            String value = vars.getOrDefault(name, request.getParameter(name));
            if (value != null && !value.equals(String.valueOf(me.id()))) {
                throw ApiException.forbidden("You can only act on your own account");
            }
        }
    }

    private void checkReservationScope(HttpServletRequest request, Map<String, String> vars, String resource) {
        String reservationId = vars.getOrDefault("reservationId", request.getParameter("reservationId"));
        if (reservationId != null) {
            reservationRepository.findById(parseId(reservationId)).ifPresent(guard::requireReservationAccess);
        }
        if (!"reservations".equals(resource)) {
            return;
        }
        if (vars.containsKey("id")) {
            reservationRepository.findById(parseId(vars.get("id"))).ifPresent(guard::requireReservationAccess);
        }
        if (vars.containsKey("reservationCode")) {
            reservationRepository.findByReservationCode(vars.get("reservationCode"))
                    .ifPresent(guard::requireReservationAccess);
        }
        if (vars.containsKey("parkingLotId")) {
            guard.requireLotManager(parseId(vars.get("parkingLotId")));
        }
        if (vars.containsKey("parkingSpaceId")) {
            spaceRepository.findById(parseId(vars.get("parkingSpaceId")))
                    .ifPresent(space -> guard.requireLotManager(space.getParkingZone().getParkingLot().getId()));
        }
        if (vars.containsKey("vehicleId")) {
            vehicleRepository.findById(parseId(vars.get("vehicleId"))).ifPresent(this::requireVehicleOwner);
        }
    }

    private void checkPayment(Map<String, String> vars, String resource) {
        if ("payments".equals(resource) && vars.containsKey("id")) {
            paymentRepository.findById(parseId(vars.get("id")))
                    .ifPresent(payment -> guard.requireReservationAccess(payment.getReservation()));
        }
    }

    private void checkVehicle(Map<String, String> vars, String resource, boolean read, CurrentUser me) {
        if (!"vehicles".equals(resource) || !vars.containsKey("id")) {
            return;
        }
        vehicleRepository.findById(parseId(vars.get("id"))).ifPresent(vehicle -> {
            boolean staffReading = read && (me.isOperator() || me.isAttendant());
            if (!staffReading) {
                requireVehicleOwner(vehicle);
            }
        });
    }

    private void checkNotification(Map<String, String> vars, String resource, CurrentUser me) {
        if ("notifications".equals(resource) && vars.containsKey("id")) {
            notificationRepository.findById(parseId(vars.get("id"))).ifPresent(notification -> {
                if (!notification.getUser().getId().equals(me.id())) {
                    throw ApiException.forbidden("This notification is not yours");
                }
            });
        }
    }

    private void requireVehicleOwner(Vehicle vehicle) {
        guard.requireSelfOrAdmin(vehicle.getUser().getId());
    }

    private Long resolveLot(String resource, Map<String, String> vars) {
        if (vars.containsKey("parkingLotId")) {
            return parseId(vars.get("parkingLotId"));
        }
        if ("parking-spaces".equals(resource) && vars.containsKey("parkingZoneId")) {
            return zoneRepository.findById(parseId(vars.get("parkingZoneId")))
                    .map(zone -> zone.getParkingLot().getId()).orElse(null);
        }
        if (!vars.containsKey("id")) {
            return null;
        }
        Long id = parseId(vars.get("id"));
        return switch (resource) {
            case "parking-lots" -> id;
            case "parking-zones" -> zoneRepository.findById(id).map(z -> z.getParkingLot().getId()).orElse(null);
            case "parking-spaces" -> spaceRepository.findById(id)
                    .map(s -> s.getParkingZone().getParkingLot().getId()).orElse(null);
            case "pricing-rules" -> pricingRuleRepository.findById(id).map(r -> r.getParkingLot().getId()).orElse(null);
            case "special-rates" -> specialRateRepository.findById(id).map(r -> r.getParkingLot().getId()).orElse(null);
            case "parking-operating-hours" -> hoursRepository.findById(id).map(h -> h.getParkingLot().getId()).orElse(null);
            case "parking-features" -> featureRepository.findById(id).map(f -> f.getParkingLot().getId()).orElse(null);
            case "parking-lot-staff" -> staffRepository.findById(id).map(s -> s.getParkingLot().getId()).orElse(null);
            default -> null;
        };
    }

    private static Long parseId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw ApiException.badRequest("Invalid id: " + value);
        }
    }
}

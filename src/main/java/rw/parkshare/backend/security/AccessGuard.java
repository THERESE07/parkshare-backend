package rw.parkshare.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.parking.entity.ParkingLot;
import rw.parkshare.backend.parking.repository.ParkingLotRepository;
import rw.parkshare.backend.parking.repository.ParkingLotStaffRepository;
import rw.parkshare.backend.reservation.entity.Reservation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Central place for "who is calling" and "may they touch this record" checks.
 * Role checks live in SecurityConfig; this class adds ownership checks so a
 * driver cannot read or change another driver's data by changing an ID.
 */
@Component
public class AccessGuard {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingLotStaffRepository parkingLotStaffRepository;

    public AccessGuard(
            ParkingLotRepository parkingLotRepository,
            ParkingLotStaffRepository parkingLotStaffRepository
    ) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingLotStaffRepository = parkingLotStaffRepository;
    }

    public CurrentUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw ApiException.unauthorized("Authentication is required");
        }
        Object userIdClaim = jwtAuth.getToken().getClaims().get("userId");
        Long userId = switch (userIdClaim) {
            case Number number -> number.longValue();
            case String text -> Long.valueOf(text);
            case null, default -> throw ApiException.unauthorized("Token has no user id");
        };
        List<String> roleClaim = jwtAuth.getToken().getClaimAsStringList("roles");
        Set<String> roles = new HashSet<>();
        if (roleClaim != null) {
            roleClaim.forEach(role -> roles.add(role.toUpperCase()));
        }
        return new CurrentUser(userId, jwtAuth.getToken().getSubject(), Set.copyOf(roles));
    }

    /** The caller must be this user, or an admin. */
    public void requireSelfOrAdmin(Long userId) {
        CurrentUser user = current();
        if (!user.isAdmin() && !user.id().equals(userId)) {
            throw ApiException.forbidden("You can only access your own data");
        }
    }

    public void requireAdmin() {
        if (!current().isAdmin()) {
            throw ApiException.forbidden("Administrator access is required");
        }
    }

    /** True if the caller is an admin, the lot's operator, or active staff at the lot. */
    public boolean canManageLot(Long parkingLotId) {
        CurrentUser user = current();
        if (user.isAdmin()) {
            return true;
        }
        ParkingLot lot = parkingLotRepository.findById(parkingLotId).orElse(null);
        if (lot == null) {
            return false;
        }
        if (lot.getOperator() != null && lot.getOperator().getId().equals(user.id())) {
            return true;
        }
        return parkingLotStaffRepository.existsByParkingLot_IdAndUser_IdAndActiveTrue(parkingLotId, user.id());
    }

    public void requireLotManager(Long parkingLotId) {
        if (!canManageLot(parkingLotId)) {
            throw ApiException.forbidden("You do not manage this parking lot");
        }
    }

    /** The lot's operator (owner) or an admin; staff are excluded. */
    public void requireLotOwner(Long parkingLotId) {
        CurrentUser user = current();
        if (user.isAdmin()) {
            return;
        }
        ParkingLot lot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> ApiException.notFound("Parking lot not found: " + parkingLotId));
        if (lot.getOperator() == null || !lot.getOperator().getId().equals(user.id())) {
            throw ApiException.forbidden("You do not own this parking lot");
        }
    }

    /** Driver who made it, staff/operator of the lot, or admin. */
    public void requireReservationAccess(Reservation reservation) {
        CurrentUser user = current();
        if (user.isAdmin()) {
            return;
        }
        if (reservation.getUser() != null && reservation.getUser().getId().equals(user.id())) {
            return;
        }
        if (reservation.getParkingLot() != null && canManageLot(reservation.getParkingLot().getId())) {
            return;
        }
        throw ApiException.forbidden("You do not have access to this reservation");
    }
}

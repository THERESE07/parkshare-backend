package rw.parkshare.backend.security;

import java.util.Set;

/**
 * The authenticated caller, taken from the verified JWT.
 */
public record CurrentUser(Long id, String email, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isOperator() {
        return hasRole("OPERATOR");
    }

    public boolean isAttendant() {
        return hasRole("ATTENDANT");
    }
}

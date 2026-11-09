package rw.parkshare.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.parkshare.backend.common.RwandaPhone;
import rw.parkshare.backend.common.exception.ApiException;
import rw.parkshare.backend.security.AccessGuard;
import rw.parkshare.backend.security.CurrentUser;
import rw.parkshare.backend.user.entity.User;
import rw.parkshare.backend.user.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

/**
 * The logged-in user's own account. Everything else under /api/users is admin-only.
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessGuard guard;

    public MeController(UserRepository userRepository, PasswordEncoder passwordEncoder, AccessGuard guard) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.guard = guard;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Profile> me() {
        CurrentUser me = guard.current();
        return ResponseEntity.ok(Profile.of(load(me.id()), me.roles()));
    }

    /** Body may contain firstName, lastName, phone. */
    @PatchMapping
    @Transactional
    public ResponseEntity<Profile> update(@RequestBody Map<String, String> body) {
        CurrentUser me = guard.current();
        User user = load(me.id());
        if (body.get("firstName") != null && !body.get("firstName").isBlank()) {
            user.setFirstName(body.get("firstName").trim());
        }
        if (body.get("lastName") != null && !body.get("lastName").isBlank()) {
            user.setLastName(body.get("lastName").trim());
        }
        if (body.get("phone") != null) {
            String phone = RwandaPhone.requireValid(body.get("phone"));
            if (!phone.equals(user.getPhone())) {
                if (userRepository.existsByPhone(phone)) {
                    throw ApiException.conflict("Phone number is already registered");
                }
                user.setPhone(phone);
                user.setPhoneVerifiedAt(null); // new number must be verified again
            }
        }
        user.setUpdatedAt(OffsetDateTime.now());
        return ResponseEntity.ok(Profile.of(userRepository.save(user), me.roles()));
    }

    /** Body: {"currentPassword": "...", "newPassword": "..."} */
    @PostMapping("/password")
    @Transactional
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> body) {
        User user = load(guard.current().id());
        String current = body.get("currentPassword");
        String next = body.get("newPassword");
        if (current == null || !passwordEncoder.matches(current, user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        if (next == null || next.length() < 8 || !next.matches(".*[A-Za-z].*") || !next.matches(".*\\d.*")) {
            throw ApiException.badRequest("New password needs at least 8 characters, including a letter and a number");
        }
        user.setPasswordHash(passwordEncoder.encode(next));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed"));
    }

    private User load(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    public record Profile(Long id, String firstName, String lastName, String email, String phone,
                          String status, Set<String> roles, boolean phoneVerified, boolean emailVerified) {
        static Profile of(User user, Set<String> roles) {
            return new Profile(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getPhone(), user.getStatus(), roles,
                    user.getPhoneVerifiedAt() != null, user.getEmailVerifiedAt() != null);
        }
    }
}

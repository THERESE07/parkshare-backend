package rw.parkshare.backend.auth.otp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import rw.parkshare.backend.security.AccessGuard;

import java.util.Map;

@RestController
public class AccountVerificationController {

    private final AccountVerificationService service;
    private final AccessGuard guard;

    public AccountVerificationController(AccountVerificationService service, AccessGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    /** Body: {"identifier": "0788123456" or "me@example.com"}. Same answer whether or not the account exists. */
    @PostMapping("/api/auth/password/forgot")
    public ResponseEntity<Map<String, String>> forgot(@RequestBody Map<String, String> body) {
        service.forgotPassword(body.get("identifier"));
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message", "If an account exists for this phone or email, a reset code has been sent."));
    }

    /** Body: {"identifier": "...", "code": "123456", "newPassword": "..."} */
    @PostMapping("/api/auth/password/reset")
    public ResponseEntity<Map<String, String>> reset(@RequestBody Map<String, String> body) {
        service.resetPassword(body.get("identifier"), body.get("code"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Password changed. You can now log in."));
    }

    @PostMapping("/api/me/verify/phone")
    public ResponseEntity<Map<String, String>> sendPhoneCode() {
        service.requestPhoneCode(guard.current().id());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Code sent by SMS"));
    }

    /** Body: {"code": "123456"} */
    @PostMapping("/api/me/verify/phone/confirm")
    public ResponseEntity<Map<String, String>> confirmPhone(@RequestBody Map<String, String> body) {
        service.confirmPhone(guard.current().id(), body.get("code"));
        return ResponseEntity.ok(Map.of("message", "Phone number verified"));
    }

    @PostMapping("/api/me/verify/email")
    public ResponseEntity<Map<String, String>> sendEmailCode() {
        service.requestEmailCode(guard.current().id());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Code sent by email"));
    }

    /** Body: {"code": "123456"} */
    @PostMapping("/api/me/verify/email/confirm")
    public ResponseEntity<Map<String, String>> confirmEmail(@RequestBody Map<String, String> body) {
        service.confirmEmail(guard.current().id(), body.get("code"));
        return ResponseEntity.ok(Map.of("message", "Email verified"));
    }
}

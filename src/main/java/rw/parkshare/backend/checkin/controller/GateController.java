package rw.parkshare.backend.checkin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import rw.parkshare.backend.checkin.service.GateService;
import rw.parkshare.backend.checkin.service.GateService.GateResult;

import java.util.Map;

/**
 * Endpoints for the attendant's scanner app.
 */
@RestController
public class GateController {

    private final GateService gateService;

    public GateController(GateService gateService) {
        this.gateService = gateService;
    }

    /** Body: {"token": "PS1...."} as read from the driver's QR code. */
    @PostMapping("/api/check-ins/scan")
    public ResponseEntity<GateResult> scan(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(gateService.checkInByQr(body.get("token")));
    }

    /** Body: {"reservationCode": "PS-261109-ABC123"} */
    @PostMapping("/api/check-ins/code")
    public ResponseEntity<GateResult> byCode(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(gateService.checkInByCode(body.get("reservationCode")));
    }

    @PostMapping("/api/check-outs/reservation/{reservationId}")
    public ResponseEntity<GateResult> checkOut(@PathVariable Long reservationId) {
        return ResponseEntity.ok(gateService.checkOut(reservationId));
    }
}

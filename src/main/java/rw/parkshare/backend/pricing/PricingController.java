package rw.parkshare.backend.pricing;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rw.parkshare.backend.security.AccessGuard;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;
    private final AccessGuard guard;

    public PricingController(PricingService pricingService, AccessGuard guard) {
        this.pricingService = pricingService;
        this.guard = guard;
    }

    /** The exact price a driver will pay, with VAT shown. No commission or payout details. */
    @GetMapping("/quote")
    public ResponseEntity<PriceQuote.DriverQuote> quote(
            @RequestParam Long parkingLotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endTime
    ) {
        return ResponseEntity.ok(pricingService.quote(parkingLotId, startTime, endTime).toDriverQuote());
    }

    /**
     * For the lot's operator: what drivers pay and what you earn for typical stays.
     * Pass hourlyRate / dailyCap to try new prices before saving them.
     */
    @GetMapping("/operator-preview")
    public ResponseEntity<OperatorPreview> operatorPreview(
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) BigDecimal hourlyRate,
            @RequestParam(required = false) BigDecimal dailyCap,
            @RequestParam(required = false) String paymentMethod
    ) {
        guard.requireLotOwner(parkingLotId);
        if (paymentMethod != null && !paymentMethod.matches("MTN_MOMO|AIRTEL_MONEY|CASH")) {
            throw new IllegalArgumentException("paymentMethod must be MTN_MOMO, AIRTEL_MONEY or CASH");
        }
        return ResponseEntity.ok(pricingService.operatorPreview(parkingLotId, hourlyRate, dailyCap, paymentMethod));
    }
}

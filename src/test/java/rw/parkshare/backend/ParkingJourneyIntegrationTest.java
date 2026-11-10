package rw.parkshare.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import rw.parkshare.backend.payment.service.PaymentCallbackService;
import rw.parkshare.backend.reservation.service.ReservationScheduler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Walks through ParkShare the way real users would, over HTTP, against a
 * real PostgreSQL: operator lists a lot, admin approves it, driver books,
 * pays with MoMo, checks in with the QR code and checks out.
 */
@SpringBootTest(properties = {
        "PARKSHARE_JWT_SECRET=test-secret-that-is-long-enough-for-hs256-signing",
        "parkshare.payments.webhook-secret=test-webhook-secret",
        "parkshare.security.admin-email=admin@parkshare.rw",
        "parkshare.security.admin-password=AdminPassword123",
        "parkshare.scheduler.initial-delay-ms=3600000",
        "parkshare.messaging.async=false",
        "parkshare.payments.sandbox-auto-approve-seconds=0"
})
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, ParkingJourneyIntegrationTest.CapturedMessages.class})
class ParkingJourneyIntegrationTest {

    private static final String WEBHOOK_SECRET = "test-webhook-secret";
    private static final AtomicInteger SEQ = new AtomicInteger(100);

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ReservationScheduler scheduler;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired rw.parkshare.backend.config.ParkShareProperties properties;

    /** Replaces real SMS/email delivery so tests can read what would have been sent. */
    @org.springframework.boot.test.context.TestConfiguration
    static class CapturedMessages {
        static final java.util.Queue<String[]> SMS = new java.util.concurrent.ConcurrentLinkedQueue<>();
        static final java.util.Queue<String[]> EMAIL = new java.util.concurrent.ConcurrentLinkedQueue<>();

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        rw.parkshare.backend.notification.sms.SmsSender capturingSms() {
            return (phone, message) -> SMS.add(new String[]{phone, message});
        }

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        rw.parkshare.backend.notification.email.EmailSender capturingEmail() {
            return (to, subject, body) -> EMAIL.add(new String[]{to, subject, body});
        }
    }

    /** Latest 6-digit code sent to this phone or email. */
    private static String lastCodeSentTo(String destination) {
        String code = null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("code is (\\d{6})");
        for (String[] m : CapturedMessages.SMS) {
            if (m[0].equals(destination)) {
                java.util.regex.Matcher matcher = p.matcher(m[1]);
                if (matcher.find()) code = matcher.group(1);
            }
        }
        for (String[] m : CapturedMessages.EMAIL) {
            if (m[0].equals(destination)) {
                java.util.regex.Matcher matcher = p.matcher(m[2]);
                if (matcher.find()) code = matcher.group(1);
            }
        }
        return code;
    }

    private String adminToken;
    private String operatorToken;
    private Long operatorId;
    private String driverToken;
    private Long driverId;
    private Long driverVehicleId;
    private String otherDriverToken;
    private Long otherDriverId;
    private Long otherVehicleId;

    @BeforeEach
    void users() throws Exception {
        adminToken = login("admin@parkshare.rw", "AdminPassword123");

        int n = SEQ.incrementAndGet();
        operatorId = register("operator" + n + "@example.rw", "0722" + pad(n), "OPERATOR");
        operatorToken = login("operator" + n + "@example.rw", "Password123");

        driverId = register("driver" + n + "@example.rw", "0788" + pad(n), null);
        driverToken = login("driver" + n + "@example.rw", "Password123");
        driverVehicleId = vehicle(driverToken, driverId, "RAD" + n + "A");

        otherDriverId = register("other" + n + "@example.rw", "0789" + pad(n), null);
        otherDriverToken = login("other" + n + "@example.rw", "Password123");
        otherVehicleId = vehicle(otherDriverToken, otherDriverId, "RAE" + n + "B");

        // Booking requires a verified phone or email; these drivers entered their email code.
        jdbc.update("update users set email_verified_at = now() where id in (?, ?)", driverId, otherDriverId);
    }

    @Test
    void fullParkingJourney() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        verifyOperatorAsVatRegistered();

        // Driver sees the price first: 2h at the operator's 400 RWF/h
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.ofHours(2)).plusMinutes(5).truncatedTo(ChronoUnit.MINUTES);
        OffsetDateTime end = start.plusHours(2);
        call(get("/api/pricing/quote").param("parkingLotId", lotId.toString())
                .param("startTime", start.toString()).param("endTime", end.toString()), driverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(800.0))
                .andExpect(jsonPath("$.vatAmount").value(122.0))
                .andExpect(jsonPath("$.amountExcludingVat").value(678.0))
                .andExpect(jsonPath("$.vatIncluded").value(true))
                .andExpect(jsonPath("$.currency").value("RWF"))
                // Drivers never see ParkShare's or the operator's share
                .andExpect(jsonPath("$.platformCommission").doesNotExist())
                .andExpect(jsonPath("$.mobileMoneyFee").doesNotExist())
                .andExpect(jsonPath("$.operatorReceives").doesNotExist());

        // Booking on behalf of someone else is refused for non-admins
        call(post("/api/reservations")
                .param("userId", otherDriverId.toString())
                .param("vehicleId", driverVehicleId.toString())
                .param("parkingLotId", lotId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(window(start, end)), driverToken)
                .andExpect(status().isForbidden());

        // Book: client-sent amount, status and code are ignored; user comes from the token
        String booking = call(post("/api/reservations")
                        .param("vehicleId", driverVehicleId.toString())
                        .param("parkingLotId", lotId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"startTime":"%s","endTime":"%s","totalAmount":1,"status":"CONFIRMED","reservationCode":"HACK"}
                                """.formatted(start, end)), driverToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(800.0))
                .andExpect(jsonPath("$.vatAmount").value(122.0))
                .andExpect(jsonPath("$.userId").value(driverId))
                .andExpect(jsonPath("$.reservationCode").value(startsWith("PS-")))
                .andExpect(jsonPath("$.parkingSpaceId").isNumber())
                .andReturn().getResponse().getContentAsString();
        Long reservationId = id(booking);

        // Another driver cannot see it, and the only space is now taken
        call(get("/api/reservations/" + reservationId), otherDriverToken).andExpect(status().isForbidden());
        call(post("/api/reservations")
                .param("vehicleId", otherVehicleId.toString())
                .param("parkingLotId", lotId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(window(start.plusMinutes(30), end)), otherDriverToken)
                .andExpect(status().isConflict());

        // Paying with the wrong network is rejected
        call(post("/api/payments/reservation/" + reservationId + "/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\"AIRTEL_MONEY\"}"), driverToken)
                .andExpect(status().isBadRequest());

        // MTN MoMo prompt sent to the account phone
        String payment = call(post("/api/payments/reservation/" + reservationId + "/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\":\"MTN_MOMO\"}"), driverToken)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.amount").value(800.0))
                .andReturn().getResponse().getContentAsString();
        String txRef = JsonPath.read(payment, "$.transactionReference");
        Long paymentId = id(payment);

        // Provider callback: bad signature refused, good one applied once
        String callback = """
                {"eventId":"evt-%s","eventType":"payment.completed","transactionReference":"%s","providerTransactionId":"MTN-123","status":"SUCCESSFUL"}
                """.formatted(txRef, txRef).trim();
        mvc.perform(post("/api/payments/callback/mtn").header("X-ParkShare-Signature", "deadbeef")
                        .contentType(MediaType.APPLICATION_JSON).content(callback))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/payments/callback/mtn")
                        .header("X-ParkShare-Signature", PaymentCallbackService.sign(WEBHOOK_SECRET, callback))
                        .contentType(MediaType.APPLICATION_JSON).content(callback))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(false));
        mvc.perform(post("/api/payments/callback/mtn")
                        .header("X-ParkShare-Signature", PaymentCallbackService.sign(WEBHOOK_SECRET, callback))
                        .contentType(MediaType.APPLICATION_JSON).content(callback))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true));

        call(get("/api/reservations/" + reservationId), driverToken)
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Receipt shows VAT and the lot
        call(get("/api/payments/" + paymentId + "/receipt"), driverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(800.0))
                .andExpect(jsonPath("$.vatAmount").value(122.0))
                .andExpect(jsonPath("$.paymentMethod").value("MTN_MOMO"))
                .andExpect(jsonPath("$.payerPhone").value(startsWith("+25078")));

        // Driver's app gets the QR token; the other driver cannot
        call(get("/api/reservations/" + reservationId + "/qr"), otherDriverToken).andExpect(status().isForbidden());
        String qr = call(get("/api/reservations/" + reservationId + "/qr"), driverToken)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(qr, "$.token");
        assertThat(token).startsWith("PS1.");

        // Drivers cannot operate the gate; forged tokens fail
        call(post("/api/check-ins/scan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\"}"), driverToken)
                .andExpect(status().isForbidden());
        call(post("/api/check-ins/scan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token.substring(0, token.length() - 2) + "xx\"}"), operatorToken)
                .andExpect(status().isBadRequest());

        // Operator scans at the entrance
        call(post("/api/check-ins/scan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\"}"), operatorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.spaceCode").value("A1"));
        assertThat(spaceStatusFor(reservationId)).isEqualTo("OCCUPIED");

        // The same QR cannot be reused
        call(post("/api/check-ins/scan").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + token + "\"}"), operatorToken)
                .andExpect(status().isConflict());

        // Exit before the end: no overstay
        call(post("/api/check-outs/reservation/" + reservationId), operatorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.overstayAmount").value(0.0));
        assertThat(spaceStatusFor(reservationId)).isEqualTo("AVAILABLE");

        // Status history captured every step
        assertThat(jdbc.queryForList(
                "select new_status from reservation_status_history where reservation_id = ? order by id",
                String.class, reservationId))
                .containsExactly("PENDING", "CONFIRMED", "CHECKED_IN", "COMPLETED");

        // Notifications were created for the driver
        assertThat(jdbc.queryForList("select type from notifications where user_id = ?", String.class, driverId))
                .contains("RESERVATION_CREATED", "RESERVATION_CONFIRMED", "CHECK_IN_SUCCESS", "CHECK_OUT_SUCCESS");
    }

    @Test
    void earlyCancellationIsRefundedInFull() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        OffsetDateTime start = OffsetDateTime.now().plusHours(5).truncatedTo(ChronoUnit.MINUTES);
        Long reservationId = book(driverToken, driverVehicleId, lotId, start, start.plusHours(1));
        String txRef = JsonPath.read(call(post("/api/payments/reservation/" + reservationId + "/initiate")
                .contentType(MediaType.APPLICATION_JSON).content("{\"method\":\"MTN_MOMO\"}"), driverToken)
                .andReturn().getResponse().getContentAsString(), "$.transactionReference");

        // Sandbox helper simulates the customer approving the prompt
        call(post("/api/payments/sandbox/" + txRef + "/complete"), driverToken).andExpect(status().isOk());

        call(patch("/api/reservations/" + reservationId + "/cancel"), driverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservation.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(400.0));
        assertThat(jdbc.queryForObject("select status from payments where reservation_id = ?", String.class, reservationId))
                .isEqualTo("REFUNDED");

        // Space is free again for someone else
        book(otherDriverToken, otherVehicleId, lotId, start, start.plusHours(1));
    }

    @Test
    void lateCancellationGetsHalfBack() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        OffsetDateTime start = OffsetDateTime.now().plusMinutes(40).truncatedTo(ChronoUnit.MINUTES);
        Long reservationId = book(driverToken, driverVehicleId, lotId, start, start.plusHours(2));
        String txRef = JsonPath.read(call(post("/api/payments/reservation/" + reservationId + "/initiate")
                .contentType(MediaType.APPLICATION_JSON).content("{\"method\":\"MTN_MOMO\"}"), driverToken)
                .andReturn().getResponse().getContentAsString(), "$.transactionReference");
        call(post("/api/payments/sandbox/" + txRef + "/complete"), driverToken).andExpect(status().isOk());

        // Less than 60 minutes before start: 50% of 800
        call(patch("/api/reservations/" + reservationId + "/cancel"), driverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundAmount").value(400.0));
    }

    @Test
    void unpaidReservationsExpireAndReleaseTheSpace() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        OffsetDateTime start = OffsetDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MINUTES);
        Long reservationId = book(driverToken, driverVehicleId, lotId, start, start.plusHours(1));

        jdbc.update("update reservations set created_at = now() - interval '20 minutes' where id = ?", reservationId);
        new TransactionTemplate(transactionManager).executeWithoutResult(s -> scheduler.expireUnpaid());

        assertThat(jdbc.queryForObject("select status from reservations where id = ?", String.class, reservationId))
                .isEqualTo("EXPIRED");
        call(post("/api/payments/reservation/" + reservationId + "/initiate")
                .contentType(MediaType.APPLICATION_JSON).content("{\"method\":\"MTN_MOMO\"}"), driverToken)
                .andExpect(status().isConflict());
        book(otherDriverToken, otherVehicleId, lotId, start, start.plusHours(1));
    }

    @Test
    void cashPaymentAtTheGateConfirmsTheReservation() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        OffsetDateTime start = OffsetDateTime.now().plusMinutes(10).truncatedTo(ChronoUnit.MINUTES);
        Long reservationId = book(driverToken, driverVehicleId, lotId, start, start.plusHours(1));

        call(post("/api/payments/reservation/" + reservationId + "/cash"), driverToken).andExpect(status().isForbidden());
        call(post("/api/payments/reservation/" + reservationId + "/cash"), operatorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.method").value("CASH"));

        String code = jdbc.queryForObject("select reservation_code from reservations where id = ?", String.class, reservationId);
        call(post("/api/check-ins/code").contentType(MediaType.APPLICATION_JSON)
                .content("{\"reservationCode\":\"" + code + "\"}"), operatorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));
    }

    @Test
    void securityRulesAreEnforced() throws Exception {
        Long lotId = activeLotWithSpaces(1);

        // No token
        mvc.perform(get("/api/reservations/user/" + driverId)).andExpect(status().isUnauthorized());
        // Admin-only areas
        call(get("/api/users"), driverToken).andExpect(status().isForbidden());
        call(post("/api/user-roles").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + driverId + ",\"roleId\":5}"), driverToken).andExpect(status().isForbidden());
        // Someone else's data
        call(get("/api/reservations/user/" + otherDriverId), driverToken).andExpect(status().isForbidden());
        call(get("/api/vehicles/" + otherVehicleId), driverToken).andExpect(status().isForbidden());
        call(post("/api/vehicles/user/" + otherDriverId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"plateNumber\":\"RAZ999Z\",\"vehicleType\":\"CAR\"}"), driverToken).andExpect(status().isForbidden());
        // Using someone else's vehicle
        call(post("/api/reservations").param("vehicleId", otherVehicleId.toString()).param("parkingLotId", lotId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(window(OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2))), driverToken)
                .andExpect(status().isForbidden());
        // Operators cannot approve their own lots or touch other operators' lots
        call(patch("/api/parking-lots/" + lotId + "/status").param("status", "SUSPENDED"), operatorToken)
                .andExpect(status().isForbidden());
        Long otherOperator = register("op2-" + SEQ.incrementAndGet() + "@example.rw", "0733" + pad(SEQ.get()), "OPERATOR");
        String otherOperatorToken = login("op2-" + SEQ.get() + "@example.rw", "Password123");
        assertThat(otherOperator).isNotNull();
        call(post("/api/parking-zones/parking-lot/" + lotId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Intruder\",\"capacity\":1,\"status\":\"ACTIVE\"}"), otherOperatorToken)
                .andExpect(status().isForbidden());
        // Operators cannot price above the affordability ceiling
        call(post("/api/pricing-rules/parking-lot/" + lotId).contentType(MediaType.APPLICATION_JSON)
                .content(rule("Greedy", 5000)), operatorToken)
                .andExpect(status().isBadRequest());
        // Registration enforces Rwandan phone numbers
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName":"A","lastName":"B","email":"kenya@example.com","phone":"+254712345678","password":"Password123"}
                        """)).andExpect(status().isBadRequest());
    }

    @Test
    void databaseRejectsDoubleBookingEvenIfAppChecksAreBypassed() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        OffsetDateTime start = OffsetDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES);
        Long reservationId = book(driverToken, driverVehicleId, lotId, start, start.plusHours(2));

        // Simulates a second request racing past the application check
        assertThatThrownBy(() -> jdbc.update("""
                insert into reservations (user_id, vehicle_id, parking_lot_id, parking_space_id, reservation_code,
                                          start_time, end_time, status, total_amount, currency)
                select ?, ?, parking_lot_id, parking_space_id, 'RACE-1', start_time + interval '1 hour',
                       end_time + interval '1 hour', 'PENDING', 0, 'RWF'
                from reservations where id = ?
                """, otherDriverId, otherVehicleId, reservationId))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class)
                .hasMessageContaining("excl_reservation_space_overlap");
    }

    @Test
    void requestsAreValidatedWithClearMessages() throws Exception {
        // District must be one of Rwanda's 30 districts; coordinates must be inside Rwanda
        call(post("/api/parking-lots/operator/" + operatorId).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Lot","address":"Somewhere","district":"Westlands","city":"Nairobi","totalCapacity":5,
                 "latitude":-1.29,"longitude":36.82}
                """), operatorToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("district")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("longitude")));
        call(post("/api/vehicles/user/" + driverId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"plateNumber\":\"RAB 001 C\",\"vehicleType\":\"SPACESHIP\"}"), driverToken)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("vehicleType")));
    }

    @Test
    void reservationListsArePaged() throws Exception {
        Long lotId = activeLotWithSpaces(3);
        OffsetDateTime start = OffsetDateTime.now().plusHours(4).truncatedTo(ChronoUnit.MINUTES);
        for (int i = 0; i < 3; i++) {
            book(driverToken, driverVehicleId, lotId, start.plusHours(i * 2L), start.plusHours(i * 2L + 1));
        }
        call(get("/api/reservations/user/" + driverId).param("size", "2"), driverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));
        // Size is capped at 100
        call(get("/api/users").param("size", "5000"), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void operatorSeesWhatDriversPayAndWhatTheyEarn() throws Exception {
        Long lotId = activeLotWithSpaces(1);

        // Not VAT-verified yet: no VAT, operator keeps 800 - 80 commission - 12 MoMo fee
        call(get("/api/pricing/operator-preview").param("parkingLotId", lotId.toString())
                .param("paymentMethod", "MTN_MOMO"), operatorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vatApplied").value(false))
                .andExpect(jsonPath("$.examples[1].stay").value("2 hours"))
                .andExpect(jsonPath("$.examples[1].driverPays").value(800.0))
                .andExpect(jsonPath("$.examples[1].vatInPrice").value(0.0))
                .andExpect(jsonPath("$.examples[1].platformCommission").value(80.0))
                .andExpect(jsonPath("$.examples[1].mobileMoneyFee").value(12.0))
                .andExpect(jsonPath("$.examples[1].youReceive").value(708.0));

        // After admin verifies VAT registration, VAT is inside the same price
        verifyOperatorAsVatRegistered();
        call(get("/api/pricing/operator-preview").param("parkingLotId", lotId.toString())
                .param("paymentMethod", "MTN_MOMO").param("hourlyRate", "500"), operatorToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vatApplied").value(true))
                .andExpect(jsonPath("$.basedOn").value(org.hamcrest.Matchers.containsString("Proposed")))
                .andExpect(jsonPath("$.examples[0].driverPays").value(500.0))
                .andExpect(jsonPath("$.examples[0].vatToDeclareToRra").value(76.0));

        // Drivers and other operators cannot see it
        call(get("/api/pricing/operator-preview").param("parkingLotId", lotId.toString()), driverToken)
                .andExpect(status().isForbidden());
        int n = SEQ.incrementAndGet();
        register("op3-" + n + "@example.rw", "0733" + pad(n), "OPERATOR");
        call(get("/api/pricing/operator-preview").param("parkingLotId", lotId.toString()),
                login("op3-" + n + "@example.rw", "Password123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorsCannotVerifyThemselves() throws Exception {
        call(post("/api/operator-verifications").param("userId", operatorId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"businessName\":\"Self\",\"tin\":\"123456789\",\"vatRegistered\":true,\"verificationStatus\":\"VERIFIED\"}"),
                operatorToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
        // TIN must be 9 digits
        call(post("/api/operator-verifications").param("userId", operatorId.toString())
                .contentType(MediaType.APPLICATION_JSON).content("{\"tin\":\"12\"}"), operatorToken)
                .andExpect(status().isBadRequest());
        // Nobody can submit a verification for someone else
        call(post("/api/operator-verifications").param("userId", driverId.toString())
                .contentType(MediaType.APPLICATION_JSON).content("{\"tin\":\"123456789\"}"), operatorToken)
                .andExpect(status().isForbidden());
    }

    @Test
    void phoneAndEmailAreVerifiedWithCodes() throws Exception {
        String phone = jdbc.queryForObject("select phone from users where id = ?", String.class, driverId);
        String email = jdbc.queryForObject("select email from users where id = ?", String.class, driverId);

        // Codes were sent at registration
        String phoneCode = lastCodeSentTo(phone);
        String emailCode = lastCodeSentTo(email);
        assertThat(phoneCode).isNotNull();
        assertThat(emailCode).isNotNull();

        call(post("/api/me/verify/phone/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"000000\"}"), driverToken).andExpect(status().isBadRequest());
        call(post("/api/me/verify/phone/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + phoneCode + "\"}"), driverToken).andExpect(status().isOk());
        call(post("/api/me/verify/email/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + emailCode + "\"}"), driverToken).andExpect(status().isOk());
        // A used code cannot be reused
        call(post("/api/me/verify/email/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + emailCode + "\"}"), driverToken).andExpect(status().isBadRequest());

        call(get("/api/me"), driverToken)
                .andExpect(jsonPath("$.phoneVerified").value(true))
                .andExpect(jsonPath("$.emailVerified").value(true));

        // Asking again straight away is rate limited
        call(post("/api/me/verify/phone"), otherDriverToken).andExpect(status().isTooManyRequests());
    }

    @Test
    void forgottenPasswordIsResetWithSmsCode() throws Exception {
        String phone = jdbc.queryForObject("select phone from users where id = ?", String.class, driverId);
        String email = jdbc.queryForObject("select email from users where id = ?", String.class, driverId);

        // Same answer for unknown accounts, so attackers cannot discover who is registered
        mvc.perform(post("/api/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"0788999111\"}")).andExpect(status().isAccepted());
        mvc.perform(post("/api/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"" + phone + "\"}")).andExpect(status().isAccepted());
        String code = lastCodeSentTo(phone);
        assertThat(CapturedMessages.SMS).anyMatch(m -> m[0].equals(phone) && m[1].contains("password reset"));

        mvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + phone + "\",\"code\":\"" + code + "\",\"newPassword\":\"NewPassw0rd\"}"))
                .andExpect(status().isOk());
        assertThat(login(email, "NewPassw0rd")).isNotBlank();
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void confirmationIsSentBySmsAndEmailButRemindersAreNotSms() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        String phone = jdbc.queryForObject("select phone from users where id = ?", String.class, driverId);
        OffsetDateTime start = OffsetDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MINUTES);
        Long reservationId = book(driverToken, driverVehicleId, lotId, start, start.plusHours(1));
        String txRef = JsonPath.read(call(post("/api/payments/reservation/" + reservationId + "/initiate")
                .contentType(MediaType.APPLICATION_JSON).content("{\"method\":\"MTN_MOMO\"}"), driverToken)
                .andReturn().getResponse().getContentAsString(), "$.transactionReference");
        call(post("/api/payments/sandbox/" + txRef + "/complete"), driverToken).andExpect(status().isOk());

        assertThat(CapturedMessages.SMS).anyMatch(m -> m[0].equals(phone) && m[1].contains("is confirmed"));
        // "Reservation created" went in-app + email only (no SMS cost)
        assertThat(CapturedMessages.SMS).noneMatch(m -> m[0].equals(phone) && m[1].contains("Pay within"));
        // The MoMo fee is recorded against the payment for operator payouts
        assertThat(jdbc.queryForObject("select provider_fee from payments where reservation_id = ?",
                java.math.BigDecimal.class, reservationId)).isEqualByComparingTo("6"); // 400 x 1.5%
    }

    @Test
    void sandboxPhoneApprovesOrDeclinesAutomatically() throws Exception {
        Long lotId = activeLotWithSpaces(2);
        int n = SEQ.incrementAndGet();
        String decliningPhone = "0785" + String.format("%03d", n % 1000) + "000"; // ends in 000 = declines
        Long decliningId = register("broke" + n + "@example.rw", decliningPhone, null);
        String decliningToken = login("broke" + n + "@example.rw", "Password123");
        Long decliningVehicle = vehicle(decliningToken, decliningId, "RAF" + n + "C");
        jdbc.update("update users set email_verified_at = now() where id = ?", decliningId);

        OffsetDateTime start = OffsetDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES);
        Long approved = book(driverToken, driverVehicleId, lotId, start, start.plusHours(1));
        Long declined = book(decliningToken, decliningVehicle, lotId, start, start.plusHours(1));

        properties.getPayments().setSandboxAutoApproveSeconds(1);
        try {
            for (Object[] pair : new Object[][]{{approved, driverToken}, {declined, decliningToken}}) {
                call(post("/api/payments/reservation/" + pair[0] + "/initiate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\":\"MTN_MOMO\"}"), (String) pair[1]).andExpect(status().isAccepted());
            }
            waitForStatus(approved, "CONFIRMED");
            waitForPaymentStatus(declined, "FAILED");
        } finally {
            properties.getPayments().setSandboxAutoApproveSeconds(0);
        }
        assertThat(jdbc.queryForObject("select status from reservations where id = ?", String.class, declined))
                .as("declined payment leaves the booking unpaid").isEqualTo("PENDING");
    }

    @Test
    void driversOnlySeeApprovedLotsAndLotsIncludeDistrict() throws Exception {
        String pending = call(post("/api/parking-lots/operator/" + operatorId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Hidden Lot " + SEQ.incrementAndGet() + "\",\"address\":\"KN 1 Rd\",\"district\":\"Nyarugenge\",\"city\":\"Kigali\",\"totalCapacity\":2}"),
                operatorToken).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Long pendingId = id(pending);
        Long activeId = activeLotWithSpaces(1);

        String driverList = call(get("/api/parking-lots"), driverToken).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        java.util.List<Integer> driverIds = JsonPath.read(driverList, "$[*].id");
        java.util.List<String> statuses = JsonPath.read(driverList, "$[*].status");
        assertThat(driverIds).contains(activeId.intValue()).doesNotContain(pendingId.intValue());
        assertThat(statuses).containsOnly("ACTIVE");
        call(get("/api/parking-lots/status/PENDING"), driverToken)
                .andExpect(jsonPath("$[?(@.id == " + pendingId + ")]").isEmpty());
        call(get("/api/parking-lots/" + pendingId), driverToken).andExpect(status().isNotFound());

        // The owner and admins still see it
        call(get("/api/parking-lots/" + pendingId), operatorToken).andExpect(status().isOk())
                .andExpect(jsonPath("$.district").value("Nyarugenge"));
        call(get("/api/parking-lots/" + pendingId), adminToken).andExpect(status().isOk());
        call(get("/api/parking-lots/" + activeId), driverToken).andExpect(jsonPath("$.district").value("Gasabo"));
    }

    @Test
    void refreshTokensRotateAndStolenTokensAreDetected() throws Exception {
        String email = jdbc.queryForObject("select email from users where id = ?", String.class, driverId);
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").value(1800))
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn().getResponse().getContentAsString();
        String first = JsonPath.read(login, "$.refreshToken");

        String refreshed = refresh(first).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String second = JsonPath.read(refreshed, "$.refreshToken");
        String access = JsonPath.read(refreshed, "$.token");
        assertThat(second).isNotEqualTo(first);
        call(get("/api/me"), access).andExpect(status().isOk());

        // Someone replays the old token: refused, and the whole session is shut down
        refresh(first).andExpect(status().isUnauthorized());
        refresh(second).andExpect(status().isUnauthorized());

        // Logout ends a session
        String again = JsonPath.read(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andReturn().getResponse().getContentAsString(), "$.refreshToken");
        mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + again + "\"}")).andExpect(status().isNoContent());
        refresh(again).andExpect(status().isUnauthorized());

        // Changing the password signs out other devices
        String device = JsonPath.read(mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andReturn().getResponse().getContentAsString(), "$.refreshToken");
        call(post("/api/me/password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"Password123\",\"newPassword\":\"Changed123\"}"), driverToken)
                .andExpect(status().isOk());
        refresh(device).andExpect(status().isUnauthorized());
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"));
    }

    @Test
    void driversMustVerifyPhoneOrEmailBeforeBooking() throws Exception {
        Long lotId = activeLotWithSpaces(1);
        int n = SEQ.incrementAndGet();
        String email = "new" + n + "@example.rw";
        Long userId = register(email, "0786" + pad(n), null);
        String token = login(email, "Password123");
        Long vehicleId = vehicle(token, userId, "RAG" + n + "D");
        OffsetDateTime start = OffsetDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES);

        call(post("/api/reservations").param("vehicleId", vehicleId.toString()).param("parkingLotId", lotId.toString())
                .contentType(MediaType.APPLICATION_JSON).content(window(start, start.plusHours(1))), token)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("VERIFICATION_REQUIRED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("verify")));

        // The email code sent at sign-up unlocks booking (SMS is not needed)
        call(post("/api/me/verify/email/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + lastCodeSentTo(email) + "\"}"), token).andExpect(status().isOk());
        book(token, vehicleId, lotId, start, start.plusHours(1));
    }

    private void waitForStatus(Long reservationId, String expected) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (expected.equals(jdbc.queryForObject("select status from reservations where id = ?", String.class, reservationId))) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError("Reservation " + reservationId + " never became " + expected);
    }

    private void waitForPaymentStatus(Long reservationId, String expected) throws InterruptedException {
        for (int i = 0; i < 50; i++) {
            if (expected.equals(jdbc.queryForObject("select status from payments where reservation_id = ?", String.class, reservationId))) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError("Payment for " + reservationId + " never became " + expected);
    }

    /** Operator submits TIN + VAT status; admin verifies it. */
    private void verifyOperatorAsVatRegistered() throws Exception {
        String tin = String.format("%09d", operatorId * 1000 + SEQ.incrementAndGet() % 1000);
        String created = call(post("/api/operator-verifications").param("userId", operatorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Kigali Parking Ltd\",\"tin\":\"" + tin + "\",\"vatRegistered\":true}"),
                operatorToken)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long adminId = jdbc.queryForObject("select id from users where email = 'admin@parkshare.rw'", Long.class);
        call(patch("/api/operator-verifications/" + id(created) + "/review").param("reviewerId", adminId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"verificationStatus\":\"VERIFIED\"}"), adminToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vatRegistered").value(true));
    }

    // ------------------------------------------------------------------ helpers

    private Long activeLotWithSpaces(int spaces) throws Exception {
        Long lotId = id(call(post("/api/parking-lots/operator/" + operatorId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Kigali Heights Parking %d","address":"KG 7 Ave","district":"Gasabo","city":"Kigali",
                         "contactPhone":"0788000000","totalCapacity":%d,"status":"ACTIVE"}
                        """.formatted(SEQ.incrementAndGet(), spaces)), operatorToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING")) // operator cannot self-activate
                .andReturn().getResponse().getContentAsString());

        // Not bookable until an admin approves it
        call(post("/api/reservations").param("vehicleId", driverVehicleId.toString()).param("parkingLotId", lotId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(window(OffsetDateTime.now().plusHours(1), OffsetDateTime.now().plusHours(2))), driverToken)
                .andExpect(status().isConflict());
        call(patch("/api/parking-lots/" + lotId + "/status").param("status", "ACTIVE"), adminToken)
                .andExpect(status().isOk());

        Long zoneId = id(call(post("/api/parking-zones/parking-lot/" + lotId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ground floor\",\"capacity\":" + spaces + ",\"status\":\"ACTIVE\"}"), operatorToken)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        for (int i = 1; i <= spaces; i++) {
            call(post("/api/parking-spaces/zone/" + zoneId).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"spaceCode\":\"A" + i + "\",\"spaceType\":\"STANDARD\",\"status\":\"AVAILABLE\"}"), operatorToken)
                    .andExpect(status().isCreated());
        }
        call(post("/api/pricing-rules/parking-lot/" + lotId).contentType(MediaType.APPLICATION_JSON)
                .content(rule("Standard", 400)), operatorToken)
                .andExpect(status().isCreated());
        return lotId;
    }

    private Long book(String token, Long vehicleId, Long lotId, OffsetDateTime start, OffsetDateTime end) throws Exception {
        return id(call(post("/api/reservations").param("vehicleId", vehicleId.toString())
                .param("parkingLotId", lotId.toString())
                .contentType(MediaType.APPLICATION_JSON).content(window(start, end)), token)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private Long register(String email, String phone, String accountType) throws Exception {
        String body = """
                {"firstName":"Test","lastName":"User","email":"%s","phone":"%s","password":"Password123"%s}
                """.formatted(email, phone, accountType == null ? "" : ",\"accountType\":\"" + accountType + "\"");
        String response = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.userId")).longValue();
    }

    private String login(String email, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private Long vehicle(String token, Long userId, String plate) throws Exception {
        return id(call(post("/api/vehicles/user/" + userId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"plateNumber\":\"" + plate + "\",\"vehicleType\":\"CAR\"}"), token)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private String spaceStatusFor(Long reservationId) {
        return jdbc.queryForObject("""
                select s.status from parking_spaces s join reservations r on r.parking_space_id = s.id where r.id = ?
                """, String.class, reservationId);
    }

    private ResultActions call(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token));
    }

    private static String window(OffsetDateTime start, OffsetDateTime end) {
        return "{\"startTime\":\"" + start + "\",\"endTime\":\"" + end + "\"}";
    }

    private static String rule(String name, int amount) {
        return """
                {"name":"%s","pricingType":"HOURLY","amount":%d,"currency":"RWF","active":true,"effectiveFrom":"%s"}
                """.formatted(name, amount, OffsetDateTime.now().minusDays(1));
    }

    private static Long id(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private static String pad(int n) {
        return String.format("%06d", n);
    }
}

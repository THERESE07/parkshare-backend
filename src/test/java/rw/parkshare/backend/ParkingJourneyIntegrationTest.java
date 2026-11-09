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
        "parkshare.scheduler.initial-delay-ms=3600000"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ParkingJourneyIntegrationTest {

    private static final String WEBHOOK_SECRET = "test-webhook-secret";
    private static final AtomicInteger SEQ = new AtomicInteger(100);

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ReservationScheduler scheduler;
    @Autowired PlatformTransactionManager transactionManager;

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
    }

    @Test
    void fullParkingJourney() throws Exception {
        Long lotId = activeLotWithSpaces(1);

        // Driver sees the price first: 2h at the operator's 400 RWF/h
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.ofHours(2)).plusMinutes(5).truncatedTo(ChronoUnit.MINUTES);
        OffsetDateTime end = start.plusHours(2);
        call(get("/api/pricing/quote").param("parkingLotId", lotId.toString())
                .param("startTime", start.toString()).param("endTime", end.toString()), driverToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(800.0))
                .andExpect(jsonPath("$.vatAmount").value(122.0))
                .andExpect(jsonPath("$.amountExcludingVat").value(678.0))
                .andExpect(jsonPath("$.currency").value("RWF"));

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

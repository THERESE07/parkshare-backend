package rw.parkshare.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rw.parkshare.backend.config.ParkShareProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Role rules for every route. Ownership (is this *your* reservation, lot,
 * vehicle...) is enforced afterwards by {@link OwnershipPolicy}.
 */
@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String OPERATOR = "OPERATOR";
    private static final String ATTENDANT = "ATTENDANT";
    private static final String DRIVER = "DRIVER";
    private static final String ORGANIZER = "EVENT_ORGANIZER";

    @Value("${PARKSHARE_JWT_SECRET}")
    private String jwtSecret;

    private final ParkShareProperties properties;

    public SecurityConfig(ParkShareProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("PARKSHARE_JWT_SECRET must be at least 32 bytes long");
        }

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // ---------- Public ----------
                        .requestMatchers("/api/auth/**", "/actuator/health", "/error").permitAll()
                        // API docs (turn off in production via SPRINGDOC_* settings)
                        .requestMatchers(HttpMethod.GET, "/", "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        // Payment providers call back without a JWT; the body is HMAC-signed.
                        .requestMatchers(HttpMethod.POST, "/api/payments/callback/**").permitAll()

                        // ---------- Current user ----------
                        .requestMatchers("/api/me/**", "/api/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/pricing/quote").authenticated()

                        // ---------- Administration ----------
                        .requestMatchers("/api/users/**", "/api/users").hasRole(ADMIN)
                        .requestMatchers("/api/roles/**", "/api/roles").hasRole(ADMIN)
                        .requestMatchers("/api/user-roles/**", "/api/user-roles").hasRole(ADMIN)
                        .requestMatchers("/api/audit-logs/**", "/api/audit-logs").hasRole(ADMIN)
                        .requestMatchers("/api/payment-webhooks/**", "/api/payment-webhooks").hasRole(ADMIN)
                        .requestMatchers("/api/reservation-qr-codes/**").hasRole(ADMIN)
                        .requestMatchers("/api/payment-transactions/**").hasRole(ADMIN)
                        .requestMatchers("/api/refunds/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/reservation-status-history/**")
                        .hasAnyRole(OPERATOR, ADMIN)
                        .requestMatchers("/api/reservation-status-history/**", "/api/reservation-status-history")
                        .hasRole(ADMIN)

                        // ---------- Operator & lot verification ----------
                        .requestMatchers(HttpMethod.POST, "/api/operator-verifications").hasAnyRole(OPERATOR, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/operator-verifications/user/*").hasAnyRole(OPERATOR, ADMIN)
                        .requestMatchers("/api/operator-verifications/**", "/api/operator-verifications").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/parking-verifications/**").hasAnyRole(OPERATOR, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/parking-verifications/**").hasAnyRole(OPERATOR, ADMIN)
                        .requestMatchers("/api/parking-verifications/**").hasRole(ADMIN)

                        // ---------- Parking lots and their contents ----------
                        // Activating/suspending a lot is an admin decision after verification.
                        .requestMatchers(HttpMethod.PATCH, "/api/parking-lots/*/status").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/parking-spaces/*/status", "/api/parking-spaces/*/available",
                                "/api/parking-spaces/*/occupied", "/api/parking-spaces/*/reserved",
                                "/api/parking-spaces/*/maintenance").hasAnyRole(OPERATOR, ATTENDANT, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/parking-lot-staff/**").hasAnyRole(OPERATOR, ATTENDANT, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/parking-lots/**", "/api/parking-lots",
                                "/api/parking-zones/**", "/api/parking-spaces/**", "/api/pricing-rules/**",
                                "/api/special-rates/**", "/api/parking-operating-hours/**",
                                "/api/parking-features/**", "/api/occupancy-records/**").authenticated()
                        .requestMatchers("/api/parking-lots/**", "/api/parking-zones/**", "/api/parking-spaces/**",
                                "/api/pricing-rules/**", "/api/special-rates/**", "/api/parking-operating-hours/**",
                                "/api/parking-features/**", "/api/parking-lot-staff/**").hasAnyRole(OPERATOR, ADMIN)
                        .requestMatchers("/api/occupancy-records/**", "/api/occupancy-records")
                        .hasAnyRole(OPERATOR, ATTENDANT, ADMIN)

                        // ---------- Vehicles ----------
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/plate", "/api/vehicles/plate/exists")
                        .hasAnyRole(OPERATOR, ATTENDANT, ADMIN)
                        .requestMatchers("/api/vehicles/**").authenticated()

                        // ---------- Reservations ----------
                        .requestMatchers(HttpMethod.POST, "/api/reservations").hasAnyRole(DRIVER, OPERATOR, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/reservations", "/api/reservations/status/**",
                                "/api/reservations/between", "/api/reservations/code/exists").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/reservations/parking-lot/**",
                                "/api/reservations/parking-space/**").hasAnyRole(OPERATOR, ATTENDANT, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/reservations/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/notes").authenticated()
                        // Prices and statuses are set by the system, not edited by hand.
                        .requestMatchers("/api/reservations/**").hasRole(ADMIN)

                        // ---------- Payments ----------
                        .requestMatchers(HttpMethod.POST, "/api/payments/reservation/*/initiate")
                        .hasAnyRole(DRIVER, OPERATOR, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/payments/reservation/*/cash")
                        .hasAnyRole(OPERATOR, ATTENDANT, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/payments/sandbox/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/*", "/api/payments/*/receipt",
                                "/api/payments/reservation/*").authenticated()
                        .requestMatchers("/api/payments/**").hasRole(ADMIN)

                        // ---------- Check-in / check-out ----------
                        .requestMatchers(HttpMethod.POST, "/api/check-ins/scan", "/api/check-ins/code",
                                "/api/check-outs/reservation/*").hasAnyRole(OPERATOR, ATTENDANT, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/check-ins/**", "/api/check-outs/**")
                        .hasAnyRole(OPERATOR, ATTENDANT, ADMIN)
                        .requestMatchers("/api/check-ins/**", "/api/check-ins",
                                "/api/check-outs/**", "/api/check-outs").hasRole(ADMIN)

                        // ---------- Notifications ----------
                        .requestMatchers(HttpMethod.GET, "/api/notifications/user/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/notifications/*/read").authenticated()
                        .requestMatchers("/api/notifications/**", "/api/notifications").hasRole(ADMIN)

                        // ---------- Events ----------
                        .requestMatchers(HttpMethod.GET, "/api/events/**", "/api/events",
                                "/api/event-parking-zones/**", "/api/event-parking-zones").authenticated()
                        .requestMatchers("/api/events/**", "/api/events").hasAnyRole(ORGANIZER, ADMIN)
                        .requestMatchers("/api/event-parking-zones/**", "/api/event-parking-zones")
                        .hasAnyRole(ORGANIZER, OPERATOR, ADMIN)
                        .requestMatchers("/api/event-reservations/**", "/api/event-reservations").authenticated()

                        // ---------- Reviews & support ----------
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole(DRIVER)
                        .requestMatchers("/api/reviews/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/support-tickets", "/api/issue-reports",
                                "/api/disputes/reservation/*/raised-by/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/support-tickets/user/**", "/api/issue-reports/user/**",
                                "/api/disputes/raised-by/**").authenticated()
                        .requestMatchers("/api/support-tickets/**", "/api/support-tickets", "/api/issue-reports/**",
                                "/api/issue-reports", "/api/disputes/**").hasRole(ADMIN)

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(properties.getSecurity().getAllowedOrigins().split(","))
                .map(String::trim).filter(origin -> !origin.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles == null) {
                return List.of();
            }
            return roles.stream()
                    .map(String::toUpperCase)
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });
        return converter;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return NimbusJwtEncoder.withSecretKey(secretKey()).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtService.ISSUER));
        return decoder;
    }

    private SecretKey secretKey() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}

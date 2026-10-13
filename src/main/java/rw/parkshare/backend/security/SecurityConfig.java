package rw.parkshare.backend.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Value("${PARKSHARE_JWT_SECRET}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health"
                        ).permitAll()

                        // Vehicles
                        .requestMatchers("/api/vehicles/**")
                        .hasRole("DRIVER")

                        // Public read-only parking discovery. Management operations remain role-protected below.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/parking-lots",
                                "/api/parking-lots/city",
                                "/api/parking-lots/search",
                                "/api/parking-lots/status/**",
                                "/api/parking-lots/*"
                        ).permitAll()

                        // Parking-lot management
                        .requestMatchers(
                                "/api/parking-lots/operator/**",
                                "/api/parking-lots/*"
                        ).hasAnyRole("OPERATOR", "ADMIN")

                        // Parking-lot staff management
                        .requestMatchers(
                                "/api/parking-lot-staff/**"
                        ).hasAnyRole("OPERATOR", "ATTENDANT", "ADMIN")

                        // Parking operating hours
                        .requestMatchers(
                                "/api/parking-operating-hours/**"
                        ).hasAnyRole("DRIVER", "OPERATOR", "ATTENDANT", "ADMIN")

                        // Parking space operations
                        .requestMatchers(
                                "/api/parking-spaces/**"
                        ).hasAnyRole("DRIVER", "OPERATOR", "ATTENDANT", "ADMIN")

                                // Parking verification - creation and viewing
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/parking-verifications/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/parking-verifications/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                               // Parking verification - administrative actions
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/parking-verifications/*/approve",
                                        "/api/parking-verifications/*/reject",
                                        "/api/parking-verifications/*/status",
                                        "/api/parking-verifications/*/notes"
                                ).hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/parking-verifications/**"
                                ).hasRole("ADMIN")

                        // Parking zone operations
                        .requestMatchers(
                                "/api/parking-zones/**"
                        ).hasAnyRole("DRIVER", "OPERATOR", "ATTENDANT", "ADMIN")

                        // Pricing rule operations
                        .requestMatchers(
                                "/api/pricing-rules/**"
                        ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                        // Special rate operations
                        .requestMatchers(
                                "/api/special-rates/**"
                        ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                        // Parking feature operations
                        .requestMatchers(
                                "/api/parking-features/**"
                        ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                                // Payment creation
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/payments/reservation/**"
                                ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                               // Payment viewing
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/payments/**"
                                ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                               // Payment management
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/payments/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/payments/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/payments/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                // Payment transaction creation
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/payment-transactions/payment/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

// Payment transaction viewing
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/payment-transactions/**"
                                ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                                // Payment transaction management
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/payment-transactions/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/payment-transactions/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/payment-transactions/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                // Payment webhook operations
                                .requestMatchers(
                                        "/api/payment-webhooks/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                // Refund operations
                                .requestMatchers(
                                        "/api/refunds/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                // Reservation creation
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/reservations"
                                ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                               // Reservation viewing - individual/user/vehicle
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/reservations/{id}",
                                        "/api/reservations/code/**",
                                        "/api/reservations/user/**",
                                        "/api/reservations/vehicle/**"
                                ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                                // Reservation viewing - parking operations
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/reservations/parking-lot/**",
                                        "/api/reservations/parking-space/**"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                // Reservation administration / reporting
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/reservations",
                                        "/api/reservations/status/**",
                                        "/api/reservations/between",
                                        "/api/reservations/code/exists"
                                ).hasRole("ADMIN")

                                // Reservation cancellation
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/reservations/*/cancel"
                                ).hasAnyRole("DRIVER", "OPERATOR", "ADMIN")

                               // Reservation status management
                                .requestMatchers(
                                        HttpMethod.PATCH,
                                        "/api/reservations/*/status"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                // Reservation update
                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/reservations/*"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                                // Reservation deletion
                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/reservations/*"
                                ).hasAnyRole("OPERATOR", "ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null) {
                return List.of();
            }

            return roles.stream()
                    .map(String::toUpperCase)
                    .map(role -> (GrantedAuthority)
                            new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });

        return converter;
    }
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${PARKSHARE_CORS_ALLOWED_ORIGINS:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList()
        );
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "Origin"
        ));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
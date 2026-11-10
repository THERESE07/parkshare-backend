package rw.parkshare.backend.security;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    public static final String ISSUER = "parkshare";

    private final JwtEncoder jwtEncoder;
    private final rw.parkshare.backend.config.ParkShareProperties properties;

    public JwtService(JwtEncoder jwtEncoder, rw.parkshare.backend.config.ParkShareProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public long accessTokenSeconds() {
        return properties.getSecurity().getAccessTokenMinutes() * 60L;
    }

    public String generateToken(
            String email,
            Long userId,
            List<String> roles
    ) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(accessTokenSeconds());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(email)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("userId", userId)
                .claim("roles", roles)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }
}

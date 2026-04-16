package devs.group5.rms.services;

import devs.group5.rms.entities.Role;
import devs.group5.rms.entities.User;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.UUID;

@Service
public class JwtService {
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtDecoder decoder;
    private final JwtEncoder encoder;
    private final TemporalAmount accessDuration;
    private final TemporalAmount refreshDuration;

    public JwtService(
            JwtDecoder decoder,
            JwtEncoder encoder,
            @Value("${jwt.accessDuration}") int accessDuration,
            @Value("${jwt.refreshDuration}") int refreshDuration
    ) {
        this.decoder = decoder;
        this.encoder = encoder;
        this.accessDuration = Duration.ofMillis(accessDuration);
        this.refreshDuration = Duration.ofMillis(refreshDuration);
    }

    private String generate(User user, @NonNull TemporalAmount duration, String tokenType) {
        val now = Instant.now();

        val claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(now)
                .expiresAt(now.plus(duration))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateAccessToken(User user) {
        return generate(user, accessDuration, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(User user) {
        return generate(user, refreshDuration, REFRESH_TOKEN_TYPE);
    }

    public UUID extractUserId(String token) {
        val jwt = decoder.decode(token);
        val subject = jwt.getSubject();
        return UUID.fromString(subject);
    }

    public Role extractUserRole(String token) {
        val jwt = decoder.decode(token);
        return Role.valueOf(jwt.getClaimAsString("role"));
    }

    public boolean isRefreshToken(String token) {
        val jwt = decoder.decode(token);
        val tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
        return REFRESH_TOKEN_TYPE.equals(tokenType);
    }
}

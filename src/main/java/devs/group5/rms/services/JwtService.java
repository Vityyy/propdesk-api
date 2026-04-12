package devs.group5.rms.services;

import devs.group5.rms.entities.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final int accessDuration;

    public JwtService(JwtEncoder encoder, @Value("${jwt.accessDuration}") int accessDuration) {
        this.encoder = encoder;
        this.accessDuration = accessDuration;
    }

    public String generate(@NonNull UUID userId, @NonNull Role role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessDuration))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}

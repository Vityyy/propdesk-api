package devs.group5.rms.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Duration accessDuration,
        Duration refreshDuration,
        String algorithm
) {
}

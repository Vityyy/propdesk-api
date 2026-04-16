package devs.group5.rms.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import devs.group5.rms.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
    private final String secret;
    private final String algorithm;

    public JwtConfig(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.algorithm}") String algorithm
    ) {
        this.secret = secret;
        this.algorithm = algorithm;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(), algorithm);
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey key = new SecretKeySpec(secret.getBytes(), algorithm);
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }
}

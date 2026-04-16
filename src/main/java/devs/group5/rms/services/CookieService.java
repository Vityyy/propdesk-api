package devs.group5.rms.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {
    private final boolean secureCookies;
    private final Duration refreshCookieMaxAge;

    public CookieService(
            @Value("${ENVIRONMENT:production}") String environment,
            @Value("${jwt.refreshDuration}") long refreshDurationMillis
    ) {
        this.secureCookies = !"development".equalsIgnoreCase(environment);
        this.refreshCookieMaxAge = Duration.ofMillis(refreshDurationMillis);
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(secureCookies)
                .path("/auth/refresh")
                .maxAge(refreshCookieMaxAge)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secureCookies)
                .path("/auth/refresh")
                .maxAge(0)
                .build();
    }
}

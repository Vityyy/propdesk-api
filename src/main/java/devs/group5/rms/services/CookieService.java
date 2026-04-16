package devs.group5.rms.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {
    private final boolean secureCookies;
    private final Duration refreshCookieMaxAge;
    private final String refreshCookiePath;
    private final String refreshCookieSameSite;
    private final boolean refreshCookiePartitioned;

    public CookieService(
            @Value("${ENVIRONMENT:production}") String environment,
            @Value("${jwt.refreshDuration}") long refreshDurationMillis,
            @Value("${server.servlet.context-path:}") String contextPath
    ) {
        this.secureCookies = !"development".equalsIgnoreCase(environment);
        this.refreshCookieMaxAge = Duration.ofMillis(refreshDurationMillis);
        this.refreshCookiePath = buildRefreshCookiePath(contextPath);
        this.refreshCookieSameSite = this.secureCookies ? "None" : "Lax";
        this.refreshCookiePartitioned = this.secureCookies;
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(refreshCookieSameSite)
                .partitioned(refreshCookiePartitioned)
                .path(refreshCookiePath)
                .maxAge(refreshCookieMaxAge)
                .build();
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(refreshCookieSameSite)
                .partitioned(refreshCookiePartitioned)
                .path(refreshCookiePath)
                .maxAge(0)
                .build();
    }

    private String buildRefreshCookiePath(String contextPath) {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return "/auth/refresh";
        }

        var normalized = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized + "/auth/refresh";
    }
}

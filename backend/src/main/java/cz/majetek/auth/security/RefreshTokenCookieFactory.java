package cz.majetek.auth.security;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Factory responsible for building HttpOnly refresh token cookies.
 */
@Component
public class RefreshTokenCookieFactory {

    @Value("${jwt.refresh.cookie.secure:true}")
    private boolean secure;

    public Cookie build(String token, Instant expiry) {
        Cookie c = new Cookie("refreshToken", token);
        c.setHttpOnly(true);
        c.setSecure(secure);
        c.setPath("/api/auth");
        long maxAgeSeconds = Math.max(0, Duration.between(Instant.now(), expiry).getSeconds());
        c.setMaxAge((int) maxAgeSeconds);
        c.setAttribute("SameSite", "Strict");
        return c;
    }
}

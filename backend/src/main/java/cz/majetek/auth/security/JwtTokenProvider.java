package cz.majetek.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Provider for JWT token operations.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.access.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.access.secret}")
    private String jwtSecret;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationInMs;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    /**
     * Creates JwtTokenProvider with secret key from configuration.
     *
     * @throws IllegalStateException if JWT secret is not configured
     */
    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        this.accessKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        if (refreshSecret == null || refreshSecret.trim().isEmpty()) {
            throw new IllegalStateException("Refresh JWT secret is not configured");
        }
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates JWT token for authenticated user.
     *
     * @param authentication Authentication object
     * @return JWT token
     */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtExpirationInMs);
        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(accessKey)
                .compact();
    }

    /**
     * Gets username from JWT token.
     *
     * @param token JWT token
     * @return Username
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Validates JWT token.
     *
     * @param token JWT token
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Generates refresh token for authenticated user.
     * Longer-lived token used only to obtain new access tokens.
     */
    public String generateRefreshToken(Authentication authentication) {
        return generateRefreshTokenFromUsername(authentication.getName());
    }

    public String generateRefreshTokenFromUsername(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshExpirationInMs);
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(refreshKey)
                .compact();
    }

    /**
     * Validates refresh token integrity & expiration.
     */
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract username from refresh token.
     */
    public String getUsernameFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Extract JTI (unique identifier) from refresh token.
     */
    public String getJtiFromRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getId();
    }

    /**
     * Gets expiration instant from refresh token.
     */
    public Instant getExpirationDateFromRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload().getExpiration().toInstant();
    }

    /**
     * Gets expiration instant from JWT token.
     *
     * @param token JWT token
     * @return Expiration instant
     */
    public Instant getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload().getExpiration().toInstant();
    }
}

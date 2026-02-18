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
import java.util.Date;

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
        String subject = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
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
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationInMs);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
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
     * Gets expiration date from refresh token.
     */
    public Date getExpirationDateFromRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload().getExpiration();
    }

    /**
     * Gets expiration date from JWT token.
     *
     * @param token JWT token
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload().getExpiration();
    }
} 
package cz.majetek.auth.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenCookieFactoryTest {

    private RefreshTokenCookieFactory cookieFactory;

    @BeforeEach
    void setUp() {
        cookieFactory = new RefreshTokenCookieFactory();
        ReflectionTestUtils.setField(cookieFactory, "secure", true);
    }

    @Test
    void build_ShouldCreateCookieWithCorrectName() {
        Cookie cookie = cookieFactory.build("token123", Instant.parse("2030-01-01T12:00:00Z"));

        assertEquals("refreshToken", cookie.getName());
        assertEquals("token123", cookie.getValue());
    }

    @Test
    void build_ShouldSetHttpOnlyAndSecure() {
        Cookie cookie = cookieFactory.build("token123", Instant.parse("2030-01-01T12:00:00Z"));

        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
    }

    @Test
    void build_ShouldSetCorrectPath() {
        Cookie cookie = cookieFactory.build("token123", Instant.parse("2030-01-01T12:00:00Z"));

        assertEquals("/api/auth", cookie.getPath());
    }

    @Test
    void build_ShouldSetPositiveMaxAge() {
        Cookie cookie = cookieFactory.build("token123", Instant.parse("2030-01-01T12:00:00Z"));

        assertTrue(cookie.getMaxAge() > 0);
    }

    @Test
    void build_WithPastExpiry_ShouldSetZeroMaxAge() {
        Cookie cookie = cookieFactory.build("token123", Instant.EPOCH);

        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    void build_WithSecureFalse_ShouldCreateNonSecureCookie() {
        ReflectionTestUtils.setField(cookieFactory, "secure", false);
        Cookie cookie = cookieFactory.build("token123", Instant.parse("2030-01-01T12:00:00Z"));

        assertFalse(cookie.getSecure());
    }

    @Test
    void build_ShouldSetSameSiteStrict() {
        Cookie cookie = cookieFactory.build("token123", Instant.parse("2030-01-01T12:00:00Z"));

        assertEquals("Strict", cookie.getAttribute("SameSite"));
    }
}

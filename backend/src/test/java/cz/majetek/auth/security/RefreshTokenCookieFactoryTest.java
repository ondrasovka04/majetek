package cz.majetek.auth.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

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
        Date expiry = new Date(System.currentTimeMillis() + 3600_000);

        Cookie cookie = cookieFactory.build("token123", expiry);

        assertEquals("refreshToken", cookie.getName());
        assertEquals("token123", cookie.getValue());
    }

    @Test
    void build_ShouldSetHttpOnlyAndSecure() {
        Date expiry = new Date(System.currentTimeMillis() + 3600_000);

        Cookie cookie = cookieFactory.build("token123", expiry);

        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
    }

    @Test
    void build_ShouldSetCorrectPath() {
        Date expiry = new Date(System.currentTimeMillis() + 3600_000);

        Cookie cookie = cookieFactory.build("token123", expiry);

        assertEquals("/api/auth", cookie.getPath());
    }

    @Test
    void build_ShouldSetPositiveMaxAge() {
        Date expiry = new Date(System.currentTimeMillis() + 3600_000);

        Cookie cookie = cookieFactory.build("token123", expiry);

        assertTrue(cookie.getMaxAge() > 0);
    }

    @Test
    void build_WithPastExpiry_ShouldSetZeroMaxAge() {
        Date expiry = new Date(System.currentTimeMillis() - 1000);

        Cookie cookie = cookieFactory.build("token123", expiry);

        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    void build_WithSecureFalse_ShouldCreateNonSecureCookie() {
        ReflectionTestUtils.setField(cookieFactory, "secure", false);
        Date expiry = new Date(System.currentTimeMillis() + 3600_000);

        Cookie cookie = cookieFactory.build("token123", expiry);

        assertFalse(cookie.getSecure());
    }

    @Test
    void build_ShouldSetSameSiteStrict() {
        Date expiry = new Date(System.currentTimeMillis() + 3600_000);

        Cookie cookie = cookieFactory.build("token123", expiry);

        assertEquals("Strict", cookie.getAttribute("SameSite"));
    }
}

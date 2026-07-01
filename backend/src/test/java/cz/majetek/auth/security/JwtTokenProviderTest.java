package cz.majetek.auth.security;

import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_ACCESS_SECRET = "testSecretKeyForAccessTokenThatIsLongEnoughForHS256Algorithm";
    private static final String TEST_REFRESH_SECRET = "testSecretKeyForRefreshTokenThatIsLongEnoughForHS256Algorithm";
    private static final int TEST_ACCESS_EXPIRATION = 900000;
    private static final int TEST_REFRESH_EXPIRATION = 604800000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_ACCESS_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshSecret", TEST_REFRESH_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", TEST_ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationInMs", TEST_REFRESH_EXPIRATION);
        jwtTokenProvider.init();
    }

    @Test
    void init_WithValidSecrets_ShouldInitializeSuccessfully() {
        assertDoesNotThrow(() -> jwtTokenProvider.init());
    }

    @Test
    void init_WithNullAccessSecret_ShouldThrowException() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", null);
        ReflectionTestUtils.setField(provider, "refreshSecret", TEST_REFRESH_SECRET);

        assertThrows(IllegalStateException.class, provider::init);
    }

    @Test
    void init_WithEmptyAccessSecret_ShouldThrowException() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "");
        ReflectionTestUtils.setField(provider, "refreshSecret", TEST_REFRESH_SECRET);

        assertThrows(IllegalStateException.class, provider::init);
    }

    @Test
    void init_WithNullRefreshSecret_ShouldThrowException() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", TEST_ACCESS_SECRET);
        ReflectionTestUtils.setField(provider, "refreshSecret", null);
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", TEST_ACCESS_EXPIRATION);

        assertThrows(IllegalStateException.class, provider::init);
    }

    @Test
    void generateToken_WithValidAuthentication_ShouldGenerateToken() {
        Authentication auth = createAuthentication("testuser");

        String token = jwtTokenProvider.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUsernameFromJWT_WithValidToken_ShouldReturnUsername() {
        String expectedUsername = "testuser";
        Authentication auth = createAuthentication(expectedUsername);
        String token = jwtTokenProvider.generateToken(auth);

        String actualUsername = jwtTokenProvider.getUsernameFromJWT(token);

        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        Authentication auth = createAuthentication("testuser");
        String token = jwtTokenProvider.generateToken(auth);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void generateRefreshToken_WithValidAuthentication_ShouldGenerateToken() {
        Authentication auth = createAuthentication("testuser");

        String refreshToken = jwtTokenProvider.generateRefreshToken(auth);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void generateRefreshTokenFromUsername_WithValidUsername_ShouldGenerateToken() {
        String username = "testuser";

        String refreshToken = jwtTokenProvider.generateRefreshTokenFromUsername(username);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void validateRefreshToken_WithValidToken_ShouldReturnTrue() {
        Authentication auth = createAuthentication("testuser");
        String refreshToken = jwtTokenProvider.generateRefreshToken(auth);

        boolean isValid = jwtTokenProvider.validateRefreshToken(refreshToken);

        assertTrue(isValid);
    }

    @Test
    void validateRefreshToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.refresh.token";

        boolean isValid = jwtTokenProvider.validateRefreshToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void getUsernameFromRefreshToken_WithValidToken_ShouldReturnUsername() {
        String expectedUsername = "testuser";
        Authentication auth = createAuthentication(expectedUsername);
        String refreshToken = jwtTokenProvider.generateRefreshToken(auth);

        String actualUsername = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);

        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void getExpirationDateFromToken_WithValidToken_ShouldReturnFutureDate() {
        Authentication auth = createAuthentication("testuser");
        String token = jwtTokenProvider.generateToken(auth);

        Instant expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.isAfter(Instant.EPOCH));
    }

    @Test
    void getExpirationDateFromRefreshToken_WithValidToken_ShouldReturnFutureDate() {
        Authentication auth = createAuthentication("testuser");
        String refreshToken = jwtTokenProvider.generateRefreshToken(auth);

        Instant expirationDate = jwtTokenProvider.getExpirationDateFromRefreshToken(refreshToken);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.isAfter(Instant.EPOCH));
    }

    @Test
    void accessTokenAndRefreshToken_ShouldBeDifferent() {
        Authentication auth = createAuthentication("testuser");

        String accessToken = jwtTokenProvider.generateToken(auth);
        String refreshToken = jwtTokenProvider.generateRefreshToken(auth);

        assertNotEquals(accessToken, refreshToken);
    }

    @Test
    void refreshToken_ShouldHaveLongerExpiration() {
        Authentication auth = createAuthentication("testuser");
        String accessToken = jwtTokenProvider.generateToken(auth);
        String refreshToken = jwtTokenProvider.generateRefreshToken(auth);

        Instant accessExpiration = jwtTokenProvider.getExpirationDateFromToken(accessToken);
        Instant refreshExpiration = jwtTokenProvider.getExpirationDateFromRefreshToken(refreshToken);

        assertTrue(refreshExpiration.isAfter(accessExpiration));
    }

    private Authentication createAuthentication(String username) {
        User user = new User("1", username, "password", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}


package cz.majetek.exception.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthExceptionTest {
    @Test
    void constructor_ShouldSetMessage() {
        AuthException ex = new AuthException("test message");
        assertEquals("test message", ex.getMessage());
    }
}

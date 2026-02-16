package cz.majetek.exception.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserExceptionTest {
    @Test
    void notFound_ShouldContainId() {
        UserException ex = UserException.notFound("U123");
        assertTrue(ex.getMessage().contains("U123"));
        assertEquals("U123", ex.getUserId());
    }

    @Test
    void constructor_WithMessage_ShouldSetMessage() {
        UserException ex = new UserException("custom msg");
        assertEquals("custom msg", ex.getMessage());
        assertNull(ex.getUserId());
    }
}

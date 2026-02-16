package cz.majetek.exception.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameAlreadyExistsExceptionTest {
    @Test
    void constructor_ShouldSetMessage() {
        UsernameAlreadyExistsException ex = new UsernameAlreadyExistsException("username taken");
        assertEquals("username taken", ex.getMessage());
    }
}

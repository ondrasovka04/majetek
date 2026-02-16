package cz.majetek.exception.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserIdAlreadyExistsExceptionTest {
    @Test
    void constructor_ShouldSetMessage() {
        UserIdAlreadyExistsException ex = new UserIdAlreadyExistsException("id taken");
        assertEquals("id taken", ex.getMessage());
    }
}

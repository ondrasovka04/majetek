package cz.majetek.exception.auth;

/**
 * Exception thrown when attempting to register with an existing username.
 */
public class UserIdAlreadyExistsException extends RuntimeException {

    /**
     * Creates a new UsernameAlreadyExistsException.
     *
     * @param message Error message
     */
    public UserIdAlreadyExistsException(String message) {
        super(message);
    }
} 
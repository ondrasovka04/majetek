package cz.majetek.exception.auth;

/**
 * Exception thrown when attempting to register with an existing username.
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    /**
     * Creates a new UsernameAlreadyExistsException.
     *
     * @param message Error message
     */
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
} 
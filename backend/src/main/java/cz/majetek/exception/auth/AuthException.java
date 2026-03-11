package cz.majetek.exception.auth;

/**
 * Exception thrown for authentication and authorization errors.
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}

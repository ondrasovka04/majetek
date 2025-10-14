package cz.majetek.exception.auth;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}

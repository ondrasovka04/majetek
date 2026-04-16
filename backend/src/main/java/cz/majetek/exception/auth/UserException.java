package cz.majetek.exception.auth;

import lombok.Getter;

/**
 * Exception thrown for user validation or not-found errors.
 */
@Getter
public final class UserException extends RuntimeException {
    private final String userId;

    private UserException(String message, String userId) {
        super(message);
        this.userId = userId;
    }

    public UserException(String message) {
        this(message, null);
    }

    public static UserException notFound(String id) {
        return new UserException("User with id " + id + " not found", id);
    }
}

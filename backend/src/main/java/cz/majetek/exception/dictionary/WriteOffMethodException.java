package cz.majetek.exception.dictionary;

import lombok.Getter;

/**
 * Exception thrown for write-off method validation or not-found errors.
 */
@Getter
public final class WriteOffMethodException extends RuntimeException {
    private final String writeOffMethodId;

    private WriteOffMethodException(String message, String writeOffMethodId) {
        super(message);
        this.writeOffMethodId = writeOffMethodId;
    }

    public WriteOffMethodException(String message) {
        this(message, null);
    }

    public static WriteOffMethodException notFound(String id) {
        return new WriteOffMethodException("Write off method with id " + id + " not found", id);
    }
}
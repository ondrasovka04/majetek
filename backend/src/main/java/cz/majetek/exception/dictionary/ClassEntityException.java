package cz.majetek.exception.dictionary;

import lombok.Getter;

/**
 * Exception thrown for class entity validation or not-found errors.
 */
@Getter
public final class ClassEntityException extends RuntimeException {
    private final String classId;

    private ClassEntityException(String message, String classId) {
        super(message);
        this.classId = classId;
    }

    public ClassEntityException(String message) {
        this(message, null);
    }

    public static ClassEntityException notFound(String id) {
        return new ClassEntityException("Class with id " + id + " not found", id);
    }
}

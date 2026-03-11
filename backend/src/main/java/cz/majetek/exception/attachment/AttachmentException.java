package cz.majetek.exception.attachment;

/**
 * Exception thrown for attachment upload, download or deletion errors.
 */
public class AttachmentException extends RuntimeException {
    public AttachmentException(String message) {
        super(message);
    }
}

package cz.majetek.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Utility class for building standardized error responses.
 */
@Component
public class ErrorResponseUtils {


    /**
     * Builds error response with status and exception.
     *
     * @param status    HTTP status
     * @param exception Exception
     * @return ResponseEntity with ErrorResponse
     */
    public ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, Exception exception) {
        return buildErrorResponse(status, exception.getMessage());
    }

    /**
     * Builds error response with status and message.
     *
     * @param status  HTTP status
     * @param message Error message
     * @return ResponseEntity with ErrorResponse
     */
    public ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        return buildErrorResponse(status, message, List.of());
    }

    /**
     * Builds error response with status, message and field errors.
     *
     * @param status  HTTP status
     * @param message Error message
     * @param errors  List of field errors
     * @return ResponseEntity with ErrorResponse
     */
    public ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, List<ErrorFieldResponse> errors) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                errors,
                Instant.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
} 
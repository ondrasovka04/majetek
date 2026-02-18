package cz.majetek.exception;

import cz.majetek.exception.attachment.AttachmentException;
import cz.majetek.exception.auth.AuthException;
import cz.majetek.exception.auth.UserException;
import cz.majetek.exception.auth.UserIdAlreadyExistsException;
import cz.majetek.exception.auth.UsernameAlreadyExistsException;
import cz.majetek.exception.card.CardException;
import cz.majetek.exception.dictionary.*;
import cz.majetek.exception.inventoryaudit.InventoryAuditException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling across all controllers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorResponseUtils errorResponseUtils;

    public GlobalExceptionHandler(ErrorResponseUtils errorResponseUtils) {
        this.errorResponseUtils = errorResponseUtils;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "The requested endpoint does not exist"
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = ex.getCause() != null ? ex.getCause().getMessage() : "Invalid JSON format in request body";
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<ErrorFieldResponse> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorFieldResponse(error.getField(), error.getDefaultMessage()))
                .toList();

        return errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler({UsernameAlreadyExistsException.class, UserIdAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleUsernameOrUserIdAlreadyExistsException(RuntimeException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex) {
        HttpStatus status = ex.getUserId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(CardException.class)
    public ResponseEntity<ErrorResponse> handleCardException(CardException ex) {
        HttpStatus status = ex.getCardId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(AssetGroupException.class)
    public ResponseEntity<ErrorResponse> handleAssetGroupException(AssetGroupException ex) {
        HttpStatus status = ex.getAssetGroupId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(ClassEntityException.class)
    public ResponseEntity<ErrorResponse> handleClassEntityException(ClassEntityException ex) {
        HttpStatus status = ex.getClassId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(LocationException.class)
    public ResponseEntity<ErrorResponse> handleLocationException(LocationException ex) {
        HttpStatus status = ex.getLocationId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(PurchaseMethodException.class)
    public ResponseEntity<ErrorResponse> handlePurchaseMethodException(PurchaseMethodException ex) {
        HttpStatus status = ex.getPurchaseMethodId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(WriteOffMethodException.class)
    public ResponseEntity<ErrorResponse> handleWriteOffMethodException(WriteOffMethodException ex) {
        HttpStatus status = ex.getWriteOffMethodId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(InventoryAuditException.class)
    public ResponseEntity<ErrorResponse> handleInventoryAuditException(InventoryAuditException ex) {
        HttpStatus status = ex.getCardId() != null ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return errorResponseUtils.buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(AttachmentException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentException(AttachmentException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected exception that was not handled ", ex);
        return errorResponseUtils.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }
} 
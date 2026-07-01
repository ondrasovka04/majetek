package cz.majetek.exception;

import cz.majetek.exception.attachment.AttachmentException;
import cz.majetek.exception.auth.AuthException;
import cz.majetek.exception.auth.UserException;
import cz.majetek.exception.auth.UserIdAlreadyExistsException;
import cz.majetek.exception.auth.UsernameAlreadyExistsException;
import cz.majetek.exception.card.CardException;
import cz.majetek.exception.dictionary.*;
import cz.majetek.exception.inventoryaudit.InventoryAuditException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorResponseUtils errorResponseUtils;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(errorResponseUtils.buildErrorResponse(any(HttpStatus.class), anyString()))
                .thenAnswer(invocation -> {
                    HttpStatus status = invocation.getArgument(0);
                    String message = invocation.getArgument(1);
                    ErrorResponse errorResponse = new ErrorResponse(
                            status.value(),
                            status.getReasonPhrase(),
                            message,
                            List.of(),
                            Instant.parse("2030-01-01T12:00:00Z")
                    );
                    return ResponseEntity.status(status).body(errorResponse);
                });
        when(errorResponseUtils.buildErrorResponse(any(HttpStatus.class), anyString(), anyList()))
                .thenAnswer(invocation -> {
                    HttpStatus status = invocation.getArgument(0);
                    String message = invocation.getArgument(1);
                    List<ErrorFieldResponse> errors = invocation.getArgument(2);
                    return ResponseEntity.status(status).body(
                            new ErrorResponse(status.value(), status.getReasonPhrase(), message, errors, Instant.parse("2030-01-01T12:00:00Z")));
                });
    }

    @Test
    void handleCardException_WithCardId_ShouldReturnNotFound() {
        CardException exception = CardException.notFound(1);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCardException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(errorResponseUtils).buildErrorResponse(eq(HttpStatus.NOT_FOUND), anyString());
    }

    @Test
    void handleCardException_WithoutCardId_ShouldReturnBadRequest() {
        CardException exception = new CardException("No cards found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCardException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No cards found", response.getBody().getMessage());
        verify(errorResponseUtils).buildErrorResponse(HttpStatus.BAD_REQUEST, "No cards found");
    }

    @Test
    void handleUserException_ShouldReturnNotFound() {
        UserException exception = UserException.notFound("user123");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(errorResponseUtils).buildErrorResponse(eq(HttpStatus.NOT_FOUND), anyString());
    }

    @Test
    void handleUserException_WithoutUserId_ShouldReturnBadRequest() {
        UserException exception = new UserException("No users found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(HttpStatus.BAD_REQUEST, "No users found");
    }

    @Test
    void handleAuthException_ShouldReturnUnauthorized() {
        AuthException exception = new AuthException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleBadCredentialsException_ShouldReturnUnauthorized() {
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleAssetGroupException_ShouldReturnNotFound() {
        AssetGroupException exception = AssetGroupException.notFound("group1");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAssetGroupException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(eq(HttpStatus.NOT_FOUND), anyString());
    }

    @Test
    void handleAssetGroupException_WithoutId_ShouldReturnBadRequest() {
        AssetGroupException exception = new AssetGroupException("No asset groups");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAssetGroupException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(HttpStatus.BAD_REQUEST, "No asset groups");
    }

    @Test
    void handleLocationException_ShouldReturnNotFound() {
        LocationException exception = LocationException.notFound("loc1");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleLocationException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(eq(HttpStatus.NOT_FOUND), anyString());
    }

    @Test
    void handleLocationException_WithoutId_ShouldReturnBadRequest() {
        LocationException exception = new LocationException("No locations");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleLocationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(HttpStatus.BAD_REQUEST, "No locations");
    }

    @Test
    void handleMaxUploadSizeExceededException_ShouldReturnPayloadTooLarge() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1000L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxUploadSizeExceededException(exception);

        assertEquals(HttpStatus.CONTENT_TOO_LARGE, response.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(eq(HttpStatus.CONTENT_TOO_LARGE), anyString());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @Test
    void handleNoHandlerFoundException_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleNoHandlerFoundException();
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void handleAuthenticationException_ShouldReturnUnauthorized() {
        AuthenticationException ex = new AuthenticationException("Auth failed") {
        };
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleAuthenticationException(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void handleHttpMessageNotReadable_WithCause_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleHttpMessageNotReadableException();
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequest() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "field1", "must not be null"));
        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleMethodArgumentNotValidException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        verify(errorResponseUtils).buildErrorResponse(eq(HttpStatus.BAD_REQUEST), eq("Validation failed"), anyList());
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleIllegalArgumentException(new IllegalArgumentException("bad arg"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleUsernameAlreadyExistsException_ShouldReturnConflict() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleUsernameOrUserIdAlreadyExistsException(new UsernameAlreadyExistsException("taken"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    @Test
    void handleUserIdAlreadyExistsException_ShouldReturnConflict() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleUsernameOrUserIdAlreadyExistsException(new UserIdAlreadyExistsException("taken"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    @Test
    void handleHttpRequestMethodNotSupported_ShouldReturnMethodNotAllowed() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PATCH");
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleHttpRequestMethodNotSupportedException(ex);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp.getStatusCode());
    }

    @Test
    void handleMissingServletRequestParameter_ShouldReturnBadRequest() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("param", "String");
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleMissingServletRequestParameterException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleMethodArgumentTypeMismatch_ShouldReturnBadRequest() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getMessage()).thenReturn("type mismatch");
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleMethodArgumentTypeMismatchException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleClassEntityException_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleClassEntityException(ClassEntityException.notFound("C1"));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void handleClassEntityException_WithoutId_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleClassEntityException(new ClassEntityException("No classes"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handlePurchaseMethodException_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handlePurchaseMethodException(PurchaseMethodException.notFound("PM1"));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void handlePurchaseMethodException_WithoutId_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handlePurchaseMethodException(new PurchaseMethodException("No methods"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleWriteOffMethodException_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleWriteOffMethodException(WriteOffMethodException.notFound("WO1"));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void handleWriteOffMethodException_WithoutId_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleWriteOffMethodException(new WriteOffMethodException("No methods"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleInventoryAuditException_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleInventoryAuditException(new InventoryAuditException("audit error"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleInventoryAuditException_WithCardId_ShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleInventoryAuditException(InventoryAuditException.notFound(42));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void handleAttachmentException_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleAttachmentException(new AttachmentException("att error"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleInvalidDataAccessApiUsageException_ShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> resp = exceptionHandler.handleInvalidDataAccessApiUsageException();
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}

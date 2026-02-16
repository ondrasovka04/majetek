package cz.majetek.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseUtilsTest {

    private final ErrorResponseUtils errorResponseUtils = new ErrorResponseUtils();

    @Test
    void buildErrorResponse_WithStatusAndMessage_ShouldReturnCorrectResponse() {
        ResponseEntity<ErrorResponse> response = errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Something went wrong");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Something went wrong", response.getBody().getMessage());
        assertTrue(response.getBody().getErrors().isEmpty());
        assertNotNull(response.getBody().getTimeStamp());
    }

    @Test
    void buildErrorResponse_WithStatusAndException_ShouldUseExceptionMessage() {
        Exception ex = new RuntimeException("Test exception message");

        ResponseEntity<ErrorResponse> response = errorResponseUtils.buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Test exception message", response.getBody().getMessage());
    }

    @Test
    void buildErrorResponse_WithFieldErrors_ShouldIncludeErrors() {
        List<ErrorFieldResponse> fieldErrors = List.of(
                new ErrorFieldResponse("name", "must not be blank"),
                new ErrorFieldResponse("price", "must be positive")
        );

        ResponseEntity<ErrorResponse> response = errorResponseUtils.buildErrorResponse(
                HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getErrors().size());
        assertEquals("name", response.getBody().getErrors().getFirst().getField());
    }

    @Test
    void buildErrorResponse_WithNotFound_ShouldReturn404() {
        ResponseEntity<ErrorResponse> response = errorResponseUtils.buildErrorResponse(
                HttpStatus.NOT_FOUND, "Resource not found");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
    }
}

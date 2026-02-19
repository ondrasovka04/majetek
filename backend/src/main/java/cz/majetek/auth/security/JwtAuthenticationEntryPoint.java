package cz.majetek.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.exception.ErrorResponse;
import cz.majetek.exception.ErrorResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles JWT authentication failures and returns appropriate error responses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseUtils errorResponseUtils;
    private final ObjectMapper objectMapper;

    /**
     * Handles authentication failures by returning a JSON error response.
     *
     * @param request       HTTP request
     * @param response      HTTP response
     * @param authException Authentication exception
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Authentication failed: {}", authException.getMessage());

        String errorMessage;
        HttpStatus status;

        if (authException instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            errorMessage = "Invalid username or password";
        } else {
            status = HttpStatus.UNAUTHORIZED;
            errorMessage = "Unauthorized";
        }

        ResponseEntity<ErrorResponse> errorResponse = errorResponseUtils.buildErrorResponse(
                status,
                errorMessage
        );

        response.setStatus(errorResponse.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse.getBody()));
    }
} 
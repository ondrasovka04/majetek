package cz.majetek.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.exception.ErrorResponse;
import cz.majetek.exception.ErrorResponseUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles basic authentication failures and returns JSON error responses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    private final ErrorResponseUtils errorResponseUtils;
    private final ObjectMapper objectMapper;

    /**
     * Sets the realm name for basic authentication.
     */
    @PostConstruct
    public void init() {
        super.setRealmName("Majetek API");
    }

    /**
     * Handles basic authentication failures by returning a JSON error response.
     *
     * @param request       HTTP request
     * @param response      HTTP response
     * @param authException Authentication exception
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Basic authentication failed: {}", authException.getMessage());

        ResponseEntity<ErrorResponse> errorResponse;
        if (authException instanceof BadCredentialsException) {
            errorResponse = errorResponseUtils.buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        } else {
            errorResponse = errorResponseUtils.buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }


        response.setStatus(errorResponse.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse.getBody()));
    }
} 
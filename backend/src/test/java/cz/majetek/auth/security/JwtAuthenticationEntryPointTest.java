package cz.majetek.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.exception.ErrorResponse;
import cz.majetek.exception.ErrorResponseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private ErrorResponseUtils errorResponseUtils;

    @Mock
    private ObjectMapper objectMapper;

    private JwtAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint(errorResponseUtils, objectMapper);
    }

    @Test
    void commence_WithBadCredentials_ShouldReturn400() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException ex = new BadCredentialsException("bad creds");

        ErrorResponse body = new ErrorResponse(400, "Bad Request", "Invalid username or password", List.of(), Instant.now());
        when(errorResponseUtils.buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid username or password"))
                .thenReturn(ResponseEntity.status(400).body(body));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"Invalid username or password\"}");

        entryPoint.commence(request, response, ex);

        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid username or password"));
    }

    @Test
    void commence_WithOtherAuthException_ShouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        InsufficientAuthenticationException ex = new InsufficientAuthenticationException("unauth");

        ErrorResponse body = new ErrorResponse(401, "Unauthorized", "Unauthorized", List.of(), Instant.now());
        when(errorResponseUtils.buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized"))
                .thenReturn(ResponseEntity.status(401).body(body));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"Unauthorized\"}");

        entryPoint.commence(request, response, ex);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Unauthorized"));
    }
}

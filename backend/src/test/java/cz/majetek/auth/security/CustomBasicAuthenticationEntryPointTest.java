package cz.majetek.auth.security;

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
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomBasicAuthenticationEntryPointTest {

    @Mock
    private ErrorResponseUtils errorResponseUtils;

    @Mock
    private ObjectMapper objectMapper;

    private CustomBasicAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomBasicAuthenticationEntryPoint(errorResponseUtils, objectMapper);
        entryPoint.init();
    }

    @Test
    void commence_WithBadCredentials_ShouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException ex = new BadCredentialsException("bad");

        ErrorResponse body = new ErrorResponse(401, "Unauthorized", "Invalid username or password", List.of(), Instant.parse("2030-01-01T12:00:00Z"));
        when(errorResponseUtils.buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password"))
                .thenReturn(ResponseEntity.status(401).body(body));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        entryPoint.commence(request, response, ex);

        assertEquals(401, response.getStatus());
    }

    @Test
    void commence_WithOtherAuthException_ShouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        InsufficientAuthenticationException ex = new InsufficientAuthenticationException("unauth");

        ErrorResponse body = new ErrorResponse(401, "Unauthorized", "Unauthorized", List.of(), Instant.parse("2030-01-01T12:00:00Z"));
        when(errorResponseUtils.buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized"))
                .thenReturn(ResponseEntity.status(401).body(body));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        entryPoint.commence(request, response, ex);

        assertEquals(401, response.getStatus());
    }

    @Test
    void init_ShouldSetRealmName() {
        assertEquals("Majetek API", entryPoint.getRealmName());
    }
}

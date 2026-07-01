package cz.majetek.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.auth.dto.ChangePasswordRequest;
import cz.majetek.auth.dto.LoginResponse;
import cz.majetek.auth.dto.RegisterRequest;
import cz.majetek.auth.dto.UserDto;
import cz.majetek.auth.security.JwtTokenProvider;
import cz.majetek.auth.security.RefreshTokenCookieFactory;
import cz.majetek.auth.service.IAuthService;
import cz.majetek.entity.user.UserRole;
import cz.majetek.exception.ErrorResponseUtils;
import cz.majetek.exception.GlobalExceptionHandler;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAuthService authService;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenCookieFactory refreshTokenCookieFactory;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private UserDto testUserDto;
    private LoginResponse testLoginResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();

        testUserDto = new UserDto("U1", "Test User", "testuser", UserRole.USER);
        testLoginResponse = new LoginResponse("jwt-token", testUserDto, Instant.parse("2030-01-01T12:00:00Z"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_ShouldReturnLoginResponse() throws Exception {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(authService.login()).thenReturn(testLoginResponse);
        when(tokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn("refresh-token");
        when(tokenProvider.getExpirationDateFromRefreshToken("refresh-token")).thenReturn(Instant.parse("2030-01-01T12:00:00Z"));
        when(refreshTokenCookieFactory.build(anyString(), any(Instant.class))).thenReturn(new Cookie("refreshToken", "refresh-token"));

        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void refresh_WithValidCookie_ShouldReturnNewTokens() throws Exception {
        when(authService.refresh("old-refresh")).thenReturn(testLoginResponse);
        when(tokenProvider.getUsernameFromRefreshToken("old-refresh")).thenReturn("testuser");
        when(tokenProvider.generateRefreshTokenFromUsername("testuser")).thenReturn("new-refresh");
        when(tokenProvider.getExpirationDateFromRefreshToken("new-refresh")).thenReturn(Instant.parse("2030-01-01T12:00:00Z"));
        when(refreshTokenCookieFactory.build(anyString(), any(Instant.class))).thenReturn(new Cookie("refreshToken", "new-refresh"));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_WithValidData_ShouldReturnUser() throws Exception {
        RegisterRequest req = new RegisterRequest("U2", "newuser", "pass123", "New User");
        when(authService.register(any(RegisterRequest.class))).thenReturn(testUserDto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void changePassword_WithValidData_ShouldReturnOk() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("oldpass", "newpass");
        when(authService.changePassword(any(ChangePasswordRequest.class))).thenReturn(testUserDto);

        mockMvc.perform(put("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getAll_ShouldReturnUserList() throws Exception {
        when(authService.getAll()).thenReturn(List.of(testUserDto));

        mockMvc.perform(get("/api/auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void refresh_NoCookies_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_WrongCookieName_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("otherCookie", "someValue")))
                .andExpect(status().isUnauthorized());
    }
}

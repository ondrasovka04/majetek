package cz.majetek.auth.controller;

import cz.majetek.auth.dto.ChangePasswordRequest;
import cz.majetek.auth.dto.LoginResponse;
import cz.majetek.auth.dto.RegisterRequest;
import cz.majetek.auth.dto.UserDto;
import cz.majetek.auth.security.JwtTokenProvider;
import cz.majetek.auth.security.RefreshTokenCookieFactory;
import cz.majetek.auth.service.IAuthService;
import cz.majetek.entity.user.User;
import cz.majetek.exception.auth.UsernameAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

/**
 * Controller for handling authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    /**
     * Authenticates user and returns JWT token.
     *
     * @return LoginResponse containing JWT token
     * @throws BadCredentialsException (400) if credentials are invalid
     */
    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticates user using Basic Authentication and returns JWT token",
            security = @SecurityRequirement(name = "basicAuth")
    )
    public ResponseEntity<LoginResponse> login(HttpServletResponse response) {
        LoginResponse loginResponse = authService.login();
        String refreshToken = tokenProvider.generateRefreshToken(getContext().getAuthentication());
        Date refreshExp = tokenProvider.getExpirationDateFromRefreshToken(refreshToken);
        Cookie cookie = refreshTokenCookieFactory.build(refreshToken, refreshExp);
        response.addCookie(cookie);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Refreshes access & refresh tokens.
     *
     * @param request  containing the current refresh token
     * @param response to set the new refresh token cookie
     * @return LoginResponse with rotated tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Uses HttpOnly refresh token cookie to issue new access token and rotate refresh token")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshCookie(request);
        LoginResponse newAccess = authService.refresh(refreshToken);
        String username = tokenProvider.getUsernameFromRefreshToken(refreshToken);
        String newRefresh = tokenProvider.generateRefreshTokenFromUsername(username);
        Date refreshExp = tokenProvider.getExpirationDateFromRefreshToken(newRefresh);
        Cookie cookie = refreshTokenCookieFactory.build(newRefresh, refreshExp);
        response.addCookie(cookie);
        return ResponseEntity.ok(newAccess);
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new BadCredentialsException("Missing refresh token cookie");
        }
        for (Cookie c : request.getCookies()) {
            if ("refreshToken".equals(c.getName())) {
                return c.getValue();
            }
        }
        throw new BadCredentialsException("Refresh token cookie not found");
    }

    /**
     * Registers a new user.
     *
     * @param registerRequest User registration data
     * @return Created user entity
     * @throws UsernameAlreadyExistsException (400) if username is already taken
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    /**
     * Changes the password of the currently authenticated user.
     *
     * @param changePasswordRequest ChangePasswordRequest object containing current and new passwords
     * @return 200 OK if the password was changed successfully
     * @throws BadCredentialsException (400) if the current password is incorrect
     */
    @Operation(summary = "Change user password", description = "Allows an authenticated user to change their password")
    @PutMapping("/change-password")
    public ResponseEntity<UserDto> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.ok(authService.changePassword(changePasswordRequest));
    }

    /**
     * Get all asset groups.
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all users")
    @ApiResponse(responseCode = "200", description = "List of users",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(authService.getAll());
    }
} 
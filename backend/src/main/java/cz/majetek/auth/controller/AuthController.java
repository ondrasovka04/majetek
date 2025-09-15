package cz.majetek.auth.controller;

import cz.majetek.auth.dto.ChangePasswordRequest;
import cz.majetek.auth.dto.LoginResponse;
import cz.majetek.auth.dto.RegisterRequest;
import cz.majetek.auth.dto.UserDto;
import cz.majetek.auth.service.IAuthService;
import cz.majetek.entity.user.User;
import cz.majetek.exception.auth.UsernameAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

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
    public ResponseEntity<LoginResponse> login() {
        return ResponseEntity.ok(authService.login());
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
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
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
    public ResponseEntity<User> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
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
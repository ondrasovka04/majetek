package cz.majetek.auth.service;

import cz.majetek.auth.dto.ChangePasswordRequest;
import cz.majetek.auth.dto.LoginResponse;
import cz.majetek.auth.dto.RegisterRequest;
import cz.majetek.auth.dto.UserDto;
import cz.majetek.entity.user.User;
import cz.majetek.exception.auth.UsernameAlreadyExistsException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * Service interface for handling authentication operations.
 * Defines the contract for authentication-related business logic.
 */
public interface IAuthService {

    /**
     * Retrieves all User entities.
     *
     * @return a list of all User
     */
    List<UserDto> getAll();

    /**
     * Retrieves a User by its ID.
     *
     * @param id the ID of the User to retrieve
     * @return the User with the given ID
     */
    User getById(String id);

    /**
     * Authenticates user and generates JWT token.
     *
     * @return LoginResponse with JWT token and user info
     */
    LoginResponse login();

    /**
     * Refreshes access token using a valid refresh token from cookie.
     *
     * @param refreshToken the refresh token extracted from HttpOnly cookie
     * @return LoginResponse containing new access token & expiration
     */
    LoginResponse refresh(String refreshToken);

    /**
     * Registers a new user.
     *
     * @param registerRequest User registration data
     * @return Created user entity
     * @throws UsernameAlreadyExistsException if username is already taken
     */
    UserDto register(RegisterRequest registerRequest);

    /**
     * Changes the password of the currently authenticated user.
     *
     * @param changePasswordRequest object containing the current and new passwords
     * @return the updated user DTO after the password change
     * @throws RuntimeException          if the user is not authenticated
     * @throws UsernameNotFoundException if the user cannot be found in the repository
     * @throws BadCredentialsException   if the current password does not match the stored password
     */
    UserDto changePassword(ChangePasswordRequest changePasswordRequest);
} 
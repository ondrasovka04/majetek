package cz.majetek.auth.service;

import cz.majetek.auth.dto.ChangePasswordRequest;
import cz.majetek.auth.dto.LoginResponse;
import cz.majetek.auth.dto.RegisterRequest;
import cz.majetek.auth.dto.UserDto;
import cz.majetek.auth.mapper.IUserMapper;
import cz.majetek.auth.repository.IUserRepository;
import cz.majetek.auth.security.JwtTokenProvider;
import cz.majetek.auth.security.UserPrincipal;
import cz.majetek.auth.service.implementation.AuthService;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import cz.majetek.exception.auth.AuthException;
import cz.majetek.exception.auth.UserException;
import cz.majetek.exception.auth.UserIdAlreadyExistsException;
import cz.majetek.exception.auth.UsernameAlreadyExistsException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IUserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDto testUserDto;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        testUser = new User("1", "testuser", "encodedPassword", "Test User", UserRole.USER);
        testUserDto = new UserDto("1", "testuser", "Test User", UserRole.USER);
        userPrincipal = UserPrincipal.create(testUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAll_ShouldReturnAllUsers() {
        List<User> users = List.of(testUser);
        List<UserDto> userDtos = List.of(testUserDto);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(users)).thenReturn(userDtos);

        List<UserDto> result = authService.getAll();

        assertEquals(1, result.size());
        assertEquals(testUserDto, result.getFirst());
        verify(userRepository).findAll();
        verify(userMapper).toDto(users);
    }

    @Test
    void getById_WithExistingUser_ShouldReturnUser() {
        when(userRepository.findById("1")).thenReturn(Optional.of(testUser));

        User result = authService.getById("1");

        assertEquals(testUser, result);
        verify(userRepository).findById("1");
    }

    @Test
    void getById_WithNonExistingUser_ShouldThrowException() {
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> authService.getById("999"));
        verify(userRepository).findById("999");
    }

    @Test
    void login_WithAuthenticatedUser_ShouldReturnLoginResponse() {
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);
        when(tokenProvider.generateToken(auth)).thenReturn("accessToken");
        when(tokenProvider.getExpirationDateFromToken("accessToken")).thenReturn(Instant.parse("2030-01-01T12:00:00Z"));

        LoginResponse response = authService.login();

        assertNotNull(response);
        assertEquals("accessToken", response.getToken());
        assertEquals(testUserDto, response.getUser());
        verify(tokenProvider).generateToken(auth);
    }

    @Test
    void login_WithoutAuthentication_ShouldThrowException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(AuthException.class, () -> authService.login());
    }

    @Test
    void login_WithUnauthenticatedUser_ShouldThrowException() {
        Authentication auth = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.login());
    }

    @Test
    void refresh_WithValidToken_ShouldReturnNewAccessToken() {
        String refreshToken = "validRefreshToken";
        String jti = "test-jti-123";
        testUser.setRefreshJti(jti);
        when(tokenProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getJtiFromRefreshToken(refreshToken)).thenReturn(jti);
        when(tokenProvider.getUsernameFromRefreshToken(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("newAccessToken");
        when(tokenProvider.getExpirationDateFromToken("newAccessToken")).thenReturn(Instant.parse("2030-01-01T12:00:00Z"));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        LoginResponse response = authService.refresh(refreshToken);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getToken());
        assertEquals(testUserDto, response.getUser());
        verify(tokenProvider).validateRefreshToken(refreshToken);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void refresh_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalidRefreshToken";
        when(tokenProvider.validateRefreshToken(invalidToken)).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.refresh(invalidToken));
        verify(tokenProvider).validateRefreshToken(invalidToken);
        verify(userRepository, never()).findByUsername(any(String.class));
    }

    @Test
    void refresh_WithNonExistingUser_ShouldThrowException() {
        String refreshToken = "validRefreshToken";
        when(tokenProvider.validateRefreshToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getUsernameFromRefreshToken(refreshToken)).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    void register_WithValidRequest_ShouldCreateUser() {
        RegisterRequest request = new RegisterRequest("2", "newuser", "password", "New User");
        User newUser = new User("2", "newuser", "encodedPassword", "New User", UserRole.USER);
        UserDto newUserDto = new UserDto("2", "New User", "newuser", UserRole.USER);

        when(userRepository.existsById("2")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userMapper.toDto(newUser)).thenReturn(newUserDto);

        UserDto result = authService.register(request);

        assertNotNull(result);
        assertEquals("2", result.getId());
        assertEquals("newuser", result.getUsername());
        verify(userRepository).existsById("2");
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingUserId_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest("1", "newuser", "password", "New User");
        when(userRepository.existsById("1")).thenReturn(true);

        assertThrows(UserIdAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository).existsById("1");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest("2", "testuser", "password", "New User");
        when(userRepository.existsById("2")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository).existsById("2");
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_WithCorrectCurrentPassword_ShouldUpdatePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = authService.changePassword(request);

        assertNotNull(result);
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_ShouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword");
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.changePassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_WithoutAuthentication_ShouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(AuthException.class, () -> authService.changePassword(request));
        verify(userRepository, never()).findByUsername(any(String.class));
    }

    @Test
    void changePassword_WithNonExistingUser_ShouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.changePassword(request));
        verify(passwordEncoder, never()).matches(any(String.class), any(String.class));
    }
}




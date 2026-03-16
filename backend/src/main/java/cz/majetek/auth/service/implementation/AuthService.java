package cz.majetek.auth.service.implementation;

import cz.majetek.auth.dto.ChangePasswordRequest;
import cz.majetek.auth.dto.LoginResponse;
import cz.majetek.auth.dto.RegisterRequest;
import cz.majetek.auth.dto.UserDto;
import cz.majetek.auth.mapper.IUserMapper;
import cz.majetek.auth.repository.IUserRepository;
import cz.majetek.auth.security.JwtTokenProvider;
import cz.majetek.auth.security.UserPrincipal;
import cz.majetek.auth.service.IAuthService;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import cz.majetek.exception.auth.AuthException;
import cz.majetek.exception.auth.UserException;
import cz.majetek.exception.auth.UserIdAlreadyExistsException;
import cz.majetek.exception.auth.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Implementation of the AuthService interface.
 * Provides concrete implementation of authentication-related business logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final JwtTokenProvider tokenProvider;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userMapper.toDto(userRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> UserException.notFound(id));
    }

    @Override
    public LoginResponse login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("User is not authenticated");
        }
        User user = ((UserPrincipal) authentication.getPrincipal()).user();
        UserDto userDto = userMapper.toDto(user);
        String accessToken = tokenProvider.generateToken(authentication);
        Date accessExpiration = tokenProvider.getExpirationDateFromToken(accessToken);
        return new LoginResponse(accessToken, userDto, accessExpiration);
    }

    @Override
    @Transactional
    public LoginResponse refresh(String refreshToken) {
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new AuthException("Invalid refresh token");
        }
        String jti = tokenProvider.getJtiFromRefreshToken(refreshToken);
        String username = tokenProvider.getUsernameFromRefreshToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (jti == null || !jti.equals(user.getRefreshJti())) {
            throw new AuthException("Refresh token has been revoked");
        }
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        String newAccess = tokenProvider.generateToken(authentication);
        Date newAccessExp = tokenProvider.getExpirationDateFromToken(newAccess);
        UserDto userDto = userMapper.toDto(user);
        return new LoginResponse(newAccess, userDto, newAccessExp);
    }


    @Override
    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        if (userRepository.existsById(registerRequest.getId())) {
            throw new UserIdAlreadyExistsException("User Id is already taken");
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        User saved = userRepository.save(new User(
                registerRequest.getId(),
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getName(),
                UserRole.USER));
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void saveRefreshJti(String username, String jti) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setRefreshJti(jti);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDto changePassword(ChangePasswordRequest changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException("User is not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

} 
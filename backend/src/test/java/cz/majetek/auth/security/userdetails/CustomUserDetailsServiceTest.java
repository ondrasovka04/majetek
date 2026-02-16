package cz.majetek.auth.security.userdetails;

import cz.majetek.auth.repository.IUserRepository;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_WithNonExistingUser_ShouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void loadUserByUsername_WithAdminUser_ShouldReturnAdminRole() {
        User admin = new User("2", "admin", "password", "Admin User", UserRole.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }
}


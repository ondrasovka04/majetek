package cz.majetek.auth.security;

import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    void create_ShouldReturnUserPrincipal() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);

        UserPrincipal principal = UserPrincipal.create(user);

        assertNotNull(principal);
        assertEquals(user, principal.user());
    }

    @Test
    void getAuthorities_WithUserRole_ShouldContainRoleUser() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void getAuthorities_WithAdminRole_ShouldContainRoleAdmin() {
        User user = new User("1", "admin", "password", "Admin User", UserRole.ADMIN);
        UserPrincipal principal = UserPrincipal.create(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void getUsername_ShouldReturnUsername() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);

        assertEquals("testuser", principal.getUsername());
    }

    @Test
    void getPassword_ShouldReturnPassword() {
        User user = new User("1", "testuser", "secret123", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);

        assertEquals("secret123", principal.getPassword());
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);

        assertTrue(principal.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);

        assertTrue(principal.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);

        assertTrue(principal.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnTrue() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        UserPrincipal principal = UserPrincipal.create(user);

        assertTrue(principal.isEnabled());
    }
}

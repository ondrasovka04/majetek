package cz.majetek.auth.security;

import cz.majetek.entity.user.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation for Spring Security.
 */
public record UserPrincipal(User user) implements UserDetails {

    /**
     * Creates UserPrincipal from User entity.
     *
     * @param user User entity
     * @return UserPrincipal instance
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(user);
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return user.getUsername();
    }
} 
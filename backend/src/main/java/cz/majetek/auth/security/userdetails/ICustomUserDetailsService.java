package cz.majetek.auth.security.userdetails;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Service interface for loading user details.
 * Extends Spring Security's UserDetailsService.
 */
public interface ICustomUserDetailsService extends UserDetailsService {

    /**
     * Loads user by username.
     *
     * @param username Username to load
     * @return UserDetails instance
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @NullMarked
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
} 
package cz.majetek.auth.security.userdetails;

import cz.majetek.auth.repository.IUserRepository;
import cz.majetek.auth.security.UserPrincipal;
import cz.majetek.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of the CustomUserDetailsService interface.
 * Provides concrete implementation for loading user details.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ICustomUserDetailsService {

    private final IUserRepository userRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return UserPrincipal.create(user);
    }
} 
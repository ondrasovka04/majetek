package cz.majetek.auth.repository;

import cz.majetek.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity operations.
 */
@Repository
public interface IUserRepository extends JpaRepository<User, String> {

    /**
     * Finds user by username.
     *
     * @param username Username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if username exists.
     *
     * @param username Username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);
} 
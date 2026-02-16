package cz.majetek.auth.repository;

import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IUserRepository userRepository;

    @Test
    void findByUsername_WithExistingUser_ShouldReturnUser() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void findByUsername_WithNonExistingUser_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void existsByUsername_WithExistingUser_ShouldReturnTrue() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByUsername("testuser");

        assertTrue(exists);
    }

    @Test
    void existsByUsername_WithNonExistingUser_ShouldReturnFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertFalse(exists);
    }

    @Test
    void save_ShouldPersistUser() {
        User user = new User("2", "newuser", "password", "New User", UserRole.ADMIN);

        User saved = userRepository.save(user);

        assertNotNull(saved);
        assertEquals("newuser", saved.getUsername());
        assertEquals(UserRole.ADMIN, saved.getRole());

        User found = entityManager.find(User.class, "2");
        assertNotNull(found);
        assertEquals("newuser", found.getUsername());
    }

    @Test
    void findById_WithExistingUser_ShouldReturnUser() {
        User user = new User("1", "testuser", "password", "Test User", UserRole.USER);
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findById("1");

        assertTrue(found.isPresent());
        assertEquals("1", found.get().getId());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user1 = new User("1", "user1", "pass1", "User One", UserRole.USER);
        User user2 = new User("2", "user2", "pass2", "User Two", UserRole.ADMIN);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        var users = userRepository.findAll();

        assertEquals(2, users.size());
    }
}


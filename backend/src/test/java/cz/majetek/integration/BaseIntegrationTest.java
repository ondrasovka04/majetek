package cz.majetek.integration;

import cz.majetek.auth.repository.IUserRepository;
import cz.majetek.entity.user.User;
import cz.majetek.entity.user.UserRole;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for integration tests providing common setup and utilities.
 * Sets up test users (admin and regular user) and provides authentication helpers.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected IUserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    // Instance tokens - each test class gets fresh tokens
    protected String adminAccessToken;
    protected String userAccessToken;
    protected Cookie adminRefreshCookie;

    protected static final String ADMIN_ID = "ADM01";
    protected static final String ADMIN_USERNAME = "integrationAdmin";
    protected static final String ADMIN_PASSWORD = "adminPass123";

    protected static final String USER_ID = "USR01";
    protected static final String USER_USERNAME = "integrationUser";
    protected static final String USER_PASSWORD = "userPass123";

    @BeforeAll
    void setUpBase() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Create ADMIN user if not exists
        if (!userRepository.existsById(ADMIN_ID)) {
            User admin = new User(ADMIN_ID, ADMIN_USERNAME, passwordEncoder.encode(ADMIN_PASSWORD), "Integration Admin", UserRole.ADMIN);
            userRepository.save(admin);
        }

        // Create regular USER if not exists
        if (!userRepository.existsById(USER_ID)) {
            User user = new User(USER_ID, USER_USERNAME, passwordEncoder.encode(USER_PASSWORD), "Integration User", UserRole.USER);
            userRepository.save(user);
        }

        // Always login to get fresh tokens for this test class
        loginAsAdmin();
        loginAsUser();
    }

    protected void loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        adminAccessToken = json.get("token").asString();
        adminRefreshCookie = result.getResponse().getCookie("refreshToken");
    }

    protected void loginAsUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(USER_USERNAME, USER_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        userAccessToken = json.get("token").asString();
    }

    protected String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    protected String bearerAdmin() {
        return "Bearer " + adminAccessToken;
    }

    protected String bearerUser() {
        return "Bearer " + userAccessToken;
    }
}

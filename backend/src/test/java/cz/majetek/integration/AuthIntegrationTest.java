package cz.majetek.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication and user management.
 * Tests login, refresh token, registration, password change, and role-based access.
 */
class AuthIntegrationTest extends BaseIntegrationTest {

    // ===================== Unauthenticated Access =====================

    @Test
    @Order(10)
    void unauthenticatedRequest_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/card"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(11)
    void unauthenticatedRequest_toProtectedEndpoint_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/asset-group"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(12)
    void refreshEndpoint_withoutCookie_shouldReturn400() throws Exception {
        // /api/auth/refresh is permitAll() but requires valid cookie
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest());
    }

    // ===================== Login with BasicAuth =====================

    @Test
    @Order(20)
    void login_withValidCredentials_shouldReturnTokenAndCookie() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value(ADMIN_USERNAME))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.expiresIn").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @Order(21)
    void login_withInvalidPassword_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(ADMIN_USERNAME, "wrongPassword")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(22)
    void login_withNonExistentUser_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth("nonexistent", "password")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    void login_withoutAuthHeader_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(24)
    void login_asRegularUser_shouldReturnUserRole() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(USER_USERNAME, USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    // ===================== Refresh Token =====================

    @Test
    @Order(30)
    void refresh_withValidCookie_shouldReturnNewToken() throws Exception {
        assertNotNull(adminRefreshCookie, "Admin must be logged in first");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(adminRefreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value(ADMIN_USERNAME))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @Order(31)
    void refresh_withInvalidCookieName_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("wrongName", "someValue")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(32)
    void refresh_withInvalidCookieValue_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid.token.value")))
                .andExpect(status().isBadRequest());
    }

    // ===================== JWT-protected Access =====================

    @Test
    @Order(40)
    void authenticatedRequest_withValidJwt_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/asset-group")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(41)
    void authenticatedRequest_withInvalidJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/asset-group")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(42)
    void authenticatedRequest_withExpiredJwt_shouldReturn401() throws Exception {
        // Simulating expired token (malformed token will also fail validation)
        mockMvc.perform(get("/api/asset-group")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxfQ.invalid"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== User Registration (Admin only) =====================

    @Test
    @Order(50)
    void register_asAdmin_shouldCreateUser() throws Exception {
        String registerJson = objectMapper.writeValueAsString(
                Map.of("id", "REG01", "username", "newregistered", "password", "pass123", "name", "Registered User"));

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newregistered"))
                .andExpect(jsonPath("$.name").value("Registered User"));
    }

    @Test
    @Order(51)
    void register_asRegularUser_shouldReturn403() throws Exception {
        String registerJson = objectMapper.writeValueAsString(
                Map.of("id", "REG02", "username", "forbidden", "password", "pass123", "name", "Forbidden"));

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", bearerUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(52)
    void register_withDuplicateUsername_shouldReturn400() throws Exception {
        String registerJson = objectMapper.writeValueAsString(
                Map.of("id", "REG03", "username", ADMIN_USERNAME, "password", "pass123", "name", "Duplicate"));

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(53)
    void register_withBlankUsername_shouldReturn400() throws Exception {
        String invalidJson = """
                {"id": "REG04", "username": "", "password": "pass123", "name": "Invalid"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(54)
    void register_withDuplicateId_shouldReturn400() throws Exception {
        String registerJson = objectMapper.writeValueAsString(
                Map.of("id", ADMIN_ID, "username", "uniquename", "password", "pass123", "name", "Duplicate ID"));

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());
    }

    // ===================== Get Users =====================

    @Test
    @Order(60)
    void getUsers_withValidToken_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/auth")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].username", hasItems(ADMIN_USERNAME, USER_USERNAME)));
    }

    @Test
    @Order(61)
    void getUsers_asRegularUser_shouldAlsoSucceed() throws Exception {
        // Assuming regular users can also view the user list
        mockMvc.perform(get("/api/auth")
                        .header("Authorization", bearerUser()))
                .andExpect(status().isOk());
    }

    // ===================== Password Change =====================

    @Test
    @Order(70)
    void changePassword_withCorrectCurrentPassword_shouldSucceed() throws Exception {
        // Generate unique user ID for this test run (max 5 chars per RegisterRequest validation)
        String uniqueId = "P" + (System.currentTimeMillis() % 1000);
        String username = "pwduser" + uniqueId;
        String oldPassword = "oldPass123";
        String newPassword = "newPass456";

        // Create a temporary user for password change test
        String tempUserJson = objectMapper.writeValueAsString(
                Map.of("id", uniqueId, "username", username, "password", oldPassword, "name", "PWD User"));

        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tempUserJson))
                .andExpect(status().isOk());

        // Login as the temp user
        var result = mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(username, oldPassword)))
                .andExpect(status().isOk())
                .andReturn();

        String tempToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        // Change password
        String changeJson = objectMapper.writeValueAsString(
                Map.of("currentPassword", oldPassword, "newPassword", newPassword));

        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", "Bearer " + tempToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        // Verify new password works
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(username, newPassword)))
                .andExpect(status().isOk());

        // Verify old password no longer works
        mockMvc.perform(post("/api/auth/login")
                        .header("Authorization", basicAuth(username, oldPassword)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(71)
    void changePassword_withWrongCurrentPassword_shouldReturn400() throws Exception {
        String changeJson = objectMapper.writeValueAsString(
                Map.of("currentPassword", "totallyWrong", "newPassword", "anything"));

        mockMvc.perform(put("/api/auth/change-password")
                        .header("Authorization", bearerUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeJson))
                .andExpect(status().isBadRequest());
    }
}

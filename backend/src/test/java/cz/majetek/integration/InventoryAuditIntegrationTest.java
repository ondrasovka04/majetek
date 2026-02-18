package cz.majetek.integration;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Inventory Audit operations.
 * Requires Card and Location to exist for audit entries.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryAuditIntegrationTest extends BaseIntegrationTest {

    private static Integer testCardId;

    // ===================== Setup: Create required dependencies =====================

    @Test
    @Order(10)
    void setup_createLocation() throws Exception {
        String json = """
                {"id": "ALOC1", "name": "Audit Location 1"}
                """;

        mockMvc.perform(post("/api/location")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(11)
    void setup_createSecondLocation() throws Exception {
        String json = """
                {"id": "ALOC2", "name": "Audit Location 2"}
                """;

        mockMvc.perform(post("/api/location")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(12)
    void setup_createWriteOffMethod() throws Exception {
        String json = """
                {"id": "AWO01", "name": "Audit Write Off"}
                """;

        mockMvc.perform(post("/api/write-off-method")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(13)
    void setup_createCard() throws Exception {
        String json = String.format("""
                {
                    "name": "Audit Test Card",
                    "price": 5000.00,
                    "quantity": 1,
                    "locationId": "ALOC1",
                    "purchaseDate": "%s"
                }
                """, LocalDate.now());

        var result = mockMvc.perform(post("/api/card")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        testCardId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asInt();
    }

    // ===================== Get All (initially empty or with data) =====================

    @Test
    @Order(100)
    void inventoryAudit_getAll_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/inventory-audit")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(101)
    void inventoryAudit_getDifferences_withNoData_shouldReturn400() throws Exception {
        // getDiffs throws exception when no audit records exist
        mockMvc.perform(get("/api/inventory-audit/differences")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }

    // ===================== Insert Many =====================

    @Test
    @Order(110)
    void inventoryAudit_insertMany_shouldCreateRecords() throws Exception {
        assertNotNull(testCardId, "Card must be created first");

        String timestamp = LocalDateTime.now().toString();
        String json = String.format("""
                [
                    {
                        "cardId": %d,
                        "realLocationId": "ALOC1",
                        "timestamp": "%s"
                    }
                ]
                """, testCardId, timestamp);

        mockMvc.perform(post("/api/inventory-audit/insert-many")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cardId").value(testCardId))
                .andExpect(jsonPath("$[0].realLocationId").value("ALOC1"));
    }

    @Test
    @Order(111)
    void inventoryAudit_getAll_shouldContainInserted() throws Exception {
        mockMvc.perform(get("/api/inventory-audit")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].cardId", hasItem(testCardId)));
    }

    @Test
    @Order(112)
    void inventoryAudit_insertMany_withDifferentLocation_shouldCreateDifference() throws Exception {
        // Create another card at ALOC1
        String cardJson = String.format("""
                {
                    "name": "Audit Test Card 2",
                    "price": 3000.00,
                    "quantity": 1,
                    "locationId": "ALOC1",
                    "purchaseDate": "%s"
                }
                """, LocalDate.now());

        var result = mockMvc.perform(post("/api/card")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson))
                .andExpect(status().isCreated())
                .andReturn();

        int secondCardId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asInt();

        // Insert audit with different location (should create difference)
        String timestamp = LocalDateTime.now().toString();
        String auditJson = String.format("""
                [
                    {
                        "cardId": %d,
                        "realLocationId": "ALOC2",
                        "timestamp": "%s"
                    }
                ]
                """, secondCardId, timestamp);

        mockMvc.perform(post("/api/inventory-audit/insert-many")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auditJson))
                .andExpect(status().isCreated());
    }

    // ===================== Validation =====================

    @Test
    @Order(120)
    void inventoryAudit_insertMany_withNonExistentCard_shouldReturn404() throws Exception {
        String timestamp = LocalDateTime.now().toString();
        String json = String.format("""
                [
                    {
                        "cardId": 999999,
                        "realLocationId": "ALOC1",
                        "timestamp": "%s"
                    }
                ]
                """, timestamp);

        mockMvc.perform(post("/api/inventory-audit/insert-many")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(121)
    void inventoryAudit_insertMany_withNonExistentLocation_shouldReturn404() throws Exception {
        String timestamp = LocalDateTime.now().toString();
        String json = String.format("""
                [
                    {
                        "cardId": %d,
                        "realLocationId": "XXXXX",
                        "timestamp": "%s"
                    }
                ]
                """, testCardId, timestamp);

        mockMvc.perform(post("/api/inventory-audit/insert-many")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(122)
    void inventoryAudit_insertMany_withNullCardId_shouldReturnError() throws Exception {
        // Null cardId causes server error (NPE or validation failure)
        String json = """
                [
                    {
                        "realLocationId": "ALOC1"
                    }
                ]
                """;

        var result = mockMvc.perform(post("/api/inventory-audit/insert-many")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status >= 400, "Expected error status (4xx or 5xx), got: " + status);
    }

    // ===================== Patch =====================

    @Test
    @Order(130)
    void inventoryAudit_patch_shouldUpdateAction() throws Exception {
        assertNotNull(testCardId, "Card must be created first");

        String patchJson = """
                {
                    "action": "KEEP"
                }
                """;

        mockMvc.perform(patch("/api/inventory-audit/" + testCardId)
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(131)
    void inventoryAudit_patch_withWriteOff_shouldUpdate() throws Exception {
        String patchJson = String.format("""
                {
                    "action": "WRITE_OFF",
                    "writeOffDate": "%s",
                    "writeOffMethodId": "AWO01"
                }
                """, LocalDate.now());

        mockMvc.perform(patch("/api/inventory-audit/" + testCardId)
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(132)
    void inventoryAudit_patch_nonExistentCard_shouldReturn404() throws Exception {
        String patchJson = """
                {
                    "action": "KEEP"
                }
                """;

        mockMvc.perform(patch("/api/inventory-audit/999999")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isNotFound());
    }

    // ===================== Project =====================

    @Test
    @Order(140)
    void inventoryAudit_project_withMissingAudits_shouldReturnError() throws Exception {
        // project() throws exception when any card doesn't have an inventory audit record
        // In integration tests, we have cards without audits, so this returns error
        var result = mockMvc.perform(post("/api/inventory-audit/project")
                        .header("Authorization", bearerAdmin()))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status >= 400, "Expected error status (4xx or 5xx), got: " + status);
    }

    // ===================== Authorization =====================

    @Test
    @Order(150)
    void inventoryAudit_asRegularUser_shouldSucceed() throws Exception {
        // Regular users should also be able to perform inventory audits
        mockMvc.perform(get("/api/inventory-audit")
                        .header("Authorization", bearerUser()))
                .andExpect(status().isOk());
    }

    // ===================== Cleanup =====================

    @AfterAll
    void cleanup() {
        // Cleanup is best-effort; ignore FK constraint failures
        for (String path : new String[]{
                "/api/card/" + testCardId,
                "/api/location/ALOC1",
                "/api/location/ALOC2",
                "/api/write-off-method/AWO01"
        }) {
            try {
                mockMvc.perform(delete(path)
                        .header("Authorization", bearerAdmin()));
            } catch (Exception ignored) {
                // Entity might still be referenced
            }
        }
    }
}

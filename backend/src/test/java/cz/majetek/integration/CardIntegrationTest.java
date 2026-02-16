package cz.majetek.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Card CRUD operations.
 * Cards require existing Location, so setup creates necessary dependencies.
 */
class CardIntegrationTest extends BaseIntegrationTest {

    private static Integer createdCardId;

    // ===================== Setup: Create required dictionary entries =====================

    @Test
    @Order(10)
    void setup_createLocation() throws Exception {
        String json = """
                {"id": "CLOC1", "name": "Card Test Location"}
                """;

        mockMvc.perform(post("/api/location")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    void setup_createAssetGroup() throws Exception {
        String json = """
                {"id": "CGRP1", "name": "Card Test Group"}
                """;

        mockMvc.perform(post("/api/asset-group")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @Order(12)
    void setup_createClass() throws Exception {
        String json = """
                {"id": "CCLS1", "name": "Card Test Class"}
                """;

        mockMvc.perform(post("/api/class")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // ===================== Card CRUD =====================

    @Test
    @Order(100)
    void card_create_shouldPersistAndReturn() throws Exception {
        String json = String.format("""
                {
                    "name": "Test Laptop",
                    "price": 25000.00,
                    "quantity": 1,
                    "serialNumber": "SN123456",
                    "locationId": "CLOC1",
                    "classId": "CCLS1",
                    "groupId": "CGRP1",
                    "purchaseDate": "%s",
                    "note": "Integration test card"
                }
                """, LocalDate.now());

        var result = mockMvc.perform(post("/api/card")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Laptop"))
                .andExpect(jsonPath("$.price").value(25000.00))
                .andExpect(jsonPath("$.serialNumber").value("SN123456"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        // Store card ID for later tests
        String responseBody = result.getResponse().getContentAsString();
        createdCardId = objectMapper.readTree(responseBody).get("id").asInt();
    }

    @Test
    @Order(101)
    void card_getById_shouldReturnCreated() throws Exception {
        assertNotNull(createdCardId, "Card must be created first");

        mockMvc.perform(get("/api/card/" + createdCardId)
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCardId))
                .andExpect(jsonPath("$.name").value("Test Laptop"));
    }

    @Test
    @Order(102)
    void card_getAll_shouldContainCreated() throws Exception {
        mockMvc.perform(get("/api/card")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem(createdCardId)));
    }

    @Test
    @Order(103)
    void card_filter_byLocation_shouldReturnMatching() throws Exception {
        mockMvc.perform(get("/api/card/filter")
                        .header("Authorization", bearerAdmin())
                        .param("locationId", "CLOC1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem(createdCardId)));
    }

    @Test
    @Order(104)
    void card_filter_byName_shouldReturnMatching() throws Exception {
        mockMvc.perform(get("/api/card/filter")
                        .header("Authorization", bearerAdmin())
                        .param("name", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @Order(105)
    void card_filter_byNonExistentLocation_shouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/api/card/filter")
                        .header("Authorization", bearerAdmin())
                        .param("locationId", "XXXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Order(110)
    void card_update_shouldChangeValues() throws Exception {
        assertNotNull(createdCardId, "Card must be created first");

        String updateJson = String.format("""
                {
                    "name": "Updated Laptop",
                    "price": 30000.00,
                    "quantity": 2,
                    "locationId": "CLOC1",
                    "purchaseDate": "%s",
                    "note": "Updated note"
                }
                """, LocalDate.now());

        mockMvc.perform(patch("/api/card/" + createdCardId)
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.price").value(30000.00))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @Order(111)
    void card_update_verifyChangePersisted() throws Exception {
        mockMvc.perform(get("/api/card/" + createdCardId)
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"))
                .andExpect(jsonPath("$.note").value("Updated note"));
    }

    // ===================== Validation =====================

    static Stream<Arguments> invalidCardPayloads() {
        String date = LocalDate.now().toString();
        return Stream.of(
                Arguments.of("""
                        {"price": 1000.00, "locationId": "CLOC1", "purchaseDate": "%s"}
                        """.formatted(date)),
                Arguments.of("""
                        {"name": "No Location Card", "purchaseDate": "%s"}
                        """.formatted(date)),
                Arguments.of("""
                        {"name": "Bad Location Card", "locationId": "XXXXX", "purchaseDate": "%s"}
                        """.formatted(date)),
                Arguments.of("""
                        {"name": "No Date Card", "locationId": "CLOC1"}
                        """)
        );
    }

    @ParameterizedTest(name = "card_create with {0} shouldReturn400")
    @MethodSource("invalidCardPayloads")
    @Order(120)
    void card_create_withInvalidPayload_shouldReturn400(String invalidJson) throws Exception {
        mockMvc.perform(post("/api/card")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ===================== Create second card for filter tests =====================

    @Test
    @Order(130)
    void card_createSecond_forFilterTests() throws Exception {
        String json = String.format("""
                {
                    "name": "Test Monitor",
                    "price": 8000.00,
                    "quantity": 3,
                    "locationId": "CLOC1",
                    "groupId": "CGRP1",
                    "purchaseDate": "%s"
                }
                """, LocalDate.now().minusDays(30));

        mockMvc.perform(post("/api/card")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(131)
    void card_filter_byPriceRange_shouldReturnMatching() throws Exception {
        mockMvc.perform(get("/api/card/filter")
                        .header("Authorization", bearerAdmin())
                        .param("minPrice", "5000")
                        .param("maxPrice", "10000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].name", hasItem("Test Monitor")));
    }

    @Test
    @Order(132)
    void card_filter_byGroup_shouldReturnMatching() throws Exception {
        mockMvc.perform(get("/api/card/filter")
                        .header("Authorization", bearerAdmin())
                        .param("groupId", "CGRP1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ===================== Delete =====================

    @Test
    @Order(200)
    void card_delete_shouldReturn204() throws Exception {
        assertNotNull(createdCardId, "Card must be created first");

        mockMvc.perform(delete("/api/card/" + createdCardId)
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(201)
    void card_getDeleted_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/card/" + createdCardId)
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(202)
    void card_deleteNonExistent_shouldReturn400() throws Exception {
        mockMvc.perform(delete("/api/card/999999")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }

    // ===================== Cleanup =====================

    @AfterAll
    void cleanup() {
        // Delete in reverse dependency order; ignore FK constraint failures
        for (String path : new String[]{"/api/location/CLOC1", "/api/asset-group/CGRP1", "/api/class/CCLS1"}) {
            try {
                mockMvc.perform(delete(path)
                        .header("Authorization", bearerAdmin()));
            } catch (Exception ignored) {
                // Entity might still be referenced by a card
            }
        }
    }
}

package cz.majetek.integration;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for dictionary entities (AssetGroup, Location, Class, PurchaseMethod, WriteOffMethod).
 * Each dictionary follows the same CRUD pattern: create, read, update, delete.
 */
class DictionaryIntegrationTest extends BaseIntegrationTest {

    // ===================== AssetGroup =====================

    @Test
    @Order(100)
    void assetGroup_create_shouldPersistAndReturn() throws Exception {
        String json = """
                {"id": "AG001", "name": "Buildings"}
                """;

        mockMvc.perform(post("/api/asset-group")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("AG001"))
                .andExpect(jsonPath("$.name").value("Buildings"));
    }

    @Test
    @Order(101)
    void assetGroup_getById_shouldReturnCreated() throws Exception {
        mockMvc.perform(get("/api/asset-group/AG001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("AG001"))
                .andExpect(jsonPath("$.name").value("Buildings"));
    }

    @Test
    @Order(102)
    void assetGroup_getAll_shouldContainCreated() throws Exception {
        mockMvc.perform(get("/api/asset-group")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem("AG001")));
    }

    @Test
    @Order(103)
    void assetGroup_update_shouldChangeName() throws Exception {
        String updateJson = """
                {"name": "Updated Buildings"}
                """;

        mockMvc.perform(patch("/api/asset-group/AG001")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Buildings"));
    }

    @Test
    @Order(104)
    void assetGroup_delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/asset-group/AG001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(105)
    void assetGroup_getDeleted_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/asset-group/AG001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(106)
    void assetGroup_createWithBlankId_shouldReturn400() throws Exception {
        String invalidJson = """
                {"id": "", "name": "Invalid"}
                """;

        mockMvc.perform(post("/api/asset-group")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(107)
    void assetGroup_createDuplicate_shouldReturn400() throws Exception {
        // First create
        String json = """
                {"id": "AG002", "name": "Equipment"}
                """;
        mockMvc.perform(post("/api/asset-group")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Duplicate create
        mockMvc.perform(post("/api/asset-group")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // Cleanup
        mockMvc.perform(delete("/api/asset-group/AG002")
                .header("Authorization", bearerAdmin()));
    }

    // ===================== Location =====================

    @Test
    @Order(200)
    void location_create_shouldPersistAndReturn() throws Exception {
        String json = """
                {"id": "LOC01", "name": "Office A"}
                """;

        mockMvc.perform(post("/api/location")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("LOC01"))
                .andExpect(jsonPath("$.name").value("Office A"));
    }

    @Test
    @Order(201)
    void location_getById_shouldReturnCreated() throws Exception {
        mockMvc.perform(get("/api/location/LOC01")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("LOC01"))
                .andExpect(jsonPath("$.name").value("Office A"));
    }

    @Test
    @Order(202)
    void location_getAll_shouldContainCreated() throws Exception {
        mockMvc.perform(get("/api/location")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem("LOC01")));
    }

    @Test
    @Order(203)
    void location_update_shouldChangeName() throws Exception {
        String updateJson = """
                {"name": "Updated Office A"}
                """;

        mockMvc.perform(patch("/api/location/LOC01")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Office A"));
    }

    @Test
    @Order(204)
    void location_delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/location/LOC01")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(205)
    void location_getDeleted_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/location/LOC01")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(206)
    void location_createWithIdTooLong_shouldReturn400() throws Exception {
        // Location ID max is 5 chars
        String invalidJson = """
                {"id": "TOOLONGID", "name": "Invalid"}
                """;

        mockMvc.perform(post("/api/location")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // ===================== ClassEntity =====================

    @Test
    @Order(300)
    void classEntity_create_shouldPersistAndReturn() throws Exception {
        String json = """
                {"id": "CLS001", "name": "Computers"}
                """;

        mockMvc.perform(post("/api/class")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CLS001"))
                .andExpect(jsonPath("$.name").value("Computers"));
    }

    @Test
    @Order(301)
    void classEntity_getById_shouldReturnCreated() throws Exception {
        mockMvc.perform(get("/api/class/CLS001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CLS001"))
                .andExpect(jsonPath("$.name").value("Computers"));
    }

    @Test
    @Order(302)
    void classEntity_getAll_shouldContainCreated() throws Exception {
        mockMvc.perform(get("/api/class")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem("CLS001")));
    }

    @Test
    @Order(303)
    void classEntity_update_shouldChangeName() throws Exception {
        String updateJson = """
                {"name": "Updated Computers"}
                """;

        mockMvc.perform(patch("/api/class/CLS001")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Computers"));
    }

    @Test
    @Order(304)
    void classEntity_delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/class/CLS001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(305)
    void classEntity_getDeleted_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/class/CLS001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }

    // ===================== PurchaseMethod =====================

    @Test
    @Order(400)
    void purchaseMethod_create_shouldPersistAndReturn() throws Exception {
        String json = """
                {"id": "PM001", "name": "Direct Purchase"}
                """;

        mockMvc.perform(post("/api/purchase-method")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PM001"))
                .andExpect(jsonPath("$.name").value("Direct Purchase"));
    }

    @Test
    @Order(401)
    void purchaseMethod_getById_shouldReturnCreated() throws Exception {
        mockMvc.perform(get("/api/purchase-method/PM001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PM001"))
                .andExpect(jsonPath("$.name").value("Direct Purchase"));
    }

    @Test
    @Order(402)
    void purchaseMethod_getAll_shouldContainCreated() throws Exception {
        mockMvc.perform(get("/api/purchase-method")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem("PM001")));
    }

    @Test
    @Order(403)
    void purchaseMethod_update_shouldChangeName() throws Exception {
        String updateJson = """
                {"name": "Updated Direct Purchase"}
                """;

        mockMvc.perform(patch("/api/purchase-method/PM001")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Direct Purchase"));
    }

    @Test
    @Order(404)
    void purchaseMethod_delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/purchase-method/PM001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(405)
    void purchaseMethod_getDeleted_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/purchase-method/PM001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }

    // ===================== WriteOffMethod =====================

    @Test
    @Order(500)
    void writeOffMethod_create_shouldPersistAndReturn() throws Exception {
        String json = """
                {"id": "WO001", "name": "Depreciation"}
                """;

        mockMvc.perform(post("/api/write-off-method")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("WO001"))
                .andExpect(jsonPath("$.name").value("Depreciation"));
    }

    @Test
    @Order(501)
    void writeOffMethod_getById_shouldReturnCreated() throws Exception {
        mockMvc.perform(get("/api/write-off-method/WO001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("WO001"))
                .andExpect(jsonPath("$.name").value("Depreciation"));
    }

    @Test
    @Order(502)
    void writeOffMethod_getAll_shouldContainCreated() throws Exception {
        mockMvc.perform(get("/api/write-off-method")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem("WO001")));
    }

    @Test
    @Order(503)
    void writeOffMethod_update_shouldChangeName() throws Exception {
        String updateJson = """
                {"name": "Updated Depreciation"}
                """;

        mockMvc.perform(patch("/api/write-off-method/WO001")
                        .header("Authorization", bearerAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Depreciation"));
    }

    @Test
    @Order(504)
    void writeOffMethod_delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/write-off-method/WO001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(505)
    void writeOffMethod_getDeleted_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/write-off-method/WO001")
                        .header("Authorization", bearerAdmin()))
                .andExpect(status().isBadRequest());
    }
}

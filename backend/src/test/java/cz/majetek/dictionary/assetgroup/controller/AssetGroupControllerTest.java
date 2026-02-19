package cz.majetek.dictionary.assetgroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.dictionary.assetgroup.dto.UpdateAssetGroupRequest;
import cz.majetek.dictionary.assetgroup.service.IAssetGroupService;
import cz.majetek.entity.AssetGroup;
import cz.majetek.exception.ErrorResponseUtils;
import cz.majetek.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssetGroupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAssetGroupService assetGroupService;

    @InjectMocks
    private AssetGroupController assetGroupController;

    private ObjectMapper objectMapper;
    private AssetGroup testGroup;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(assetGroupController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        testGroup = new AssetGroup("GRP1", "Computers");
    }

    @Test
    void getAll_ShouldReturnGroupList() throws Exception {
        when(assetGroupService.getAll()).thenReturn(List.of(testGroup));

        mockMvc.perform(get("/api/asset-group"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("GRP1"));
    }

    @Test
    void getById_ShouldReturnGroup() throws Exception {
        when(assetGroupService.getById("GRP1")).thenReturn(testGroup);

        mockMvc.perform(get("/api/asset-group/GRP1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Computers"));
    }

    @Test
    void create_ShouldReturnCreatedGroup() throws Exception {
        when(assetGroupService.create(any(AssetGroup.class))).thenReturn(testGroup);

        mockMvc.perform(post("/api/asset-group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testGroup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("GRP1"));
    }

    @Test
    void update_ShouldReturnUpdatedGroup() throws Exception {
        UpdateAssetGroupRequest request = new UpdateAssetGroupRequest();
        AssetGroup updated = new AssetGroup("GRP1", "Servers");
        when(assetGroupService.update(eq("GRP1"), any(UpdateAssetGroupRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/asset-group/GRP1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Servers"));
    }

    @Test
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(assetGroupService).delete("GRP1");

        mockMvc.perform(delete("/api/asset-group/GRP1"))
                .andExpect(status().isNoContent());

        verify(assetGroupService).delete("GRP1");
    }
}

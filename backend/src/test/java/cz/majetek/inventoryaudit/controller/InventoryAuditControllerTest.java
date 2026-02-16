package cz.majetek.inventoryaudit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.majetek.entity.inventoryaudit.InventoryAuditAction;
import cz.majetek.exception.ErrorResponseUtils;
import cz.majetek.exception.GlobalExceptionHandler;
import cz.majetek.inventoryaudit.dto.InventoryAuditDiffDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditInsertDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditPatchDto;
import cz.majetek.inventoryaudit.service.IInventoryAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryAuditControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IInventoryAuditService auditService;

    @InjectMocks
    private InventoryAuditController auditController;

    private ObjectMapper objectMapper;
    private InventoryAuditDto testAuditDto;
    private InventoryAuditDiffDto testDiffDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testAuditDto = new InventoryAuditDto();
        testAuditDto.setCardId(1);
        testAuditDto.setCardName("Test Card");
        testAuditDto.setRealLocationId("LOC1");
        testAuditDto.setTimestamp(LocalDateTime.now());

        testDiffDto = new InventoryAuditDiffDto();
        testDiffDto.setCardId(1);
        testDiffDto.setRealLocationId("LOC1");
        testDiffDto.setExpectedLocationId("LOC2");
    }

    @Test
    void getAll_ShouldReturnOkWithAuditList() throws Exception {
        when(auditService.getAll()).thenReturn(List.of(testAuditDto));

        mockMvc.perform(get("/api/inventory-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cardId").value(1));
    }

    @Test
    void getDiffs_ShouldReturnOkWithDiffList() throws Exception {
        when(auditService.getDiffs()).thenReturn(List.of(testDiffDto));

        mockMvc.perform(get("/api/inventory-audit/differences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].realLocationId").value("LOC1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void insertMany_WithValidData_ShouldReturn201() throws Exception {
        InventoryAuditInsertDto insertDto = new InventoryAuditInsertDto();
        insertDto.setCardId(1);
        insertDto.setRealLocationId("LOC1");
        insertDto.setTimestamp(LocalDateTime.now());

        List<InventoryAuditInsertDto> request = List.of(insertDto);
        when(auditService.insertMany(any(List.class))).thenReturn(request);

        mockMvc.perform(post("/api/inventory-audit/insert-many")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void patch_WithValidData_ShouldReturnOk() throws Exception {
        InventoryAuditPatchDto patchDto = new InventoryAuditPatchDto();
        patchDto.setAction(InventoryAuditAction.KEEP);

        doNothing().when(auditService).patch(eq(1), any(InventoryAuditPatchDto.class));

        mockMvc.perform(patch("/api/inventory-audit/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk());

        verify(auditService).patch(eq(1), any(InventoryAuditPatchDto.class));
    }

    @Test
    void project_ShouldReturnOk() throws Exception {
        doNothing().when(auditService).project();

        mockMvc.perform(post("/api/inventory-audit/project"))
                .andExpect(status().isOk());

        verify(auditService).project();
    }
}

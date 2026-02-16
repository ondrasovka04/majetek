package cz.majetek.dictionary.puchasemethod.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.dictionary.puchasemethod.dto.UpdatePurchaseMethodRequest;
import cz.majetek.dictionary.puchasemethod.service.IPurchaseMethodService;
import cz.majetek.entity.PurchaseMethod;
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
class PurchaseMethodControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IPurchaseMethodService purchaseMethodService;

    @InjectMocks
    private PurchaseMethodController purchaseMethodController;

    private ObjectMapper objectMapper;
    private PurchaseMethod testMethod;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(purchaseMethodController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        testMethod = new PurchaseMethod("PM1", "Purchase");
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        when(purchaseMethodService.getAll()).thenReturn(List.of(testMethod));

        mockMvc.perform(get("/api/purchase-method"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("PM1"));
    }

    @Test
    void getById_ShouldReturnMethod() throws Exception {
        when(purchaseMethodService.getById("PM1")).thenReturn(testMethod);

        mockMvc.perform(get("/api/purchase-method/PM1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Purchase"));
    }

    @Test
    void create_ShouldReturnCreatedMethod() throws Exception {
        when(purchaseMethodService.create(any(PurchaseMethod.class))).thenReturn(testMethod);

        mockMvc.perform(post("/api/purchase-method")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMethod)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PM1"));
    }

    @Test
    void update_ShouldReturnUpdatedMethod() throws Exception {
        UpdatePurchaseMethodRequest request = new UpdatePurchaseMethodRequest();
        PurchaseMethod updated = new PurchaseMethod("PM1", "Lease");
        when(purchaseMethodService.update(eq("PM1"), any(UpdatePurchaseMethodRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/purchase-method/PM1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lease"));
    }

    @Test
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(purchaseMethodService).delete("PM1");

        mockMvc.perform(delete("/api/purchase-method/PM1"))
                .andExpect(status().isNoContent());

        verify(purchaseMethodService).delete("PM1");
    }
}

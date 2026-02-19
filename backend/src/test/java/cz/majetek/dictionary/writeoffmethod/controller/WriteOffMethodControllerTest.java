package cz.majetek.dictionary.writeoffmethod.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.dictionary.writeoffmethod.dto.UpdateWriteOffMethodRequest;
import cz.majetek.dictionary.writeoffmethod.service.IWriteOffMethodService;
import cz.majetek.entity.WriteOffMethod;
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
class WriteOffMethodControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IWriteOffMethodService writeOffMethodService;

    @InjectMocks
    private WriteOffMethodController writeOffMethodController;

    private ObjectMapper objectMapper;
    private WriteOffMethod testMethod;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(writeOffMethodController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        testMethod = new WriteOffMethod("WO1", "Depreciation");
    }

    @Test
    void getAll_ShouldReturnList() throws Exception {
        when(writeOffMethodService.getAll()).thenReturn(List.of(testMethod));

        mockMvc.perform(get("/api/write-off-method"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("WO1"));
    }

    @Test
    void getById_ShouldReturnMethod() throws Exception {
        when(writeOffMethodService.getById("WO1")).thenReturn(testMethod);

        mockMvc.perform(get("/api/write-off-method/WO1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Depreciation"));
    }

    @Test
    void create_ShouldReturnCreatedMethod() throws Exception {
        when(writeOffMethodService.create(any(WriteOffMethod.class))).thenReturn(testMethod);

        mockMvc.perform(post("/api/write-off-method")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMethod)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("WO1"));
    }

    @Test
    void update_ShouldReturnUpdatedMethod() throws Exception {
        UpdateWriteOffMethodRequest request = new UpdateWriteOffMethodRequest();
        WriteOffMethod updated = new WriteOffMethod("WO1", "Amortization");
        when(writeOffMethodService.update(eq("WO1"), any(UpdateWriteOffMethodRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/write-off-method/WO1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amortization"));
    }

    @Test
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(writeOffMethodService).delete("WO1");

        mockMvc.perform(delete("/api/write-off-method/WO1"))
                .andExpect(status().isNoContent());

        verify(writeOffMethodService).delete("WO1");
    }
}

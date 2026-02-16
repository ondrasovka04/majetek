package cz.majetek.dictionary.classentity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.dictionary.classentity.dto.UpdateClassRequest;
import cz.majetek.dictionary.classentity.service.IClassService;
import cz.majetek.entity.ClassEntity;
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
class ClassControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IClassService classService;

    @InjectMocks
    private ClassController classController;

    private ObjectMapper objectMapper;
    private ClassEntity testClass;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(classController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        testClass = new ClassEntity("CL1", "Furniture");
    }

    @Test
    void getAll_ShouldReturnClassList() throws Exception {
        when(classService.getAll()).thenReturn(List.of(testClass));

        mockMvc.perform(get("/api/class"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("CL1"));
    }

    @Test
    void getById_ShouldReturnClass() throws Exception {
        when(classService.getById("CL1")).thenReturn(testClass);

        mockMvc.perform(get("/api/class/CL1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Furniture"));
    }

    @Test
    void create_ShouldReturnCreatedClass() throws Exception {
        when(classService.create(any(ClassEntity.class))).thenReturn(testClass);

        mockMvc.perform(post("/api/class")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testClass)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CL1"));
    }

    @Test
    void update_ShouldReturnUpdatedClass() throws Exception {
        UpdateClassRequest request = new UpdateClassRequest();
        ClassEntity updated = new ClassEntity("CL1", "Electronics");
        when(classService.update(eq("CL1"), any(UpdateClassRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/class/CL1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(classService).delete("CL1");

        mockMvc.perform(delete("/api/class/CL1"))
                .andExpect(status().isNoContent());

        verify(classService).delete("CL1");
    }
}

package cz.majetek.dictionary.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.dictionary.location.dto.PrintRequest;
import cz.majetek.dictionary.location.dto.UpdateLocationRequest;
import cz.majetek.dictionary.location.service.ILocationService;
import cz.majetek.entity.Location;
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
class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ILocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private ObjectMapper objectMapper;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        testLocation = new Location("LOC1", "Office A");
    }

    @Test
    void getAll_ShouldReturnLocationList() throws Exception {
        when(locationService.getAll()).thenReturn(List.of(testLocation));

        mockMvc.perform(get("/api/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("LOC1"));
    }

    @Test
    void getById_ShouldReturnLocation() throws Exception {
        when(locationService.getById("LOC1")).thenReturn(testLocation);

        mockMvc.perform(get("/api/location/LOC1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Office A"));
    }

    @Test
    void create_ShouldReturnCreatedLocation() throws Exception {
        when(locationService.create(any(Location.class))).thenReturn(testLocation);

        mockMvc.perform(post("/api/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLocation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("LOC1"));
    }

    @Test
    void update_ShouldReturnUpdatedLocation() throws Exception {
        UpdateLocationRequest request = new UpdateLocationRequest();
        Location updated = new Location("LOC1", "Office B");
        when(locationService.update(eq("LOC1"), any(UpdateLocationRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/location/LOC1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Office B"));
    }

    @Test
    void delete_ShouldReturn204() throws Exception {
        doNothing().when(locationService).delete("LOC1");

        mockMvc.perform(delete("/api/location/LOC1"))
                .andExpect(status().isNoContent());

        verify(locationService).delete("LOC1");
    }

    @Test
    void exportLabel_ShouldReturnPrintResponse() throws Exception {
        PrintRequest request = new PrintRequest("LOC1");
        when(locationService.exportLocation(anyString())).thenReturn(null);

        mockMvc.perform(post("/api/location/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Label printed successfully to cloud."));
    }
}

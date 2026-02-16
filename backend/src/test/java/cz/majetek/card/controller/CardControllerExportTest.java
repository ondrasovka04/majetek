package cz.majetek.card.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.card.dto.CardReportRequest;
import cz.majetek.card.dto.GroupReportRequest;
import cz.majetek.card.dto.LocationReportRequest;
import cz.majetek.card.service.ICardService;
import cz.majetek.exception.ErrorResponseUtils;
import cz.majetek.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerExportTest {

    private MockMvc mockMvc;

    @Mock
    private ICardService cardService;

    @InjectMocks
    private CardController cardController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void exportLabel_PrintCloud_ShouldReturnJsonResponse() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("zpl".getBytes()));
        when(cardService.exportLabel(any(CardFilterQuery.class), eq(true))).thenReturn(resource);

        CardFilterQuery query = new CardFilterQuery();
        query.setLocationId("LOC1");

        mockMvc.perform(post("/api/card/print")
                        .param("printCloud", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Label printed successfully to cloud."));
    }

    @Test
    void exportLabel_LocalExport_ShouldReturnFileStream() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("zpl-content".getBytes()));
        when(cardService.exportLabel(any(CardFilterQuery.class), eq(false))).thenReturn(resource);

        CardFilterQuery query = new CardFilterQuery();

        mockMvc.perform(post("/api/card/print")
                        .param("printCloud", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=label.zpl"));
    }

    @Test
    void exportExcel_ShouldReturnFileStream() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("xlsx".getBytes()));
        when(cardService.exportExcel(any(CardFilterQuery.class))).thenReturn(resource);

        mockMvc.perform(post("/api/card/export-excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CardFilterQuery())))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=cards.xlsx"));
    }

    @Test
    void exportGroupReport_SingleGroup_ShouldReturnPdf() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("pdf".getBytes()));
        when(cardService.exportGroupReport(eq("GRP1"), any(LocalDate.class))).thenReturn(resource);

        GroupReportRequest req = new GroupReportRequest();
        req.setGroupId("GRP1");
        req.setAllGroups(false);
        req.setAsOf(LocalDate.of(2024, 1, 1));

        mockMvc.perform(post("/api/card/export-group-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void exportGroupReport_AllGroups_ShouldReturnPdf() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("pdf".getBytes()));
        when(cardService.exportAllGroupsReport(any(LocalDate.class))).thenReturn(resource);

        GroupReportRequest req = new GroupReportRequest();
        req.setGroupId("GRP1");
        req.setAllGroups(true);
        req.setAsOf(LocalDate.of(2024, 1, 1));

        mockMvc.perform(post("/api/card/export-group-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void exportGroupReport_NullAsOf_ShouldUseCurrentDate() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("pdf".getBytes()));
        when(cardService.exportGroupReport(eq("GRP1"), any(LocalDate.class))).thenReturn(resource);

        GroupReportRequest req = new GroupReportRequest();
        req.setGroupId("GRP1");
        req.setAllGroups(false);
        // asOf is null -> should default to LocalDate.now()

        mockMvc.perform(post("/api/card/export-group-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void exportLocationReport_ShouldReturnPdf() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("pdf".getBytes()));
        when(cardService.exportLocationReport(eq("LOC1"), any(LocalDate.class))).thenReturn(resource);

        LocationReportRequest req = new LocationReportRequest();
        req.setLocationId("LOC1");
        req.setAsOf(LocalDate.of(2024, 6, 1));

        mockMvc.perform(post("/api/card/export-location-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void exportLocationReport_NullAsOf_ShouldUseCurrentDate() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("pdf".getBytes()));
        when(cardService.exportLocationReport(eq("LOC1"), any(LocalDate.class))).thenReturn(resource);

        LocationReportRequest req = new LocationReportRequest();
        req.setLocationId("LOC1");
        // asOf is null

        mockMvc.perform(post("/api/card/export-location-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void exportCardReport_ShouldReturnPdf() throws Exception {
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("pdf".getBytes()));
        when(cardService.exportCardReport(42)).thenReturn(resource);

        CardReportRequest req = new CardReportRequest();
        req.setCardId(42);

        mockMvc.perform(post("/api/card/export-card-pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=card_report_0042.pdf"));
    }
}

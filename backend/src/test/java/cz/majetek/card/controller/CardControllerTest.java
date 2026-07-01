package cz.majetek.card.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.majetek.card.dto.CardChangeDto;
import cz.majetek.card.dto.CardDto;
import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.card.service.ICardService;
import cz.majetek.exception.ErrorResponseUtils;
import cz.majetek.exception.GlobalExceptionHandler;
import cz.majetek.exception.card.CardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICardService cardService;

    @InjectMocks
    private CardController cardController;

    private ObjectMapper objectMapper;
    private CardDto testCardDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cardController)
                .setControllerAdvice(new GlobalExceptionHandler(new ErrorResponseUtils()))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testCardDto = new CardDto();
        testCardDto.setId(1);
        testCardDto.setName("Test Card");
        testCardDto.setPrice(BigDecimal.valueOf(1000));
    }

    @Test
    void getAllCards_ShouldReturnOkWithCardList() throws Exception {
        when(cardService.getAllCards()).thenReturn(List.of(testCardDto));

        mockMvc.perform(get("/api/card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Test Card"));
    }

    @Test
    void getCardById_WithExistingCard_ShouldReturnOk() throws Exception {
        when(cardService.getCardById(1)).thenReturn(testCardDto);

        mockMvc.perform(get("/api/card/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Card"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCardById_WithNonExistingCard_ShouldReturn404() throws Exception {
        when(cardService.getCardById(999)).thenThrow(CardException.notFound(999));

        mockMvc.perform(get("/api/card/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void filterCards_ShouldReturnFilteredCards() throws Exception {
        when(cardService.getFilteredCards(any(CardFilterQuery.class))).thenReturn(List.of(testCardDto));

        mockMvc.perform(get("/api/card/filter").param("locationId", "LOC1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createCard_WithValidData_ShouldReturn201() throws Exception {
        CardChangeDto changeDto = new CardChangeDto();
        changeDto.setName("New Card");
        changeDto.setLocationId("LOC1");
        changeDto.setPurchaseDate(LocalDate.of(2025, Month.JUNE, 15));

        when(cardService.createCard(any(CardChangeDto.class))).thenReturn(testCardDto);

        mockMvc.perform(post("/api/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Card"));
    }

    @Test
    void updateCard_WithValidData_ShouldReturnOk() throws Exception {
        CardChangeDto changeDto = new CardChangeDto();
        changeDto.setName("Updated Card");
        changeDto.setLocationId("LOC1");
        changeDto.setPurchaseDate(LocalDate.of(2025, Month.JUNE, 15));

        when(cardService.updateCard(eq(1), any(CardChangeDto.class))).thenReturn(testCardDto);

        mockMvc.perform(patch("/api/card/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Card"));
    }

    @Test
    void deleteCard_ShouldReturn204() throws Exception {
        doNothing().when(cardService).deleteCard(1);

        mockMvc.perform(delete("/api/card/1"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(1);
    }

    @Test
    void deleteCard_WithNonExistingCard_ShouldReturn404() throws Exception {
        doThrow(CardException.notFound(999)).when(cardService).deleteCard(999);

        mockMvc.perform(delete("/api/card/999"))
                .andExpect(status().isNotFound());
    }
}

package cz.majetek.card.service;

import cz.majetek.card.dto.CardChangeDto;
import cz.majetek.card.dto.CardDto;
import cz.majetek.card.dto.CardFilterQuery;
import org.springframework.core.io.InputStreamResource;

import java.time.LocalDate;
import java.util.List;

public interface ICardService {
    List<CardDto> getFilteredCards(CardFilterQuery query);

    InputStreamResource exportLabel(CardFilterQuery query, boolean printCloud);

    InputStreamResource exportExcel(CardFilterQuery query);

    InputStreamResource exportGroupReport(String groupId, LocalDate asOf);

    InputStreamResource exportAllGroupsReport(LocalDate asOf);

    InputStreamResource exportLocationReport(String locationId, LocalDate asOf);

    InputStreamResource exportCardReport(Integer cardId);

    List<CardDto> getAllCards();

    CardDto getCardById(Integer id);

    CardDto createCard(CardChangeDto cardDto);

    CardDto updateCard(Integer id, CardChangeDto cardDto);

    void deleteCard(Integer id);
}

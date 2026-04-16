package cz.majetek.card.service;

import cz.majetek.card.dto.CardChangeDto;
import cz.majetek.card.dto.CardDto;
import cz.majetek.card.dto.CardFilterQuery;
import org.springframework.core.io.InputStreamResource;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for asset card management.
 * Provides CRUD operations, filtering, and export functionality.
 */
public interface ICardService {

    /**
     * Returns cards matching the given filter criteria.
     *
     * @param query filter parameters (ID range, name, price range, location, etc.)
     * @return filtered list of cards
     */
    List<CardDto> getFilteredCards(CardFilterQuery query);

    /**
     * Generates ZPL labels for cards matching the filter.
     *
     * @param query      filter parameters to select cards
     * @param printCloud true to send directly to Zebra cloud print, false to return .zpl file
     * @return ZPL label data as a stream
     */
    InputStreamResource exportLabel(CardFilterQuery query, boolean printCloud);

    /**
     * Exports filtered cards to an Excel (XLSX) file.
     *
     * @param query filter parameters to select cards
     * @return Excel file as a stream
     */
    InputStreamResource exportExcel(CardFilterQuery query);

    /**
     * Generates a PDF report for a specific asset group as of the given date.
     *
     * @param groupId asset group ID
     * @param asOf    reference date for the report
     * @return PDF report as a stream
     */
    InputStreamResource exportGroupReport(String groupId, LocalDate asOf);

    /**
     * Generates a PDF report covering all asset groups as of the given date.
     *
     * @param asOf reference date for the report
     * @return PDF report as a stream
     */
    InputStreamResource exportAllGroupsReport(LocalDate asOf);

    /**
     * Generates a PDF report for a specific location as of the given date.
     *
     * @param locationId location ID
     * @param asOf       reference date for the report
     * @return PDF report as a stream
     */
    InputStreamResource exportLocationReport(String locationId, LocalDate asOf);

    /**
     * Generates a PDF report for a single card.
     *
     * @param cardId card ID
     * @return PDF report as a stream
     */
    InputStreamResource exportCardReport(Integer cardId);

    /**
     * Returns all cards without filtering.
     *
     * @return list of all cards
     */
    List<CardDto> getAllCards();

    /**
     * Retrieves a single card by its ID.
     *
     * @param id card ID
     * @return card DTO
     */
    CardDto getCardById(Integer id);

    /**
     * Creates a new asset card.
     *
     * @param cardDto card data
     * @return created card DTO with assigned ID
     */
    CardDto createCard(CardChangeDto cardDto);

    /**
     * Updates an existing asset card.
     *
     * @param id      card ID
     * @param cardDto updated card data
     * @return updated card DTO
     */
    CardDto updateCard(Integer id, CardChangeDto cardDto);

    /**
     * Deletes an asset card.
     *
     * @param id card ID
     */
    void deleteCard(Integer id);
}

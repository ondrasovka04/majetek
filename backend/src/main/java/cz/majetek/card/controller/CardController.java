package cz.majetek.card.controller;

import cz.majetek.card.dto.*;
import cz.majetek.card.service.ICardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
@Tag(name = "Card Controller", description = "Handles card filtering and cloud/local printing")
public class CardController {

    private final ICardService cardService;

    @GetMapping()
    @Operation(summary = "Get all cards", description = "Returns a list of all cards")
    @ApiResponse(responseCode = "200", description = "List of cards",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CardDto.class)))
    public ResponseEntity<List<CardDto>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("{id}")
    @Operation(summary = "Get card by id", description = "Returns card by id")
    @ApiResponse(responseCode = "200", description = "Card",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CardDto.class)))
    public ResponseEntity<CardDto> getCardById(@Parameter(name = "id", required = true, in = ParameterIn.PATH) @PathVariable("id") Integer id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("filter")
    @Operation(summary = "Filter cards", description = "Returns a list of cards matching the filter criteria")
    @ApiResponse(responseCode = "200", description = "List of filtered cards",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CardDto.class)))
    public ResponseEntity<List<CardDto>> filterCards(@Parameter(description = "Filtering options") @ModelAttribute CardFilterQuery query) {
        return ResponseEntity.ok(cardService.getFilteredCards(query));
    }

    @PostMapping("print")
    @Operation(summary = "Export or print label", description = "Exports label as a file or prints to cloud, based on the parameter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Label exported or printed successfully",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = PrintResponse.class)),
                            @Content(mediaType = "application/octet-stream")
                    }),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<?> exportLabel(
            @RequestBody @Parameter(description = "Filter query for selecting cards") CardFilterQuery query,
            @RequestParam @Parameter(description = "Whether to print to the cloud instead of exporting") boolean printCloud) {

        InputStreamResource resource = cardService.exportLabel(query, printCloud);

        if (printCloud)
            return ResponseEntity.ok(new PrintResponse("Label printed successfully to cloud."));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=label.zpl")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("export-excel")
    @Operation(summary = "Export cards to Excel", description = "Exports filtered cards to an XLSX file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel file exported successfully",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestBody @Parameter(description = "Filter query for selecting cards") CardFilterQuery query) {

        InputStreamResource resource = cardService.exportExcel(query);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cards.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("export-group-pdf")
    @Operation(summary = "Export group report to PDF", description = "Generates a PDF report for a given asset group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF report exported successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    public ResponseEntity<InputStreamResource> exportGroupReport(
            @RequestBody @Parameter(description = "Request body containing group ID, flag for all groups and the date to which the report relates") GroupReportRequest req
    ) {
        LocalDate asOf = req.getAsOf() != null ? req.getAsOf() : LocalDate.now();

        InputStreamResource resource = req.isAllGroups() ? cardService.exportAllGroupsReport(asOf)
                : cardService.exportGroupReport(req.getGroupId(), asOf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=group_" + req.getGroupId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping("export-location-pdf")
    @Operation(summary = "Export location report to PDF", description = "Generates a PDF report for a given location")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF report exported successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid input or location not found", content = @Content)
    })
    public ResponseEntity<InputStreamResource> exportLocationReport(
            @RequestBody @Parameter(description = "Request body containing location ID and the date to which the report relates") LocationReportRequest req
    ) {
        LocalDate asOf = req.getAsOf() != null ? req.getAsOf() : LocalDate.now();

        InputStreamResource resource = cardService.exportLocationReport(req.getLocationId(), asOf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + "location_report_" + req.getLocationId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping("export-card-pdf")
    @Operation(summary = "Export card report to PDF", description = "Generates a PDF report for a given card")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF report exported successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Card not found", content = @Content)
    })
    public ResponseEntity<InputStreamResource> exportCardReport(
            @RequestBody @Parameter(description = "Request body containing card ID") CardReportRequest req
    ) {
        InputStreamResource resource = cardService.exportCardReport(req.getCardId());

        String filename = String.format("card_report_%04d.pdf", req.getCardId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping
    @Operation(summary = "Create card", description = "Creates a new card")
    @ApiResponse(responseCode = "201", description = "Card created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardDto.class)))
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardChangeDto createDto) {
        CardDto created = cardService.createCard(createDto);
        return ResponseEntity.status(201).body(created);
    }

    @PatchMapping("{id}")
    @Operation(summary = "Update card", description = "Updates existing card by ID")
    @ApiResponse(responseCode = "200", description = "Updated card",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardDto.class)))
    public ResponseEntity<CardDto> updateCard(
            @PathVariable Integer id,
            @Valid @RequestBody CardChangeDto cardChangeDto) {
        CardDto updated = cardService.updateCard(id, cardChangeDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete card", description = "Deletes card by ID")
    @ApiResponse(responseCode = "204", description = "Card deleted")
    public ResponseEntity<Void> deleteCard(@PathVariable Integer id) {
        cardService.deleteCard(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

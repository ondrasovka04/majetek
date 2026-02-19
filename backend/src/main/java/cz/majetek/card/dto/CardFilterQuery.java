package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardFilterQuery {

    @Schema(description = "Filter by ID of the card")
    private Integer id;

    @Schema(description = "Filter by ID of the card")
    private Integer minId;

    @Schema(description = "Filter by ID of the card")
    private Integer maxId;

    @Schema(description = "Filter by name")
    private String name;

    @Schema(description = "Filter by price from")
    private BigDecimal minPrice;

    @Schema(description = "Filter by price to")
    private BigDecimal maxPrice;

    @Schema(description = "Filter by quantity from")
    private BigDecimal minQuantity;

    @Schema(description = "Filter by quantity to")
    private BigDecimal maxQuantity;

    @Schema(description = "Filter by location ID")
    private String locationId;

    @Schema(description = "Filter by class ID")
    private String classId;

    @Schema(description = "Filter by group ID")
    private String groupId;

    @Schema(description = "Filter by purchase date from (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchasedAfter;

    @Schema(description = "Filter by purchase date to (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchasedBefore;

    @Schema(description = "Filter by purchase method ID")
    private String purchaseMethodId;

    @Schema(description = "Filter by purchase invoice")
    private String purchaseInvoice;

    @Schema(description = "Filter by write-off date from (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate writeOffAfter;

    @Schema(description = "Filter by write-off date to (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate writeOffBefore;

    @Schema(description = "Filter by write-off method ID")
    private String writeOffMethodId;

    @Schema(description = "Filter by created by (user ID)")
    private String createdBy;

    @Schema(description = "Filter by created date from (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAfter;

    @Schema(description = "Filter by created date to (yyyy-MM-dd)")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdBefore;

    @Schema(description = "Filter by response person (user ID)")
    private String responsePerson;

    @Schema(description = "Filter by note (full text contains)")
    private String note;

    @Schema(description = "Fulltext filter for ID and name")
    private String fulltextSearch;

    @Schema(description = "Filter write-offs")
    private Boolean includeWriteOff;

    @Schema(description = "Date to filter the report by")
    private LocalDate asOfDate;
}

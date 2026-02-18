package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CardDto {

    @Schema(description = "Unique identifier of the card")
    private Integer id;

    @Schema(description = "Name of the asset")
    private String name;

    @Schema(description = "Price of the asset")
    private BigDecimal price;

    @Schema(description = "Quantity of the asset")
    private BigDecimal quantity;

    @Schema(description = "Serial number of the asset")
    private String serialNumber;

    @Schema(description = "ID of the location")
    private String locationId;

    @Schema(description = "Name of the location")
    private String locationName;

    @Schema(description = "ID of the asset class")
    private String classId;

    @Schema(description = "Name of the asset class")
    private String className;

    @Schema(description = "ID of the asset group")
    private String groupId;

    @Schema(description = "Name of the asset group")
    private String groupName;

    @Schema(description = "Date of purchase")
    private LocalDate purchaseDate;

    @Schema(description = "ID of the purchase method")
    private String purchaseMethodId;

    @Schema(description = "Name of the purchase method")
    private String purchaseMethodName;

    @Schema(description = "Invoice number of the purchase")
    private String purchaseInvoice;

    @Schema(description = "Date of write-off")
    private LocalDate writeOffDate;

    @Schema(description = "ID of the write-off method")
    private String writeOffMethodId;

    @Schema(description = "Name of the write-off method")
    private String writeOffMethodName;

    @Schema(description = "ID of the user who created the record")
    private String createdById;

    @Schema(description = "Username of the user who created the record")
    private String createdByName;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdDate;

    @Schema(description = "ID of the person responsible for the asset")
    private String responsiblePersonId;

    @Schema(description = "Name of the person responsible for the asset")
    private String responsiblePersonName;

    @Schema(description = "Additional note")
    private String note;
}


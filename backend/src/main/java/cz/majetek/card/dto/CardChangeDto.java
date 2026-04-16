package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating or updating a card.
 */
@Getter
@Setter
public class CardChangeDto {
    // not null = name, locationId, purchaseDate
    @Schema(description = "Name of the asset")
    @NotNull
    @Size(max = 100)
    private String name;

    @Schema(description = "Price of the asset")
    @Digits(integer = 36, fraction = 2)
    private BigDecimal price;

    @Schema(description = "Quantity of the asset")
    @Digits(integer = 36, fraction = 2)
    private BigDecimal quantity;

    @Schema(description = "Serial number of the asset")
    @Size(max = 30)
    private String serialNumber;

    @Schema(description = "ID of the location")
    @NotBlank
    @Size(max = 5)
    private String locationId;

    @Schema(description = "ID of the asset class")
    @Size(max = 10)
    private String classId;

    @Schema(description = "ID of the asset group")
    @Size(max = 20)
    private String groupId;

    @Schema(description = "Date of purchase")
    @NotNull
    private LocalDate purchaseDate;

    @Schema(description = "ID of the purchase method")
    @Size(max = 20)
    private String purchaseMethodId;

    @Schema(description = "Invoice number of the purchase")
    @Size(max = 30)
    private String purchaseInvoice;

    @Schema(description = "Date of write-off")
    private LocalDate writeOffDate;

    @Schema(description = "ID of the write-off method")
    @Size(max = 20)
    private String writeOffMethodId;

    @Schema(description = "ID of the user who created the record")
    @Size(max = 5)
    private String createdById;

    @Schema(description = "ID of the person responsible for the asset")
    @Size(max = 5)
    private String responsiblePersonId;

    @Schema(description = "Additional note")
    private String note;
}

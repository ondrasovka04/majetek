package cz.majetek.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardChangeDto {
    // not null = name, locationId, purchaseDate
    @Schema(description = "Name of the asset")
    @JsonProperty("name")
    @NotNull
    @Size(max = 100)
    private String name;

    @Schema(description = "Price of the asset")
    @JsonProperty("price")
    @Digits(integer = 36, fraction = 2)
    private BigDecimal price;

    @Schema(description = "Quantity of the asset")
    @JsonProperty("quantity")
    @Digits(integer = 36, fraction = 2)
    private BigDecimal quantity;

    @Schema(description = "Serial number of the asset")
    @JsonProperty("serialNumber")
    @Size(max = 30)
    private String serialNumber;

    @Schema(description = "ID of the location")
    @JsonProperty("locationId")
    @NotBlank
    @Size(max = 5)
    private String locationId;

    @Schema(description = "ID of the asset class")
    @JsonProperty("classId")
    @Size(max = 10)
    private String classId;

    @Schema(description = "ID of the asset group")
    @JsonProperty("groupId")
    @Size(max = 20)
    private String groupId;

    @Schema(description = "Date of purchase")
    @JsonProperty("purchaseDate")
    @NotNull
    private LocalDate purchaseDate;

    @Schema(description = "ID of the purchase method")
    @JsonProperty("purchaseMethodId")
    @Size(max = 20)
    private String purchaseMethodId;

    @Schema(description = "Invoice number of the purchase")
    @JsonProperty("purchaseInvoice")
    @Size(max = 30)
    private String purchaseInvoice;

    @Schema(description = "Date of write-off")
    @JsonProperty("writeOffDate")
    private LocalDate writeOffDate;

    @Schema(description = "ID of the write-off method")
    @JsonProperty("writeOffMethodId")
    @Size(max = 20)
    private String writeOffMethodId;

    @Schema(description = "ID of the user who created the record")
    @JsonProperty("createdById")
    @Size(max = 5)
    private String createdById;

    @Schema(description = "ID of the person responsible for the asset")
    @JsonProperty("responsiblePersonId")
    @Size(max = 5)
    private String responsiblePersonId;

    @Schema(description = "Additional note")
    @JsonProperty("note")
    private String note;
}

package cz.majetek.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CardDto {

    @Schema(description = "Unique identifier of the card")
    @JsonProperty("id")
    private Integer id;

    @Schema(description = "Name of the asset")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Price of the asset")
    @JsonProperty("price")
    private BigDecimal price;

    @Schema(description = "Quantity of the asset")
    @JsonProperty("quantity")
    private BigDecimal quantity;

    @Schema(description = "Serial number of the asset")
    @JsonProperty("serialNumber")
    private String serialNumber;

    @Schema(description = "ID of the location")
    @JsonProperty("locationId")
    private String locationId;

    @Schema(description = "Name of the location")
    @JsonProperty("locationName")
    private String locationName;

    @Schema(description = "ID of the asset class")
    @JsonProperty("classId")
    private String classId;

    @Schema(description = "Name of the asset class")
    @JsonProperty("className")
    private String className;

    @Schema(description = "ID of the asset group")
    @JsonProperty("groupId")
    private String groupId;

    @Schema(description = "Name of the asset group")
    @JsonProperty("groupName")
    private String groupName;

    @Schema(description = "Date of purchase")
    @JsonProperty("purchaseDate")
    private LocalDate purchaseDate;

    @Schema(description = "ID of the purchase method")
    @JsonProperty("purchaseMethodId")
    private String purchaseMethodId;

    @Schema(description = "Name of the purchase method")
    @JsonProperty("purchaseMethodName")
    private String purchaseMethodName;

    @Schema(description = "Invoice number of the purchase")
    @JsonProperty("purchaseInvoice")
    private String purchaseInvoice;

    @Schema(description = "Date of write-off")
    @JsonProperty("writeOffDate")
    private LocalDate writeOffDate;

    @Schema(description = "ID of the write-off method")
    @JsonProperty("writeOffMethodId")
    private String writeOffMethodId;

    @Schema(description = "Name of the write-off method")
    @JsonProperty("writeOffMethodName")
    private String writeOffMethodName;

    @Schema(description = "ID of the user who created the record")
    @JsonProperty("createdById")
    private String createdById;

    @Schema(description = "Username of the user who created the record")
    @JsonProperty("createdByName")
    private String createdByName;

    @Schema(description = "Creation timestamp")
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @Schema(description = "ID of the person responsible for the asset")
    @JsonProperty("responsiblePersonId")
    private String responsiblePersonId;

    @Schema(description = "Name of the person responsible for the asset")
    @JsonProperty("responsiblePersonName")
    private String responsiblePersonName;

    @Schema(description = "Additional note")
    @JsonProperty("note")
    private String note;
}


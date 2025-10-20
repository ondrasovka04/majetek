package cz.majetek.inventoryaudit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.majetek.entity.inventoryaudit.InventoryAuditAction;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryAuditDiffDto {
    @JsonProperty("cardId")
    private Integer cardId;

    @JsonProperty("cardName")
    private String cardName;

    @JsonProperty("realLocationId")
    private String realLocationId;

    @JsonProperty("realLocationName")
    private String realLocationName;

    @JsonProperty("expectedLocationId")
    private String expectedLocationId;

    @JsonProperty("expectedLocationName")
    private String expectedLocationName;

    @JsonProperty("status")
    private InventoryAuditStatus status;

    @JsonProperty("action")
    private InventoryAuditAction action;

    @JsonProperty("writeOffDate")
    private LocalDate writeOffDate;

    @JsonProperty("writeOffMethodId")
    private String writeOffMethodId;
}

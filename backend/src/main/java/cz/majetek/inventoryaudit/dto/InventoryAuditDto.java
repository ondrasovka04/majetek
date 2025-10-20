package cz.majetek.inventoryaudit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryAuditDto {

    @JsonProperty("cardId")
    private Integer cardId;

    @JsonProperty("cardName")
    private String cardName;

    @JsonProperty("realLocationId")
    private String realLocationId;

    @JsonProperty("realLocationName")
    private String realLocationName;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}


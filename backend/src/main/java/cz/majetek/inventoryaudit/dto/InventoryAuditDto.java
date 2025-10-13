package cz.majetek.inventoryaudit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryAuditDto {

    @Schema(description = "ID of the card for which the audit entry is inserted")
    @NotNull(message = "cardId must not be null")
    @JsonProperty("cardId")
    private Integer cardId;

    @Schema(description = "ID of the real (verified) location")
    @NotNull(message = "realLocationId must not be null")
    @JsonProperty("realLocationId")
    private String realLocationId;

    @Schema(description = "Timestamp of the audit event")
    @JsonProperty("timestamp")
    @NotNull(message = "timestamp must not be null")
    private LocalDateTime timestamp;
}


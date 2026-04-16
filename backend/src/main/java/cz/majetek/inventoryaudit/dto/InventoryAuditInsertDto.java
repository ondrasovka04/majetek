package cz.majetek.inventoryaudit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for inserting a new inventory audit scan from the mobile app.
 */
@Getter
@Setter
public class InventoryAuditInsertDto {

    @Schema(description = "ID of the card for which the audit entry is inserted")
    @NotNull(message = "cardId must not be null")
    private Integer cardId;

    @Schema(description = "ID of the real (verified) location")
    @NotNull(message = "realLocationId must not be null")
    private String realLocationId;

    @Schema(description = "Timestamp of the audit event")
    private LocalDateTime timestamp;
}


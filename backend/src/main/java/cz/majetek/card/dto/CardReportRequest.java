package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardReportRequest {
    @NotNull(message = "Card ID must not be null")
    @Schema(description = "Card ID")
    private Integer cardId;
}

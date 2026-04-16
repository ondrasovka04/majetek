package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for generating a single-card PDF report.
 */
@Getter
@Setter
public class CardReportRequest {
    @NotNull(message = "Card ID must not be null")
    @Schema(description = "Card ID")
    private Integer cardId;
}

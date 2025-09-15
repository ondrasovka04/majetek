package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CardReportRequest {
    @Schema(description = "Card ID")
    private Integer cardId;
}

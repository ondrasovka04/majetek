package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LocationReportRequest {
    @Schema(description = "ID of the location for the report")
    @NotNull
    private String locationId;

    @Schema(description = "Date to which the report relates")
    private LocalDate asOf;
}

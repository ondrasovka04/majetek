package cz.majetek.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Request DTO for generating a location PDF report.
 */
@Getter
@Setter
public class LocationReportRequest {
    @Schema(description = "ID of the location for the report")
    @NotNull
    private String locationId;

    @Schema(description = "Date to which the report relates")
    private LocalDate asOf;
}

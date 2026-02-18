package cz.majetek.card.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GroupReportRequest {
    @Schema(description = "ID of the asset group")
    @NotNull
    private String groupId;

    @Schema(description = "Include all asset groups")
    private boolean allGroups = false;

    @Schema(description = "Date to which the report relates")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate asOf;
}

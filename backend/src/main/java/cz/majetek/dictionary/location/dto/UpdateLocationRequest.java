package cz.majetek.dictionary.location.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLocationRequest {
    /**
     * DTO for updating the name of a location entity.
     */
    @Schema(description = "New name of the location")
    @Size(max = 255, message = "Name must be at most 255 characters long")
    private String name;

}

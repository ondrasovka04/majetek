package cz.majetek.dictionary.classentity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateClassRequest {
    /**
     * DTO for updating the name of a class entity.
     */
    @Schema(description = "New name of the class entity")
    @Size(max = 255, message = "Name must be at most 255 characters long")
    private String name;

}

package cz.majetek.dictionary.writeoffmethod.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWriteOffMethodRequest {
    /**
     * DTO for updating the name of a write-off method entity.
     */
    @Schema(description = "New name of the write-off method")
    @Size(max = 255, message = "Name must be at most 255 characters long")
    private String name;

}

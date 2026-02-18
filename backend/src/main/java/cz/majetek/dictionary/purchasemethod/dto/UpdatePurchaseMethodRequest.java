package cz.majetek.dictionary.purchasemethod.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePurchaseMethodRequest {
    /**
     * DTO for updating the name of a purchase method entity.
     */
    @Schema(description = "New name of the class entity")
    @Size(max = 255, message = "Name must be at most 255 characters long")
    private String name;

}

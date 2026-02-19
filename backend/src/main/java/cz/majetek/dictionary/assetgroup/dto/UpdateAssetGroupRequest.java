package cz.majetek.dictionary.assetgroup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating the name of an asset group.
 */
@Getter
@Setter
public class UpdateAssetGroupRequest {

    @Schema(description = "New name of the asset group")
    @Size(max = 255, message = "Name must be at most 255 characters long")
    private String name;
}

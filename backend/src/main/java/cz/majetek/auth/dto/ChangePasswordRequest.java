package cz.majetek.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @NotNull(message = "currentPassword field is required")
    @NotBlank(message = "currentPassword cannot be empty")
    @JsonProperty("currentPassword")
    private String currentPassword;

    @NotNull(message = "newPassword field is required")
    @NotBlank(message = "newPassword cannot be empty")
    @JsonProperty("newPassword")
    private String newPassword;
}

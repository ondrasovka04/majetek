package cz.majetek.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for user registration requests.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotNull(message = "Id field is required")
    @NotBlank(message = "Id cannot be empty")
    @Size(max = 5, message = "Id cannot be longer than 5 characters")
    @JsonProperty("id")
    private String id;

    @NotNull(message = "Username field is required")
    @NotBlank(message = "Username cannot be empty")
    @JsonProperty("username")
    private String username;

    @NotNull(message = "Password field is required")
    @NotBlank(message = "Password cannot be empty")
    @JsonProperty("password")
    private String password;

    @JsonProperty("name")
    private String name;
} 
package cz.majetek.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a field-level validation error.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorFieldResponse {
    @JsonProperty("field")
    private String field;

    @JsonProperty("message")
    private String message;
} 
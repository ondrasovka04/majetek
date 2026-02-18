package cz.majetek.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a field-level validation error.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorFieldResponse {
    private String field;

    private String message;
} 
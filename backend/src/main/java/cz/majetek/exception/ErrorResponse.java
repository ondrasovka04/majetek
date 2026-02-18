package cz.majetek.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response format for the API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;

    private String error;

    private String message;

    private List<ErrorFieldResponse> errors;

    @JsonProperty("timestamp")
    private Instant timeStamp;
} 
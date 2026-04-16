package cz.majetek.dictionary.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for printing a location label via Zebra Cloud.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrintRequest {
    private String id;
}

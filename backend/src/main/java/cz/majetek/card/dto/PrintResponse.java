package cz.majetek.card.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO wrapping a Zebra Cloud print job result.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrintResponse {
    String message;
}

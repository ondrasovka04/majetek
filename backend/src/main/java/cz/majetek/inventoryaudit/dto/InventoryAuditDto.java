package cz.majetek.inventoryaudit.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO representing an inventory audit record in API responses.
 */
@Getter
@Setter
public class InventoryAuditDto {

    private Integer cardId;

    private String cardName;

    private String realLocationId;

    private String realLocationName;

    private LocalDateTime timestamp;
}


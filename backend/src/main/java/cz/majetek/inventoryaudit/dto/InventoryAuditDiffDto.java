package cz.majetek.inventoryaudit.dto;

import cz.majetek.entity.inventoryaudit.InventoryAuditAction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO representing a diff between expected and scanned card state.
 */
@Getter
@Setter
public class InventoryAuditDiffDto {
    private Integer cardId;

    private String cardName;

    private String realLocationId;

    private String realLocationName;

    private String expectedLocationId;

    private String expectedLocationName;

    private InventoryAuditStatus status;

    private InventoryAuditAction action;

    private LocalDate writeOffDate;

    private String writeOffMethodId;
}

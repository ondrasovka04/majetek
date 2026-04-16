package cz.majetek.inventoryaudit.dto;

/**
 * Result status of an inventory audit comparison: CORRECT (location matches),
 * WARN (location differs), WRONG (card not scanned).
 */
public enum InventoryAuditStatus {
    CORRECT, WARN, WRONG
}

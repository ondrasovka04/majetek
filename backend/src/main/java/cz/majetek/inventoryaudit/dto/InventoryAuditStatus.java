package cz.majetek.inventoryaudit.dto;

/**
 * Status of an inventory audit record (NEW, FOUND, MISSING, etc.).
 */
public enum InventoryAuditStatus {
    CORRECT, WARN, WRONG
}

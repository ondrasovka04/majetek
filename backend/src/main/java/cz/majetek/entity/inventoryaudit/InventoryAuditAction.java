package cz.majetek.entity.inventoryaudit;

/**
 * Actions available during inventory audit resolution: KEEP (keep current location),
 * RL (accept real/scanned location), EL (keep expected/evidence location), WRITE_OFF (dispose).
 */
public enum InventoryAuditAction {
    KEEP, RL, EL, WRITE_OFF
}

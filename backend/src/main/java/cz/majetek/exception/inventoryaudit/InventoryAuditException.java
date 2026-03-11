package cz.majetek.exception.inventoryaudit;

import lombok.Getter;

/**
 * Exception thrown for inventory audit validation or not-found errors.
 */
@Getter
public class InventoryAuditException extends RuntimeException {
    private final Integer cardId;

    private InventoryAuditException(String message, Integer cardId) {
        super(message);
        this.cardId = cardId;
    }

    public InventoryAuditException(String message) {
        this(message, null);
    }

    public static InventoryAuditException notFound(Integer id) {
        return new InventoryAuditException("Inventory audit not found for cardId: " + id, id);
    }
}

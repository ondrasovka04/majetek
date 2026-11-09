package cz.majetek.inventoryaudit.service;

import cz.majetek.inventoryaudit.dto.InventoryAuditDiffDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditInsertDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditPatchDto;

import java.util.List;

/**
 * Service interface for inventory audit operations.
 * Handles import of scanned data, diff computation, and projection of changes.
 */
public interface IInventoryAuditService {

    /**
     * Returns all inventory audit records.
     *
     * @return list of audit records
     */
    List<InventoryAuditDto> getAll();

    /**
     * Computes differences between registered and scanned locations for all cards.
     *
     * @return list of diff records with status (CORRECT, WARN, WRONG) and suggested action;
     *         empty if no scans have been uploaded yet
     */
    List<InventoryAuditDiffDto> getDiffs();

    /**
     * Bulk-inserts inventory audit records from the mobile application.
     *
     * @param items list of scanned card-location pairs with timestamps
     * @return inserted records
     */
    List<InventoryAuditInsertDto> insertMany(List<InventoryAuditInsertDto> items);

    /**
     * Updates the chosen action for a single inventory audit record.
     *
     * @param cardId card ID
     * @param dto    patch data (action, optional write-off date and method)
     */
    void patch(Integer cardId, InventoryAuditPatchDto dto);

    /**
     * Projects all confirmed actions to the database (updates locations, writes off cards).
     */
    void project();
}


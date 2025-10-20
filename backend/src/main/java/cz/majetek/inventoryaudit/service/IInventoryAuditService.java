package cz.majetek.inventoryaudit.service;

import cz.majetek.inventoryaudit.dto.InventoryAuditDiffDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditInsertDto;
import cz.majetek.inventoryaudit.dto.InventoryAuditPatchDto;

import java.util.List;

public interface IInventoryAuditService {
    List<InventoryAuditDto> getAll();

    List<InventoryAuditDiffDto> getDiffs();

    List<InventoryAuditInsertDto> insertMany(List<InventoryAuditInsertDto> items);

    void patch(Integer cardId, InventoryAuditPatchDto dto);

    void project();
}


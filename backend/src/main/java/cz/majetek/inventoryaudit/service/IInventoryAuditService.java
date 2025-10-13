package cz.majetek.inventoryaudit.service;

import cz.majetek.inventoryaudit.dto.InventoryAuditDto;

import java.util.List;

public interface IInventoryAuditService {
    List<InventoryAuditDto> getAll();

    List<InventoryAuditDto> insertMany(List<InventoryAuditDto> items);
}


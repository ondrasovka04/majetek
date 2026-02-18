package cz.majetek.inventoryaudit.dto;

import cz.majetek.entity.inventoryaudit.InventoryAuditAction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InventoryAuditPatchDto {

    private InventoryAuditAction action;

    private LocalDate writeOffDate;

    private String writeOffMethodId;
}


package cz.majetek.inventoryaudit.repository;

import cz.majetek.entity.InventoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IInventoryAuditRepository extends JpaRepository<InventoryAudit, Integer> {
}

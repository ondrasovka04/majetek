package cz.majetek.inventoryaudit.repository;

import cz.majetek.entity.inventoryaudit.InventoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IInventoryAuditRepository extends JpaRepository<InventoryAudit, Integer> {

    List<InventoryAudit> findAllByRealLocationIsNotNull();

    boolean existsByRealLocationIsNotNull();
}

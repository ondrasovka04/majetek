package cz.majetek.dictionary.purchasemethod.repository;

import cz.majetek.entity.PurchaseMethod;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for PurchaseMethod dictionary entity.
 */
public interface IPurchaseMethodRepository extends JpaRepository<PurchaseMethod, String> {
}

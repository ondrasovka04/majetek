package cz.majetek.dictionary.puchasemethod.repository;

import cz.majetek.entity.PurchaseMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPurchaseMethodRepository extends JpaRepository<PurchaseMethod, String> {
}

package cz.majetek.dictionary.puchasemethod.service.implementation;

import cz.majetek.dictionary.puchasemethod.dto.UpdatePurchaseMethodRequest;
import cz.majetek.dictionary.puchasemethod.repository.IPurchaseMethodRepository;
import cz.majetek.dictionary.puchasemethod.service.IPurchaseMethodService;
import cz.majetek.entity.PurchaseMethod;
import cz.majetek.exception.dictionary.PurchaseMethodException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of purchase method service interface.
 */
@Service
@RequiredArgsConstructor
public class PurchaseMethodService implements IPurchaseMethodService {
    private final IPurchaseMethodRepository purchaseMethodRepository;

    @Override
    public List<PurchaseMethod> getAll() {
        return purchaseMethodRepository.findAll();
    }

    @Override
    public PurchaseMethod getById(String id) {
        return purchaseMethodRepository.findById(id).orElseThrow(
                () -> new PurchaseMethodException("Purchase method with id " + id + " not found"));
    }

    @Override
    public PurchaseMethod create(PurchaseMethod purchaseMethod) {
        if (purchaseMethodRepository.existsById(purchaseMethod.getId())) {
            throw new PurchaseMethodException("Purchase method with id " + purchaseMethod.getId() + " already exists");
        }
        return purchaseMethodRepository.save(purchaseMethod);
    }

    @Override
    public PurchaseMethod update(String id, UpdatePurchaseMethodRequest request) {
        PurchaseMethod existing = purchaseMethodRepository.findById(id)
                .orElseThrow(() -> new PurchaseMethodException("Purchase method with id " + id + " not found"));

        existing.setName(request.getName());
        return purchaseMethodRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        try {
            purchaseMethodRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new PurchaseMethodException("Purchase method cannot be deleted because it is being used.");
        }
    }
}

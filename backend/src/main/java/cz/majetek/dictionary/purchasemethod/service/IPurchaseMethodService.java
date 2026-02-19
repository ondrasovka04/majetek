package cz.majetek.dictionary.purchasemethod.service;

import cz.majetek.dictionary.purchasemethod.dto.UpdatePurchaseMethodRequest;
import cz.majetek.entity.PurchaseMethod;
import cz.majetek.exception.dictionary.PurchaseMethodException;

import java.util.List;

/**
 * Service interface for managing purchase method entities.
 */
public interface IPurchaseMethodService {
    /**
     * Retrieves all purchase method entities.
     *
     * @return a list of all purchase methods
     */
    List<PurchaseMethod> getAll();

    /**
     * Retrieves a purchase method by its ID.
     *
     * @param id the ID of the purchase method to retrieve, must not be null or exceed 20 characters
     * @return the purchase method with the given ID
     * @throws PurchaseMethodException if the purchase method with the given ID does not exist
     */
    PurchaseMethod getById(String id);

    /**
     * Creates a new purchase method.
     *
     * @param purchaseMethod the purchase method entity to create; must be valid
     * @return the created purchase method
     * @throws PurchaseMethodException if a purchase method with the same ID already exists
     */
    PurchaseMethod create(PurchaseMethod purchaseMethod);

    /**
     * Updates an existing purchase method.
     *
     * @param id      the ID of the purchase method to update; must not be null or exceed 20 characters
     * @param request the new name for purchase method to update; must be valid
     * @return the updated PurchaseMethod
     * @throws PurchaseMethodException if the purchase method with the given ID does not exist
     */
    PurchaseMethod update(String id, UpdatePurchaseMethodRequest request);

    /**
     * Deletes the purchase method with the given ID.
     *
     * @param id the ID of the purchase method to delete; must not be null or exceed 20 characters
     * @throws PurchaseMethodException if the purchase method cannot be deleted because it is referenced by other entities or does not exist
     */
    void delete(String id);
}

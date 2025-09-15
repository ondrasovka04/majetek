package cz.majetek.dictionary.writeoffmethod.service;

import cz.majetek.dictionary.writeoffmethod.dto.UpdateWriteOffMethodRequest;
import cz.majetek.entity.WriteOffMethod;
import cz.majetek.exception.dictionary.WriteOffMethodException;

import java.util.List;

/**
 * Service interface for managing write-off method entities.
 */
public interface IWriteOffMethodService {
    /**
     * Retrieves all write-off method entities.
     *
     * @return a list of all write-off methods
     */
    List<WriteOffMethod> getAll();

    /**
     * Retrieves a write-off method by its ID.
     *
     * @param id the ID of the write-off method to retrieve, must not be null or exceed 20 characters
     * @return the write-off method with the given ID
     * @throws WriteOffMethodException if the write-off method with the given ID does not exist
     */
    WriteOffMethod getById(String id);

    /**
     * Creates a new write-off method.
     *
     * @param writeOffMethod the write-off method entity to create; must be valid
     * @return the created write-off method
     * @throws WriteOffMethodException if a write-off method with the same ID already exists
     */
    WriteOffMethod create(WriteOffMethod writeOffMethod);

    /**
     * Updates an existing write-off method.
     *
     * @param id      the ID of the write-off method to update; must not be null or exceed 20 characters
     * @param request the new name for write-off method to update; must be valid
     * @return the updated WriteOffMethod
     * @throws WriteOffMethodException if the write-off method with the given ID does not exist
     */
    WriteOffMethod update(String id, UpdateWriteOffMethodRequest request);

    /**
     * Deletes the write-off method with the given ID.
     *
     * @param id the ID of the write-off method to delete; must not be null or exceed 20 characters
     * @throws WriteOffMethodException if the write-off method cannot be deleted because it is referenced by other entities or does not exist
     */
    void delete(String id);
}

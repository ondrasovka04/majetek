package cz.majetek.dictionary.classentity.service;

import cz.majetek.dictionary.classentity.dto.UpdateClassRequest;
import cz.majetek.entity.ClassEntity;
import cz.majetek.exception.dictionary.ClassEntityException;

import java.util.List;

/**
 * Service interface for managing Class entities.
 */
public interface IClassService {
    /**
     * Retrieves all class entities.
     *
     * @return a list of all classes
     */
    List<ClassEntity> getAll();

    /**
     * Retrieves a class by its ID.
     *
     * @param id the ID of the class to retrieve, must not be null or exceed 10 characters
     * @return the class with the given ID
     * @throws ClassEntityException if the class with the given ID does not exist
     */
    ClassEntity getById(String id);

    /**
     * Creates a new class.
     *
     * @param classEntity the class entity to create; must be valid
     * @return the created class
     * @throws ClassEntityException if a class with the same ID already exists
     */
    ClassEntity create(ClassEntity classEntity);

    /**
     * Updates an existing class.
     *
     * @param id      the ID of the class to update; must not be null or exceed 20 characters
     * @param request the new name for class to update; must be valid
     * @return the updated ClassEntity
     * @throws ClassEntityException if the class with the given ID does not exist
     */
    ClassEntity update(String id, UpdateClassRequest request);

    /**
     * Deletes the class with the given ID.
     *
     * @param id the ID of the class to delete; must not be null or exceed 20 characters
     * @throws ClassEntityException if the class cannot be deleted because it is referenced by other entities or does not exist
     */
    void delete(String id);
}

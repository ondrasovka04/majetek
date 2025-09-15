package cz.majetek.dictionary.location.service;

import cz.majetek.dictionary.location.dto.UpdateLocationRequest;
import cz.majetek.entity.Location;
import cz.majetek.exception.dictionary.LocationException;
import org.springframework.core.io.InputStreamResource;

import java.util.List;

/**
 * Service interface for managing location entities.
 */
public interface ILocationService {
    /**
     * Retrieves all location entities.
     *
     * @return a list of all locations
     */
    List<Location> getAll();

    /**
     * Retrieves a location by its ID.
     *
     * @param id the ID of the location to retrieve, must not be null or exceed 5 characters
     * @return the location with the given ID
     * @throws LocationException if the location with the given ID does not exist
     */
    Location getById(String id);

    /**
     * Creates a new location.
     *
     * @param location the location entity to create; must be valid
     * @return the created location
     * @throws LocationException if a location with the same ID already exists
     */
    Location create(Location location);

    /**
     * Updates an existing location.
     *
     * @param id      the ID of the location to update; must not be null or exceed 5 characters
     * @param request the new name for location to update; must be valid
     * @return the updated Location
     * @throws LocationException if the location with the given ID does not exist
     */
    Location update(String id, UpdateLocationRequest request);

    /**
     * Deletes the location with the given ID.
     *
     * @param id the ID of the location to delete
     * @throws LocationException if the location cannot be deleted because it is referenced by other entities or does not exist
     */
    void delete(String id);

    /**
     * Exports the location with the given ID.
     *
     * @param id the ID of the location to export
     * @throws LocationException if the location with the given ID does not exist
     */
    InputStreamResource exportLocation(String id);
}

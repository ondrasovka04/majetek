package cz.majetek.dictionary.assetgroup.service;

import cz.majetek.dictionary.assetgroup.dto.UpdateAssetGroupRequest;
import cz.majetek.entity.AssetGroup;
import cz.majetek.exception.dictionary.AssetGroupException;

import java.util.List;

/**
 * Service interface for managing AssetGroup entities.
 */
public interface IAssetGroupService {

    /**
     * Retrieves all AssetGroup entities.
     *
     * @return a list of all AssetGroups
     */
    List<AssetGroup> getAll();

    /**
     * Retrieves an AssetGroup by its ID.
     *
     * @param id the ID of the AssetGroup to retrieve, must not be null or exceed 20 characters
     * @return the AssetGroup with the given ID
     * @throws AssetGroupException if the AssetGroup with the given ID does not exist
     */
    AssetGroup getById(String id);

    /**
     * Creates a new AssetGroup.
     *
     * @param assetGroup the AssetGroup entity to create; must be valid
     * @return the created AssetGroup
     * @throws AssetGroupException if an AssetGroup with the same ID already exists
     */
    AssetGroup create(AssetGroup assetGroup);

    /**
     * Updates an existing AssetGroup.
     *
     * @param id      the ID of the AssetGroup to update; must not be null or exceed 20 characters
     * @param request the new name for asset group to update; must be valid
     * @return the updated AssetGroup
     * @throws AssetGroupException if the AssetGroup with the given ID does not exist
     */
    AssetGroup update(String id, UpdateAssetGroupRequest request);

    /**
     * Deletes the AssetGroup with the given ID.
     *
     * @param id the ID of the AssetGroup to delete; must not be null or exceed 20 characters
     * @throws AssetGroupException if the AssetGroup cannot be deleted because it is referenced by other entities or does not exist
     */
    void delete(String id);
}

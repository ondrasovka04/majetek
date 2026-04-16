package cz.majetek.dictionary.assetgroup.service.implementation;

import cz.majetek.dictionary.assetgroup.dto.UpdateAssetGroupRequest;
import cz.majetek.dictionary.assetgroup.repository.IAssetGroupRepository;
import cz.majetek.dictionary.assetgroup.service.IAssetGroupService;
import cz.majetek.entity.AssetGroup;
import cz.majetek.exception.dictionary.AssetGroupException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of AssetGroup service interface.
 */
@Service
@RequiredArgsConstructor
public class AssetGroupService implements IAssetGroupService {

    private final IAssetGroupRepository assetGroupRepository;

    @Override
    @Cacheable("assetGroups")
    public List<AssetGroup> getAll() {
        return assetGroupRepository.findAll();
    }

    @Override
    @Cacheable(value = "assetGroups", key = "#id")
    public AssetGroup getById(String id) {
        return assetGroupRepository.findById(id).orElseThrow(
                () -> AssetGroupException.notFound(id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "assetGroups", allEntries = true)
    public AssetGroup create(AssetGroup assetGroup) {
        if (assetGroupRepository.existsById(assetGroup.getId())) {
            throw new AssetGroupException("Asset group with id " + assetGroup.getId() + " already exists");
        }
        return assetGroupRepository.save(assetGroup);
    }

    @Override
    @Transactional
    @CacheEvict(value = "assetGroups", allEntries = true)
    public AssetGroup update(String id, UpdateAssetGroupRequest request) {
        AssetGroup existing = assetGroupRepository.findById(id)
                .orElseThrow(() -> AssetGroupException.notFound(id));

        existing.setName(request.getName());
        return assetGroupRepository.save(existing);
    }

    @Override
    @CacheEvict(value = "assetGroups", allEntries = true)
    public void delete(String id) {
        try {
            assetGroupRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new AssetGroupException("Asset group cannot be deleted because it is being used.");
        }
    }
}

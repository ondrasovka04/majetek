package cz.majetek.dictionary.assetgroup.service.implementation;

import cz.majetek.dictionary.assetgroup.dto.UpdateAssetGroupRequest;
import cz.majetek.dictionary.assetgroup.repository.IAssetGroupRepository;
import cz.majetek.dictionary.assetgroup.service.IAssetGroupService;
import cz.majetek.entity.AssetGroup;
import cz.majetek.exception.dictionary.AssetGroupException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of AssetGroup service interface.
 */
@Service
@RequiredArgsConstructor
public class AssetGroupService implements IAssetGroupService {

    private final IAssetGroupRepository assetGroupRepository;

    @Override
    public List<AssetGroup> getAll() {
        return assetGroupRepository.findAll();
    }

    @Override
    public AssetGroup getById(String id) {
        return assetGroupRepository.findById(id).orElseThrow(
                () -> new AssetGroupException("Asset group with id " + id + " not found"));
    }

    @Override
    public AssetGroup create(AssetGroup assetGroup) {
        if (assetGroupRepository.existsById(assetGroup.getId())) {
            throw new AssetGroupException("Asset group with id " + assetGroup.getId() + " already exists");
        }
        return assetGroupRepository.save(assetGroup);
    }

    @Override
    public AssetGroup update(String id, UpdateAssetGroupRequest request) {
        AssetGroup existing = assetGroupRepository.findById(id)
                .orElseThrow(() -> new AssetGroupException("Asset group with id " + id + " not found"));

        existing.setName(request.getName());
        return assetGroupRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        try {
            assetGroupRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new AssetGroupException("Asset group cannot be deleted because it is being used.");
        }
    }
}

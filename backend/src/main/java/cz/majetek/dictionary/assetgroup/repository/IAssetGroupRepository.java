package cz.majetek.dictionary.assetgroup.repository;

import cz.majetek.entity.AssetGroup;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for AssetGroup dictionary entity.
 */
public interface IAssetGroupRepository extends JpaRepository<AssetGroup, String> {
}

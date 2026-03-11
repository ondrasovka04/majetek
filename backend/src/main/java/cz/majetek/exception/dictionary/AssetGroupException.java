package cz.majetek.exception.dictionary;

import lombok.Getter;

/**
 * Exception thrown for asset group validation or not-found errors.
 */
@Getter
public final class AssetGroupException extends RuntimeException {
    private final String assetGroupId;

    private AssetGroupException(String message, String assetGroupId) {
        super(message);
        this.assetGroupId = assetGroupId;
    }

    public AssetGroupException(String message) {
        this(message, null);
    }

    public static AssetGroupException notFound(String id) {
        return new AssetGroupException("Asset group with id " + id + " not found", id);
    }
}

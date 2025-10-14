package cz.majetek.exception.dictionary;

import lombok.Getter;

@Getter
public final class LocationException extends RuntimeException {
    private final String locationId;

    private LocationException(String message, String locationId) {
        super(message);
        this.locationId = locationId;
    }

    public LocationException(String message) {
        this(message, null);
    }

    public static LocationException notFound(String id) {
        return new LocationException("Location with id " + id + " not found", id);
    }
}

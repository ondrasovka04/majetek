package cz.majetek.data.db;

import androidx.annotation.NonNull;

/**
 * Card relevant to a location during inventory – belongs there and/or was scanned there.
 */
public class LocationCardRow {

    public enum Status {
        /** Belongs to the location but has not been scanned anywhere yet. */
        MISSING,
        /** Location mismatch – scanned here but belongs elsewhere, or belongs here but scanned elsewhere. */
        MISPLACED,
        /** Belongs to the location and was scanned there. */
        OK
    }

    public int cardId;
    public String name;
    public String expectedLocationId;
    public String expectedLocationName;
    public String realLocationId;
    public String realLocationName;

    @NonNull
    public Status statusFor(@NonNull String locationId) {
        if (realLocationId == null) return Status.MISSING;
        if (locationId.equals(realLocationId) && locationId.equals(expectedLocationId)) return Status.OK;
        return Status.MISPLACED;
    }
}

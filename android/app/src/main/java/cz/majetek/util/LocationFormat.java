package cz.majetek.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cz.majetek.R;

/**
 * Formats a location as "ID – name" (or just ID when the name is missing).
 */
public final class LocationFormat {

    private LocationFormat() {
    }

    @NonNull
    public static String format(@NonNull Context ctx, @Nullable String id, @Nullable String name) {
        if (id == null) return "";
        if (name == null || name.isEmpty()) return id;
        return ctx.getString(R.string.format_location_with_name, id, name);
    }
}

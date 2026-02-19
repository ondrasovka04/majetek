package cz.majetek.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class BaseUrlStorage {
    private static final String PREF = "config_pref";
    private static final String KEY_BASE_URL = "base_url";
    private static final String DEFAULT_BASE_URL = "https://majetek.zs-ustavni.cz/api";

    private BaseUrlStorage() {}

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static String getBaseUrl(Context ctx) {
        String raw = sp(ctx).getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        return sanitize(raw);
    }

    public static boolean setBaseUrl(Context ctx, String newUrl) {
        if (!isValid(newUrl)) return false;
        String sanitized = sanitize(newUrl);
        sp(ctx).edit().putString(KEY_BASE_URL, sanitized).apply();
        return true;
    }

    static boolean isValid(String url) {
        if (url == null) return false;
        url = url.trim();
        if (!url.startsWith("https://")) return false;
        return url.length() >= "https://x.y".length();
    }

    static String sanitize(String url) {
        if (url == null) return DEFAULT_BASE_URL;
        url = url.trim();
        while (url.endsWith("/") && url.length() > 8) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}

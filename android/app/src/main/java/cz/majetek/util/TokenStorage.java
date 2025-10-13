package cz.majetek.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenStorage {
    private static final String PREF = "auth_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EXPIRY = "token_expiry"; // epoch millis

    private static SharedPreferences sp(Context ctx) {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void saveToken(Context ctx, String token, long expiryEpochMillis) {
        sp(ctx).edit().putString(KEY_TOKEN, token).putLong(KEY_EXPIRY, expiryEpochMillis).apply();
    }

    public static String getValidToken(Context ctx) {
        SharedPreferences s = sp(ctx);
        String token = s.getString(KEY_TOKEN, null);
        long exp = s.getLong(KEY_EXPIRY, 0L);
        if (token == null) return null;
        if (exp == 0L) return token;
        long now = System.currentTimeMillis();
        if (now + 5000 > exp) return null;
        return token;
    }

    public static void clear(Context ctx) {
        sp(ctx).edit().clear().apply();
    }
}

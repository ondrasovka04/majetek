package cz.majetek.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * EncryptedSharedPreferences wrapper for JWT token with expiration.
 */
public class TokenStorage {
    private static final String TAG = "TokenStorage";
    private static final String PREF = "auth_pref_enc";
    private static final String LEGACY_PREF = "auth_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EXPIRY = "token_expiry";
    private static volatile boolean legacyCleaned;

    private TokenStorage() {
    }

    /**
     * Creates encrypted prefs (AES-256-GCM), falls back to plain on failure.
     */
    private static SharedPreferences sp(Context ctx) {
        cleanLegacy(ctx);
        try {
            MasterKey masterKey = new MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            return EncryptedSharedPreferences.create(ctx, PREF, masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "EncryptedSharedPreferences failed, falling back to plain", e);
            return ctx.getSharedPreferences(PREF + "_fallback", Context.MODE_PRIVATE);
        }
    }

    /**
     * Clears legacy unencrypted prefs (one-time migration).
     */
    private static void cleanLegacy(Context ctx) {
        if (!legacyCleaned) {
            legacyCleaned = true;
            ctx.getSharedPreferences(LEGACY_PREF, Context.MODE_PRIVATE).edit().clear().apply();
        }
    }

    /**
     * Saves the token + expiration epoch millis.
     */
    public static void saveToken(Context ctx, String token, long expiryEpochMillis) {
        sp(ctx).edit().putString(KEY_TOKEN, token).putLong(KEY_EXPIRY, expiryEpochMillis).apply();
    }

    /**
     * Returns the token only if not expired (5s buffer for network request).
     */
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
}

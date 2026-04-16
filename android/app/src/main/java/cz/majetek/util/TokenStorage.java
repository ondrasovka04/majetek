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
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EXPIRY = "token_expiry";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private TokenStorage() {
    }

    /**
     * Creates encrypted prefs (AES-256-GCM). Throws on failure — credentials
     * must never be stored unencrypted.
     */
    private static SharedPreferences sp(Context ctx) {
        try {
            MasterKey masterKey = new MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            return EncryptedSharedPreferences.create(ctx, PREF, masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "EncryptedSharedPreferences failed", e);
            throw new IllegalStateException("Cannot create encrypted storage", e);
        }
    }

    /**
     * Saves the token + expiration epoch millis.
     */
    public static void saveToken(Context ctx, String token, long expiryEpochMillis) {
        sp(ctx).edit().putString(KEY_TOKEN, token).putLong(KEY_EXPIRY, expiryEpochMillis).apply();
    }

    /**
     * Saves login credentials to encrypted storage.
     */
    public static void saveCredentials(Context ctx, String username, String password) {
        sp(ctx).edit().putString(KEY_USERNAME, username).putString(KEY_PASSWORD, password).apply();
    }

    /**
     * Returns saved username, or null if storage is unavailable or not set.
     */
    public static String getUsername(Context ctx) {
        try {
            return sp(ctx).getString(KEY_USERNAME, null);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Cannot read username", e);
            return null;
        }
    }

    /**
     * Returns saved password, or null if storage is unavailable or not set.
     */
    public static String getPassword(Context ctx) {
        try {
            return sp(ctx).getString(KEY_PASSWORD, null);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Cannot read password", e);
            return null;
        }
    }

    /**
     * Returns the token only if not expired (5s buffer for network request).
     * Returns null if storage is unavailable.
     */
    public static String getValidToken(Context ctx) {
        SharedPreferences s;
        try {
            s = sp(ctx);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Cannot read token", e);
            return null;
        }
        String token = s.getString(KEY_TOKEN, null);
        long exp = s.getLong(KEY_EXPIRY, 0L);
        if (token == null) return null;
        if (exp == 0L) return token;
        long now = System.currentTimeMillis();
        if (now + 5000 > exp) return null;
        return token;
    }
}

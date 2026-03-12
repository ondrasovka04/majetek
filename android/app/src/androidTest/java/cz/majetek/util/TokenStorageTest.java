package cz.majetek.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TokenStorageTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();

        // Delete shared-prefs files to ensure test isolation.
        context.deleteSharedPreferences("auth_pref_enc");
        context.deleteSharedPreferences("auth_pref");

        // Reset the static legacyCleaned flag so each test can exercise
        // the legacy-cleanup path independently.
        Field field = TokenStorage.class.getDeclaredField("legacyCleaned");
        field.setAccessible(true);
        field.setBoolean(null, false);
    }

    // ── round-trip ───────────────────────────────────────────

    @Test
    public void saveToken_thenGetValidToken_returnsToken() {
        long futureExpiry = System.currentTimeMillis() + 60_000; // +60 s
        TokenStorage.saveToken(context, "abc123", futureExpiry);

        String result = TokenStorage.getValidToken(context);
        assertNotNull(result);
        assertEquals("abc123", result);
    }

    // ── no token saved ───────────────────────────────────────

    @Test
    public void getValidToken_noTokenSaved_returnsNull() {
        assertNull(TokenStorage.getValidToken(context));
    }

    // ── expired token ────────────────────────────────────────

    @Test
    public void getValidToken_expiredToken_returnsNull() {
        long pastExpiry = System.currentTimeMillis() - 10_000; // 10 s ago
        TokenStorage.saveToken(context, "expired", pastExpiry);

        assertNull(TokenStorage.getValidToken(context));
    }

    // ── 5-second buffer ──────────────────────────────────────

    @Test
    public void getValidToken_expiryWithin5Seconds_returnsNull() {
        // Expiry exactly 3 s from now → within the 5 s buffer → null
        long nearExpiry = System.currentTimeMillis() + 3_000;
        TokenStorage.saveToken(context, "almostExpired", nearExpiry);

        assertNull(TokenStorage.getValidToken(context));
    }

    // ── unknown expiry (0) ───────────────────────────────────

    @Test
    public void getValidToken_expiryZero_returnsToken() {
        TokenStorage.saveToken(context, "noExpiry", 0L);

        String result = TokenStorage.getValidToken(context);
        assertNotNull(result);
        assertEquals("noExpiry", result);
    }

    // ── legacy pref cleanup ──────────────────────────────────

    @Test
    public void getValidToken_clearsLegacyPreferences() {
        // Write a value into the old unencrypted "auth_pref" file.
        SharedPreferences legacyPrefs = context.getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        legacyPrefs.edit().putString("token", "legacySecret").commit();

        // Access TokenStorage — this should trigger legacy cleanup.
        TokenStorage.getValidToken(context);

        // Re-read the legacy file and verify it was cleared.
        SharedPreferences legacyAfter = context.getSharedPreferences("auth_pref", Context.MODE_PRIVATE);
        assertNull(legacyAfter.getString("token", null));
        assertEquals(0, legacyAfter.getAll().size());
    }
}

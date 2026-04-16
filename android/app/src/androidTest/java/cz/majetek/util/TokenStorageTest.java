package cz.majetek.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TokenStorageTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteSharedPreferences("auth_pref_enc");
    }

    @Test
    public void saveToken_thenGetValidToken_returnsToken() {
        long futureExpiry = System.currentTimeMillis() + 60_000;
        TokenStorage.saveToken(context, "abc123", futureExpiry);

        String result = TokenStorage.getValidToken(context);
        assertNotNull(result);
        assertEquals("abc123", result);
    }

    @Test
    public void getValidToken_noTokenSaved_returnsNull() {
        assertNull(TokenStorage.getValidToken(context));
    }

    @Test
    public void getValidToken_expiredToken_returnsNull() {
        long pastExpiry = System.currentTimeMillis() - 10_000;
        TokenStorage.saveToken(context, "expired", pastExpiry);

        assertNull(TokenStorage.getValidToken(context));
    }

    @Test
    public void getValidToken_expiryWithin5Seconds_returnsNull() {
        long nearExpiry = System.currentTimeMillis() + 3_000;
        TokenStorage.saveToken(context, "almostExpired", nearExpiry);

        assertNull(TokenStorage.getValidToken(context));
    }

    @Test
    public void getValidToken_expiryZero_returnsToken() {
        TokenStorage.saveToken(context, "noExpiry", 0L);

        String result = TokenStorage.getValidToken(context);
        assertNotNull(result);
        assertEquals("noExpiry", result);
    }

    @Test
    public void saveCredentials_thenRetrieve() {
        TokenStorage.saveCredentials(context, "user", "pass");

        assertEquals("user", TokenStorage.getUsername(context));
        assertEquals("pass", TokenStorage.getPassword(context));
    }

    @Test
    public void getUsername_noCredentialsSaved_returnsNull() {
        assertNull(TokenStorage.getUsername(context));
        assertNull(TokenStorage.getPassword(context));
    }
}

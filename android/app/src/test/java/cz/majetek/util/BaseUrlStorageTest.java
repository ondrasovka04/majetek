package cz.majetek.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class BaseUrlStorageTest {

    // ── isValid ──────────────────────────────────────────────

    @Test
    public void isValid_null_returnsFalse() {
        assertFalse(BaseUrlStorage.isValid(null));
    }

    @Test
    public void isValid_emptyString_returnsFalse() {
        assertFalse(BaseUrlStorage.isValid(""));
    }

    @Test
    public void isValid_httpUrl_returnsFalse() {
        assertFalse(BaseUrlStorage.isValid("http://example.com"));
    }

    @Test
    public void isValid_tooShort_returnsFalse() {
        assertFalse(BaseUrlStorage.isValid("https://x"));
    }

    @Test
    public void isValid_validUrl_returnsTrue() {
        assertTrue(BaseUrlStorage.isValid("https://example.com"));
    }

    // ── sanitize ─────────────────────────────────────────────

    @Test
    public void sanitize_null_returnsDefault() {
        assertEquals("https://majetek.zs-ustavni.cz/api", BaseUrlStorage.sanitize(null));
    }

    @Test
    public void sanitize_trailingSlashes_removed() {
        assertEquals("https://example.com", BaseUrlStorage.sanitize("https://example.com///"));
    }

    @Test
    public void sanitize_whitespace_trimmed() {
        assertEquals("https://example.com", BaseUrlStorage.sanitize("  https://example.com  "));
    }

    @Test
    public void sanitize_normalUrl_unchanged() {
        assertEquals("https://example.com/api", BaseUrlStorage.sanitize("https://example.com/api"));
    }
}

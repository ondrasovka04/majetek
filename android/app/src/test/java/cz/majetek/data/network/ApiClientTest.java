package cz.majetek.data.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class ApiClientTest {
    private MockWebServer server;
    private ApiClient client;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new ApiClient(server.url("/").toString());
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    // ---- login tests ----

    @Test
    public void login_success_returnsTokenAndExpiry() throws Exception {
        String json = "{\"token\":\"abc123\",\"expiresIn\":\"2025-01-01T00:00:00.000+00:00\"}";
        server.enqueue(new MockResponse().setBody(json).setResponseCode(200).addHeader("Content-Type", "application/json"));

        LoginResult result = client.login("dGVzdDp0ZXN0");

        assertEquals("abc123", result.token());
        assertEquals(1735689600000L, result.expiresAtEpochMillis());

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertNotNull(req.getPath());
        assertTrue(req.getPath().contains("/auth/login"));
        assertEquals("Basic dGVzdDp0ZXN0", req.getHeader("Authorization"));
    }

    @Test
    public void login_httpError_throwsIOException() {
        server.enqueue(new MockResponse().setResponseCode(401));

        IOException ex = assertThrows(IOException.class, () -> client.login("dGVzdDp0ZXN0"));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("HTTP 401"));
    }

    @Test
    public void login_invalidJson_throwsIOException() {
        server.enqueue(new MockResponse().setBody("not json").setResponseCode(200));

        assertThrows(IOException.class, () -> client.login("dGVzdDp0ZXN0"));
    }

    @Test
    public void login_emptyBody_throwsIOException() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(""));

        assertThrows(IOException.class, () -> client.login("dGVzdDp0ZXN0"));
    }

    // ---- downloadCards tests ----

    @Test
    public void downloadCards_success_returnsJsonString() throws Exception {
        String jsonArray = "[{\"id\":1,\"name\":\"Card1\"},{\"id\":2,\"name\":\"Card2\"}]";
        server.enqueue(new MockResponse().setBody(jsonArray).setResponseCode(200).addHeader("Content-Type", "application/json"));

        String result = client.downloadCards("bearer-token-123");

        assertEquals(jsonArray, result);

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertNotNull(req.getPath());
        assertTrue(req.getPath().contains("/card/filter"));
        assertEquals("Bearer bearer-token-123", req.getHeader("Authorization"));
    }

    @Test
    public void downloadCards_httpError_throwsIOException() {
        server.enqueue(new MockResponse().setResponseCode(500));

        IOException ex = assertThrows(IOException.class, () -> client.downloadCards("token"));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("HTTP 500"));
    }

    // ---- downloadLocations tests ----

    @Test
    public void downloadLocations_success_returnsJsonString() throws Exception {
        String jsonArray = "[{\"id\":10,\"name\":\"Loc1\"}]";
        server.enqueue(new MockResponse().setBody(jsonArray).setResponseCode(200).addHeader("Content-Type", "application/json"));

        String result = client.downloadLocations("bearer-token-456");

        assertEquals(jsonArray, result);

        RecordedRequest req = server.takeRequest();
        assertEquals("GET", req.getMethod());
        assertNotNull(req.getPath());
        assertTrue(req.getPath().contains("/location"));
        assertEquals("Bearer bearer-token-456", req.getHeader("Authorization"));
    }

    // ---- uploadInventoryAudit tests ----

    @Test
    public void uploadInventoryAudit_success_noException() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200));
        String payload = "[{\"cardId\":1,\"locationId\":2}]";

        client.uploadInventoryAudit("bearer-token-789", payload);

        RecordedRequest req = server.takeRequest();
        assertEquals("POST", req.getMethod());
        assertNotNull(req.getPath());
        assertTrue(req.getPath().contains("/inventory-audit/insert-many"));
        assertEquals("Bearer bearer-token-789", req.getHeader("Authorization"));
        assertEquals(payload, req.getBody().readUtf8());
        assertEquals("application/json; charset=utf-8", req.getHeader("Content-Type"));
    }

    @Test
    public void uploadInventoryAudit_httpError_throwsIOException() {
        server.enqueue(new MockResponse().setResponseCode(500));

        IOException ex = assertThrows(IOException.class, () -> client.uploadInventoryAudit("token", "[{}]"));
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("HTTP 500"));
    }
}

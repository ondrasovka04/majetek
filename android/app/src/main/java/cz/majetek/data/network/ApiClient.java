package cz.majetek.data.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.time.OffsetDateTime;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * HTTP client for backend REST API communication (OkHttp + Gson).
 */
public class ApiClient {
    private final String baseUrl;
    private final OkHttpClient client = new OkHttpClient();

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Authenticates via Basic Auth, returns token + expiration.
     */
    public LoginResult login(String basicAuth) throws IOException {
        Request request = new Request.Builder().url(baseUrl + "/auth/login").post(RequestBody.create(new byte[0], MediaType.parse("application/json"))).addHeader("Authorization", "Basic " + basicAuth).build();
        try (Response r = client.newCall(request).execute()) {
            if (!r.isSuccessful()) throw new IOException("HTTP " + r.code());
            String body = r.body().string().trim();
            try {
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                String token = obj.has("token") ? obj.get("token").getAsString() : body;
                long expiresAt = 0L;
                if (obj.has("expiresIn")) {
                    String raw = obj.get("expiresIn").getAsString();
                    OffsetDateTime odt = OffsetDateTime.parse(raw);
                    expiresAt = odt.toInstant().toEpochMilli();
                }
                return new LoginResult(token, expiresAt);
            } catch (Exception e) {
                throw new IOException("Failed to parse login response", e);
            }
        }
    }

    public String downloadCards(String bearerToken) throws IOException {
        Request request = finalizeRequest(new Request.Builder().url(baseUrl + "/card/filter?includeWriteOff=false").get(), bearerToken);
        try (Response r = client.newCall(request).execute()) {
            if (!r.isSuccessful()) throw new IOException("HTTP " + r.code());
            return r.body().string();
        }
    }

    public String downloadLocations(String bearerToken) throws IOException {
        Request request = finalizeRequest(new Request.Builder().url(baseUrl + "/location").get(), bearerToken);
        try (Response r = client.newCall(request).execute()) {
            if (!r.isSuccessful()) throw new IOException("HTTP " + r.code());
            return r.body().string();
        }
    }

    public void uploadInventoryAudit(String bearerToken, String jsonArray) throws IOException {
        RequestBody body = RequestBody.create(jsonArray, MediaType.parse("application/json"));
        Request request = finalizeRequest(new Request.Builder().url(baseUrl + "/inventory-audit/insert-many").post(body), bearerToken);
        try (Response r = client.newCall(request).execute()) {
            if (!r.isSuccessful()) throw new IOException("HTTP " + r.code());
        }
    }

    private Request finalizeRequest(Request.Builder builder, String bearerToken) {
        return builder.addHeader("Authorization", "Bearer " + bearerToken).build();
    }
}

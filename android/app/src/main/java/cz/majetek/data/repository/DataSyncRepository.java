package cz.majetek.data.repository;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.encodeToString;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.majetek.data.db.AppDatabase;
import cz.majetek.data.entity.CardEntity;
import cz.majetek.data.entity.InventoryAuditEntity;
import cz.majetek.data.entity.LocationEntity;
import cz.majetek.data.network.ApiClient;
import cz.majetek.data.network.LoginResult;
import cz.majetek.util.BaseUrlStorage;
import cz.majetek.util.TokenStorage;

/**
 * Orchestrates data synchronization between the API and local Room DB.
 */
public class DataSyncRepository {
    private ApiClient api;
    private String lastBaseUrl;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public interface Callback {
        void onSuccess(int cardCount);

        void onError(String msg);
    }

    public interface AuditUploadCallback {
        void onSuccess(int uploadedCount);

        void onError(String msg);
    }

    // Backend stores the scan time as LocalDateTime in Europe/Prague – send local Prague time without offset
    private static final DateTimeFormatter TS_FMT_BASE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.of("Europe/Prague"));

    /**
     * Downloads locations + cards from the API and saves them to Room DB in a transaction.
     */
    public void downloadToDevice(Context ctx, Callback cb) {
        io.execute(() -> {
            try {
                String token = ensureToken(ctx);
                List<LocationEntity> locations = fetchLocations(ctx, token);
                List<CardEntity> cards = fetchCards(ctx, token);
                AppDatabase db = AppDatabase.getInstance(ctx);
                db.runInTransaction(() -> {
                    db.cardDao().clear();
                    db.locationDao().clear();
                    db.locationDao().insertAll(locations);
                    db.cardDao().insertAll(cards);
                });
                cb.onSuccess(cards.size());
            } catch (Exception ex) {
                cb.onError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            }
        });
    }

    /**
     * Uploads inventory audit records as a JSON array to the server.
     */
    public void uploadInventoryAudit(Context ctx, AuditUploadCallback cb) {
        io.execute(() -> {
            try {
                String token = ensureToken(ctx);
                AppDatabase db = AppDatabase.getInstance(ctx);
                List<InventoryAuditEntity> audits = db.inventoryAuditDao().listAll();
                if (audits.isEmpty()) {
                    cb.onSuccess(0);
                    return;
                }
                String json = buildAuditJson(audits);
                getApi(ctx).uploadInventoryAudit(token, json);
                cb.onSuccess(audits.size());
            } catch (Exception ex) {
                cb.onError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            }
        });
    }

    /**
     * Returns a valid JWT token – from cache or via fresh login.
     */
    private String ensureToken(Context ctx) throws IOException {
        String token = TokenStorage.getValidToken(ctx);
        if (token != null && !token.isEmpty()) return token;
        return login(ctx);
    }

    private List<LocationEntity> fetchLocations(Context ctx, String token) throws IOException {
        String locJson = getApi(ctx).downloadLocations(token);
        if (locJson == null) throw new IOException("Empty locations body");
        List<LocationEntity> locations = new ArrayList<>();
        JsonArray locArr = JsonParser.parseString(locJson).getAsJsonArray();
        for (JsonElement el : locArr) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            locations.add(new LocationEntity(safeString(o, "id"), safeString(o, "name")));
        }
        return locations;
    }

    private List<CardEntity> fetchCards(Context ctx, String token) throws IOException {
        String json = getApi(ctx).downloadCards(token);
        if (json == null) throw new IOException("Empty cards body");
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        List<CardEntity> cards = new ArrayList<>();
        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            cards.add(new CardEntity(o.get("id").getAsInt(), safeString(o, "name"), safeDouble(o, "price"), safeDouble(o, "quantity"), safeStringOrNull(o, "locationId")));
        }
        return cards;
    }

    private String login(Context ctx) throws IOException {
        String user = TokenStorage.getUsername(ctx);
        String pass = TokenStorage.getPassword(ctx);
        if (user == null || user.isEmpty() || pass == null || pass.isEmpty())
            throw new IOException("Přihlašovací údaje nejsou nastaveny");
        String credentials = user + ":" + pass;
        String basic = encodeToString(credentials.getBytes(), NO_WRAP);
        LoginResult lr = getApi(ctx).login(basic);
        if (lr == null || lr.token() == null || lr.token().isEmpty())
            throw new IOException("Login selhal");
        long expiryMillis = lr.expiresAtEpochMillis();
        TokenStorage.saveToken(ctx, lr.token(), expiryMillis);
        return lr.token();
    }

    /**
     * Lazy ApiClient initialization – rebuilds when baseUrl changes.
     */
    private ApiClient getApi(Context ctx) {
        String base = BaseUrlStorage.getBaseUrl(ctx);
        if (api == null || lastBaseUrl == null || !lastBaseUrl.equals(base)) {
            api = new ApiClient(base);
            lastBaseUrl = base;
        }
        return api;
    }

    /**
     * Serializes inventory audit records to JSON with ISO timestamps in Europe/Prague local time.
     */
    String buildAuditJson(List<InventoryAuditEntity> audits) {
        JsonArray arr = new JsonArray();
        for (InventoryAuditEntity a : audits) {
            JsonObject obj = new JsonObject();
            obj.addProperty("cardId", a.getCardId());
            obj.addProperty("realLocationId", a.getRealLocationId());
            obj.addProperty("timestamp", TS_FMT_BASE.format(Instant.ofEpochMilli(a.getTimestamp())));
            arr.add(obj);
        }
        return arr.toString();
    }

    public void shutdown() {
        io.shutdownNow();
    }

    private static String safeString(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static String safeStringOrNull(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : null;
    }

    private static double safeDouble(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : 0.0;
    }
}

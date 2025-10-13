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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.majetek.BuildConfig;
import cz.majetek.data.db.AppDatabase;
import cz.majetek.data.entity.CardEntity;
import cz.majetek.data.entity.InventoryAuditEntity;
import cz.majetek.data.entity.LocationEntity;
import cz.majetek.data.network.ApiClient;
import cz.majetek.data.network.LoginResult;
import cz.majetek.util.TokenStorage;

public class DataSyncRepository {
    private final ApiClient api = new ApiClient();
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    public interface Callback {
        void onSuccess(int cardCount);

        void onError(String msg);
    }

    public interface AuditUploadCallback {
        void onSuccess(int uploadedCount);

        void onError(String msg);
    }

    private static final DateTimeFormatter TS_FMT_BASE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneOffset.UTC);

    public void downloadToDevice(Context ctx, Callback cb) {
        io.execute(() -> {
            try {
                String token = ensureToken(ctx);
                List<LocationEntity> locations = fetchLocations(token);
                List<CardEntity> cards = fetchCards(token);
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
                api.uploadInventoryAudit(token, json);
                cb.onSuccess(audits.size());
            } catch (Exception ex) {
                cb.onError(ex.getMessage() != null ? ex.getMessage() : ex.toString());
            }
        });
    }

    private String ensureToken(Context ctx) throws IOException {
        String token = TokenStorage.getValidToken(ctx);
        if (token != null && !token.isEmpty()) return token;
        return login(ctx);
    }

    private List<LocationEntity> fetchLocations(String token) throws IOException {
        String locJson = api.downloadLocations(token);
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

    private List<CardEntity> fetchCards(String token) throws IOException {
        String json = api.downloadCards(token);
        if (json == null) throw new IOException("Empty cards body");
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        List<CardEntity> cards = new ArrayList<>();
        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            cards.add(new CardEntity(o.get("id").getAsInt(), safeString(o, "name"), safeDouble(o, "price"), safeDouble(o, "quantity"), safeString(o, "locationId")));
        }
        return cards;
    }

    private String login(Context ctx) throws IOException {
        String credentials = BuildConfig.ADMIN_USER + ":" + BuildConfig.ADMIN_PASS;
        String basic = encodeToString(credentials.getBytes(), NO_WRAP);
        LoginResult lr = api.login(basic);
        if (lr == null || lr.token() == null || lr.token().isEmpty())
            throw new IOException("Login selhal");
        long expiryMillis = lr.expiresAtEpochMillis();
        TokenStorage.saveToken(ctx, lr.token(), expiryMillis);
        return lr.token();
    }

    private String buildAuditJson(List<InventoryAuditEntity> audits) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < audits.size(); i++) {
            InventoryAuditEntity a = audits.get(i);
            if (i > 0) sb.append(',');
            String ts = TS_FMT_BASE.format(Instant.ofEpochMilli(a.timestamp));
            sb.append('{').append("\"cardId\":").append(a.cardId).append(',').append("\"realLocationId\":\"").append(a.realLocationId).append("\",").append("\"timestamp\":\"").append(ts).append("\"").append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String safeString(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static double safeDouble(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsDouble() : 0.0;
    }
}

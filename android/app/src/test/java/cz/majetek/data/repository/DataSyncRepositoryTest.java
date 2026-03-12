package cz.majetek.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.majetek.data.entity.InventoryAuditEntity;

public class DataSyncRepositoryTest {

    private DataSyncRepository repository;

    @Before
    public void setUp() {
        repository = new DataSyncRepository();
    }

    // ── buildAuditJson ──────────────────────────────────────────────────

    @Test
    public void buildAuditJson_emptyList_returnsEmptyArray() {
        String json = repository.buildAuditJson(Collections.emptyList());
        assertEquals("[]", json);
    }

    @Test
    public void buildAuditJson_singleItem_correctJson() {
        long ts = 1_700_000_000_000L; // 2023-11-14T22:13:20.000 UTC
        InventoryAuditEntity audit = new InventoryAuditEntity(42, "LOC-7", ts);

        String json = repository.buildAuditJson(Collections.singletonList(audit));

        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        assertEquals(1, arr.size());

        JsonObject obj = arr.get(0).getAsJsonObject();
        assertEquals(42, obj.get("cardId").getAsInt());
        assertEquals("LOC-7", obj.get("realLocationId").getAsString());
        assertTrue(obj.has("timestamp"));

        // Verify the timestamp value matches expected UTC ISO format
        String expectedTs = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneOffset.UTC).format(Instant.ofEpochMilli(ts));
        assertEquals(expectedTs, obj.get("timestamp").getAsString());
    }

    @Test
    public void buildAuditJson_multipleItems_correctJson() {
        List<InventoryAuditEntity> audits = new ArrayList<>();
        audits.add(new InventoryAuditEntity(1, "A", 1_000L));
        audits.add(new InventoryAuditEntity(2, "B", 2_000L));
        audits.add(new InventoryAuditEntity(3, "C", 3_000L));

        String json = repository.buildAuditJson(audits);

        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        assertEquals(3, arr.size());

        // Verify each element has the correct cardId
        assertEquals(1, arr.get(0).getAsJsonObject().get("cardId").getAsInt());
        assertEquals(2, arr.get(1).getAsJsonObject().get("cardId").getAsInt());
        assertEquals(3, arr.get(2).getAsJsonObject().get("cardId").getAsInt());

        // Verify each element has the correct realLocationId
        assertEquals("A", arr.get(0).getAsJsonObject().get("realLocationId").getAsString());
        assertEquals("B", arr.get(1).getAsJsonObject().get("realLocationId").getAsString());
        assertEquals("C", arr.get(2).getAsJsonObject().get("realLocationId").getAsString());
    }

    @Test
    public void buildAuditJson_timestampFormat_isUtcIso() {
        // 2024-06-15T08:30:45.123 UTC
        long ts = Instant.parse("2024-06-15T08:30:45.123Z").toEpochMilli();
        InventoryAuditEntity audit = new InventoryAuditEntity(99, "X", ts);

        String json = repository.buildAuditJson(Collections.singletonList(audit));

        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        String timestamp = arr.get(0).getAsJsonObject().get("timestamp").getAsString();

        // Must be exactly "2024-06-15T08:30:45.123" (no Z suffix, millisecond precision)
        assertEquals("2024-06-15T08:30:45.123", timestamp);

        // Verify the format matches the pattern yyyy-MM-dd'T'HH:mm:ss.SSS
        assertTrue("Timestamp should contain 'T' separator", timestamp.contains("T"));
        assertTrue("Timestamp should have milliseconds (dot followed by 3 digits)", timestamp.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
    }
}

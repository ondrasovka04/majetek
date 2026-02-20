package cz.majetek.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.majetek.data.db.dao.CardDao;
import cz.majetek.data.db.dao.InventoryAuditDao;
import cz.majetek.data.db.dao.LocationDao;
import cz.majetek.data.entity.CardEntity;
import cz.majetek.data.entity.InventoryAuditEntity;
import cz.majetek.data.entity.LocationEntity;

@RunWith(AndroidJUnit4.class)
public class DaoTest {

    private AppDatabase db;
    private CardDao cardDao;
    private LocationDao locationDao;
    private InventoryAuditDao inventoryAuditDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        cardDao = db.cardDao();
        locationDao = db.locationDao();
        inventoryAuditDao = db.inventoryAuditDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    // ── CardDao tests ──

    @Test
    public void insertAndGetCard() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        CardEntity card = new CardEntity(1, "Card A", 100.0, 5.0, "L1");
        cardDao.insertAll(Collections.singletonList(card));

        CardEntity result = cardDao.getById(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Card A", result.getName());
        assertEquals(100.0, result.getPrice(), 0.001);
        assertEquals(5.0, result.getQuantity(), 0.001);
        assertEquals("L1", result.getLocationId());
    }

    @Test
    public void countAll_returnsCorrectCount() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        cardDao.insertAll(Arrays.asList(
                new CardEntity(1, "Card A", 10.0, 1.0, "L1"),
                new CardEntity(2, "Card B", 20.0, 2.0, "L1"),
                new CardEntity(3, "Card C", 30.0, 3.0, "L1")
        ));

        assertEquals(3, cardDao.countAll());
    }

    @Test
    public void countByLocation_filtersCorrectly() {
        locationDao.insertAll(Arrays.asList(
                new LocationEntity("L1", "Location 1"),
                new LocationEntity("L2", "Location 2")
        ));

        cardDao.insertAll(Arrays.asList(
                new CardEntity(1, "Card A", 10.0, 1.0, "L1"),
                new CardEntity(2, "Card B", 20.0, 2.0, "L1"),
                new CardEntity(3, "Card C", 30.0, 3.0, "L2")
        ));

        assertEquals(2, cardDao.countByLocation("L1"));
        assertEquals(1, cardDao.countByLocation("L2"));
        assertEquals(0, cardDao.countByLocation("L3"));
    }

    @Test
    public void clear_removesAllCards() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        cardDao.insertAll(Arrays.asList(
                new CardEntity(1, "Card A", 10.0, 1.0, "L1"),
                new CardEntity(2, "Card B", 20.0, 2.0, "L1")
        ));

        cardDao.clear();
        assertEquals(0, cardDao.countAll());
    }

    // ── LocationDao tests ──

    @Test
    public void insertAndGetLocation() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        LocationEntity result = locationDao.getById("L1");
        assertNotNull(result);
        assertEquals("L1", result.getId());
        assertEquals("Location 1", result.getName());
    }

    @Test
    public void clear_removesAllLocations() {
        locationDao.insertAll(Arrays.asList(
                new LocationEntity("L1", "Location 1"),
                new LocationEntity("L2", "Location 2")
        ));

        locationDao.clear();
        assertNull(locationDao.getById("L1"));
        assertNull(locationDao.getById("L2"));
    }

    // ── InventoryAuditDao tests ──

    @Test
    public void upsertAndCount() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        CardEntity card = new CardEntity(1, "Card A", 10.0, 1.0, "L1");
        cardDao.insertAll(Collections.singletonList(card));

        InventoryAuditEntity audit = new InventoryAuditEntity(1, "L1", System.currentTimeMillis());
        inventoryAuditDao.upsert(audit);

        assertEquals(1, inventoryAuditDao.countCardsTotal());
    }

    @Test
    public void countCardsInLocation_filtersCorrectly() {
        locationDao.insertAll(Arrays.asList(
                new LocationEntity("L1", "Location 1"),
                new LocationEntity("L2", "Location 2")
        ));

        cardDao.insertAll(Arrays.asList(
                new CardEntity(1, "Card A", 10.0, 1.0, "L1"),
                new CardEntity(2, "Card B", 20.0, 2.0, "L1"),
                new CardEntity(3, "Card C", 30.0, 3.0, "L2")
        ));

        inventoryAuditDao.upsert(new InventoryAuditEntity(1, "L1", 1000L));
        inventoryAuditDao.upsert(new InventoryAuditEntity(2, "L1", 2000L));
        inventoryAuditDao.upsert(new InventoryAuditEntity(3, "L2", 3000L));

        assertEquals(2, inventoryAuditDao.countCardsInLocation("L1"));
        assertEquals(1, inventoryAuditDao.countCardsInLocation("L2"));
    }

    @Test
    public void deleteByCardId_removesCorrectRecord() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        cardDao.insertAll(Arrays.asList(
                new CardEntity(1, "Card A", 10.0, 1.0, "L1"),
                new CardEntity(2, "Card B", 20.0, 2.0, "L1")
        ));

        inventoryAuditDao.upsert(new InventoryAuditEntity(1, "L1", 1000L));
        inventoryAuditDao.upsert(new InventoryAuditEntity(2, "L1", 2000L));

        inventoryAuditDao.deleteByCardId(1);

        assertEquals(1, inventoryAuditDao.countCardsTotal());
        List<InventoryAuditEntity> remaining = inventoryAuditDao.listAll();
        assertEquals(1, remaining.size());
        assertEquals(2, remaining.getFirst().getCardId());
    }

    @Test
    public void listAll_orderedByTimestampDesc() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        cardDao.insertAll(Arrays.asList(
                new CardEntity(1, "Card A", 10.0, 1.0, "L1"),
                new CardEntity(2, "Card B", 20.0, 2.0, "L1"),
                new CardEntity(3, "Card C", 30.0, 3.0, "L1")
        ));

        inventoryAuditDao.upsert(new InventoryAuditEntity(1, "L1", 1000L));
        inventoryAuditDao.upsert(new InventoryAuditEntity(2, "L1", 3000L));
        inventoryAuditDao.upsert(new InventoryAuditEntity(3, "L1", 2000L));

        List<InventoryAuditEntity> list = inventoryAuditDao.listAll();
        assertEquals(3, list.size());
        assertEquals(2, list.get(0).getCardId()); // timestamp 3000 first
        assertEquals(3, list.get(1).getCardId()); // timestamp 2000 second
        assertEquals(1, list.get(2).getCardId()); // timestamp 1000 last
    }

    @Test
    public void upsert_replacesDuplicate() {
        LocationEntity location = new LocationEntity("L1", "Location 1");
        locationDao.insertAll(Collections.singletonList(location));

        LocationEntity location2 = new LocationEntity("L2", "Location 2");
        locationDao.insertAll(Collections.singletonList(location2));

        CardEntity card = new CardEntity(1, "Card A", 10.0, 1.0, "L1");
        cardDao.insertAll(Collections.singletonList(card));

        inventoryAuditDao.upsert(new InventoryAuditEntity(1, "L1", 1000L));
        inventoryAuditDao.upsert(new InventoryAuditEntity(1, "L2", 2000L)); // same cardId, replaces

        assertEquals(1, inventoryAuditDao.countCardsTotal());

        List<InventoryAuditEntity> list = inventoryAuditDao.listAll();
        assertEquals(1, list.size());
        assertEquals("L2", list.getFirst().getRealLocationId());
        assertEquals(2000L, list.getFirst().getTimestamp());
    }
}

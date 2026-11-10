package cz.majetek.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cz.majetek.data.db.LocationCardRow;
import cz.majetek.data.entity.InventoryAuditEntity;

/**
 * DAO for inventory audit records – upsert, counts, deletion.
 */
@Dao
public interface InventoryAuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(InventoryAuditEntity entity);

    @Query("SELECT COUNT(*) FROM inventory_audit WHERE real_location_id = :locationId")
    int countCardsInLocation(String locationId);

    @Query("SELECT COUNT(*) FROM inventory_audit")
    int countCardsTotal();

    @Query("DELETE FROM inventory_audit WHERE card_id = :cardId")
    void deleteByCardId(int cardId);

    @Query("DELETE FROM inventory_audit")
    void clear();

    @Query("SELECT * FROM inventory_audit ORDER BY timestamp DESC")
    List<InventoryAuditEntity> listAll();

    /**
     * Cards that belong to the location or were scanned there, with their expected and real location.
     */
    @Query("SELECT c.id AS cardId, c.name AS name, "
            + "c.location_id AS expectedLocationId, el.name AS expectedLocationName, "
            + "a.real_location_id AS realLocationId, rl.name AS realLocationName "
            + "FROM card c "
            + "LEFT JOIN inventory_audit a ON a.card_id = c.id "
            + "LEFT JOIN location el ON el.id = c.location_id "
            + "LEFT JOIN location rl ON rl.id = a.real_location_id "
            + "WHERE c.location_id = :locationId OR a.real_location_id = :locationId "
            + "ORDER BY c.id")
    List<LocationCardRow> listCardsForLocation(String locationId);

}

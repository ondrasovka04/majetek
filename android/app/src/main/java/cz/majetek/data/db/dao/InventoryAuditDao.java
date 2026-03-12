package cz.majetek.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

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

}

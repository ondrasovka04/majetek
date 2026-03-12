package cz.majetek.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity for inventory audit records (actual location of a scanned card).
 */
@Entity(tableName = "inventory_audit", foreignKeys = {@ForeignKey(entity = CardEntity.class, parentColumns = "id", childColumns = "card_id"), @ForeignKey(entity = LocationEntity.class, parentColumns = "id", childColumns = "real_location_id")}, indices = {@Index(value = {"real_location_id"})})
public class InventoryAuditEntity {
    @PrimaryKey
    @ColumnInfo(name = "card_id")
    private int cardId;

    @ColumnInfo(name = "real_location_id")
    private String realLocationId;

    private long timestamp;

    public InventoryAuditEntity(int cardId, String realLocationId, long timestamp) {
        this.cardId = cardId;
        this.realLocationId = realLocationId;
        this.timestamp = timestamp;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getRealLocationId() {
        return realLocationId;
    }

    public void setRealLocationId(String realLocationId) {
        this.realLocationId = realLocationId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

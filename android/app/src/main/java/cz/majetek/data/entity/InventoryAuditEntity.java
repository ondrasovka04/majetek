package cz.majetek.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_audit", foreignKeys = {@ForeignKey(entity = LocationEntity.class, parentColumns = "id", childColumns = "real_location_id", onDelete = ForeignKey.NO_ACTION, onUpdate = ForeignKey.CASCADE)}, indices = {@Index(value = {"real_location_id"})})
public class InventoryAuditEntity {
    @PrimaryKey
    @ColumnInfo(name = "card_id")
    public int cardId;

    @ColumnInfo(name = "real_location_id")
    public String realLocationId;

    public long timestamp;

    public InventoryAuditEntity(int cardId, String realLocationId, long timestamp) {
        this.cardId = cardId;
        this.realLocationId = realLocationId;
        this.timestamp = timestamp;
    }
}

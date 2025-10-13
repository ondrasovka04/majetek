package cz.majetek.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "card", foreignKeys = @ForeignKey(entity = LocationEntity.class, parentColumns = "id", childColumns = "location_id", onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.SET_NULL), indices = {@Index(value = {"location_id"})})
public class CardEntity {
    @PrimaryKey
    public int id;
    public String name;
    public double price;
    public double quantity;
    @ColumnInfo(name = "location_id")
    public String locationId;

    public CardEntity(int id, String name, double price, double quantity, String locationId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.locationId = locationId;
    }
}


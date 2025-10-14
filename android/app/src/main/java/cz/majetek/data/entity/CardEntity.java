package cz.majetek.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;


@Entity(tableName = "card", foreignKeys = @ForeignKey(entity = LocationEntity.class, parentColumns = "id", childColumns = "location_id", onUpdate = ForeignKey.CASCADE, onDelete = ForeignKey.SET_NULL), indices = {@Index(value = {"location_id"})})
public class CardEntity {
    @PrimaryKey
    private int id;
    private String name;
    private double price;
    private double quantity;
    @ColumnInfo(name = "location_id")
    private String locationId;

    public CardEntity(int id, String name, double price, double quantity, String locationId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.locationId = locationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
}


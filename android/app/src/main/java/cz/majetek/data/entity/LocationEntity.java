package cz.majetek.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "location")
public class LocationEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String name;

    public LocationEntity(@NonNull String id, String name) {
        this.id = id;
        this.name = name;
    }
}

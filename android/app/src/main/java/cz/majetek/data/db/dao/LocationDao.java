package cz.majetek.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cz.majetek.data.entity.LocationEntity;

@Dao
public interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LocationEntity> locations);

    @Query("SELECT * FROM location WHERE id = :id LIMIT 1")
    LocationEntity getById(String id);

    @Query("DELETE FROM location")
    void clear();
}


package cz.majetek.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cz.majetek.data.entity.CardEntity;

@Dao
public interface CardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CardEntity> items);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CardEntity item);

    @Query("SELECT * FROM card WHERE id = :id LIMIT 1")
    CardEntity getById(int id);

    @Query("DELETE FROM card")
    void clear();

    @Query("SELECT COUNT(*) FROM card")
    int countAll();

    @Query("SELECT COUNT(*) FROM card WHERE location_id = :locationId")
    int countByLocation(String locationId);
}

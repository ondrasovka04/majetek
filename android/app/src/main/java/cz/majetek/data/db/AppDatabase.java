package cz.majetek.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import cz.majetek.data.db.dao.CardDao;
import cz.majetek.data.db.dao.InventoryAuditDao;
import cz.majetek.data.db.dao.LocationDao;
import cz.majetek.data.entity.CardEntity;
import cz.majetek.data.entity.InventoryAuditEntity;
import cz.majetek.data.entity.LocationEntity;

/**
 * Room database with destructive migration (cards, locations, inventory).
 */
@Database(entities = {CardEntity.class, LocationEntity.class, InventoryAuditEntity.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract CardDao cardDao();

    public abstract LocationDao locationDao();

    public abstract InventoryAuditDao inventoryAuditDao();

    /**
     * Singleton factory – creates the DB with fallbackToDestructiveMigration on first call.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "majetek.db").fallbackToDestructiveMigration(true).build();
        }
        return instance;
    }
}

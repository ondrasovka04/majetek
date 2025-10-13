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

@Database(entities = {CardEntity.class, LocationEntity.class, InventoryAuditEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract CardDao cardDao();

    public abstract LocationDao locationDao();

    public abstract InventoryAuditDao inventoryAuditDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "majetek.db").fallbackToDestructiveMigration(true).build();
                }
            }
        }
        return INSTANCE;
    }
}

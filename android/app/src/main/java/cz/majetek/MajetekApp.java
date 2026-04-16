package cz.majetek;

import android.app.Application;

import cz.majetek.data.db.AppDatabase;

/**
 * Application singleton – holds the global Room database instance.
 */
public class MajetekApp extends Application {
    private AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = AppDatabase.getInstance(this);
    }

    public AppDatabase getDatabase() {
        return db;
    }
}



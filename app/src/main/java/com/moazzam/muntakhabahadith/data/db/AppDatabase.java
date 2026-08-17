package com.moazzam.muntakhabahadith.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Main Room database for Muntakhab Ahadith.
 *
 * Tables:
 *   section_progress  – reading state for each of the six main sections
 *   general_last_seen – the most recently opened section (Continue Reading)
 *   imported_pdf      – user's personal PDF library
 *
 * Singleton pattern – use {@link #getInstance(Context)} everywhere.
 */
@Database(
    entities = {
        SectionProgress.class,
        GeneralLastSeen.class,
        ImportedPdf.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "muntakhab_ahadith.db";
    private static volatile AppDatabase INSTANCE;

    public abstract SectionProgressDao sectionProgressDao();
    public abstract GeneralLastSeenDao generalLastSeenDao();
    public abstract ImportedPdfDao     importedPdfDao();

    /**
     * Returns the singleton database instance.
     * Thread-safe via double-checked locking.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}

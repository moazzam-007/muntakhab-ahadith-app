package com.moazzam.muntakhabahadith.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface GeneralLastSeenDao {

    /** Saves or replaces the singleton general last-seen record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(GeneralLastSeen lastSeen);

    /** Returns the general last-seen record (null if nothing has been read yet). */
    @Query("SELECT * FROM general_last_seen WHERE id = 1")
    GeneralLastSeen get();

    /** Observable general last-seen record – used for the Continue Reading card. */
    @Query("SELECT * FROM general_last_seen WHERE id = 1")
    LiveData<GeneralLastSeen> getLive();

    /** Deletes the general last-seen record (e.g., when resetting all progress). */
    @Query("DELETE FROM general_last_seen")
    void deleteAll();
}

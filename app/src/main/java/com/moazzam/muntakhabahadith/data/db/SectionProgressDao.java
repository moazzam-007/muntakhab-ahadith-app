package com.moazzam.muntakhabahadith.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SectionProgressDao {

    @Query("SELECT * FROM section_progress ORDER BY updatedAt DESC LIMIT 1")
    LiveData<SectionProgress> getLatestProgressLive();

    /** Saves or replaces the progress for a section. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveProgress(SectionProgress progress);

    /** Returns the saved progress for a single section (null if never read). */
    @Query("SELECT * FROM section_progress WHERE sectionId = :sectionId")
    SectionProgress getProgress(String sectionId);

    /** Observable list of all section progress records. */
    @Query("SELECT * FROM section_progress")
    LiveData<List<SectionProgress>> getAllProgressLive();

    /** Synchronous fetch of all section progress records. */
    @Query("SELECT * FROM section_progress")
    List<SectionProgress> getAllProgress();



    /** Deletes all six section progress records. */
    @Query("DELETE FROM section_progress")
    void deleteAllProgress();
}

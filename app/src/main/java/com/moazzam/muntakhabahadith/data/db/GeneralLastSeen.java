package com.moazzam.muntakhabahadith.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity that stores the most recently opened Muntakhab Ahadith section.
 *
 * This is a singleton record – there will always be at most one row (id = 1).
 * It is used to populate the "Continue Reading" card on the home screen.
 *
 * This record does NOT replace the six independent SectionProgress records.
 * Each section maintains its own progress independently.
 */
@Entity(tableName = "general_last_seen")
public class GeneralLastSeen {

    /** Always 1 – this table only ever contains a single row. */
    @PrimaryKey
    public int id;

    /** Section ID of the most recently opened section. */
    @NonNull
    public String sectionId;

    /** 0-based PDF page index of the last seen page in that section. */
    public int currentPage;

    /** Timestamp of last update (System.currentTimeMillis()). */
    public long updatedAt;

    public GeneralLastSeen(int id, @NonNull String sectionId,
                           int currentPage, long updatedAt) {
        this.id          = id;
        this.sectionId   = sectionId;
        this.currentPage = currentPage;
        this.updatedAt   = updatedAt;
    }
}

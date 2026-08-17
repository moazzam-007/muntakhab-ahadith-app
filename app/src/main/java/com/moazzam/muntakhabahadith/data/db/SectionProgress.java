package com.moazzam.muntakhabahadith.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity storing the reading state for one of the six main sections.
 *
 * sectionId is the primary key and corresponds to the IDs defined in SectionConfig.
 *
 * PAGE INDEX CONVENTION:
 *   currentPage is a 0-based PDF viewer page index (matches the PDF library's indexing).
 */
@Entity(tableName = "section_progress")
public class SectionProgress {

    /** Section ID – one of the IDs from SectionConfig (e.g. "kalimah_tayyibah") */
    @PrimaryKey
    @NonNull
    public String sectionId;

    /** 0-based PDF page index of the last seen page in this section. */
    public int currentPage;

    /** Timestamp of last update (System.currentTimeMillis()). */
    public long updatedAt;

    public SectionProgress(@NonNull String sectionId, int currentPage, long updatedAt) {
        this.sectionId    = sectionId;
        this.currentPage  = currentPage;
        this.updatedAt    = updatedAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
}

package com.moazzam.muntakhabahadith.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a user-imported PDF in the PDF Library feature.
 *
 * URIs are stored as strings and accessed via Android's Storage Access Framework.
 * A URI may become inaccessible if:
 *   - The user deletes the original file
 *   - The document provider revokes access
 * The app handles this gracefully and does not crash.
 */
@Entity(tableName = "imported_pdf")
public class ImportedPdf {

    /** Auto-generated unique identifier. */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Human-readable display name (typically the file name from the document picker). */
    @NonNull
    public String displayName;

    /** Persisted content URI string – access via Storage Access Framework. */
    @NonNull
    public String uriString;

    /** 0-based PDF page index of the last seen page. */
    public int lastPage;

    /** Reading progress as a fraction in [0.0, 1.0]. */
    public float progress;

    /** Timestamp when this PDF was added to the library. */
    public long addedAt;

    /** Timestamp of the last reading update. */
    public long updatedAt;

    public ImportedPdf(@NonNull String displayName, @NonNull String uriString,
                       int lastPage, float progress, long addedAt, long updatedAt) {
        this.displayName = displayName;
        this.uriString   = uriString;
        this.lastPage    = lastPage;
        this.progress    = progress;
        this.addedAt     = addedAt;
        this.updatedAt   = updatedAt;
    }
}

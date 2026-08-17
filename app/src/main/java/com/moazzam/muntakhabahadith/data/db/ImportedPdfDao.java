package com.moazzam.muntakhabahadith.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImportedPdfDao {

    /** Inserts a new imported PDF record. Returns the auto-generated row ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ImportedPdf pdf);

    /** Updates an existing imported PDF record (e.g., after reading progress changes). */
    @Update
    void update(ImportedPdf pdf);

    /** Deletes an imported PDF record. Does not delete the actual file on disk. */
    @Delete
    void delete(ImportedPdf pdf);

    /** Observable list of all imported PDFs, newest first. */
    @Query("SELECT * FROM imported_pdf ORDER BY updatedAt DESC")
    LiveData<List<ImportedPdf>> getAllLive();

    /** Synchronous fetch of all imported PDFs. */
    @Query("SELECT * FROM imported_pdf ORDER BY updatedAt DESC")
    List<ImportedPdf> getAll();

    /** Returns a single imported PDF by its row ID (null if not found). */
    @Query("SELECT * FROM imported_pdf WHERE id = :id")
    ImportedPdf getById(long id);

    /** Deletes a single imported PDF by its row ID. */
    @Query("DELETE FROM imported_pdf WHERE id = :id")
    void deleteById(long id);

    /** Deletes all imported PDF records. */
    @Query("DELETE FROM imported_pdf")
    void deleteAll();
}

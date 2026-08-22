package com.moazzam.muntakhabahadith.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.moazzam.muntakhabahadith.data.db.AppDatabase;

import com.moazzam.muntakhabahadith.data.db.ImportedPdf;
import com.moazzam.muntakhabahadith.data.db.SectionProgress;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.util.Log;

/**
 * Single source of truth for all reading-state persistence.
 *
 * All database write operations run on a dedicated background thread via ExecutorService.
 * LiveData-returning methods are safe to call on the main thread.
 * Non-LiveData query methods (getPosition, getGeneralLastSeen, etc.) must NOT be called
 * on the main thread – call them from an executor or background thread.
 */
public class ReadingRepository {

    private static final String TAG = "ReadingRepository";
    private static volatile ReadingRepository INSTANCE;

    private final AppDatabase db;
    private final ExecutorService executor;

    private ReadingRepository(Context context) {
        db       = AppDatabase.getInstance(context.getApplicationContext());
        executor = Executors.newSingleThreadExecutor();
    }

    /** Returns the singleton repository instance. Thread-safe. */
    public static ReadingRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReadingRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ReadingRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ─── Section Progress ─────────────────────────────────────────────────────

    /**
     * Saves the reading position for a section and updates the general last-seen record.
     * Safe to call from the main thread (runs on background executor).
     */
    @MainThread
    public Future<?> savePosition(String sectionId, int page, Runnable onSuccess) {
        return executor.submit(() -> {
            try {
                long now = System.currentTimeMillis();
                SectionProgress sp = new SectionProgress(sectionId, page, now);
                db.sectionProgressDao().saveProgress(sp);
                if (onSuccess != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving position for section: " + sectionId, e);
                throw new RuntimeException("Database error saving position", e);
            }
        });
    }

    /**
     * Overload for internal safety saves without a callback.
     */
    @MainThread
    public Future<?> savePosition(String sectionId, int page) {
        return savePosition(sectionId, page, null);
    }

    /**
     * Returns the saved progress for a section, or null if the section has never been read.
     * Must NOT be called on the main thread.
     */
    @WorkerThread
    public SectionProgress getPosition(String sectionId) {
        return db.sectionProgressDao().getProgress(sectionId);
    }

    /** Observable list of all section progress records. Safe to observe from the UI. */
    @MainThread
    public LiveData<List<SectionProgress>> getAllProgressLive() {
        return db.sectionProgressDao().getAllProgressLive();
    }

    /** Observable single most recent section progress. Safe to observe from UI. */
    @MainThread
    public LiveData<SectionProgress> getLatestProgressLive() {
        return db.sectionProgressDao().getLatestProgressLive();
    }



    /**
     * Resets all Muntakhab Ahadith progress:
     *   - All six section reading positions
     *   - The general last-seen record
     * Safe to call from the main thread.
     */
    @MainThread
    public Future<?> resetAllProgress() {
        return executor.submit(() -> {
            try {
                db.sectionProgressDao().deleteAllProgress();
            } catch (Exception e) {
                Log.e(TAG, "Error resetting all progress", e);
                throw new RuntimeException("Database error resetting all progress", e);
            }
        });
    }



    // ─── Imported PDFs ────────────────────────────────────────────────────────

    /**
     * Observable list of all imported PDFs, newest first.
     * Safe to observe from the UI.
     */
    @MainThread
    public LiveData<List<ImportedPdf>> getAllImportedPdfsLive() {
        return db.importedPdfDao().getAllLive();
    }

    /** Adds a new imported PDF to the library. Safe to call from the main thread. */
    @MainThread
    public Future<?> addImportedPdf(ImportedPdf pdf) {
        return executor.submit(() -> {
            try {
                db.importedPdfDao().insert(pdf);
            } catch (Exception e) {
                Log.e(TAG, "Error adding imported PDF", e);
                throw new RuntimeException("Database error adding imported PDF", e);
            }
        });
    }

    /** Updates an existing imported PDF record. Safe to call from the main thread. */
    @MainThread
    public Future<?> updateImportedPdf(ImportedPdf pdf) {
        return executor.submit(() -> {
            try {
                db.importedPdfDao().update(pdf);
            } catch (Exception e) {
                Log.e(TAG, "Error updating imported PDF", e);
                throw new RuntimeException("Database error updating imported PDF", e);
            }
        });
    }

    /**
     * Saves the reading position for an imported PDF.
     * Calculates progress from currentPage / totalPages.
     * Safe to call from the main thread.
     */
    @MainThread
    public Future<?> saveImportedPdfPosition(ImportedPdf pdf, int currentPage, int totalPages, Runnable onSuccess) {
        return executor.submit(() -> {
            try {
                pdf.lastPage   = currentPage;
                pdf.progress   = totalPages > 0 ? (float) (currentPage + 1) / totalPages : 0f;
                pdf.updatedAt  = System.currentTimeMillis();
                db.importedPdfDao().update(pdf);
                if (onSuccess != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(onSuccess);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving imported PDF position", e);
                throw new RuntimeException("Database error saving imported PDF position", e);
            }
        });
    }

    /**
     * Overload for internal safety saves without a callback.
     */
    @MainThread
    public Future<?> saveImportedPdfPosition(ImportedPdf pdf, int currentPage, int totalPages) {
        return saveImportedPdfPosition(pdf, currentPage, totalPages, null);
    }

    /** Deletes an imported PDF record (does not delete the actual file). Safe from main thread. */
    @MainThread
    public Future<?> deleteImportedPdf(ImportedPdf pdf) {
        return executor.submit(() -> {
            try {
                db.importedPdfDao().delete(pdf);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting imported PDF", e);
                throw new RuntimeException("Database error deleting imported PDF", e);
            }
        });
    }

    /** Deletes all imported PDF records. Safe to call from the main thread. */
    @MainThread
    public Future<?> deleteAllImportedPdfs() {
        return executor.submit(() -> {
            try {
                db.importedPdfDao().deleteAll();
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all imported PDFs", e);
                throw new RuntimeException("Database error deleting all imported PDFs", e);
            }
        });
    }

    /**
     * Returns an imported PDF by its database ID.
     * Must NOT be called on the main thread.
     */
    @WorkerThread
    public ImportedPdf getImportedPdfById(long id) {
        return db.importedPdfDao().getById(id);
    }
}

package com.moazzam.muntakhabahadith.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.moazzam.muntakhabahadith.data.db.AppDatabase;
import com.moazzam.muntakhabahadith.data.db.GeneralLastSeen;
import com.moazzam.muntakhabahadith.data.db.ImportedPdf;
import com.moazzam.muntakhabahadith.data.db.SectionProgress;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for all reading-state persistence.
 *
 * All database write operations run on a dedicated background thread via ExecutorService.
 * LiveData-returning methods are safe to call on the main thread.
 * Non-LiveData query methods (getPosition, getGeneralLastSeen, etc.) must NOT be called
 * on the main thread – call them from an executor or background thread.
 */
public class ReadingRepository {

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
    public void savePosition(String sectionId, int page) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();

            // Save section-specific position
            SectionProgress sp = new SectionProgress(sectionId, page, now);
            db.sectionProgressDao().saveProgress(sp);

            // Update the general last-seen (for Continue Reading)
            GeneralLastSeen gls = new GeneralLastSeen(1, sectionId, page, now);
            db.generalLastSeenDao().save(gls);
        });
    }

    /**
     * Returns the saved progress for a section, or null if the section has never been read.
     * Must NOT be called on the main thread.
     */
    public SectionProgress getPosition(String sectionId) {
        return db.sectionProgressDao().getProgress(sectionId);
    }

    /** Observable list of all section progress records. Safe to observe from the UI. */
    public LiveData<List<SectionProgress>> getAllProgressLive() {
        return db.sectionProgressDao().getAllProgressLive();
    }

    /** Resets progress for a single section. Safe to call from the main thread. */
    public void resetSectionProgress(String sectionId) {
        executor.execute(() -> db.sectionProgressDao().deleteProgress(sectionId));
    }

    /**
     * Resets all Muntakhab Ahadith progress:
     *   - All six section reading positions
     *   - The general last-seen record
     * Safe to call from the main thread.
     */
    public void resetAllProgress() {
        executor.execute(() -> {
            db.sectionProgressDao().deleteAllProgress();
            db.generalLastSeenDao().deleteAll();
        });
    }

    // ─── General Last Seen ────────────────────────────────────────────────────

    /**
     * Observable general last-seen record.
     * Used to populate the Continue Reading card. Safe to observe from the UI.
     */
    public LiveData<GeneralLastSeen> getGeneralLastSeenLive() {
        return db.generalLastSeenDao().getLive();
    }

    /**
     * Returns the general last-seen record synchronously.
     * Must NOT be called on the main thread.
     */
    public GeneralLastSeen getGeneralLastSeen() {
        return db.generalLastSeenDao().get();
    }

    // ─── Imported PDFs ────────────────────────────────────────────────────────

    /**
     * Observable list of all imported PDFs, newest first.
     * Safe to observe from the UI.
     */
    public LiveData<List<ImportedPdf>> getAllImportedPdfsLive() {
        return db.importedPdfDao().getAllLive();
    }

    /** Adds a new imported PDF to the library. Safe to call from the main thread. */
    public void addImportedPdf(ImportedPdf pdf) {
        executor.execute(() -> db.importedPdfDao().insert(pdf));
    }

    /** Updates an existing imported PDF record. Safe to call from the main thread. */
    public void updateImportedPdf(ImportedPdf pdf) {
        executor.execute(() -> db.importedPdfDao().update(pdf));
    }

    /**
     * Saves the reading position for an imported PDF.
     * Calculates progress from currentPage / totalPages.
     * Safe to call from the main thread.
     */
    public void saveImportedPdfPosition(ImportedPdf pdf, int currentPage, int totalPages) {
        executor.execute(() -> {
            pdf.lastPage   = currentPage;
            pdf.progress   = totalPages > 0 ? (float) currentPage / totalPages : 0f;
            pdf.updatedAt  = System.currentTimeMillis();
            db.importedPdfDao().update(pdf);
        });
    }

    /** Deletes an imported PDF record (does not delete the actual file). Safe from main thread. */
    public void deleteImportedPdf(ImportedPdf pdf) {
        executor.execute(() -> db.importedPdfDao().delete(pdf));
    }

    /** Deletes all imported PDF records. Safe to call from the main thread. */
    public void deleteAllImportedPdfs() {
        executor.execute(() -> db.importedPdfDao().deleteAll());
    }

    /**
     * Returns an imported PDF by its database ID.
     * Must NOT be called on the main thread.
     */
    public ImportedPdf getImportedPdfById(long id) {
        return db.importedPdfDao().getById(id);
    }
}

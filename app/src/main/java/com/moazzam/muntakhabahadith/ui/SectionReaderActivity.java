package com.moazzam.muntakhabahadith.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.moazzam.muntakhabahadith.R;
import com.moazzam.muntakhabahadith.data.db.SectionProgress;
import com.moazzam.muntakhabahadith.data.model.Section;
import com.moazzam.muntakhabahadith.data.repository.ReadingRepository;
import com.moazzam.muntakhabahadith.utils.ProgressCalculator;
import com.moazzam.muntakhabahadith.utils.SectionConfig;
import com.rajat.pdfviewer.PdfRendererView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import com.moazzam.muntakhabahadith.utils.AssetCopier;

/**
 * PDF reader for the six main sections of Muntakhab Ahadith.
 *
 * Behaviour:
 *   - If EXTRA_START_PAGE is provided (from Continue Reading), open at that page directly.
 *   - Otherwise, check for a saved position and ask the user whether to continue or start over.
 *   - If no saved position exists, open at the section's first page.
 *   - Auto-saves the current page on onPause() (safety save).
 *   - Provides a manual "Save Last Seen" button.
 *   - Survives rotation via onSaveInstanceState.
 */
public class SectionReaderActivity extends AppCompatActivity {

    /** Intent extras */
    public static final String EXTRA_SECTION_ID = "section_id";
    public static final String EXTRA_START_PAGE  = "start_page";

    private static final String STATE_CURRENT_PAGE = "current_page";

    private Section           section;
    private ReadingRepository repository;
    private ExecutorService   executor;

    private PdfRendererView pdfRendererView;
    private TextView        tvPageIndicator;
    private TextView        tvSectionTitle;

    private int     currentPage = 0;
    private int     totalPages  = 0;
    private boolean pdfLoaded   = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section_reader);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = ReadingRepository.getInstance(this);
        executor   = Executors.newSingleThreadExecutor();

        // Resolve section
        String sectionId = getIntent().getStringExtra(EXTRA_SECTION_ID);
        section = SectionConfig.getSectionById(sectionId);
        if (section == null) {
            Toast.makeText(this, R.string.error_invalid_section, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();

        // Decide starting page
        if (savedInstanceState != null) {
            // Rotation: restore the exact page we were on
            currentPage = savedInstanceState.getInt(STATE_CURRENT_PAGE, section.getPdfStartPage());
            initPdf(currentPage);
        } else {
            int intentPage = getIntent().getIntExtra(EXTRA_START_PAGE, -1);
            if (intentPage >= 0) {
                // Direct navigation (e.g. from Continue Reading card)
                currentPage = SectionConfig.clampToSection(intentPage, section);
                initPdf(currentPage);
            } else {
                // Check saved progress in background, then decide
                loadSavedPositionAndDecide();
            }
        }
    }

    // ─── View Binding ─────────────────────────────────────────────────────────────

    private void bindViews() {
        pdfRendererView = findViewById(R.id.pdf_renderer_view);
        tvPageIndicator = findViewById(R.id.tv_page_indicator);
        tvSectionTitle  = findViewById(R.id.tv_section_title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(section.getTitle());
        }
        tvSectionTitle.setText(section.getTitle());

        // Manual save button
        Button btnSave = findViewById(R.id.btn_save_last_seen);
        btnSave.setOnClickListener(v -> saveCurrentPosition());

        // Start from beginning button
        Button btnStart = findViewById(R.id.btn_start_beginning);
        btnStart.setOnClickListener(v -> {
            currentPage = section.getPdfStartPage();
            if (pdfLoaded) {
                pdfRendererView.jumpToPage(currentPage);
            }
        });
    }

    // ─── Saved Position Logic ─────────────────────────────────────────────────────

    private void loadSavedPositionAndDecide() {
        executor.execute(() -> {
            SectionProgress saved = repository.getPosition(section.getId());
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (saved != null && saved.currentPage > section.getPdfStartPage()) {
                    showContinueDialog(saved.currentPage);
                } else {
                    initPdf(section.getPdfStartPage());
                }
            });
        });
    }

    private void showContinueDialog(int savedPage) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_continue_title)
            .setMessage(getString(R.string.dialog_continue_message, savedPage + 1))
            .setPositiveButton(R.string.action_continue, (d, w) -> {
                currentPage = savedPage;
                initPdf(savedPage);
            })
            .setNegativeButton(R.string.action_start_beginning, (d, w) -> {
                currentPage = section.getPdfStartPage();
                initPdf(section.getPdfStartPage());
            })
            .setCancelable(false)
            .show();
    }

    // ─── PDF Initialisation ───────────────────────────────────────────────────────

    private void initPdf(int startPage) {
        currentPage = SectionConfig.clampToValidRange(startPage);

        pdfRendererView.setStatusListener(new PdfRendererView.StatusCallBack() {

            @Override
            public void onPdfLoadStart() {
                tvPageIndicator.setText(R.string.loading_pdf);
            }

            @Override
            public void onPdfLoadProgress(int progress, long downloadedBytes, Long totalBytes) {
                // Not applicable for local asset loading
            }

            @Override
            public void onPdfLoadSuccess(String absolutePath) {
                pdfLoaded = true;
                // P1-1 fix: Position the viewer at the saved/requested page
                if (currentPage > 0) {
                    pdfRendererView.jumpToPage(currentPage);
                }
            }

            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> showPdfError(
                    error.getMessage() != null ? error.getMessage() : "Unknown error"));
            }

            @Override
            public void onPageChanged(int page, int total) {
                currentPage = page;
                totalPages  = total;
                updatePageIndicator(page, total);
            }
        });

        // P0-2 fix: Extract asset to cache, then use initWithFile
        executor.execute(() -> {
            try {
                File pdfFile = AssetCopier.copyAssetToCache(this, SectionConfig.PDF_ASSET_NAME);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    try {
                        pdfRendererView.initWithFile(pdfFile);
                    } catch (Exception e) {
                        showPdfError(e.getMessage() != null ? e.getMessage() : "Failed to load PDF");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> showPdfError(e.getMessage() != null ? e.getMessage() : "Failed to extract PDF"));
            }
        });
    }

    // ─── UI Updates ───────────────────────────────────────────────────────────────

    private void updatePageIndicator(int page, int total) {
        int sectionRelative = ProgressCalculator.sectionRelativePage(page, section);
        int sectionTotal    = section.getPageCount();
        int percent         = ProgressCalculator.calculateProgressPercent(page, section);
        tvPageIndicator.setText(getString(
            R.string.page_indicator,
            page + 1, total,
            sectionRelative, sectionTotal,
            percent));
    }

    // ─── Persistence ──────────────────────────────────────────────────────────────

    private void saveCurrentPosition() {
        repository.savePosition(section.getId(), currentPage);
        Toast.makeText(this, R.string.last_seen_saved, Toast.LENGTH_SHORT).show();
    }

    // ─── Error Handling ───────────────────────────────────────────────────────────

    private void showPdfError(String message) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.error_pdf_title)
            .setMessage(getString(R.string.error_pdf_message, message))
            .setPositiveButton(android.R.string.ok, (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────────

    @Override
    protected void onPause() {
        super.onPause();
        // Safety auto-save: persist current position whenever the activity loses focus.
        // This protects against the user forgetting to press "Save Last Seen".
        if (pdfLoaded && section != null) {
            repository.savePosition(section.getId(), currentPage);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

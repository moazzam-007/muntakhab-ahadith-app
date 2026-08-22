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
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.SeekBar;

/**
 * PDF reader for the six main sections of Muntakhab Ahadith.
 *
 * Behaviour:
 *   - Checks for a saved position and asks the user whether to continue or start over.
 *   - If no saved position exists, opens at the section's first page.
 *   - Auto-saves the current page on onPause() (safety save).
 *   - Provides a manual "Save Last Seen" button.
 *   - Survives process death via onSaveInstanceState.
 */
public class SectionReaderActivity extends AppCompatActivity {

    /** Intent extras */
    public static final String EXTRA_SECTION_ID = "section_id";

    private static final String STATE_CURRENT_PAGE = "current_page";

    private Section           section;
    private ReadingRepository repository;
    private ExecutorService   executor;

    private PdfRendererView pdfRendererView;
    private TextView        tvPageIndicator;
    private TextView        tvSectionTitle;
    private SeekBar         seekBarPage;

    private RecyclerView    pdfRecyclerView;

    private int     currentPage = 0;
    private int     targetPage  = -1;
    private boolean isJumpSettled = false;
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
            // Check saved progress in background, then decide
            loadSavedPositionAndDecide();
        }
    }

    // ─── View Binding ─────────────────────────────────────────────────────────────

    private void bindViews() {
        pdfRendererView = findViewById(R.id.pdf_renderer_view);
        tvPageIndicator = findViewById(R.id.tv_page_indicator);
        tvSectionTitle  = findViewById(R.id.tv_section_title);
        seekBarPage     = findViewById(R.id.seekbar_page);

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
            targetPage = section.getPdfStartPage();
            currentPage = targetPage;
            isJumpSettled = false;
            if (pdfLoaded) {
                jumpToPageSafely(targetPage);
            }
        });

        // Seekbar logic
        seekBarPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && pdfLoaded) {
                    targetPage = section.getPdfStartPage() + progress;
                    currentPage = targetPage;
                    isJumpSettled = false;
                    jumpToPageSafely(targetPage);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // ─── Saved Position Logic ─────────────────────────────────────────────────────

    private void loadSavedPositionAndDecide() {
        executor.execute(() -> {
            try {
                SectionProgress saved = repository.getPosition(section.getId());
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (saved != null && saved.currentPage > section.getPdfStartPage()) {
                        showContinueDialog(saved.currentPage);
                    } else {
                        initPdf(section.getPdfStartPage());
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("SectionReaderActivity", "Error loading saved position", e);
            }
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
        targetPage = SectionConfig.clampToSection(startPage, section);
        currentPage = targetPage;
        isJumpSettled = false;

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
                pdfRecyclerView = findRecyclerView(pdfRendererView);
                if (pdfRecyclerView != null) {
                    // Update seekbar max to section length
                    seekBarPage.setMax(section.getPageCount() - 1);
                }
                // Jump safely using the library's method
                if (targetPage >= 0) {
                    jumpToPageSafely(targetPage);
                }
            }

            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> showPdfError(
                    error.getMessage() != null ? error.getMessage() : "Unknown error"));
            }

            @Override
            public void onPageChanged(int page, int total) {
                // Guard against scrolling outside the section boundaries ONLY after jump settles
                if (isJumpSettled && (page < section.getPdfStartPage() || page > section.getPdfEndPage())) {
                    int clamped = SectionConfig.clampToSection(page, section);
                    targetPage = clamped;
                    currentPage = clamped;
                    isJumpSettled = false;
                    jumpToPageSafely(clamped);
                    return;
                }

                if (!isJumpSettled) {
                    // Suppress early page-changed noise until we hit target
                    if (page == targetPage || targetPage < 0) {
                        isJumpSettled = true;
                        currentPage = page;
                    }
                } else {
                    currentPage = page;
                }

                if (isJumpSettled) {
                    totalPages  = total;
                    int expectedMax = section.getPageCount() - 1;
                    if (seekBarPage.getMax() != expectedMax) {
                        seekBarPage.setMax(expectedMax);
                    }
                    // Update seekbar without triggering onProgressChanged listener's scroll
                    seekBarPage.setProgress(page - section.getPdfStartPage());
                    updatePageIndicator(page, total);
                }
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
                android.util.Log.e("SectionReaderActivity", "Error copying asset to cache", e);
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
        repository.savePosition(section.getId(), currentPage, () -> {
            if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, R.string.last_seen_saved, Toast.LENGTH_SHORT).show();
            }
        });
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
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE, currentPage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void jumpToPageSafely(int page) {
        try {
            if (pdfRendererView != null) {
                pdfRendererView.jumpToPage(page);
            }
        } catch (Exception e) {
            android.util.Log.e("SectionReaderActivity", "Failed to jump to page", e);
        }
    }

    private RecyclerView findRecyclerView(View view) {
        RecyclerView rv = findRecyclerViewRecursive(view);
        if (rv == null) {
            android.util.Log.w("SectionReaderActivity", "findRecyclerView: RecyclerView not found in PdfRendererView. Library internal structure may have changed.");
        }
        return rv;
    }

    private RecyclerView findRecyclerViewRecursive(View view) {
        if (view instanceof RecyclerView) {
            return (RecyclerView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                RecyclerView rv = findRecyclerViewRecursive(vg.getChildAt(i));
                if (rv != null) return rv;
            }
        }
        return null;
    }
}

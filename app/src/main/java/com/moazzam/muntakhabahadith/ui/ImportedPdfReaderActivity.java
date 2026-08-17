package com.moazzam.muntakhabahadith.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.moazzam.muntakhabahadith.R;
import com.moazzam.muntakhabahadith.data.db.ImportedPdf;
import com.moazzam.muntakhabahadith.data.repository.ReadingRepository;
import com.rajat.pdfviewer.PdfRendererView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PDF reader for user-imported PDFs from the PDF Library.
 *
 * Opens the PDF via a persisted content URI (Storage Access Framework).
 * Handles inaccessible URIs gracefully – shows an error message instead of crashing.
 */
public class ImportedPdfReaderActivity extends AppCompatActivity {

    public static final String EXTRA_PDF_ID = "pdf_id";
    private static final String STATE_CURRENT_PAGE = "current_page";

    private ReadingRepository repository;
    private ExecutorService   executor;

    private PdfRendererView pdfRendererView;
    private TextView        tvPageIndicator;

    private ImportedPdf importedPdf;
    private int         currentPage = 0;
    private int         totalPages  = 0;
    private boolean     pdfLoaded   = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imported_pdf_reader);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = ReadingRepository.getInstance(this);
        executor   = Executors.newSingleThreadExecutor();

        long pdfId = getIntent().getLongExtra(EXTRA_PDF_ID, -1L);
        if (pdfId == -1L) { finish(); return; }

        bindViews();

        final int restoredPage = (savedInstanceState != null)
            ? savedInstanceState.getInt(STATE_CURRENT_PAGE, -1) : -1;

        // Load the PDF record from the database on a background thread
        executor.execute(() -> {
            importedPdf = repository.getImportedPdfById(pdfId);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (importedPdf == null) {
                    Toast.makeText(this, R.string.error_pdf_not_found, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(importedPdf.displayName);
                }
                int startPage = (restoredPage >= 0) ? restoredPage : importedPdf.lastPage;
                initPdf(startPage);
            });
        });
    }

    private void bindViews() {
        pdfRendererView = findViewById(R.id.pdf_renderer_view);
        tvPageIndicator  = findViewById(R.id.tv_page_indicator);

        Button btnSave = findViewById(R.id.btn_save_last_seen);
        btnSave.setOnClickListener(v -> saveCurrentPosition());
    }

    private void initPdf(int startPage) {
        Uri uri;
        try {
            uri = Uri.parse(importedPdf.uriString);
        } catch (Exception e) {
            showUriError();
            return;
        }

        currentPage = Math.max(0, startPage);

        pdfRendererView.setStatusListener(new PdfRendererView.StatusCallBack() {

            @Override
            public void onPdfLoadStart() {
                tvPageIndicator.setText(R.string.loading_pdf);
            }

            @Override
            public void onPdfLoadProgress(int progress, long downloadedBytes, Long totalBytes) { }

            @Override
            public void onPdfLoadSuccess(String absolutePath) {
                pdfLoaded = true;
                if (currentPage > 0) {
                    pdfRendererView.jumpToPage(currentPage);
                }
            }

            @Override
            public void onError(Throwable error) {
                runOnUiThread(() -> showUriError());
            }

            @Override
            public void onPageChanged(int page, int total) {
                currentPage = page;
                totalPages  = total;
                tvPageIndicator.setText(getString(R.string.page_of, page + 1, total));
            }
        });

        try {
            pdfRendererView.initWithUri(uri);
        } catch (Exception e) {
            showUriError();
        }
    }

    private void saveCurrentPosition() {
        if (importedPdf != null) {
            repository.saveImportedPdfPosition(importedPdf, currentPage, totalPages);
            Toast.makeText(this, R.string.last_seen_saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUriError() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.error_pdf_title)
            .setMessage(R.string.error_pdf_inaccessible)
            .setPositiveButton(android.R.string.ok, (d, w) -> finish())
            .setCancelable(false)
            .show();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────────

    @Override
    protected void onPause() {
        super.onPause();
        // Safety auto-save
        if (pdfLoaded && importedPdf != null) {
            repository.saveImportedPdfPosition(importedPdf, currentPage, totalPages);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

package com.moazzam.muntakhabahadith.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moazzam.muntakhabahadith.R;
import com.moazzam.muntakhabahadith.data.db.ImportedPdf;
import com.moazzam.muntakhabahadith.data.repository.ReadingRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfLibraryActivity extends AppCompatActivity {

    private ReadingRepository repository;
    private ExecutorService executor;
    private ImportedPdfAdapter adapter;
    private TextView tvEmpty;

    /** SAF document picker launcher */
    private final ActivityResultLauncher<Intent> pickPdfLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) handlePickedPdf(uri);
                }
            }
        );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_library);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_pdf_library);
        }

        repository = ReadingRepository.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        tvEmpty    = findViewById(R.id.tv_empty);

        setupRecyclerView();
        observePdfs();

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_pdf);
        fabAdd.setOnClickListener(v -> openDocumentPicker());
    }

    private void setupRecyclerView() {
        adapter = new ImportedPdfAdapter(
            pdf -> openImportedPdf(pdf),
            pdf -> confirmDelete(pdf)
        );
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    private void observePdfs() {
        repository.getAllImportedPdfsLive().observe(this, pdfs -> {
            adapter.submitList(pdfs);
            boolean empty = (pdfs == null || pdfs.isEmpty());
            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    // ─── Document Picker (Storage Access Framework) ───────────────────────────────

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        // Request persistent read permission so we can re-open the file later
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickPdfLauncher.launch(intent);
    }

    private void handlePickedPdf(Uri uri) {
        // Persist URI permission across app restarts
        try {
            getContentResolver().takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            // Permission may already be held or not grantable – continue anyway
        }

        executor.execute(() -> {
            try {
                String displayName = resolveDisplayName(uri);
                long now = System.currentTimeMillis();
                ImportedPdf pdf = new ImportedPdf(displayName, uri.toString(), 0, 0f, now, now);
                repository.addImportedPdf(pdf);
            } catch (Exception e) {
                android.util.Log.e("PdfLibraryActivity", "Error adding imported PDF", e);
            }
        });
    }

    private String resolveDisplayName(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(
                uri,
                new String[]{android.provider.OpenableColumns.DISPLAY_NAME},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return cursor.getString(idx);
            }
        } catch (Exception ignored) { }
        // Fallback to last path segment
        String segment = uri.getLastPathSegment();
        return (segment != null && !segment.isEmpty()) ? segment : "Document";
    }

    // ─── Navigation ───────────────────────────────────────────────────────────────

    private void openImportedPdf(ImportedPdf pdf) {
        Intent intent = new Intent(this, ImportedPdfReaderActivity.class);
        intent.putExtra(ImportedPdfReaderActivity.EXTRA_PDF_ID, pdf.id);
        startActivity(intent);
    }

    private void confirmDelete(ImportedPdf pdf) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, pdf.displayName))
            .setPositiveButton(R.string.action_delete, (d, w) -> {
                try {
                    getContentResolver().releasePersistableUriPermission(
                        Uri.parse(pdf.uriString), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {}
                repository.deleteImportedPdf(pdf);
            })
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}

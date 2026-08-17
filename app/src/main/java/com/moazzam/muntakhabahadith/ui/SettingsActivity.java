package com.moazzam.muntakhabahadith.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.moazzam.muntakhabahadith.BuildConfig;
import com.moazzam.muntakhabahadith.R;
import com.moazzam.muntakhabahadith.data.repository.ReadingRepository;

/**
 * Minimal Settings screen.
 *
 * Provides:
 *   - App version display
 *   - Reset all Muntakhab Ahadith reading progress (with confirmation)
 *   - Clear the imported PDF library (with confirmation)
 *
 * Does NOT contain: account settings, cloud settings, or any network configuration.
 */
public class SettingsActivity extends AppCompatActivity {

    private ReadingRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_settings);
        }

        repository = ReadingRepository.getInstance(this);

        // Show app version
        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(getString(R.string.version_name, BuildConfig.VERSION_NAME));

        // Reset all Muntakhab Ahadith progress
        Button btnResetAll = findViewById(R.id.btn_reset_all_progress);
        btnResetAll.setOnClickListener(v -> confirmResetAll());

        // Clear imported PDF library
        Button btnResetImported = findViewById(R.id.btn_reset_imported_pdfs);
        btnResetImported.setOnClickListener(v -> confirmResetImported());
    }

    private void confirmResetAll() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_reset_title)
            .setMessage(R.string.dialog_reset_all_message)
            .setPositiveButton(R.string.action_reset, (d, w) -> {
                repository.resetAllProgress();
                Toast.makeText(this, R.string.progress_reset_done, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    private void confirmResetImported() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_reset_title)
            .setMessage(R.string.dialog_reset_imported_message)
            .setPositiveButton(R.string.action_reset, (d, w) -> {
                repository.deleteAllImportedPdfs();
                Toast.makeText(this, R.string.imported_pdfs_cleared, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}

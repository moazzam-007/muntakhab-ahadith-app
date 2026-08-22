package com.moazzam.muntakhabahadith.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.moazzam.muntakhabahadith.R;
import com.moazzam.muntakhabahadith.data.db.SectionProgress;
import com.moazzam.muntakhabahadith.data.model.Section;
import com.moazzam.muntakhabahadith.data.repository.ReadingRepository;
import com.moazzam.muntakhabahadith.utils.ProgressCalculator;
import com.moazzam.muntakhabahadith.utils.SectionConfig;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ReadingRepository repository;



    // Progress bars and percentage labels for the six sections
    private ProgressBar[] progressBars;
    private TextView[]    progressTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        repository = ReadingRepository.getInstance(this);


        bindContinueReading();
        bindProgressSection();
        bindSectionButtons();
        bindPdfLibraryButton();
    }



    // ─── Continue Reading ─────────────────────────────────────────────────────────

    private void bindContinueReading() {
        CardView cardContinue = findViewById(R.id.card_continue_reading);
        TextView tvContinueSection = findViewById(R.id.tv_continue_section);
        Button btnContinue = findViewById(R.id.btn_continue_reading);

        repository.getLatestProgressLive().observe(this, latest -> {
            if (latest != null) {
                Section section = SectionConfig.getSectionById(latest.sectionId);
                if (section != null) {
                    cardContinue.setVisibility(View.VISIBLE);
                    int sectionRelative = ProgressCalculator.sectionRelativePage(latest.currentPage, section);
                    tvContinueSection.setText(getString(R.string.page_indicator_simple, section.getTitle(), sectionRelative));
                    
                    View.OnClickListener listener = v -> openSection(latest.sectionId);
                    btnContinue.setOnClickListener(listener);
                    cardContinue.setOnClickListener(listener);
                    return;
                }
            }
            cardContinue.setVisibility(View.GONE);
        });
    }

    // ─── Progress Section ─────────────────────────────────────────────────────────

    private void bindProgressSection() {
        progressBars = new ProgressBar[]{
            findViewById(R.id.progress_kalimah),
            findViewById(R.id.progress_salah),
            findViewById(R.id.progress_ilm_zikr),
            findViewById(R.id.progress_ikram),
            findViewById(R.id.progress_ikhlas),
            findViewById(R.id.progress_dawat)
        };
        progressTexts = new TextView[]{
            findViewById(R.id.tv_progress_pct_kalimah),
            findViewById(R.id.tv_progress_pct_salah),
            findViewById(R.id.tv_progress_pct_ilm_zikr),
            findViewById(R.id.tv_progress_pct_ikram),
            findViewById(R.id.tv_progress_pct_ikhlas),
            findViewById(R.id.tv_progress_pct_dawat)
        };

        repository.getAllProgressLive().observe(this, progressList -> {
            int[] percents = new int[progressBars.length];
            if (progressList != null) {
                for (SectionProgress sp : progressList) {
                    Section section = SectionConfig.getSectionById(sp.sectionId);
                    if (section != null) {
                        int index = sectionIndex(sp.sectionId);
                        if (index >= 0) {
                            percents[index] = ProgressCalculator.calculateProgressPercent(sp.currentPage, section);
                        }
                    }
                }
            }
            for (int i = 0; i < progressBars.length; i++) {
                if (progressBars[i].getProgress() != percents[i]) {
                    progressBars[i].setProgress(percents[i]);
                }
                String pctString = percents[i] + "%";
                if (!progressTexts[i].getText().toString().equals(pctString)) {
                    progressTexts[i].setText(pctString);
                    progressBars[i].setContentDescription(
                        getString(R.string.cd_section_progress, SectionConfig.ALL_SECTIONS[i].getTitle(), percents[i]));
                }
            }
        });
    }

    private int sectionIndex(String sectionId) {
        for (int i = 0; i < SectionConfig.ALL_SECTIONS.length; i++) {
            if (SectionConfig.ALL_SECTIONS[i].getId().equals(sectionId)) return i;
        }
        return -1;
    }

    // ─── Section Buttons ──────────────────────────────────────────────────────────

    private void bindSectionButtons() {
        int[] buttonIds = {
            R.id.btn_kalimah,
            R.id.btn_salah,
            R.id.btn_ilm_zikr,
            R.id.btn_ikram,
            R.id.btn_ikhlas,
            R.id.btn_dawat
        };
        String[] sectionIds = {
            SectionConfig.ID_KALIMAH,
            SectionConfig.ID_SALAH,
            SectionConfig.ID_ILM_ZIKR,
            SectionConfig.ID_IKRAM,
            SectionConfig.ID_IKHLAS,
            SectionConfig.ID_DAWAT
        };
        for (int i = 0; i < buttonIds.length; i++) {
            final String id = sectionIds[i];
            Button btn = findViewById(buttonIds[i]);
            btn.setOnClickListener(v -> openSection(id));
        }
    }

    // ─── PDF Library ──────────────────────────────────────────────────────────────

    private void bindPdfLibraryButton() {
        Button btn = findViewById(R.id.btn_pdf_library);
        btn.setOnClickListener(v -> startActivity(
            new Intent(this, PdfLibraryActivity.class)));
    }

    // ─── Navigation ───────────────────────────────────────────────────────────────

    /**
     * Opens a section in the PDF reader.
     *
     * @param sectionId the section to open
     */
    private void openSection(String sectionId) {
        Intent intent = new Intent(this, SectionReaderActivity.class);
        intent.putExtra(SectionReaderActivity.EXTRA_SECTION_ID, sectionId);
        startActivity(intent);
    }

    // ─── Options Menu ─────────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.moazzam.muntakhabahadith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.moazzam.muntakhabahadith.data.db.ImportedPdf;
import com.moazzam.muntakhabahadith.data.db.SectionProgress;
import com.moazzam.muntakhabahadith.data.repository.ReadingRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ReadingRepositoryTest {

    private ReadingRepository repository;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        repository = ReadingRepository.getInstance(context);
        // Clear all before tests
        try {
            repository.resetAllProgress().get();
            repository.deleteAllImportedPdfs().get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSaveAndGetPosition() throws Exception {
        repository.savePosition("kalimah_tayyibah", 50).get();
        
        SectionProgress progress = repository.getPosition("kalimah_tayyibah");
        assertNotNull(progress);
        assertEquals(50, progress.currentPage);
    }



    @Test
    public void testSectionIndependence() throws Exception {
        repository.savePosition("kalimah_tayyibah", 20).get();
        repository.savePosition("salah", 150).get();
        
        SectionProgress kalimahProgress = repository.getPosition("kalimah_tayyibah");
        SectionProgress salahProgress = repository.getPosition("salah");
        
        assertNotNull(kalimahProgress);
        assertNotNull(salahProgress);
        assertEquals(20, kalimahProgress.currentPage);
        assertEquals(150, salahProgress.currentPage);
    }
}

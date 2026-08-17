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
        repository.resetAllProgress();
        repository.deleteAllImportedPdfs();
    }

    @Test
    public void testSaveAndGetPosition() throws InterruptedException {
        repository.savePosition("kalimah_tayyibah", 50);
        
        // Wait for executor to finish
        Thread.sleep(200);

        SectionProgress progress = repository.getPosition("kalimah_tayyibah");
        assertNotNull(progress);
        assertEquals(50, progress.currentPage);
    }

    @Test
    public void testGeneralLastSeenUpdated() throws InterruptedException {
        repository.savePosition("salah", 150);
        
        Thread.sleep(200);

        com.moazzam.muntakhabahadith.data.db.GeneralLastSeen generalLastSeen = repository.getGeneralLastSeen();
        assertNotNull(generalLastSeen);
        assertEquals("salah", generalLastSeen.sectionId);
        assertEquals(150, generalLastSeen.currentPage);
    }

    @Test
    public void testSectionIndependence() throws InterruptedException {
        repository.savePosition("kalimah_tayyibah", 20);
        repository.savePosition("salah", 150);
        
        Thread.sleep(200);

        SectionProgress kalimahProgress = repository.getPosition("kalimah_tayyibah");
        SectionProgress salahProgress = repository.getPosition("salah");
        
        assertNotNull(kalimahProgress);
        assertNotNull(salahProgress);
        assertEquals(20, kalimahProgress.currentPage);
        assertEquals(150, salahProgress.currentPage);
    }
}

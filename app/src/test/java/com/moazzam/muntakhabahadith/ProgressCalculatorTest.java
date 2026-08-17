package com.moazzam.muntakhabahadith;

import com.moazzam.muntakhabahadith.data.model.Section;
import com.moazzam.muntakhabahadith.utils.ProgressCalculator;
import com.moazzam.muntakhabahadith.utils.SectionConfig;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ProgressCalculator and SectionConfig.
 * These run on the JVM (no Android runtime needed).
 */
public class ProgressCalculatorTest {

    // Use a simple test section for most calculations
    private final Section testSection = new Section("test", "Test", 100, 200);

    // ─── calculateProgress ────────────────────────────────────────────────────────

    @Test
    public void progressAtStart_isZero() {
        float progress = ProgressCalculator.calculateProgress(100, testSection);
        assertEquals(0f, progress, 0.001f);
    }

    @Test
    public void progressAtEnd_isOne() {
        float progress = ProgressCalculator.calculateProgress(200, testSection);
        assertEquals(1f, progress, 0.001f);
    }

    @Test
    public void progressAtMidpoint_isHalf() {
        float progress = ProgressCalculator.calculateProgress(150, testSection);
        assertEquals(0.5f, progress, 0.001f);
    }

    @Test
    public void progressBelowStart_isClampedToZero() {
        float progress = ProgressCalculator.calculateProgress(50, testSection);
        assertEquals(0f, progress, 0.001f);
    }

    @Test
    public void progressAboveEnd_isClampedToOne() {
        float progress = ProgressCalculator.calculateProgress(300, testSection);
        assertEquals(1f, progress, 0.001f);
    }

    // ─── calculateProgressPercent ─────────────────────────────────────────────────

    @Test
    public void percentAtStart_isZero() {
        assertEquals(0, ProgressCalculator.calculateProgressPercent(100, testSection));
    }

    @Test
    public void percentAtEnd_is100() {
        assertEquals(100, ProgressCalculator.calculateProgressPercent(200, testSection));
    }

    @Test
    public void percentAtMidpoint_is50() {
        assertEquals(50, ProgressCalculator.calculateProgressPercent(150, testSection));
    }

    @Test
    public void percentBelowStart_isZero() {
        assertEquals(0, ProgressCalculator.calculateProgressPercent(0, testSection));
    }

    @Test
    public void percentAboveEnd_is100() {
        assertEquals(100, ProgressCalculator.calculateProgressPercent(9999, testSection));
    }

    // ─── sectionRelativePage ──────────────────────────────────────────────────────

    @Test
    public void relativePageAtStart_isOne() {
        assertEquals(1, ProgressCalculator.sectionRelativePage(100, testSection));
    }

    @Test
    public void relativePageAtEnd_isSectionTotal() {
        assertEquals(testSection.getPageCount(),
            ProgressCalculator.sectionRelativePage(200, testSection));
    }

    @Test
    public void relativePageBelowStart_isOne() {
        assertEquals(1, ProgressCalculator.sectionRelativePage(0, testSection));
    }

    @Test
    public void relativePageAboveEnd_isSectionTotal() {
        assertEquals(testSection.getPageCount(),
            ProgressCalculator.sectionRelativePage(9999, testSection));
    }

    // ─── SectionConfig ────────────────────────────────────────────────────────────

    @Test
    public void getSectionById_returnsCorrectSection() {
        Section s = SectionConfig.getSectionById(SectionConfig.ID_SALAH);
        assertNotNull(s);
        assertEquals("Salah", s.getTitle());
        assertEquals(SectionConfig.SALAH_START_PAGE, s.getPdfStartPage());
        assertEquals(SectionConfig.SALAH_END_PAGE,   s.getPdfEndPage());
    }

    @Test
    public void getSectionById_unknownId_returnsNull() {
        assertNull(SectionConfig.getSectionById("nonexistent_id"));
    }

    @Test
    public void allSections_haveNonZeroPageRanges() {
        for (Section s : SectionConfig.ALL_SECTIONS) {
            assertTrue("Section " + s.getId() + " has invalid range",
                s.getPdfEndPage() > s.getPdfStartPage());
        }
    }

    @Test
    public void sectionPageRanges_areWithinPdfBounds() {
        for (Section s : SectionConfig.ALL_SECTIONS) {
            assertTrue("Start page out of bounds: " + s.getId(),
                s.getPdfStartPage() >= 0);
            assertTrue("End page out of bounds: " + s.getId(),
                s.getPdfEndPage() < SectionConfig.TOTAL_PDF_PAGES);
        }
    }

    @Test
    public void clampToSection_belowStart_returnsStart() {
        Section s = SectionConfig.KALIMAH;
        assertEquals(s.getPdfStartPage(), SectionConfig.clampToSection(-5, s));
    }

    @Test
    public void clampToSection_aboveEnd_returnsEnd() {
        Section s = SectionConfig.KALIMAH;
        assertEquals(s.getPdfEndPage(), SectionConfig.clampToSection(99999, s));
    }

    @Test
    public void clampToSection_withinRange_returnsUnchanged() {
        Section s = SectionConfig.KALIMAH;
        int mid = (s.getPdfStartPage() + s.getPdfEndPage()) / 2;
        assertEquals(mid, SectionConfig.clampToSection(mid, s));
    }

    @Test
    public void clampToValidRange_negative_returnsZero() {
        assertEquals(0, SectionConfig.clampToValidRange(-100));
    }

    @Test
    public void clampToValidRange_tooLarge_returnsLastPage() {
        assertEquals(SectionConfig.TOTAL_PDF_PAGES - 1,
            SectionConfig.clampToValidRange(99999));
    }

    @Test
    public void sectionCount_isSix() {
        assertEquals(6, SectionConfig.ALL_SECTIONS.length);
    }
}

package com.moazzam.muntakhabahadith.utils;

import com.moazzam.muntakhabahadith.data.model.Section;

/**
 * Pure utility class for calculating reading progress within a section.
 *
 * Progress formula:
 *   progress = (currentPage - sectionStartPage) / (sectionEndPage - sectionStartPage)
 *
 * Result is always clamped to [0.0, 1.0].
 */
public final class ProgressCalculator {

    private ProgressCalculator() { /* no instances */ }

    /**
     * Calculates reading progress as a float in [0.0, 1.0].
     *
     * @param currentPage 0-based PDF page index of the current page
     * @param section     the section being read
     * @return progress value between 0.0 and 1.0
     */
    public static float calculateProgress(int currentPage, Section section) {
        int start = section.getPdfStartPage();
        int end   = section.getPdfEndPage();
        int total = end - start;

        if (total <= 0) return 0f;

        float raw = (float)(currentPage - start) / (float) total;
        return clamp(raw, 0f, 1f);
    }

    /**
     * Calculates reading progress as an integer percentage [0, 100].
     *
     * @param currentPage 0-based PDF page index of the current page
     * @param section     the section being read
     * @return integer percentage between 0 and 100
     */
    public static int calculateProgressPercent(int currentPage, Section section) {
        return Math.round(calculateProgress(currentPage, section) * 100f);
    }

    /**
     * Returns a 1-based page number relative to the section start.
     * For example, if the section starts at PDF page 143 and the current page is 143,
     * this returns 1. If the current page is 150, this returns 8.
     *
     * @param rawPdfPage 0-based absolute PDF page index
     * @param section    the section being read
     * @return section-relative 1-based page number, clamped to [1, section.pageCount]
     */
    public static int sectionRelativePage(int rawPdfPage, Section section) {
        int relative = rawPdfPage - section.getPdfStartPage() + 1;
        return Math.max(1, Math.min(relative, section.getPageCount()));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

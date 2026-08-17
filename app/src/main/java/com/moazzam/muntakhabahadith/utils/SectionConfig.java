package com.moazzam.muntakhabahadith.utils;

import com.moazzam.muntakhabahadith.data.model.Section;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║               MUNTAKHAB AHADITH – SECTION PAGE CONFIGURATION               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║                                                                              ║
 * ║  PAGE NUMBERING NOTES                                                        ║
 * ║  ──────────────────────────────────────────────────────────────────────────  ║
 * ║  The PDF has two distinct page numbering systems:                            ║
 * ║                                                                              ║
 * ║  1. Printed book page numbers – visible in the book's content (1-based).    ║
 * ║  2. PDF viewer page indexes   – the 0-based index used by the viewer lib.   ║
 * ║                                                                              ║
 * ║  These are NOT the same.                                                     ║
 * ║                                                                              ║
 * ║  The PDF contains 15 front-matter pages (cover, table of contents, etc.)    ║
 * ║  before the book's printed page 1 begins.                                   ║
 * ║                                                                              ║
 * ║  Conversion: pdfViewerPage (0-based) = printedPage + 14                    ║
 * ║                                                                              ║
 * ║  Verified cross-references (from PDF viewer + screenshots):                 ║
 * ║    • Salah PDF page 143 → printed page 129  ✓                               ║
 * ║    • Ikram-e-Muslim PDF page 429 → printed page 415  ✓                      ║
 * ║    • Dawat wa Tabligh PDF page 611 → printed page 597  ✓ (user confirmed)   ║
 * ║                                                                              ║
 * ║  ALL PAGE VALUES IN THIS CLASS USE 0-BASED PDF VIEWER INDEXING.             ║
 * ║                                                                              ║
 * ║  Total PDF pages: 735  (page indexes 0 – 734)                               ║
 * ║                                                                              ║
 * ║  HOW TO UPDATE                                                               ║
 * ║  ──────────────────────────────────────────────────────────────────────────  ║
 * ║  If the PDF file changes or the page mapping needs adjustment,               ║
 * ║  update ONLY the constants below. Do NOT change page numbers anywhere else. ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public final class SectionConfig {

    private SectionConfig() { /* utility class – no instances */ }

    // ─── Asset file ────────────────────────────────────────────────────────────
    /** File name of the bundled PDF inside app/src/main/assets/ */
    public static final String PDF_ASSET_NAME = "muntakhab_ahadith.pdf";

    /** Total pages in the PDF (0-based page indexes: 0 to TOTAL_PDF_PAGES - 1) */
    public static final int TOTAL_PDF_PAGES   = 735;

    // ─── Section IDs (stable, used as database primary keys) ──────────────────
    public static final String ID_KALIMAH  = "kalimah_tayyibah";
    public static final String ID_SALAH    = "salah";
    public static final String ID_ILM_ZIKR = "ilm_and_zikr";
    public static final String ID_IKRAM    = "ikram_e_muslim";
    public static final String ID_IKHLAS   = "ikhlas_e_niyyat";
    public static final String ID_DAWAT    = "dawat_wa_tabligh";

    // ─── 0-based PDF page indexes for each section ────────────────────────────
    //     Printed book pages shown for reference in comments.
    //     Update ONLY here if the mapping changes.

    public static final int KALIMAH_START_PAGE  = 15;   // printed 1
    public static final int KALIMAH_END_PAGE    = 142;  // printed 128

    public static final int SALAH_START_PAGE    = 143;  // printed 129
    public static final int SALAH_END_PAGE      = 268;  // printed 254

    public static final int ILM_ZIKR_START_PAGE = 269;  // printed 255
    public static final int ILM_ZIKR_END_PAGE   = 428;  // printed 414

    public static final int IKRAM_START_PAGE    = 429;  // printed 415
    public static final int IKRAM_END_PAGE      = 573;  // printed 559

    public static final int IKHLAS_START_PAGE   = 574;  // printed 560
    public static final int IKHLAS_END_PAGE     = 610;  // printed 596

    public static final int DAWAT_START_PAGE    = 611;  // printed 597
    public static final int DAWAT_END_PAGE      = 734;  // printed end

    // ─── Pre-built Section objects (used throughout the app) ──────────────────

    public static final Section KALIMAH  = new Section(
            ID_KALIMAH,  "Kalimah Tayyibah", KALIMAH_START_PAGE,  KALIMAH_END_PAGE);

    public static final Section SALAH    = new Section(
            ID_SALAH,    "Salah",             SALAH_START_PAGE,    SALAH_END_PAGE);

    public static final Section ILM_ZIKR = new Section(
            ID_ILM_ZIKR, "Ilm and Zikr",     ILM_ZIKR_START_PAGE, ILM_ZIKR_END_PAGE);

    public static final Section IKRAM    = new Section(
            ID_IKRAM,    "Ikram-e-Muslim",    IKRAM_START_PAGE,    IKRAM_END_PAGE);

    public static final Section IKHLAS   = new Section(
            ID_IKHLAS,   "Ikhlas-e-Niyyat",  IKHLAS_START_PAGE,   IKHLAS_END_PAGE);

    public static final Section DAWAT    = new Section(
            ID_DAWAT,    "Dawat wa Tabligh",  DAWAT_START_PAGE,    DAWAT_END_PAGE);

    /** All six sections in reading order. */
    public static final Section[] ALL_SECTIONS = {
        KALIMAH, SALAH, ILM_ZIKR, IKRAM, IKHLAS, DAWAT
    };

    // ─── Lookup helpers ────────────────────────────────────────────────────────

    /**
     * Returns the Section for the given ID, or {@code null} if unknown.
     */
    public static Section getSectionById(String sectionId) {
        if (sectionId == null) return null;
        for (Section s : ALL_SECTIONS) {
            if (s.getId().equals(sectionId)) return s;
        }
        return null;
    }

    /**
     * Clamps a PDF page index to the valid range of the given section.
     */
    public static int clampToSection(int page, Section section) {
        if (page < section.getPdfStartPage()) return section.getPdfStartPage();
        if (page > section.getPdfEndPage())   return section.getPdfEndPage();
        return page;
    }

    /**
     * Clamps a PDF page index to the valid overall PDF range [0, TOTAL_PDF_PAGES - 1].
     */
    public static int clampToValidRange(int page) {
        if (page < 0)                  return 0;
        if (page >= TOTAL_PDF_PAGES)   return TOTAL_PDF_PAGES - 1;
        return page;
    }
}

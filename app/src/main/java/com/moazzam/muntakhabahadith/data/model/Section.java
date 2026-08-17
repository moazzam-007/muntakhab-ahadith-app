package com.moazzam.muntakhabahadith.data.model;

/**
 * Represents one of the six main sections of Muntakhab Ahadith.
 *
 * This is a plain, immutable data class – NOT a Room entity.
 * All section definitions live in {@link com.moazzam.muntakhabahadith.utils.SectionConfig}.
 *
 * PAGE INDEX CONVENTION:
 *   pdfStartPage and pdfEndPage use 0-based PDF viewer page indexing.
 *   PDF viewer page 0 = the very first page of the PDF file.
 */
public class Section {

    private final String id;
    private final String title;
    private final int pdfStartPage; // 0-based, inclusive
    private final int pdfEndPage;   // 0-based, inclusive

    public Section(String id, String title, int pdfStartPage, int pdfEndPage) {
        this.id           = id;
        this.title        = title;
        this.pdfStartPage = pdfStartPage;
        this.pdfEndPage   = pdfEndPage;
    }

    public String getId()           { return id; }
    public String getTitle()        { return title; }
    public int getPdfStartPage()    { return pdfStartPage; }
    public int getPdfEndPage()      { return pdfEndPage; }

    /** Total number of pages in this section (inclusive range). */
    public int getPageCount() {
        return pdfEndPage - pdfStartPage + 1;
    }
}

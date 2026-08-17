# Build a Complete Offline Android App: Muntakhab Ahadith

You are an expert Android engineer working inside VS Code with GitHub Copilot Agent/Agent Mode.

Build a complete, clean, production-quality Android application called:

**Muntakhab Ahadith**

The application is intended for simple family ta'lim (reading/study) of the Muntakhab Ahadith book.

The app must be simple, offline-first, lightweight in functionality, reliable, easy to maintain, and written entirely in **Java with XML layouts**.

Do NOT use Kotlin for application code.

Do NOT use Jetpack Compose.

Do NOT add unnecessary features.

---

# 1. CORE PRODUCT IDEA

The app contains the Muntakhab Ahadith PDF inside the application.

The user can:

1. Open Muntakhab Ahadith.
2. Select one of its six main sections.
3. Read the original PDF.
4. Save the last-seen position for each section separately.
5. See reading progress for each section.
6. Continue from the last saved position.
7. See a general "Continue Reading" option for the most recently opened section.
8. Optionally import their own PDF files into a separate PDF Library.
9. For imported PDFs, independently save last page/position and progress.
10. Use the entire application offline.

There must be:

* No account system.
* No login.
* No Firebase.
* No backend.
* No cloud database.
* No analytics.
* No internet requirement.
* No network API.
* No remote PDF loading for the main book.

All progress data must be stored locally on the device.

If the application is uninstalled, its local application data and reading progress are expected to be deleted.

---

# 2. TECHNOLOGY REQUIREMENTS

Use:

* Java
* XML layouts
* AndroidX
* Gradle
* Material Components where appropriate
* Local persistence only
* Android Storage Access Framework for user-imported PDFs
* A reliable Android PDF viewer library compatible with Java

Prefer the current stable version of:

`io.github.afreakyelf:Pdf-Viewer`

Use a pinned, known working version rather than using an unspecified "latest" version.

Before implementing the dependency, verify its current Maven Central version and API compatibility.

Current ecosystem reference:

* Minimum SDK: API 21 or higher
* Compile/Target SDK: current stable SDK available in the environment
* Java 17 if required by the selected Android Gradle Plugin/toolchain

Do not blindly copy old tutorials.

If the chosen PDF library has Java-specific compatibility requirements, resolve them properly.

The final project must build successfully with Gradle.

---

# 3. MAIN PDF

The main bundled PDF will be supplied by the developer/user.

Expected file:

`muntakhab_ahadith.pdf`

Place it under an appropriate application resource location such as:

`app/src/main/assets/muntakhab_ahadith.pdf`

Do NOT manually convert the entire book into Java strings, JSON, XML, database records, or hundreds/thousands of individual pages.

The original PDF is the source of truth.

Do NOT OCR the entire book.

Do NOT rewrite the Arabic/Hindi/Urdu text.

Do NOT alter the PDF content.

Do NOT change the original PDF font.

The PDF must be rendered as the original document.

If the PDF is not present while generating the project, create a clear placeholder/instruction:

`app/src/main/assets/muntakhab_ahadith.pdf`

and document exactly where the real PDF must be placed.

Do not invent or generate a fake PDF.

---

# 4. SIX MAIN SECTIONS

The application must expose these six main sections:

1. Kalimah Tayyibah
2. Salah
3. Ilm and Zikr
4. Ikram-e-Muslim
5. Ikhlas-e-Niyyat
6. Dawat wa Tabligh

Use English UI labels throughout the application.

Do not add "Layanai se Bachna" as a seventh main section.

It remains part of the original PDF/book content where it occurs.

---

# 5. VERY IMPORTANT: PDF PAGE MAPPING

The PDF contains printed book page numbers and PDF viewer page indexes.

These may NOT be the same.

Do NOT assume:

`printed page 129 == PDF page 129`

Instead, create a centralized configuration/data structure for section mappings.

Example concept:

```text
Section:
id
title
pdfStartPage
pdfEndPage
```

Use zero-based or one-based indexing consistently throughout the application.

Clearly document which indexing system is used.

The section page boundaries must be easy to modify in ONE place.

Do not hard-code the same page numbers in multiple Java files.

For now, create clearly marked placeholders for the six section page ranges if the exact PDF-to-viewer-page mapping cannot be verified automatically.

Example:

```java
SectionConfig.KALIMAH_START_PAGE
SectionConfig.KALIMAH_END_PAGE
...
```

Once the actual PDF is placed in the project, make it easy for the developer to update these values.

If possible, inspect the PDF/book table of contents and determine the correct mapping automatically, but NEVER guess.

---

# 6. HOME SCREEN

Create a clean, simple home screen.

Suggested structure:

```text
MUNTAKHAB AHADITH

Continue Reading
Ilm and Zikr
Page 290
[Continue]

Your Progress

Kalimah Tayyibah      80%
Salah                 50%
Ilm and Zikr          30%
Ikram-e-Muslim         0%
Ikhlas-e-Niyyat        0%
Dawat wa Tabligh       0%

Six Qualities

[ Kalimah Tayyibah ]
[ Salah ]
[ Ilm and Zikr ]
[ Ikram-e-Muslim ]
[ Ikhlas-e-Niyyat ]
[ Dawat wa Tabligh ]

[ PDF Library ]
```

The actual design can be improved aesthetically, but keep it simple.

Do not overload the home screen.

---

# 7. GENERAL LAST SEEN

Maintain a global/general last-seen record.

Example:

```text
Last opened section:
Ilm and Zikr

Last opened PDF page:
290
```

The home screen should show:

```text
Continue Reading
Ilm and Zikr
Page 290
```

When the user taps Continue Reading, open that section and restore its saved position.

If no general last-seen record exists, hide the Continue Reading card or show an appropriate first-time state.

The general last-seen record must NOT replace the six independent section records.

---

# 8. SECTION-SPECIFIC LAST SEEN

Each of the six sections must maintain its own independent reading state.

Example:

```text
Kalimah Tayyibah
Last seen: page 32

Salah
Last seen: page 176

Ilm and Zikr
Last seen: page 290

Ikram-e-Muslim
Last seen: page 447

Ikhlas-e-Niyyat
Last seen: page 575

Dawat wa Tabligh
Last seen: page 650
```

If the user reads Ilm and Zikr and later opens Salah, the Ilm and Zikr position must remain unchanged.

Each section must have independent persistence.

---

# 9. EXACT READING POSITION

Do not store only the page number if the PDF viewer allows a more precise position.

Store:

* PDF page
* scroll position / vertical offset if supported
* optional zoom level if reliably supported

Example model:

```text
ReadingPosition
    page
    scrollOffset
    zoomLevel
    updatedAt
```

However, reliability is more important than storing an unstable value.

When restoring a position, avoid jumps caused by restoring the scroll position before the PDF has finished rendering.

Restore page first.

Then restore scroll position after the PDF viewer is ready.

If exact scroll restoration is not reliably supported by the selected PDF library, gracefully fall back to page-level restoration.

Never make the app unstable just to preserve a tiny scroll offset.

---

# 10. SAVE LAST SEEN BUTTON

Inside the PDF reading screen, provide a clear user action:

**Save Last Seen**

Example:

```text
[ Save Last Seen ]
```

When the user taps it:

1. Detect the current PDF page.
2. Detect current position if supported.
3. Save it against the current section.
4. Update the general last-seen record.
5. Show a small confirmation such as:
   "Last seen position saved."

Do not show a large dialog.

---

# 11. AUTOMATIC SAFETY SAVE

Manual Save Last Seen is required, but the app must also protect the user from losing progress.

When:

* the activity goes to background,
* the activity is paused,
* the user navigates away,
* or the app is closed normally,

attempt to persist the current reading position.

However, distinguish between:

* current session position
* explicitly saved Last Seen

The implementation may automatically update Last Seen on safe lifecycle events.

The important requirement is:

**The user's reading position must not be lost simply because they forgot to press Save Last Seen.**

Avoid excessive disk writes.

Do not write to storage on every pixel of scrolling.

Use sensible throttling/debouncing if continuous scroll callbacks are used.

---

# 12. PROGRESS BAR

Show reading progress for every main section.

Example:

```text
Ilm and Zikr
████████░░░░ 65%
```

Progress should be calculated based on the section's page range.

Conceptually:

```text
progress =
(currentPage - sectionStartPage + 1)
/
(sectionEndPage - sectionStartPage + 1)
```

Clamp progress to:

```text
0% - 100%
```

Do not calculate progress against the entire 1734-page book when displaying a section's progress.

Progress must be section-specific.

A section at 80% should mean the user has reached approximately 80% of THAT section.

---

# 13. PDF READER SCREEN

Create a dedicated PDF reader screen.

Requirements:

* Original PDF rendering
* Vertical reading
* Smooth scrolling
* Page navigation
* Pinch-to-zoom if supported by the library
* Double-tap zoom if supported
* Standard readable viewing size
* Do not alter the source PDF font
* Page indicator
* Save Last Seen button
* Back navigation
* Restore saved page/position
* Preserve state across rotation/configuration changes where possible

The reader UI should be minimal.

Do not add editing tools.

Do not add annotations.

Do not add unnecessary sharing/editing functionality.

---

# 14. SECTION OPENING BEHAVIOR

When the user taps a section:

If the section has a saved Last Seen position:

Show a small choice:

```text
Continue from last seen page?
[Continue]
[Start from beginning]
```

Alternatively, if a less intrusive UX is preferred:

Open directly at Last Seen and provide a "Start from beginning" action in the reader/menu.

Choose the cleaner UX.

The user must always have a way to start the section from its beginning.

If there is no saved position:

Open at the section's starting PDF page.

---

# 15. PDF LIBRARY FOR USER-IMPORTED PDFs

Add a separate feature called:

**PDF Library**

This is independent of Muntakhab Ahadith.

The user can import any PDF from their device.

Use Android's Storage Access Framework:

`Intent.ACTION_OPEN_DOCUMENT`

with MIME type:

`application/pdf`

Request persistent URI read permission where supported.

Do NOT request broad storage permissions such as:

`MANAGE_EXTERNAL_STORAGE`

or unnecessary storage permissions.

Use the Android system document picker.

Android's `ACTION_OPEN_DOCUMENT` is specifically designed for selecting documents and supports persisted URI access. Follow current Android documentation.

---

# 16. IMPORTED PDF STORAGE

For every imported PDF, maintain a separate record:

```text
ImportedPdf
    id
    displayName
    uri
    lastPage
    scrollOffset
    zoomLevel
    progress
    addedAt
    updatedAt
```

The user should be able to:

* Add PDF
* Open PDF
* Continue from last position
* See progress
* Delete PDF from the library
* Confirm deletion before removing it

If the user deletes an imported PDF from the app library, delete its local metadata.

If the app only stores a URI reference rather than a copied file, handle broken/missing URI cases gracefully.

If a provider revokes access, show:

"PDF is no longer accessible. Please import it again."

Do not crash.

---

# 17. IMPORTANT: IMPORTED PDF UI MUST STAY SEPARATE

Do NOT put imported PDF progress on the main Muntakhab Ahadith section progress.

Do NOT mix imported PDFs with the six main sections.

Home screen:

```text
Muntakhab Ahadith
    Six main sections
    Section progress
    General Continue Reading

PDF Library
    User imported PDFs
```

Keep these concepts separate in both UI and code.

---

# 18. LOCAL DATA STORAGE

Use a robust local persistence approach.

Because the app is simple and offline, use either:

* SharedPreferences for small settings/reading positions

OR

* Room if the imported PDF library requires structured records.

Prefer **Room** if it makes the architecture cleaner for:

* six section reading states
* general last seen
* imported PDF records

Do not introduce a database server.

Everything must remain on-device.

The application must continue working without internet access.

---

# 19. DATA MODEL

Create clean models.

Suggested:

```text
Section
    id
    title
    startPage
    endPage

ReadingPosition
    page
    scrollOffset
    zoomLevel
    updatedAt

SectionProgress
    sectionId
    readingPosition

ImportedPdf
    id
    name
    uri
    lastPage
    scrollOffset
    zoomLevel
    updatedAt
```

General last-seen can reference the section ID and its latest position.

Avoid duplicated state.

Create a clear repository/service layer for reading-state persistence.

---

# 20. ARCHITECTURE

Do not over-engineer.

Use a clean separation such as:

```text
UI
 ├── MainActivity
 ├── SectionReaderActivity
 ├── PdfLibraryActivity
 └── ImportedPdfReaderActivity

Data
 ├── ReadingState
 ├── Section
 ├── ImportedPdf
 └── DAO/Repository

Utils
 ├── PdfPageMapper
 ├── ProgressCalculator
 └── Constants
```

You may improve this structure if there is a strong technical reason.

Use clear responsibilities.

Do not put all application logic inside Activities.

---

# 21. USER INTERFACE LANGUAGE

The entire app UI must be English.

Examples:

* Continue Reading
* Save Last Seen
* Start from Beginning
* Progress
* PDF Library
* Add PDF
* Delete PDF
* Cancel
* Continue
* Last Seen
* Page
* No PDFs Added
* Reading Progress

The actual book content remains exactly as contained in the PDF.

Do not translate the book.

---

# 22. DESIGN

Design should be:

* Clean
* Calm
* Book/reading oriented
* Minimal
* Respectful
* Easy for family members to use
* Large enough touch targets
* Good readability

Do not make it look like a social media app.

Avoid excessive animations.

Avoid flashy gradients.

Use a simple typography hierarchy.

Support light theme.

Dark mode is optional; do not sacrifice core functionality to implement it.

---

# 23. ACCESSIBILITY

Include:

* Content descriptions where appropriate
* Reasonable touch target sizes
* Readable text
* Good contrast
* No information conveyed only by color
* Screen-reader-friendly buttons where practical

---

# 24. ERROR HANDLING

Handle:

* PDF missing from assets
* PDF failed to open
* PDF rendering failure
* Imported PDF unavailable
* URI permission failure
* Corrupted PDF
* Invalid saved page
* Section page mapping outside PDF range
* App recreation
* Rotation
* Activity/process recreation

Never crash because a saved page is invalid.

Clamp invalid pages to valid ranges.

Show useful human-readable error messages.

---

# 25. LARGE PDF PERFORMANCE

The main PDF may contain approximately 1700+ pages.

Design for a large PDF.

Do NOT load all PDF pages into memory simultaneously.

Use the PDF viewer's lazy rendering/page caching.

Avoid converting all pages to bitmaps.

Avoid creating thumbnails for every page at startup.

Avoid scanning the entire PDF at application startup unless absolutely necessary.

Application startup should remain fast.

The PDF should be opened only when requested.

---

# 26. APK SIZE CONSIDERATION

The bundled Muntakhab Ahadith PDF may be large.

Do NOT silently remove pages.

Do NOT lower image quality.

Do NOT recompress the book without explicit approval.

Document the expected APK-size impact.

If Android packaging imposes a limitation, explain the limitation and implement the most reliable local solution rather than silently changing the PDF.

The final app must preserve the original book.

---

# 27. PDF PAGE MAPPING TOOL/CONFIG

Create an easy way to configure the six section boundaries.

For example:

```java
public final class SectionConfig {

    public static final Section KALIMAH = ...
    public static final Section SALAH = ...
    public static final Section ILM_ZIKR = ...
    public static final Section IKRAM_MUSLIM = ...
    public static final Section IKHLAS_NIYYAT = ...
    public static final Section DAWAT_TABLIGH = ...
}
```

Do not scatter page numbers throughout the codebase.

Add comments explaining:

* PDF page numbering
* printed book page numbering
* zero-based vs one-based indexing

---

# 28. FIRST-RUN EXPERIENCE

On first launch:

Show a simple home screen.

Do not force login.

Do not request unnecessary permissions.

Do not show internet-related messages.

If the bundled PDF is missing, show:

"Muntakhab Ahadith PDF is not available. Please place muntakhab_ahadith.pdf in app/src/main/assets/."

Only show this during development/debug scenarios.

---

# 29. SETTINGS

Keep settings minimal.

A simple Settings screen may contain:

* About
* App version
* Reset Muntakhab Ahadith progress
* Reset imported PDF data

If implementing reset:

Show confirmation before deleting reading progress.

Do NOT add account/cloud settings.

Do NOT add unnecessary preferences.

---

# 30. RESET PROGRESS

Provide a safe way to reset:

### Reset section progress

Reset one section.

### Reset all Muntakhab Ahadith progress

Reset:

* six section positions
* section progress
* general last seen

Do NOT delete the bundled PDF.

For imported PDFs:

Deleting a PDF removes its associated progress.

---

# 31. TESTING

Create tests for important business logic.

At minimum test:

1. Section progress calculation.
2. Page clamping.
3. Section start/end mapping.
4. Last Seen save/retrieve.
5. General last-seen update.
6. Imported PDF metadata persistence.
7. Deleting imported PDF metadata.
8. Invalid page recovery.

Example:

```text
Section start = 100
Section end = 200
Current page = 150

Expected progress = approximately 50%
```

Also test:

```text
Current page < start
Current page > end
```

and ensure values are clamped.

---

# 32. MANUAL QA CHECKLIST

Create a `QA_CHECKLIST.md`.

Include:

* App launches offline
* Bundled PDF opens
* All six sections open
* Correct section page mapping
* Save Last Seen works
* Automatic safety save works
* Section states remain independent
* General Continue Reading works
* Progress bars update
* Start from beginning works
* PDF Library opens
* User can import PDF
* Imported PDF opens
* Imported PDF progress works
* Imported PDF can be deleted
* Missing PDF handled
* Rotation doesn't lose state
* App restart restores state
* No unnecessary permissions
* Release APK builds successfully

---

# 33. README

Create a professional `README.md`.

Include:

## Project

Muntakhab Ahadith Android App

## Features

List all implemented features.

## Technology

Explain:

* Java
* XML
* AndroidX
* PDF viewer
* Room/SharedPreferences
* Storage Access Framework

## Setup

Explain exactly:

1. Clone repository.
2. Open in Android Studio/VS Code.
3. Place PDF at:
   `app/src/main/assets/muntakhab_ahadith.pdf`
4. Verify section page mappings.
5. Sync Gradle.
6. Build.

## Run

Explain debug APK build.

## Release

Explain release APK generation.

## GitHub Actions

Explain where the APK is produced.

## PDF Page Mapping

Explain how to update the six section boundaries.

## Offline Behavior

Explain that the main application does not require internet.

## User Imported PDFs

Explain how the PDF Library works.

---

# 34. GITHUB ACTIONS

Create:

`.github/workflows/build-apk.yml`

The workflow must:

1. Checkout repository.
2. Set up JDK.
3. Set up Android SDK if necessary.
4. Restore Gradle cache.
5. Make Gradle wrapper executable.
6. Run tests.
7. Build debug APK.
8. Upload APK as a GitHub Actions artifact.

Example output artifact:

`muntakhab-ahadith-debug.apk`

The workflow must fail if the build or tests fail.

Do not upload secrets.

Do not require signing secrets for the debug APK.

Optionally provide a separate release workflow design/documentation, but the primary requirement is a working debug APK artifact.

---

# 35. GIT REPOSITORY

Create a clean `.gitignore`.

Do NOT commit:

* `.idea/`
* `build/`
* `.gradle/`
* local SDK files
* generated APKs
* signing keys
* passwords
* API keys
* machine-specific configuration

The PDF is intentionally part of the application content.

If the PDF is too large for normal GitHub repository limits, document a proper alternative and do NOT create an invalid repository setup.

Do not use Git LFS unless necessary and document why.

---

# 36. VERSIONING

Set:

Application name:

`Muntakhab Ahadith`

Use a sensible package name such as:

`com.example.muntakhabahadith`

However, if a more appropriate unique package is available, use:

`com.moazzam.muntakhabahadith`

Do not use personal identifiers in package names unless explicitly desired.

Version:

`1.0.0`

Version code:

`1`

---

# 37. CODE QUALITY

Requirements:

* Clean Java
* Meaningful class names
* Meaningful variable names
* Small methods
* No duplicated logic
* No hard-coded strings where Android resources should be used
* No magic numbers
* Comments only where they explain non-obvious logic
* No dead code
* No placeholder implementations in the final working features
* No TODOs for core functionality

Use:

`strings.xml`

for user-visible text.

Use:

`colors.xml`

and appropriate resources for design values.

Use `dimens.xml` where useful.

---

# 38. IMPORTANT COPILOT BEHAVIOR

Do NOT just describe what needs to be done.

Actually create the complete project.

Before coding:

1. Inspect the existing repository.
2. Determine whether an Android project already exists.
3. If not, initialize the project.
4. Inspect all available files.
5. Check whether `muntakhab_ahadith.pdf` exists.
6. If it exists, inspect its metadata/page count if tooling permits.
7. Identify the six section boundaries if they can be reliably determined.
8. If exact boundaries cannot be reliably determined, create the configuration placeholders and clearly document what must be filled in.

Then implement the application.

Do not ask unnecessary questions if the requirement is already specified.

Do not change the technology stack.

Do not add unrelated features.

---

# 39. BUILD AND VERIFY

After implementation:

Run:

```bash
./gradlew test
```

Then:

```bash
./gradlew assembleDebug
```

Fix all compile errors.

Fix all test failures.

Fix Android resource errors.

Fix manifest errors.

Fix Gradle dependency errors.

Verify the generated APK exists.

If the environment cannot run an emulator, still perform all possible Gradle/unit-test/build verification.

Do not claim the application is complete if it does not compile.

---

# 40. FINAL DELIVERABLE

At the end, the repository should contain:

```text
Muntakhab-Ahadith/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       ├── res/
│   │       ├── assets/
│   │       │   └── muntakhab_ahadith.pdf
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle
│
├── .github/
│   └── workflows/
│       └── build-apk.yml
│
├── README.md
├── QA_CHECKLIST.md
├── .gitignore
├── settings.gradle
├── build.gradle
└── gradlew
```

The exact structure may vary according to the final Android Gradle project, but keep it clean.

---

# 41. FINAL RESPONSE AFTER IMPLEMENTATION

After completing the implementation, report:

1. What was created.
2. Main architecture.
3. PDF location.
4. Six section mappings.
5. Last Seen implementation.
6. Progress implementation.
7. Imported PDF implementation.
8. Local storage approach.
9. Tests performed.
10. APK build result.
11. GitHub Actions workflow result.
12. Any remaining issue that genuinely requires human input.

Do not claim success without verification.

---

# 42. MOST IMPORTANT PRODUCT RULES

Keep these rules throughout development:

**Rule 1:** The original Muntakhab Ahadith PDF is the source of truth.

**Rule 2:** Do not manually copy the book's contents into the codebase.

**Rule 3:** Six main sections must have independent Last Seen positions.

**Rule 4:** General Continue Reading must remember the most recently opened Muntakhab Ahadith location.

**Rule 5:** Progress must be calculated separately for each section.

**Rule 6:** The user must be able to manually Save Last Seen.

**Rule 7:** The app should also safely persist progress automatically when the app leaves the foreground.

**Rule 8:** Imported PDFs must be completely separate from Muntakhab Ahadith.

**Rule 9:** Everything must work offline.

**Rule 10:** No account or backend.

**Rule 11:** Use Java + XML, not Kotlin + Compose.

**Rule 12:** Build and test the project before declaring it complete.

Now start implementing the project.

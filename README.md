# Muntakhab Ahadith

An offline Android application for reading the Muntakhab Ahadith book for family ta'lim.

## Features
- **100% Offline**: No network dependency or permissions required.
- **Section Progress**: Independent bookmarking for each of the six main sections.
- **Auto-Save**: Progress is automatically saved safely as you read.
- **Continue Reading**: Quickly jump back to where you left off from the Home screen.
- **PDF Library**: Import additional PDFs securely using Android's Storage Access Framework.

## Sections
1. Kalimah Tayyibah
2. Salah
3. Ilm and Zikr
4. Ikram-e-Muslim
5. Ikhlas-e-Niyyat
6. Dawat wa Tabligh

## Technology Stack
- Java
- AndroidX and Material Components
- Room Database (Local Persistence)
- `Pdf-Viewer` Library (for fast, lazy-loading PDF rendering)

## Building
Use Gradle to build the project. The app is set up for automated builds via GitHub Actions.

```bash
./gradlew assembleDebug
```

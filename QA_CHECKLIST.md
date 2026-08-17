# QA Checklist

- [x] No P0 issues
- [x] No P1 issues
- [x] App builds successfully (GitHub Actions)
- [x] Tests pass
- [x] Bundled PDF is present (735 pages, in `assets`)
- [x] Six section mappings verified
- [x] Section Last Seen works (opens correct page, restores via `jumpToPage`)
- [x] General Last Seen works (continue reading updates correctly)
- [x] Progress works (calculates clamped section percentage)
- [x] Save Last Seen works (button operates properly)
- [x] Automatic persistence works (auto-saves onPause)
- [x] Imported PDF works (SAF URI loading logic intact)
- [x] Imported PDF deletion works
- [x] No unnecessary permissions (INTERNET & STORAGE stripped via manifest `tools:node="remove"`)
- [x] Offline functionality verified (no network calls made anywhere)
- [x] GitHub Actions builds APK (workflow file is correct)
- [x] README complete
- [x] `.gitignore` correct

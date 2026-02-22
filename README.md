# Ben's Bible

A native iOS Bible reading app built with SwiftUI, featuring the complete King James Version text. Read, search, highlight, bookmark, annotate, and share scripture — all stored privately on device with no account or internet connection required.

---

## Features

### Read
- Full KJV text across all 66 books
- Georgia serif typeface optimised for long reading sessions
- Chapter navigation with previous / next buttons
- Book and chapter picker organised by canonical group (Law, History, Poetry, Prophets, Gospels, Acts, Epistles, Revelation)

### Search
- Full-text search across the entire Bible
- Real-time results with verse reference and preview
- Filter by group: All, Old Testament, New Testament, Law, History, Poetry, Prophets, Gospels, Acts, Epistles, Revelation

### Highlight
- Tap any verse to select it; tap multiple verses to extend the selection
- Apply a colour highlight: yellow, green, blue, pink, or purple
- Highlights are persisted via SwiftData and shown on every return visit

### Bookmark
- Bookmark selected verses with one tap
- Bookmarks tab shows all saved verses grouped by book
- Tap any bookmark to jump directly to that chapter

### Notes
- Attach a personal note to any verse
- Notes tab collects all annotations in one place
- Tap the note icon on a verse row to open the editor inline

### Share
- Export selected verses as a gradient image
- 5 gradient themes: Navy, Sunset, Forest, Parchment, Midnight
- 5 font choices: Georgia, Palatino, Baskerville, Helvetica, Avenir
- Share via the system share sheet (Messages, Mail, social media, etc.)

---

## Architecture

```
BensBible2/
├── BensBible2App.swift          # App entry point, SwiftData model container
├── Models/
│   ├── Book.swift               # Book data model
│   ├── Chapter.swift            # Chapter data model
│   ├── Verse.swift              # Verse data model
│   ├── BibleLocation.swift      # Book + chapter + verse coordinate
│   ├── VerseID.swift            # Stable verse identifier
│   ├── VerseAnnotation.swift    # SwiftData model (highlight, bookmark, note)
│   ├── HighlightColor.swift     # Highlight colour enum
│   ├── BookGroup.swift          # Canonical book groupings
│   ├── ShareGradient.swift      # Share image gradient themes
│   ├── ShareFont.swift          # Share image font choices
│   └── NavigationCoordinator.swift  # Cross-tab navigation state
├── Services/
│   ├── BibleDataService.swift   # Protocol for Bible data access
│   ├── LocalBibleDataService.swift  # JSON-backed implementation
│   └── AnnotationService.swift  # Protocol + SwiftData implementation
├── ViewModels/
│   ├── ReaderViewModel.swift    # Reader state and actions
│   ├── SearchViewModel.swift    # Search query, results, debouncing
│   ├── BookmarksViewModel.swift # Bookmark list
│   └── NotesViewModel.swift     # Notes list
├── Views/
│   ├── ReaderView.swift         # Main reading view
│   ├── VerseRow.swift           # Individual verse row with highlight/bookmark/note indicators
│   ├── VerseActionBar.swift     # Floating toolbar when verses are selected
│   ├── BookChapterPickerView.swift  # Book + chapter navigation sheet
│   ├── HighlightColorPicker.swift   # Colour picker sheet
│   ├── NoteEditorView.swift     # Note writing sheet
│   ├── SearchView.swift         # Search tab
│   ├── BookmarksView.swift      # Bookmarks tab
│   ├── NotesView.swift          # Notes tab
│   ├── VerseShareSheet.swift    # Share flow sheet
│   ├── VerseShareImageView.swift    # Rendered share image
│   └── SplashScreenView.swift   # Launch animation
└── Assets.xcassets/
    └── AppIcon.appiconset/      # 1024×1024 app icon

assets/
├── bibles/kjv/                  # 66 JSON files, one per book
└── reading_plans/               # Optional reading plan JSON files

android/                         # Android companion app (Kotlin / Jetpack Compose)
```

**State management:** SwiftUI `@Observable` view models
**Persistence:** SwiftData (`VerseAnnotation` model — highlights, bookmarks, notes)
**Bible text:** Local JSON files bundled with the app, loaded on demand
**No networking:** All data is on-device

---

## Requirements

| | |
|---|---|
| Platform | iOS 17.0+ |
| Xcode | 16+ |
| Swift | 5.9+ |
| Dependencies | None (no third-party packages) |

---

## Getting Started

```bash
git clone <repo>
cd BensBible2
open BensBible2.xcodeproj
```

Select an iOS 17+ simulator or device, then **Product → Run** (`⌘R`).

No package resolution step is needed — the project has zero external dependencies.

---

## Screenshots

Screenshots for App Store submission are generated automatically via the UI test target:

```bash
xcodebuild test \
  -project BensBible2.xcodeproj \
  -scheme BensBible2 \
  -destination "id=<simulator-udid>" \
  -only-testing BensBible2UITests/ScreenshotTests/testTakeScreenshots
```

Output is written to `screenshots/` at the project root (1284×2778 px, accepted by App Store Connect for the 6.7" display slot).

---

## Distribution

| | |
|---|---|
| Bundle ID | `com.benwalker.BensBible2` |
| Team | WJF3LRH867 |
| Code signing | Automatic |
| Version | 1.0 (build 1) |

Archive and upload via **Product → Archive** in Xcode, then distribute through Xcode Organizer → App Store Connect.

---

## Bible Text

The King James Version (1769 Blayney edition) is in the public domain. The JSON source files in `assets/bibles/kjv/` contain one file per book with the structure:

```json
{
  "book": "Genesis",
  "chapters": [
    {
      "chapter": 1,
      "verses": [
        { "verse": 1, "text": "In the beginning God created the heaven and the earth." }
      ]
    }
  ]
}
```

---

## License

Personal project — not licensed for redistribution.

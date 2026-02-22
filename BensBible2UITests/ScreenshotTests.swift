import XCTest

final class ScreenshotTests: XCTestCase {
    var app: XCUIApplication!
    let screenshotsDir = "/Users/benjaminwalker/Developer/BensBible2/screenshots"

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    override func tearDownWithError() throws {
        app.terminate()
    }

    func testTakeScreenshots() throws {
        // Wait for splash (2.8s) + content load
        sleep(4)

        // ── 1. Search results ──────────────────────────────────────────────
        app.tabBars.firstMatch.buttons["Search"].tap()
        sleep(2)
        let searchField = app.searchFields.firstMatch
        XCTAssertTrue(searchField.waitForExistence(timeout: 5), "Search field not found")
        searchField.tap()
        searchField.typeText("love")
        sleep(3)
        app.keyboards.buttons["search"].tapIfExists()
        sleep(1)
        save("01-search-results")

        // ── Back to Reader (Genesis 1 is loaded by default) ───────────────
        app.tabBars.firstMatch.buttons["Read"].tap()
        sleep(1)

        // ── 2. Reader — Genesis 1 ─────────────────────────────────────────
        save("02-reader")

        // ── 3. Verse selected — tap Genesis 1:1 to show the action bar ────
        let verse1 = app.staticTexts
            .matching(NSPredicate(format: "label CONTAINS[c] 'In the beginning God created'"))
            .firstMatch
        XCTAssertTrue(verse1.waitForExistence(timeout: 4), "Genesis 1:1 text not found")
        verse1.tap()
        sleep(1)
        save("03-verse-selected")

        // Bookmark the selected verse so Bookmarks has real data
        let bookmarkBtn = app.buttons
            .matching(NSPredicate(format: "label == 'Bookmark'"))
            .firstMatch
        if bookmarkBtn.waitForExistence(timeout: 2) {
            bookmarkBtn.tap()
            sleep(1)
        }

        // Deselect to clear the action bar
        let deselectBtn = app.buttons
            .matching(NSPredicate(format: "label == 'Deselect'"))
            .firstMatch
        if deselectBtn.waitForExistence(timeout: 2) {
            deselectBtn.tap()
            sleep(1)
        }

        // ── 4. Book picker sheet ──────────────────────────────────────────
        openBookPicker()
        XCTAssertTrue(app.buttons["Cancel"].waitForExistence(timeout: 5), "Book picker Cancel not found")
        save("04-book-picker")
        app.buttons["Cancel"].tap()
        sleep(1)

        // ── 5. Bookmarks — contains Genesis 1:1 ──────────────────────────
        app.tabBars.firstMatch.buttons["Bookmarks"].tap()
        sleep(1)
        save("05-bookmarks")
    }

    // MARK: - Helpers

    private func openBookPicker() {
        let navBar = app.navigationBars.firstMatch
        for btn in navBar.buttons.allElementsBoundByIndex {
            let lbl = btn.label
            if lbl.isEmpty || lbl == "Back" || lbl == "Cancel" { continue }
            btn.tap()
            sleep(1)
            return
        }
        navBar.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        sleep(1)
    }

    private func save(_ name: String) {
        let screenshot = XCUIScreen.main.screenshot()
        guard let data = screenshot.image.pngData() else { return }
        let path = "\(screenshotsDir)/\(name).png"
        FileManager.default.createFile(atPath: path, contents: data, attributes: nil)
        print("Saved: \(path)")
    }
}

private extension XCUIElement {
    func tapIfExists() { if exists { tap() } }
}

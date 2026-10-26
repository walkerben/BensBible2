# Release checklist

This is how a release build is produced from this repo. It ends when the iOS `.ipa` and the Android `.aab` are on disk. Uploading either file and submitting it for review is a separate step, called out at the end of each platform.

Versions in the tree today:

| | User-facing version | Build |
|---|---|---|
| iOS | `MARKETING_VERSION` `1.4` | `CURRENT_PROJECT_VERSION` `4` |
| Android | `versionName` `"1.4"` | `versionCode` `6` |

`README.md` still lists version 1.0 (build 1) under Distribution. The project files above are the ones the build uses.

There is no `fastlane/` directory, no `.github/` workflow, no changelog, and no store-listing text. There is no xcconfig. iOS has no `Info.plist` in the repo.

## iOS

Bundle id `com.benwalker.BensBible2`. Team id `WJF3LRH867` is set on the app target in `BensBible2.xcodeproj/project.pbxproj` and in `ExportOptions.plist`. The app target uses `CODE_SIGN_STYLE = Automatic` and `GENERATE_INFOPLIST_FILE = YES`, so the version baked into the app comes from the build settings below.

The shared scheme is `BensBible2.xcodeproj/xcshareddata/xcschemes/BensBible2.xcscheme`. Its archive action uses the Release configuration.

### Version

Edit `BensBible2.xcodeproj/project.pbxproj`. The BensBible2 app target has a Debug configuration and a Release configuration. Set `MARKETING_VERSION` to the same value in both. Set `CURRENT_PROJECT_VERSION` to the same value in both. Those are the only four lines. The 1.4 (build 4) bump changed this file and nothing else.

`MARKETING_VERSION` is the version shown to people. `CURRENT_PROJECT_VERSION` is the build number. App Store Connect already has version 1.4 build 4, so a new upload needs a higher build number.

Confirm both copies:

```bash
grep -n -E 'MARKETING_VERSION|CURRENT_PROJECT_VERSION' BensBible2.xcodeproj/project.pbxproj
```

`xcrun agvtool new-version -all <build>` updates both `CURRENT_PROJECT_VERSION` lines. It prints `Cannot find "BensBible2.xcodeproj/../YES"` because there is no Info.plist on disk. The project file still changes. `xcrun agvtool new-marketing-version` leaves `MARKETING_VERSION` unchanged and prints the same message. Set the marketing version by editing the project file.

### Archive

From the repo root:

```bash
xcodebuild archive \
  -project BensBible2.xcodeproj \
  -scheme BensBible2 \
  -configuration Release \
  -destination 'generic/platform=iOS' \
  -archivePath build/BensBible2.xcarchive
```

`build/` is in `.gitignore`. The archive's short version and build number come from the project file.

The login keychain on the machine used for the dry run has one code-signing identity: "Apple Development: Benjamin Walker (8JM5DTZE84)". The archive is signed with that identity, for team `WJF3LRH867`. A store profile is installed locally under Xcode's provisioning profiles: "iOS Team Store Provisioning Profile: com.benwalker.BensBible2". There is no Apple Distribution private key in the keychain. The export step below cloud-signs with the distribution certificate. That requires the Xcode account for this team to already be signed in.

### Export the IPA

```bash
xcodebuild -exportArchive \
  -archivePath build/BensBible2.xcarchive \
  -exportPath build/ipa \
  -exportOptionsPlist ExportOptions.plist
```

The package is `build/ipa/BensBible2.ipa`.

`ExportOptions.plist` contains four keys: `method` = `app-store-connect`, `teamID` = `WJF3LRH867`, `uploadBitcode` = false, `uploadSymbols` = true.

On Xcode 27.1, export with that file talks to App Store Connect. Xcode fills in `destination` = `export` and `manageAppVersionAndBuildNumber` = true. It lists existing builds, then sends a build-upload request for the archive's version and build. In the dry run, version 1.4 build 4 was already uploaded, App Store Connect rejected the request, and a local IPA was still written. Because version management defaulted to true, that IPA's build number was 5 while the archive stayed at 4. The IPA was signed with a cloud-managed Apple Distribution certificate for team `WJF3LRH867`, using the store profile named above, with `get-task-allow` false. The binary was not submitted for review.

A follow-up export with `destination` = `export`, `manageAppVersionAndBuildNumber` = false, and `uploadSymbols` = false kept the IPA at build 4 and still made the same App Store Connect calls. Those keys were reverted. The file in git is the original four-key plist.

If the build number is one App Store Connect has not seen, this same command may upload the binary. Run it only when that is acceptable. The section below is the stop for review submission and for any other uploader.

### Screenshots and store text

iPhone screenshots in `screenshots/` are 1284×2778:

- `screenshots/01-search-results.png`
- `screenshots/02-reader.png`
- `screenshots/03-verse-selected.png`
- `screenshots/04-book-picker.png`
- `screenshots/05-bookmarks.png`

`BensBible2UITests/ScreenshotTests.swift` writes them. `testTakeScreenshots` saves to the absolute path `/Users/benjaminwalker/Developer/BensBible2/screenshots`. The README command refreshes them:

```bash
xcodebuild test \
  -project BensBible2.xcodeproj \
  -scheme BensBible2 \
  -destination "id=<simulator-udid>" \
  -only-testing BensBible2UITests/ScreenshotTests/testTakeScreenshots
```

Choose the simulator with `xcrun simctl list devices available`. The PNG size follows that simulator. The files in git are 1284×2778. The test writes only that absolute directory. It leaves `screenshots/ipad/` and `screenshots/android/` alone.

iPad screenshots in `screenshots/ipad/` are 2064×2752: `01-read.png`, `02-search.png`, `03-bookmarks.png`, `04-notes.png`. No test or script writes that folder. Replace the PNGs.

There is no What's New text, changelog, or fastlane metadata. Release notes are entered in App Store Connect, which is past the stop below.

`docs/privacy-policy.html` and `docs/support.html` are local HTML pages. The privacy page says the policy is posted at a URL. The repo does not record that URL.

### STOP

Stop when `build/ipa/BensBible2.ipa` is on disk. Read the export section before running export: on Xcode 27.1 that command already contacts App Store Connect.

Uploading the IPA with Xcode Organizer, Transporter, `xcrun altool`, or `xcrun notarytool`, and submitting the version for review, is Benjamin's step. This checklist stops before that. The repo has no fastlane lane and no upload script to run.

## Android

Application id `com.bensbible.app`. `versionCode` and `versionName` live only in `android/app/build.gradle.kts`. `android/app/src/main/AndroidManifest.xml` does not set a version. The 1.4 (versionCode 6) bump changed `android/app/build.gradle.kts` and nothing else.

### Version

In `defaultConfig`, set `versionCode` to an integer higher than the last one uploaded to Play, and set `versionName` to the version people see. Today those are `6` and `"1.4"`. Nothing in the repo copies these from the iOS `MARKETING_VERSION` and `CURRENT_PROJECT_VERSION`.

Confirm:

```bash
grep -n -E 'versionCode|versionName' android/app/build.gradle.kts
```

### Signing

The `release` signing config in `android/app/build.gradle.kts` reads four Gradle project properties:

- `RELEASE_STORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

On this machine they are defined in `android/gradle.properties`. That file is tracked. `.gitignore` also lists `android/gradle.properties`, which has no effect on a file that is already tracked. `RELEASE_STORE_FILE` is `../keystore/release.jks`, relative to `android/app`, so the keystore is `android/keystore/release.jks`. That file is present locally. `android/keystore/` is gitignored, and the keystore is not in git.

`android/local.properties` is gitignored and, on this machine, only sets `sdk.dir`. The signing block does not read it.

Leave the passwords in that properties file. This checklist does not repeat them.

The release build type sets `isMinifyEnabled` and `isShrinkResources` and uses the default optimized ProGuard file plus `android/app/proguard-rules.pro`.

### Build the bundle

From `android/`:

```bash
./gradlew bundleRelease
```

That runs `:app:bundleRelease`. The signed bundle is:

`android/app/build/outputs/bundle/release/app-release.aab`

`android/app/build/` is gitignored.

### Screenshots and store graphics

`screenshots/android/` holds six 1080×1920 PNGs: `01-reader.png`, `02-verse-selected.png`, `03-search.png`, `04-bookmarks.png`, `05-notes.png`, `06-presentations.png`. No test or script generates them. Replace the PNGs.

Play listing images in the repo:

- `play-store/BensBible2FeatureGraphic.png` (1024×500)
- `play-store/icon-512.png` (512×512)

There is no title, short description, full description, or changelog next to those images. That text is entered in Play Console, which is past the stop below.

### STOP

Stop when `app-release.aab` is on disk. Uploading it to Play Console and sending it for review is Benjamin's step. This checklist stops before that. The repo has no fastlane supply setup, no Play publishing Gradle task, and no Play API key.

## Open questions

1. On Xcode 27.1, `xcodebuild -exportArchive` with the current `ExportOptions.plist` contacts App Store Connect (list builds, then a build-upload request) even though it also writes a local IPA. Extra keys tried during the dry run (`destination` = `export`, `manageAppVersionAndBuildNumber` = false, `uploadSymbols` = false) did not stop those calls, and they were reverted. What Xcode does when the build number is new was not run: the dry run stayed on 1.4 (4), which App Store Connect already has, and the upload request was rejected.

2. Store export on this Mac depends on Xcode cloud signing. The keychain has the Apple Development identity above and no Apple Distribution private key. The Apple ID signed into Xcode is not written down here. If that session is missing, export fails.

3. Release keystore passwords are in the tracked file `android/gradle.properties`. The keystore file is only on disk. This checklist does not move or rotate them.

4. `CLAUDE.md` says those four properties belong in `android/local.properties`. The Gradle signing block reads project properties, and the values that exist are in `android/gradle.properties`.

5. iOS build `4` and Android `versionCode` `6` are independent. Both user-facing versions happen to be 1.4.

6. No public URL is recorded for `docs/privacy-policy.html` or `docs/support.html`.

7. No file in the repo holds App Store or Play release notes.

8. iPad screenshots, Android screenshots, and the two Play images have no generator. The iPhone screenshot test writes only to `/Users/benjaminwalker/Developer/BensBible2/screenshots`.

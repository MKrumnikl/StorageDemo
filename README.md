# Android storage lecture demos

**AI-generated project.** This project was generated with OpenAI Codex. Model version for this attribution: **GPT-6**.

Open the project in Android Studio, sync Gradle, and run on Android 11 (API 30) or newer. The existing project targets API 37. Install that SDK and use a JDK supported by the project's Gradle/AGP versions. Room's generated implementation is produced by KSP during the build.

The launcher opens twelve separate Activities. Each Activity contains its own storage operations. `DemoActivity.kt` provides the common Compose form, result display, and the `work` helper for lifecycle-bound background execution. Each Activity passes its storage operation to `work` and returns a String to display. This keeps the storage API visible without repeating layout code. This is teaching code; a larger app would normally move persistence into repositories and expose state through ViewModels.

| Demo / source file | What to show |
| --- | --- |
| `DataStoreActivity.kt` | Typed preference key, atomic `edit`, one snapshot with `Flow.first` |
| `SharedPreferencesActivity.kt` | Private preferences, `apply`, read and remove |
| `InternalFilesActivity.kt` | Read/write/delete private persistent text |
| `CacheFilesActivity.kt` | Same operations, but a cache miss is expected and recoverable |
| `ExternalFilesActivity.kt` | App-specific external directory, availability check, no permission |
| `ScopedStorageActivity.kt` | Insert into MediaStore Downloads, write through a URI, publish with `IS_PENDING`, query owned files |
| `PhotoPickerActivity.kt` | Pick one image, persist its URI grant, read metadata and show a thumbnail |
| `DocumentsActivity.kt` | Additional demo: create/open a user-selected document, persist a read grant |
| `SqliteActivity.kt` | SQLiteOpenHelper, table creation, bound raw SQL, cursor mapping |
| `RoomActivity.kt` | Entity, DAO, generated database, suspend queries |
| `ContentProviderActivity.kt` / `LectureProvider.kt` | Resolver CRUD, collection/item URIs, MIME types and change notifications |
| `BackupActivity.kt` | Included file versus no-backup/cache files; cloud and device-transfer rules |

All private examples use different files/databases, so they can be taught independently. No broad storage permission is requested. The provider is intentionally private; exporting it for other apps should be a separate exercise with an explicit permission policy. MediaStore and documents are shared storage: their files can survive app uninstall, while app-specific files are removed. A URI grant can stop working after user revocation or deletion of the document. Photo Picker uses the AndroidX fallback on devices without a picker.

The result path is: the last expression in `work { ... }` returns a String → the helper assigns it to `output` on the main thread → `Text(output)` updates. `output` is Compose state (`mutableStateOf`), so assigning it refreshes the displayed text. `lifecycleScope` cancels coroutines when the Activity is destroyed. Each operation catches storage errors and rethrows cancellation; `busy` disables buttons until the operation finishes.

## Suggested lecture flow

1. Save a distinctive value, return to the menu, reopen the demo and read it.
2. Repeat after restarting the app to distinguish persistent storage from UI state.
3. Use Device Explorer to inspect `files/`, `cache/`, `shared_prefs/`, and `databases/`. Use Database Inspector to compare raw SQLite and Room.
4. Try text containing an apostrophe in SQLite; bound SQL arguments handle it safely.
5. Find MediaStore output with the Files app in `Downloads/StorageDemo`. Choose an image with Photo Picker and a document with the document picker; explain the per-URI grants.
6. Delete a cache file through Device Explorer and show the expected missing-file result.

## Backup/restore experiment

This app deliberately allowlists **only `files/backup-demo.txt`**. Other demos, preferences containing URI strings, and databases are excluded by the allowlist. `noBackupFilesDir` and cache are always excluded from automatic backup. `backup_rules.xml` applies on Android 11; `data_extraction_rules.xml` configures cloud backup and device transfer separately on Android 12+. The two sections currently include the same file; change one to teach different policies.

Use a disposable emulator/device with backup enabled. In Backup and restore, write a distinctive value, then read all three files. For a controlled test, inspect and select an available backup transport:

```sh
adb shell bmgr enabled
adb shell bmgr list transports
# Replace TRANSPORT with a transport reported above; record the original first.
adb shell bmgr transport TRANSPORT
adb shell bmgr backupnow com.vsb.tamz2.storagedemo
adb shell bmgr list sets
```

Wait for `backupnow` to report success and record the restore-set token. Then clear the app's data (this erases all its private lecture examples), and restore using that token:

```sh
adb shell pm clear com.vsb.tamz2.storagedemo
adb shell bmgr restore TOKEN com.vsb.tamz2.storagedemo
```

Relaunch the app and read the backup examples. The included file should contain the backed-up value; the no-backup and cache files should be missing. Restore the original transport if you changed it. Transport availability, account state, device policy and Android version can affect these commands; an ordinary button cannot force a successful cloud backup. Device-to-device transfer is demonstrated during device setup, not by the `bmgr` restore experiment. The notification button only calls `BackupManager.dataChanged()` and does not claim that a backup completed.

Other Android backup options: legacy key/value backup uses a `BackupAgent`/`BackupAgentHelper`, with an application-level `android:backupAgent` declaration; it is an alternative configuration rather than another screen writing into Auto Backup. For manual user-controlled export/import, demonstrate the document screen. A document export is not a complete backup of this app.

## Validation

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
# With a connected emulator/device:
./gradlew :app:connectedDebugAndroidTest
```

The instrumentation tests cover raw SQL text binding, Room insert/read/delete and provider item-URI CRUD. For the system pickers, MediaStore and backup, use the lecture flow above on a device.

Reference documentation: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore), [Room](https://developer.android.com/training/data-storage/room), [MediaStore](https://developer.android.com/training/data-storage/shared/media), [Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker), [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files), [Auto Backup](https://developer.android.com/identity/data/autobackup), [KSP releases](https://github.com/google/ksp/releases).

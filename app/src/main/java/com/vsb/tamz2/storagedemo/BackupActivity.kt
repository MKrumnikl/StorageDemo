package com.vsb.tamz2.storagedemo

import android.app.backup.BackupManager
import android.os.Bundle
import java.io.File

class BackupActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "Backup and restore",
            "Auto Backup and device transfer use the manifest + XML " +
                "rules. Only backup-demo.txt is included in this app. Cache " +
                "and noBackupFilesDir are never automatically backed up. " +
                "Actual backup scheduling and restore depend on the device " +
                "and transport; this screen cannot guarantee a backup.",
            listOf(
                "Write backup examples" to {
                    val value = input
                    work {
                        File(filesDir, "backup-demo.txt").writeText(value)
                        File(noBackupFilesDir, "excluded-demo.txt").writeText(value)
                        File(cacheDir, "backup-cache.txt").writeText(value)
                        "Wrote included, excluded and cache files. See README for " +
                            "the restore experiment."
                    }
                },
                "Read backup examples" to {
                    work {
                        listOf(
                            File(filesDir, "backup-demo.txt"),
                            File(noBackupFilesDir, "excluded-demo.txt"),
                            File(cacheDir, "backup-cache.txt")
                        )
                            .joinToString("\n") {
                                "${it.absolutePath}: ${if (it.exists()) it.readText() else "MISSING"}"
                            }
                    }
                },
                // This is a signal to the backup service, not a synchronous backup operation.
                "Notify backup service of changes" to {
                    BackupManager(this).dataChanged()
                    output = "Change notification sent; use adb for a controlled backup test."
                }
            )
        )
    }
}

package com.vsb.tamz2.storagedemo

import android.os.Bundle

class CacheFilesActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "Cache files",
            "Temporary private files. Android may delete them; never " +
                "rely on cache for important data.",
            listOf(
                "Save" to {
                    val value = input
                    work {
                        val file = java.io.File(cacheDir, "cache-demo.txt")
                        file.writeText(value)
                        "Saved to ${file.absolutePath}"
                    }
                },
                "Read" to {
                    work {
                        val file = java.io.File(cacheDir, "cache-demo.txt")
                        if (file.exists()) file.readText() else "File is missing"
                    }
                },
                "Delete" to {
                    work {
                        val file = java.io.File(cacheDir, "cache-demo.txt")
                        if (file.delete()) "Deleted" else "File is missing"
                    }
                }
            )
        )
    }
}

package com.vsb.tamz2.storagedemo

import android.os.Bundle

class InternalFilesActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "Internal app files",
            "Private persistent files; removed on uninstall. No storage permission.",
            listOf(
                "Save" to {
                    val value = input
                    work {
                        val file = java.io.File(filesDir, "internal-demo.txt")
                        file.writeText(value)
                        "Saved to ${file.absolutePath}"
                    }
                },
                "Read" to {
                    work {
                        val file = java.io.File(filesDir, "internal-demo.txt")
                        if (file.exists()) file.readText() else "File is missing"
                    }
                },
                "Delete" to {
                    work {
                        val file = java.io.File(filesDir, "internal-demo.txt")
                        if (file.delete()) "Deleted" else "File is missing"
                    }
                }
            )
        )
    }
}

package com.vsb.tamz2.storagedemo

import android.os.Bundle

class ExternalFilesActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "App-specific external storage",
            "No storage permission. Removed on uninstall; external storage may be unavailable.",
            listOf(
                "Save" to {
                    val value = input
                    work {
                        val file = java.io.File(
                            requireNotNull(
                                getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
                            ) {
                                "External storage unavailable"
                            },
                            "external-demo.txt"
                        )
                        file.writeText(value)
                        "Saved to ${file.absolutePath}"
                    }
                },
                "Read" to {
                    work {
                        val file = java.io.File(
                            requireNotNull(
                                getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
                            ) {
                                "External storage unavailable"
                            },
                            "external-demo.txt"
                        )
                        if (file.exists()) file.readText() else "File is missing"
                    }
                },
                "Delete" to {
                    work {
                        val file = java.io.File(
                            requireNotNull(
                                getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)
                            ) {
                                "External storage unavailable"
                            },
                            "external-demo.txt"
                        )
                        if (file.delete()) "Deleted" else "File is missing"
                    }
                }
            )
        )
    }
}

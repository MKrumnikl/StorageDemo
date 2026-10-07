package com.vsb.tamz2.storagedemo

import android.content.ContentValues
import android.os.Bundle

class ContentProviderActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "ContentProvider",
            "Access a custom provider through ContentResolver and " +
                "content:// URIs. The provider is private (exported=false); " +
                "cross-app access requires an explicit security policy.",
            listOf(
                "Insert note" to {
                    val value = input
                    work {
                        val values = ContentValues().apply {
                            put("message", value)
                        }
                        "Inserted: ${contentResolver.insert(LectureProvider.NOTES, values)}"
                    }
                },
                "Query notes" to {
                    work {
                        contentResolver.query(
                            LectureProvider.NOTES,
                            arrayOf("_id", "message"),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            buildList {
                                while (cursor.moveToNext()) {
                                    add("${cursor.getLong(0)}: ${cursor.getString(1)}")
                                }
                            }
                                .joinToString("\n").ifEmpty {
                                    "No notes"
                                }
                        } ?: "No cursor"
                    }
                },
                "Update all notes" to {
                    val value = input
                    work {
                        val values = ContentValues().apply {
                            put("message", value)
                        }
                        val updatedRows = contentResolver.update(
                            LectureProvider.NOTES,
                            values,
                            null,
                            null
                        )
                        "Updated $updatedRows rows"
                    }
                },
                "Delete all notes" to {
                    work {
                        "Deleted ${contentResolver.delete(LectureProvider.NOTES, null, null)} rows"
                    }
                }
            )
        )
    }
}

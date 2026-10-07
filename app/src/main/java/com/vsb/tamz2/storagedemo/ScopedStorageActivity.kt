package com.vsb.tamz2.storagedemo

import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore

class ScopedStorageActivity : DemoActivity() {
    private val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "Scoped storage · MediaStore",
            "Publish text files in Downloads/StorageDemo. No permission " +
                "is needed for files owned by this installation. Shared " +
                "files can survive uninstall.",
            listOf(
                "Publish a new text file" to {
                    val value = input
                    work {
                        val values = ContentValues().apply {
                            put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                "lecture-${System.currentTimeMillis()}.txt"
                            )
                            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                "${Environment.DIRECTORY_DOWNLOADS}/StorageDemo"
                            )
                            // Hide a partially written file from other apps until publishing it.
                            put(MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                        val uri = requireNotNull(contentResolver.insert(collection, values))
                        try {
                            requireNotNull(
                                contentResolver.openOutputStream(uri)
                            ).bufferedWriter().use {
                                it.write(value)
                            }
                            values.clear()
                            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            contentResolver.update(uri, values, null, null)
                            getSharedPreferences("media_demo", MODE_PRIVATE).edit().putString(
                                "uri",
                                uri.toString()
                            ).apply()
                            "Published: $uri"
                        } catch (e: Exception) {
                            contentResolver.delete(uri, null, null)
                            throw e
                        }
                    }
                },
                "Read last published file" to {
                    work {
                        val saved = getSharedPreferences("media_demo", MODE_PRIVATE).getString(
                            "uri",
                            null
                        )
                        if (saved == null) "Publish a file first" else
                            requireNotNull(
                                contentResolver.openInputStream(android.net.Uri.parse(saved))
                            ).bufferedReader().use {
                                it.readText()
                            }
                    }
                },
                "Delete last published file" to {
                    work {
                        val preferences = getSharedPreferences("media_demo", MODE_PRIVATE)
                        val saved = preferences.getString("uri", null)
                        if (saved == null) "Publish a file first" else {
                            val count = contentResolver.delete(
                                android.net.Uri.parse(saved),
                                null,
                                null
                            )
                            preferences.edit().remove("uri").apply()
                            "Deleted $count file(s)"
                        }
                    }
                },
                "List our published files" to {
                    work {
                        contentResolver.query(
                            collection,
                            arrayOf(
                                MediaStore.MediaColumns._ID,
                                MediaStore.MediaColumns.DISPLAY_NAME
                            ),
                            "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND " +
                                "${MediaStore.MediaColumns.OWNER_PACKAGE_NAME} = ?",
                            arrayOf("${Environment.DIRECTORY_DOWNLOADS}/StorageDemo/", packageName),
                            "${MediaStore.MediaColumns._ID} DESC"
                        )?.use { cursor ->
                            buildList {
                                while (cursor.moveToNext()) {
                                    add("${cursor.getLong(0)}: ${cursor.getString(1)}")
                                }
                            }
                                .joinToString("\n").ifEmpty {
                                    "No files"
                                }
                        } ?: "Query unavailable"
                    }
                }
            )
        )
    }
}

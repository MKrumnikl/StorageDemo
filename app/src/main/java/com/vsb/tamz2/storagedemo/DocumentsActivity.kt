package com.vsb.tamz2.storagedemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts

class DocumentsActivity : DemoActivity() {
    private var pendingText = ""
    private val create = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) output = "Creation cancelled"
        else {
            val value = pendingText
            work {
                requireNotNull(contentResolver.openOutputStream(uri, "wt")).bufferedWriter().use {
                    it.write(value)
                }
                "Written: $uri"
            }
        }
    }
    private val open = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) output = "Selection cancelled"
        else work {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            getSharedPreferences("document_demo", MODE_PRIVATE).edit().putString(
                "uri",
                uri.toString()
            ).apply()
            read(uri)
        }
    }

    private fun read(uri: android.net.Uri): String =
        // Limit the lecture preview so selecting a huge document cannot exhaust memory.
        requireNotNull(contentResolver.openInputStream(uri)).bufferedReader().use { reader ->
            val buffer = CharArray(4096)
            val count = reader.read(buffer)
            if (count < 0) "Empty document" else String(buffer, 0, count)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingText = savedInstanceState?.getString("pendingText") ?: input
        screen(
            "Storage Access Framework",
            "The user chooses a document in local or cloud storage. Work " +
                "with content URIs; no broad storage permission.",
            listOf(
                "Create text document" to {
                    pendingText = input
                    create.launch("lecture.txt")
                },
                "Open text document" to {
                    open.launch(arrayOf("text/plain"))
                },
                "Read remembered document" to {
                    work {
                        val saved = getSharedPreferences("document_demo", MODE_PRIVATE).getString(
                            "uri",
                            null
                        )
                        if (saved == null) {
                            "Open a document first"
                        } else {
                            read(android.net.Uri.parse(saved))
                        }
                    }
                }
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("pendingText", pendingText)
        super.onSaveInstanceState(outState)
    }
}

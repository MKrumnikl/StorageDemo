package com.vsb.tamz2.storagedemo

import android.graphics.Bitmap
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Intent
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class PhotoPickerActivity : DemoActivity() {
    private var thumbnail by mutableStateOf<Bitmap?>(null)
    private val picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) output = "Selection cancelled"
        else work {
            // The grant is for this URI only, not the entire photo library.
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            getSharedPreferences("picker_demo", MODE_PRIVATE).edit().putString(
                "uri",
                uri.toString()
            ).apply()
            describe(uri)
        }
    }

    private suspend fun describe(uri: android.net.Uri): String {
        val name = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) it.getString(0) else "Unnamed image"
        }
        // Open the content URI rather than trying to convert it into a filesystem path.
        val readable = requireNotNull(contentResolver.openInputStream(uri)).use {
            it.read() >= 0
        }
        val preview = contentResolver.loadThumbnail(uri, Size(640, 480), null)
        withContext(Dispatchers.Main) {
            thumbnail = preview
        }
        return "$name\nType: ${contentResolver.getType(uri)}\nReadable: $readable\n$uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "Photo Picker",
            "Select an image without storage permissions. The AndroidX " +
                "contract falls back to a document picker where needed. A " +
                "saved URI can still become invalid if the image is removed " +
                "or access is revoked.",
            listOf(
                "Pick an image" to {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                "Read remembered image" to {
                    work {
                        val saved = getSharedPreferences("picker_demo", MODE_PRIVATE).getString(
                            "uri",
                            null
                        )
                        if (saved == null) {
                            "Pick an image first"
                        } else {
                            describe(android.net.Uri.parse(saved))
                        }
                    }
                }
            ),
            extra = {
                thumbnail?.let {
                    Image(it.asImageBitmap(), "Selected image", Modifier.height(220.dp))
                }
            }
        )
    }
}

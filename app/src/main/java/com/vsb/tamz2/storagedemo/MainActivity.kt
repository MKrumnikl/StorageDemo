package com.vsb.tamz2.storagedemo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vsb.tamz2.storagedemo.ui.theme.StorageDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val demos = listOf(
            "Preferences DataStore" to DataStoreActivity::class.java,
            "SharedPreferences" to SharedPreferencesActivity::class.java,
            "Internal app files" to InternalFilesActivity::class.java,
            "Cache files" to CacheFilesActivity::class.java,
            "App-specific external files" to ExternalFilesActivity::class.java,
            "Scoped storage · MediaStore" to ScopedStorageActivity::class.java,
            "Photo Picker" to PhotoPickerActivity::class.java,
            "Documents · Storage Access Framework" to DocumentsActivity::class.java,
            "SQLite · raw SQL" to SqliteActivity::class.java,
            "Room" to RoomActivity::class.java,
            "ContentProvider" to ContentProviderActivity::class.java,
            "Backup and restore" to BackupActivity::class.java
        )
        setContent {
            StorageDemoTheme {
                Scaffold { padding ->
                    Column(
                        Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Android storage demos",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text("Each button opens an independent Activity. Save a value, " +
                                "close the screen, then return to read it.")
                        demos.forEach { (label, activity) ->
                            Button(
                                onClick = {
                                    startActivity(Intent(this@MainActivity, activity))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }
        }
    }
}

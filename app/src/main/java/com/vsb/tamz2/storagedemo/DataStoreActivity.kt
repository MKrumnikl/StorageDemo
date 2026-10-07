package com.vsb.tamz2.storagedemo

import android.content.Context
import android.os.Bundle
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// One DataStore instance per file, shared by all Activity instances.
private val Context.demoDataStore by preferencesDataStore(name = "settings")
class DataStoreActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val key = stringPreferencesKey("message")
        screen(
            "Preferences DataStore",
            "Asynchronous, transactional key/value storage in " +
                "files/datastore/settings.preferences_pb.",
            listOf(
                "Save" to {
                    val value = input
                    work {
                        demoDataStore.edit {
                            it[key] = value
                        }
                        "Saved"
                    }
                },
                // first() reads one snapshot; collect() would observe subsequent changes.
                "Read" to {
                    work {
                        demoDataStore.data.first()[key] ?: "No value saved"
                    }
                },
                "Remove" to {
                    work {
                        demoDataStore.edit {
                            it.remove(key)
                        }
                        "Removed"
                    }
                }
            )
        )
    }
}

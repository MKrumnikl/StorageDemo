package com.vsb.tamz2.storagedemo

import android.os.Bundle

class SharedPreferencesActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("preferences_demo", MODE_PRIVATE)
        screen(
            "SharedPreferences",
            "Private XML key/value storage. DataStore is the modern alternative.",
            listOf(
                "Save" to {
                    preferences.edit().putString("message", input).apply()
                    output = "Saved (apply writes to disk asynchronously)."
                },
                "Read" to {
                    output = preferences.getString("message", "No value saved")!!
                },
                "Remove" to {
                    preferences.edit().remove("message").apply()
                    output = "Removed"
                }
            )
        )
    }
}

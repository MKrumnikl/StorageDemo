package com.vsb.tamz2.storagedemo

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle

class DemoSqlite(context: Context) : SQLiteOpenHelper(context, "raw-demo.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "message TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add explicit ALTER TABLE migrations when incrementing the database version.
    }
}
class SqliteActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(
            "SQLite · raw SQL",
            "A table of notes. SQL and cursor mapping are written manually.",
            listOf(
                "Insert note" to {
                    val value = input
                    work {
                        // Use the Activity Context for the database helper.
                        DemoSqlite(this@SqliteActivity).use { helper ->
                            // Bind arguments instead of interpolating text: quotes are safe and SQL
                            // injection is avoided.
                            helper.writableDatabase.execSQL(
                                "INSERT INTO notes(message) VALUES (?)",
                                arrayOf(value)
                            )
                        }
                        "Inserted"
                    }
                },
                "Read notes" to {
                    work {
                        DemoSqlite(this@SqliteActivity).use { helper ->
                            helper.readableDatabase.rawQuery(
                                "SELECT id, message FROM notes ORDER BY id",
                                null
                            ).use { cursor ->
                                buildList {
                                    while (cursor.moveToNext()) {
                                        add("${cursor.getLong(0)}: ${cursor.getString(1)}")
                                    }
                                }.joinToString("\n").ifEmpty {
                                    "No notes"
                                }
                            }
                        }
                    }
                },
                "Delete all notes" to {
                    work {
                        DemoSqlite(this@SqliteActivity).use {
                            it.writableDatabase.execSQL("DELETE FROM notes")
                        }
                        "Deleted"
                    }
                }
            )
        )
    }
}

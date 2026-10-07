package com.vsb.tamz2.storagedemo

import android.content.ContentValues
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class StorageInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun rawSqlBindsQuotesAsText() {
        val message = "Teacher's note; DROP TABLE notes;"
        DemoSqlite(context).use { helper ->
            val db = helper.writableDatabase
            db.beginTransaction()
            try {
                db.execSQL("INSERT INTO notes(message) VALUES (?)", arrayOf(message))
                db.rawQuery("SELECT message FROM notes WHERE id = last_insert_rowid()", null).use {
                    assertTrue(it.moveToFirst())
                    assertEquals(message, it.getString(0))
                }
            } finally {
                db.endTransaction()
            }
            // Roll back so lecture data is untouched.
        }
    }

    @Test fun roomPersistsAndClearsNotes() = runBlocking {
        Room.inMemoryDatabaseBuilder(context, NotesDatabase::class.java).build().use { db ->
            db.notes().insert(Note(message = "Lecture"))
            val note = db.notes().all().single()
            assertTrue(note.id > 0)
            assertEquals("Lecture", note.message)
            db.notes().clear()
            assertTrue(db.notes().all().isEmpty())
        }
    }

    @Test fun providerSupportsItemUris() {
        val resolver = context.contentResolver
        val uri = requireNotNull(
            resolver.insert(
                LectureProvider.NOTES,
                ContentValues().apply {
                    put("message", "Original")
                }
            )
        )
        try {
            assertEquals(
                1,
                resolver.update(
                    uri,
                    ContentValues().apply {
                        put("message", "Updated")
                    },
                    null,
                    null
                )
            )
            resolver.query(uri, arrayOf("_id", "message"), null, null, null)!!.use {
                assertTrue(it.moveToFirst())
                assertEquals("Updated", it.getString(1))
                assertFalse(it.moveToNext())
            }
        } finally {
            resolver.delete(uri, null, null)
        }
        resolver.query(uri, null, null, null, null)!!.use {
            assertEquals(0, it.count)
        }
    }
}

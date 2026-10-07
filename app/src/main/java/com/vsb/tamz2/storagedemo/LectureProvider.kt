package com.vsb.tamz2.storagedemo

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

// This provider has its own database; it does not reuse the raw SQLite demo.
class LectureProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.vsb.tamz2.storagedemo.notes"
        val NOTES: Uri = Uri.parse("content://$AUTHORITY/notes")
        private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "notes", 1)
            addURI(AUTHORITY, "notes/#", 2)
        }
    }
    private lateinit var helper: SQLiteOpenHelper

    override fun onCreate(): Boolean {
        helper = object : SQLiteOpenHelper(requireNotNull(context), "provider-demo.db", null, 1) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE notes (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "message TEXT NOT NULL)")
            }
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
        }
        return true
    }

    private fun where(uri: Uri): String? = when (matcher.match(uri)) { 1 ->
        null
        2 -> "_id = ${ContentUris.parseId(uri)}" // parseId validates the numeric ID.
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

    override fun getType(uri: Uri): String = when (matcher.match(uri)) { 1 ->
        "vnd.android.cursor.dir/vnd.$AUTHORITY.note"
        2 -> "vnd.android.cursor.item/vnd.$AUTHORITY.note"
        else -> throw IllegalArgumentException("Unknown URI")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        // A small fixed API avoids exposing arbitrary SQL through the provider.
        require(selection == null && sortOrder == null) {
            "Custom SQL is not supported"
        }
        require(projection == null || projection.all {
            it == "_id" || it == "message"
        })
        return helper.readableDatabase.query(
            "notes",
            projection,
            where(uri),
            null,
            null,
            null,
            "_id"
        ).apply {
            setNotificationUri(requireNotNull(context).contentResolver, NOTES)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        require(matcher.match(uri) == 1)
        val id = helper.writableDatabase.insertOrThrow("notes", null, checkedValues(values))
        requireNotNull(context).contentResolver.notifyChange(NOTES, null)
        return ContentUris.withAppendedId(NOTES, id)
    }

    private fun checkedValues(values: ContentValues?): ContentValues {
        val message = requireNotNull(values?.getAsString("message"))
        return ContentValues().apply {
            put("message", message)
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        require(selection == null)
        return helper.writableDatabase.update("notes", checkedValues(values), where(uri), null).also {
            requireNotNull(context).contentResolver.notifyChange(NOTES, null)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        require(selection == null)
        return helper.writableDatabase.delete("notes", where(uri), null).also {
            requireNotNull(context).contentResolver.notifyChange(NOTES, null)
        }
    }
}

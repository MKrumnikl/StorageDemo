package com.vsb.tamz2.storagedemo

import android.content.Context
import android.os.Bundle
import androidx.room.*

@Entity(tableName = "notes")
data class Note(@PrimaryKey(autoGenerate = true) val id: Long = 0, val message: String)

@Dao
interface NoteDao {
    @Insert suspend fun insert(note: Note)

    @Query("SELECT * FROM notes ORDER BY id") suspend fun all(): List<Note>

    @Query("DELETE FROM notes") suspend fun clear()
}

@Database(entities = [Note::class], version = 1, exportSchema = true)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notes(): NoteDao

    companion object {
        @Volatile private var instance: NotesDatabase? = null
        fun get(context: Context): NotesDatabase = instance ?: synchronized(this) {
            // Use applicationContext so the singleton does not retain an Activity.
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NotesDatabase::class.java,
                "room-demo.db"
            )
                .build().also {
                    instance = it
                }
        }
    }
}

class RoomActivity : DemoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = NotesDatabase.get(this).notes()
        screen(
            "Room",
            "The same notes example with entities, a DAO and compile-time SQL checking.",
            listOf(
                "Insert note" to {
                    val value = input
                    work {
                        dao.insert(Note(message = value))
                        "Inserted"
                    }
                },
                "Read notes" to {
                    work {
                        dao.all().joinToString("\n") {
                            "${it.id}: ${it.message}"
                        }.ifEmpty {
                            "No notes"
                        }
                    }
                },
                "Delete all notes" to {
                    work {
                        dao.clear()
                        "Deleted"
                    }
                }
            )
        )
    }
}

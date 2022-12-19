package com.example.seattle.data

import androidx.room.*
import com.example.seattle.presentation.adapter.NoteDetailModel
import com.example.seattle.presentation.adapter.NotePreviewModel

@Entity(tableName = "notes_table")
data class Note(
    var title: String?,
    var text: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun toPreviewItem(): NotePreviewModel {
        return NotePreviewModel(id, text)
    }

    fun toDetailItem(): NoteDetailModel {
        return NoteDetailModel(text)
    }

    fun chunkText(maxTextLength: Int): List<String> {
        return text.chunked(maxTextLength)
    }
}

@Dao
interface NoteDao {
    @Insert
    fun addNote(note: Note)

    @Query("SELECT * FROM notes_table")
    fun getNotes(): List<Note>

    @Query("SELECT * FROM notes_table WHERE id = :id")
    fun getNote(id: Int): Note

    @Query("UPDATE notes_table SET text = :content WHERE id = :id")
    fun updateNote(id: Int, content: String)

    @Query("DELETE FROM notes_table WHERE id = :id")
    fun deleteNote(id: Int)
}

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
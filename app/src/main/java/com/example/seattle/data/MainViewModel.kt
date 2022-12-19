package com.example.seattle.data

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val noteDao: NoteDao) : ViewModel() {
    private var _notesFlow: Flow<List<Note>> = emptyFlow()
    val notesFlow get() = _notesFlow

    private var _currentNoteFlow: Flow<Note> = emptyFlow()
    val currentNoteFlow get() = _currentNoteFlow

    fun addNote(note: Note) {
        runBlocking(Dispatchers.IO) {
            noteDao.addNote(note)
        }
    }

    fun saveNote(noteId: Int, content: String) {
        runBlocking(Dispatchers.IO) {
            noteDao.updateNote(noteId, content)
        }
    }

    fun getNotes() {
        runBlocking(Dispatchers.IO) {
            _notesFlow = flow {
                emit(noteDao.getNotes())
            }
        }
    }

    fun getNote(id: Int) {
        runBlocking(Dispatchers.IO) {
            _currentNoteFlow = flow {
                emit(noteDao.getNote(id))
            }
        }
    }

    fun deleteNote(noteId: Int) {
        runBlocking(Dispatchers.IO) {
            noteDao.deleteNote(noteId)
        }
    }
}
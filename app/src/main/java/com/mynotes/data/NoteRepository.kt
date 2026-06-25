package com.mynotes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    val noteCount: Flow<Int> = noteDao.getNoteCount()

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)
    suspend fun togglePin(id: Long, pinned: Boolean) = noteDao.togglePin(id, pinned)
}

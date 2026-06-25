package com.mynotes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    val activeNoteCount: Flow<Int> = noteDao.getActiveNoteCount()
    val archivedNoteCount: Flow<Int> = noteDao.getArchivedNoteCount()
    val totalCharacters: Flow<Int> = noteDao.getTotalCharacters()
    val categories: Flow<List<String>> = noteDao.getCategories()

    fun searchActiveNotes(query: String): Flow<List<Note>> = noteDao.searchActiveNotes(query)
    fun getNotesByCategory(category: String): Flow<List<Note>> = noteDao.getNotesByCategory(category)

    suspend fun getNoteById(id: Long): Note? = noteDao.getNoteById(id)
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)
    suspend fun togglePin(id: Long, pinned: Boolean) = noteDao.togglePin(id, pinned)
    suspend fun toggleArchive(id: Long, archived: Boolean) = noteDao.toggleArchive(id, archived)
    suspend fun cleanOldArchived(threshold: Long) = noteDao.cleanOldArchived(threshold)
}

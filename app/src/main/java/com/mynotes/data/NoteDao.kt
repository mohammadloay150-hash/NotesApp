package com.mynotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAt DESC")
    fun searchActiveNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE notes SET isPinned = :pinned WHERE id = :id")
    suspend fun togglePin(id: Long, pinned: Boolean)

    @Query("UPDATE notes SET isArchived = :archived WHERE id = :id")
    suspend fun toggleArchive(id: Long, archived: Boolean)

    @Query("SELECT COUNT(*) FROM notes WHERE isArchived = 0")
    fun getActiveNoteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notes WHERE isArchived = 1")
    fun getArchivedNoteCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(LENGTH(title) + LENGTH(content)), 0) FROM notes WHERE isArchived = 0")
    fun getTotalCharacters(): Flow<Int>

    @Query("SELECT DISTINCT category FROM notes WHERE category != '' AND isArchived = 0 ORDER BY category")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND category = :category ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByCategory(category: String): Flow<List<Note>>

    @Query("DELETE FROM notes WHERE isArchived = 1 AND updatedAt < :threshold")
    suspend fun cleanOldArchived(threshold: Long)
}

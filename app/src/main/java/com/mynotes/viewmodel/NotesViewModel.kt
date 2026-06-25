package com.mynotes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mynotes.data.Note
import com.mynotes.data.NoteDatabase
import com.mynotes.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository by lazy {
        val database = NoteDatabase.getDatabase(application)
        NoteRepository(database.noteDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allNotes
            else repository.searchNotes(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteCount: StateFlow<Int> = repository.noteCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun saveNote(id: Long?, title: String, content: String) {
        viewModelScope.launch {
            if (id != null && id > 0) {
                val existing = repository.getNoteById(id)
                if (existing != null) {
                    repository.updateNote(
                        existing.copy(
                            title = title,
                            content = content,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                repository.insertNote(
                    Note(
                        title = title,
                        content = content
                    )
                )
            }
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }
}

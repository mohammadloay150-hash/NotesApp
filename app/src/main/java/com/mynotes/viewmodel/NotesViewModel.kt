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
import kotlinx.coroutines.flow.combine
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

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived

    private val _sortOrder = MutableStateFlow("newest")
    val sortOrder: StateFlow<String> = _sortOrder

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = combine(
        _searchQuery, _selectedCategory, _showArchived, _sortOrder
    ) { query, category, archived, _ ->
        Triple(query, category, archived)
    }.flatMapLatest { (query, category, archived) ->
        when {
            archived -> repository.archivedNotes
            query.isNotBlank() -> repository.searchActiveNotes(query)
            category.isNotBlank() -> repository.getNotesByCategory(category)
            else -> repository.activeNotes
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val noteCount: StateFlow<Int> = repository.activeNoteCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val archivedCount: StateFlow<Int> = repository.archivedNoteCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCharacters: StateFlow<Int> = repository.totalCharacters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = if (_selectedCategory.value == category) "" else category
    }

    fun clearCategory() {
        _selectedCategory.value = ""
    }

    fun toggleArchived() {
        _showArchived.value = !_showArchived.value
        _selectedCategory.value = ""
    }

    fun setSortOrder(order: String) {
        _sortOrder.value = order
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun saveNote(id: Long?, title: String, content: String, color: Long = 0xFFFFFFFF.toLong(), category: String = "") {
        viewModelScope.launch {
            if (id != null && id > 0) {
                val existing = repository.getNoteById(id)
                if (existing != null) {
                    repository.updateNote(
                        existing.copy(
                            title = title,
                            content = content,
                            color = color,
                            category = category,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                repository.insertNote(
                    Note(
                        title = title,
                        content = content,
                        color = color,
                        category = category
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

    fun togglePin(id: Long, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePin(id, !currentPinned)
        }
    }

    fun toggleArchive(id: Long, currentArchived: Boolean) {
        viewModelScope.launch {
            repository.toggleArchive(id, !currentArchived)
        }
    }

    fun getWordCount(text: String): Int {
        return text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    fun getCharCount(text: String): Int {
        return text.length
    }
}

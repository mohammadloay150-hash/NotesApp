package com.mynotes.ui.navigation

sealed class Screen(val route: String) {
    data object NotesList : Screen("notes_list")
    data object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Long?) = "note_detail/${noteId ?: -1}"
    }
}

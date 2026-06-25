package com.mynotes.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mynotes.ui.components.EmptyState
import com.mynotes.ui.components.NoteCard
import com.mynotes.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

private fun formatNoteCount(count: Int): String {
    return when (count) {
        0 -> "لا توجد ملاحظات"
        1 -> "ملاحظة واحدة"
        2 -> "ملاحظتان"
        in 3..10 -> "$count ملاحظات"
        else -> "$count ملاحظة"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val noteCount by viewModel.noteCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isSearching = searchQuery.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = !isSearching,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            Text(
                                "ملاحظاتي",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            if (notes.isNotEmpty()) {
                                Text(
                                    text = formatNoteCount(noteCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.semantics {
                                        contentDescription = "عدد الملاحظات: $noteCount"
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isSearching) viewModel.clearSearch()
                        else viewModel.updateSearchQuery(" ")
                    }) {
                        Icon(
                            if (isSearching) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (isSearching) "إغلاق البحث" else "بحث"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "إضافة ملاحظة", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search bar
            AnimatedVisibility(visible = isSearching) {
                TextField(
                    value = if (isSearching) searchQuery else "",
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("ابحث في ملاحظاتك...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { contentDescription = "حقل البحث" },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Notes list
            if (notes.isEmpty()) {
                EmptyState(
                    title = if (isSearching) "لا توجد نتائج" else "لا توجد ملاحظات بعد",
                    subtitle = if (isSearching) "جرب كلمة بحث أخرى" else "اضغط + لإضافة ملاحظة"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onPinClick = { viewModel.togglePin(note.id, note.isPinned) },
                            onDeleteClick = { viewModel.deleteNote(note.id) },
                            onShareClick = {
                                val shareText = if (note.title.isNotBlank() || note.content.isNotBlank()) {
                                    "${note.title}\n\n${note.content}"
                                } else "ملاحظة فارغة"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "مشاركة"))
                            },
                            onCopyClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("note", "${note.title}\n\n${note.content}")
                                clipboard.setPrimaryClip(clip)
                                scope.launch { snackbarHostState.showSnackbar("تم النسخ") }
                            }
                        )
                    }
                }
            }
        }
    }
}

package com.mynotes.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mynotes.data.Note
import com.mynotes.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long?,
    viewModel: NotesViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isEditing = noteId != null && noteId > 0

    LaunchedEffect(noteId) {
        if (isEditing) {
            val note = viewModel.notes.value.find { it.id == noteId }
            note?.let {
                title = it.title
                content = it.content
            }
        }
    }

    // Discard dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("تجاهل التغييرات؟") },
            text = { Text("لديك تغييرات غير محفوظة. هل تريد تجاهلها؟") },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onNavigateBack() }) {
                    Text("تجاهل", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("البقاء") }
            }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف الملاحظة") },
            text = { Text("هل تريد حذف هذه الملاحظة نهائياً؟") },
            confirmButton = {
                TextButton(onClick = {
                    noteId?.let { viewModel.deleteNote(it) }
                    showDeleteDialog = false; onNavigateBack()
                }) { Text("احذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "تعديل الملاحظة" else "ملاحظة جديدة",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) showDiscardDialog = true else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveNote(noteId, title, content)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "حفظ")
                    }
                    if (isEditing) {
                        IconButton(onClick = {
                            val shareText = if (title.isNotBlank() || content.isNotBlank()) "$title\n\n$content" else "ملاحظة فارغة"
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText)
                            }, "مشاركة"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "مشاركة")
                        }
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                    as android.content.ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("note", "$title\n\n$content")
                            )
                            scope.launch { snackbarHostState.showSnackbar("تم النسخ") }
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "نسخ")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; hasChanges = true },
                label = { Text("العنوان") },
                placeholder = { Text("العنوان") },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "عنوان الملاحظة" },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it; hasChanges = true },
                label = { Text("المحتوى") },
                placeholder = { Text("اكتب ملاحظتك هنا...") },
                modifier = Modifier.fillMaxWidth().height(350.dp).semantics { contentDescription = "محتوى الملاحظة" },
                maxLines = Int.MAX_VALUE
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = {
                    viewModel.saveNote(noteId, title, content)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "حفظ الملاحظة" }
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("حفظ الملاحظة")
            }
        }
    }
}

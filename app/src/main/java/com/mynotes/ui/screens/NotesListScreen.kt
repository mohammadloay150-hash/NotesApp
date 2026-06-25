package com.mynotes.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mynotes.data.Note
import com.mynotes.ui.components.EmptyState
import com.mynotes.ui.components.NoteCard
import com.mynotes.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesListScreen(
    viewModel: NotesViewModel,
    onNoteClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onArchivedClick: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val noteCount by viewModel.noteCount.collectAsState()
    val archivedCount by viewModel.archivedCount.collectAsState()
    val totalChars by viewModel.totalCharacters.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val showArchived by viewModel.showArchived.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    val isSearching = searchQuery.isNotEmpty()

    val searchAlpha by animateFloatAsState(
        targetValue = if (isSearching) 1f else 0f,
        label = "searchAlpha"
    )

    // Delete all dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("حذف الكل") },
            text = { Text("هل تريد حذف جميع الملاحظات نهائياً؟") },
            confirmButton = {
                TextButton(onClick = {
                    notes.forEach { viewModel.deleteNote(it.id) }
                    showDeleteAllDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("تم حذف جميع الملاحظات")
                    }
                }) { Text("حذف الكل", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Stats dialog
    if (showStatsDialog) {
        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text("📊 إحصائيات الملاحظات") },
            text = {
                Column {
                    Text("📝 إجمالي الملاحظات: $noteCount")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📁 ملاحظات مؤرشفة: $archivedCount")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📏 إجمالي الأحرف: $totalChars")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🏷️ التصنيفات: ${categories.size}")
                    if (categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📌 ${categories.joinToString(" - ")}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatsDialog = false }) { Text("حسناً") }
            }
        )
    }

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
                                if (showArchived) "📦 الأرشيف" else "📝 ملاحظاتي",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                if (showArchived) "$archivedCount ملاحظة مؤرشفة"
                                else "$noteCount ملاحظة • $totalChars حرف",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Dark mode toggle
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDarkMode) "الوضع النهاري" else "الوضع الليلي"
                        )
                    }

                    // Search
                    IconButton(onClick = {
                        if (isSearching) viewModel.clearSearch()
                        else viewModel.updateSearchQuery(" ")
                    }) {
                        Icon(
                            if (isSearching) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (isSearching) "إغلاق البحث" else "بحث في الملاحظات"
                        )
                    }

                    // Archived toggle
                    IconButton(onClick = { onArchivedClick() }) {
                        Icon(
                            Icons.Outlined.Inventory2,
                            contentDescription = if (showArchived) "العودة للملاحظات" else "عرض الأرشيف",
                            tint = if (showArchived) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stats
                    IconButton(onClick = { showStatsDialog = true }) {
                        Icon(
                            Icons.Outlined.BarChart,
                            contentDescription = "إحصائيات"
                        )
                    }

                    // Delete all
                    if (!showArchived && notes.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = "حذف الكل",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!showArchived) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "إضافة ملاحظة جديدة",
                        tint = Color.White
                    )
                }
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
                    placeholder = {
                        Text("🔍 ابحث في الملاحظات...")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { contentDescription = "حقل البحث عن الملاحظات" },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Categories row
            AnimatedVisibility(
                visible = categories.isNotEmpty() && !showArchived,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { viewModel.clearCategory() },
                            label = { Text("الكل") },
                            leadingIcon = {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedCategory.isEmpty())
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        categories.forEach { cat ->
                            AssistChip(
                                onClick = { viewModel.selectCategory(cat) },
                                label = { Text(cat) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selectedCategory == cat)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            // Main content
            if (notes.isEmpty()) {
                if (showArchived) {
                    EmptyState(
                        title = "لا توجد ملاحظات مؤرشفة",
                        subtitle = "اسحب أي ملاحظة لليسار لأرشفتها"
                    )
                } else if (isSearching) {
                    EmptyState(
                        title = "لا توجد نتائج",
                        subtitle = "جرب كلمة بحث أخرى"
                    )
                } else if (selectedCategory.isNotEmpty()) {
                    EmptyState(
                        title = "لا توجد ملاحظات في هذا التصنيف",
                        subtitle = "اختر تصنيفاً آخر"
                    )
                } else {
                    EmptyState(
                        title = "لا توجد ملاحظات بعد",
                        subtitle = "أضف ملاحظتك الأولى"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onPinClick = { viewModel.togglePin(note.id, note.isPinned) },
                            onArchiveClick = {
                                viewModel.toggleArchive(note.id, note.isArchived)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "تم أرشفة الملاحظة",
                                        actionLabel = "تراجع",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onDeleteClick = { viewModel.deleteNote(note.id) },
                            onShareClick = {
                                val shareText = "${note.title}\n\n${note.content}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "مشاركة الملاحظة"))
                            },
                            onCopyClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("note", "${note.title}\n\n${note.content}")
                                clipboard.setPrimaryClip(clip)
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم نسخ الملاحظة")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

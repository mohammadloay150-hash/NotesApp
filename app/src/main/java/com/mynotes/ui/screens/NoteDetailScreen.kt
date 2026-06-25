package com.mynotes.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mynotes.data.Note
import com.mynotes.viewmodel.NotesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val noteColors = listOf(
    0xFFFFFFFF.toLong() to "أبيض",
    0xFFFFF3E0.toLong() to "برتقالي فاتح",
    0xFFFFF8E1.toLong() to "أصفر فاتح",
    0xFFE8F5E9.toLong() to "أخضر فاتح",
    0xFFE3F2FD.toLong() to "أزرق فاتح",
    0xFFF3E5F5.toLong() to "بنفسجي فاتح",
    0xFFFBE9E7.toLong() to "نحاسي فاتح",
    0xFFE0F7FA.toLong() to "سماوي فاتح",
    0xFFF1F8E9.toLong() to "ليموني",
    0xFFFFFDE7.toLong() to "كريمي"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    noteId: Long?,
    viewModel: NotesViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFFFFFFFF.toLong()) }
    var category by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var isTextBold by remember { mutableStateOf(false) }
    var isTextItalic by remember { mutableStateOf(false) }
    var isTextUnderlined by remember { mutableStateOf(false) }
    var currentNote by remember { mutableStateOf<Note?>(null) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val categories by viewModel.categories.collectAsState()
    var newCategory by remember { mutableStateOf("") }

    val wordCount = viewModel.getWordCount(content)
    val charCount = viewModel.getCharCount(content)

    // Auto-save debounce
    LaunchedEffect(title, content, selectedColor, hasChanges) {
        if (hasChanges) {
            delay(2000) // 2 second debounce
            if (title.isNotBlank() || content.isNotBlank()) {
                viewModel.saveNote(noteId, title, content, selectedColor, category)
                scope.launch {
                    snackbarHostState.showSnackbar("تم الحفظ تلقائياً")
                }
            }
        }
    }

    // Load note if editing
    LaunchedEffect(noteId) {
        if (noteId != null && noteId > 0) {
            val note = viewModel.notes.value.find { it.id == noteId }
            currentNote = note
            note?.let {
                title = it.title
                content = it.content
                selectedColor = it.color
                category = it.category
            }
        }
    }

    // Discard dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("❗ تجاهل التغييرات؟") },
            text = { Text("لديك تغييرات غير محفوظة. هل تريد تجاهلها؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) { Text("تجاهل") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("متابعة التحرير") }
            }
        )
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("🗑️ حذف الملاحظة") },
            text = { Text("هل تريد حذف هذه الملاحظة نهائياً؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                TextButton(onClick = {
                    noteId?.let { viewModel.deleteNote(it) }
                    showDeleteDialog = false
                    onNavigateBack()
                }) { Text("احذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Color picker dialog
    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("🎨 اختر لون الملاحظة") },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    noteColors.forEach { (colorInt, colorName) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorInt))
                                    .border(
                                        width = if (selectedColor == colorInt) 3.dp else 1.dp,
                                        color = if (selectedColor == colorInt)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColor = colorInt
                                        hasChanges = true
                                        showColorPicker = false
                                    }
                                    .semantics { contentDescription = colorName }
                            )
                            Text(
                                text = colorName,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) { Text("حسناً") }
            }
        )
    }

    // Category dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("🏷️ إضافة تصنيف") },
            text = {
                Column {
                    if (categories.isNotEmpty()) {
                        Text("التصنيفات الموجودة:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                TextButton(
                                    onClick = {
                                        category = cat
                                        hasChanges = true
                                        showCategoryDialog = false
                                    }
                                ) {
                                    Text(cat, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text("أو أضف تصنيفاً جديداً:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        placeholder = { Text("اسم التصنيف الجديد") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "حقل إضافة تصنيف جديد" },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategory.isNotBlank()) {
                        category = newCategory
                        hasChanges = true
                    }
                    showCategoryDialog = false
                }) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text("إلغاء") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId != null && noteId > 0) "✏️ تعديل الملاحظة" else "➕ ملاحظة جديدة",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) showDiscardDialog = true
                        else onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(onClick = {
                        if (title.isBlank() && content.isBlank()) {
                            titleError = true
                        } else {
                            viewModel.saveNote(noteId, title, content, selectedColor, category)
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = "حفظ الملاحظة",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Share
                    IconButton(onClick = {
                        val shareText = "${title}\n\n${content}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة"))
                    }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "مشاركة الملاحظة"
                        )
                    }

                    // Copy
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("note", "${title}\n\n${content}")
                        clipboard.setPrimaryClip(clip)
                        scope.launch {
                            snackbarHostState.showSnackbar("تم نسخ الملاحظة")
                        }
                    }) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "نسخ الملاحظة"
                        )
                    }

                    // Delete
                    if (noteId != null && noteId > 0) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "حذف الملاحظة",
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Color bar indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(selectedColor))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        hasChanges = true
                        titleError = false
                    },
                    label = { Text("العنوان") },
                    placeholder = { Text("العنوان") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("الرجاء إدخال عنوان أو محتوى") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "حقل عنوان الملاحظة" },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Text formatting toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FormatButton(
                        icon = Icons.Filled.FormatBold,
                        description = "نص عريض",
                        isActive = isTextBold,
                        onClick = { isTextBold = !isTextBold }
                    )
                    FormatButton(
                        icon = Icons.Filled.FormatItalic,
                        description = "نص مائل",
                        isActive = isTextItalic,
                        onClick = { isTextItalic = !isTextItalic }
                    )
                    FormatButton(
                        icon = Icons.Filled.FormatUnderlined,
                        description = "نص تحته خط",
                        isActive = isTextUnderlined,
                        onClick = { isTextUnderlined = !isTextUnderlined }
                    )
                    FormatButton(
                        icon = Icons.Outlined.ColorLens,
                        description = "لون الملاحظة",
                        isActive = showColorPicker,
                        onClick = { showColorPicker = true }
                    )
                    FormatButton(
                        icon = Icons.Outlined.Tag,
                        description = "التصنيف",
                        isActive = false,
                        onClick = { showCategoryDialog = true }
                    )
                }

                // Category display
                if (category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { showCategoryDialog = true }
                            .semantics { contentDescription = "تصنيف: $category. اضغط لتغيير" }
                    ) {
                        Icon(
                            Icons.Outlined.Tag,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content field
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        hasChanges = true
                    },
                    label = { Text("المحتوى") },
                    placeholder = { Text("اكتب ملاحظتك هنا...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .semantics { contentDescription = "حقل محتوى الملاحظة" },
                    maxLines = Int.MAX_VALUE,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Word count bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$wordCount كلمة",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "$charCount حرف",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                TextButton(
                    onClick = {
                        viewModel.saveNote(noteId, title, content, selectedColor, category)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "حفظ الملاحظة" }
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ الملاحظة", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .semantics { contentDescription = description }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (isActive) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

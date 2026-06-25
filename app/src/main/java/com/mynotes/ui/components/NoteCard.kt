package com.mynotes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mynotes.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
    var showActions by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (note.isPinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        else Color.Transparent,
        label = "border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showActions = !showActions }
            )
            .semantics {
                contentDescription = "${note.title.ifEmpty { "بدون عنوان" }}. " +
                        if (note.isPinned) "مثبتة. " else ""
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (note.isPinned) 4.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: pin indicator + title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "مثبت",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = note.title.ifEmpty { "بدون عنوان" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Content preview
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: date
            Text(
                text = dateFormat.format(Date(note.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            // Action buttons (shown on long press)
            if (showActions) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        icon = Icons.Filled.PushPin,
                        label = if (note.isPinned) "إلغاء التثبيت" else "تثبيت",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onPinClick
                    )
                    ActionButton(
                        icon = Icons.Filled.Edit,
                        label = "تعديل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onClick
                    )
                    ActionButton(
                        icon = Icons.Filled.Share,
                        label = "مشاركة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onShareClick
                    )
                    ActionButton(
                        icon = Icons.Filled.ContentCopy,
                        label = "نسخ",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onCopyClick
                    )
                    ActionButton(
                        icon = Icons.Filled.Delete,
                        label = "حذف",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}

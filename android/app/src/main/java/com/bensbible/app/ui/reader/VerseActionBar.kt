package com.bensbible.app.ui.reader

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VerseActionBar(
    selectedCount: Int,
    onHighlight: () -> Unit,
    onNote: () -> Unit,
    onBookmark: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onHighlight) {
                Icon(Icons.Default.FormatColorFill, contentDescription = "Highlight")
            }

            IconButton(onClick = onNote) {
                Icon(Icons.Default.Edit, contentDescription = "Note")
            }

            IconButton(onClick = onBookmark) {
                Icon(Icons.Default.Bookmark, contentDescription = "Bookmark")
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(onClick = onDeselectAll) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Deselect all",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

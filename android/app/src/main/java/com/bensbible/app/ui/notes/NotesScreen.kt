package com.bensbible.app.ui.notes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.VerseAnnotationEntity
import com.bensbible.app.model.BibleLocation
import com.bensbible.app.ui.reader.NoteEditorSheet
import com.bensbible.app.viewmodel.NavigationCoordinator
import com.bensbible.app.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    coordinator: NavigationCoordinator,
    annotationRepository: AnnotationRepository,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.load()
    }

    // Reload when switching back to this tab
    LaunchedEffect(coordinator.selectedTab) {
        viewModel.load()
    }

    Column(modifier = modifier) {
        Text(
            text = "Notes",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        if (viewModel.notes.isEmpty()) {
            EmptyNotesState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = viewModel.notes,
                    key = { it.verseKey }
                ) { annotation ->
                    NoteItem(
                        annotation = annotation,
                        onTap = {
                            coordinator.navigateToReader(
                                BibleLocation(
                                    bookName = annotation.bookName,
                                    chapterNumber = annotation.chapterNumber,
                                    verseNumber = annotation.verseNumber
                                )
                            )
                        },
                        onEdit = { viewModel.beginEditing(annotation) },
                        onDelete = { viewModel.deleteNote(annotation) }
                    )
                }
            }
        }
    }

    // Note editor sheet
    if (viewModel.isEditorPresented) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.cancelEdit()
                viewModel.isEditorPresented = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            val annotation = viewModel.editingAnnotation
            NoteEditorSheet(
                verseReference = if (annotation != null) {
                    "${annotation.bookName} ${annotation.chapterNumber}:${annotation.verseNumber}"
                } else "",
                text = viewModel.editingText,
                onTextChange = { viewModel.editingText = it },
                onSave = {
                    viewModel.saveEdit()
                    viewModel.isEditorPresented = false
                },
                onCancel = {
                    viewModel.cancelEdit()
                    viewModel.isEditorPresented = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteItem(
    annotation: VerseAnnotationEntity,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    Color.Red else Color.Transparent,
                label = "bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onTap() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${annotation.bookName} ${annotation.chapterNumber}:${annotation.verseNumber}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = annotation.noteText ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyNotesState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            Icons.Default.Notes,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 8.dp)
        )
        Text(
            text = "No Notes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Your verse notes will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

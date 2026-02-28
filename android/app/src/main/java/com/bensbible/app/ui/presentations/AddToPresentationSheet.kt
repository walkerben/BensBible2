package com.bensbible.app.ui.presentations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.PresentationRepository
import com.bensbible.app.data.PresentationSlideEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class VerseEntry(
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
)

@Composable
fun AddToPresentationSheet(
    verses: List<VerseEntry>,
    repository: PresentationRepository,
    onDone: () -> Unit
) {
    val presentations by repository.getAllPresentations().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showNewDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LazyColumn {
        item {
            ListItem(
                headlineContent = { Text("New Presentation…") },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        newName = ""
                        showNewDialog = true
                    }
            )
            HorizontalDivider()
        }

        if (presentations.isEmpty()) {
            item {
                Text(
                    "No presentations yet.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(presentations) { presentation ->
                ListItem(
                    headlineContent = { Text(presentation.name) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val existingCount = repository
                                    .getSlidesForPresentation(presentation.id)
                                    .first()
                                    .size
                                verses.forEachIndexed { i, entry ->
                                    repository.insertSlide(
                                        PresentationSlideEntity(
                                            presentationId = presentation.id,
                                            bookName = entry.bookName,
                                            chapterNumber = entry.chapterNumber,
                                            verseNumber = entry.verseNumber,
                                            verseText = entry.text,
                                            order = existingCount + i
                                        )
                                    )
                                }
                                onDone()
                            }
                        }
                )
            }
        }
    }

    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false },
            title = { Text("New Presentation") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newName.trim()
                        if (trimmed.isNotEmpty()) {
                            scope.launch {
                                val presentation = repository.createPresentation(trimmed)
                                verses.forEachIndexed { i, entry ->
                                    repository.insertSlide(
                                        PresentationSlideEntity(
                                            presentationId = presentation.id,
                                            bookName = entry.bookName,
                                            chapterNumber = entry.chapterNumber,
                                            verseNumber = entry.verseNumber,
                                            verseText = entry.text,
                                            order = i
                                        )
                                    )
                                }
                                onDone()
                            }
                        }
                        showNewDialog = false
                    }
                ) { Text("Create & Add") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDialog = false }) { Text("Cancel") }
            }
        )
    }
}

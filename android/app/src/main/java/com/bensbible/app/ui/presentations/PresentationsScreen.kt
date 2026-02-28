package com.bensbible.app.ui.presentations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.PresentationEntity
import com.bensbible.app.data.PresentationRepository
import com.bensbible.app.viewmodel.PresentationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationsScreen(
    viewModel: PresentationsViewModel,
    repository: PresentationRepository,
    modifier: Modifier = Modifier
) {
    val presentations by viewModel.presentations.collectAsState()
    var showNewDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var selectedPresentation by remember { mutableStateOf<PresentationEntity?>(null) }

    selectedPresentation?.let { presentation ->
        PresentationDetailScreen(
            presentation = presentation,
            repository = repository,
            onBack = { selectedPresentation = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Presentations") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                newName = ""
                showNewDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "New Presentation")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (presentations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text("No presentations yet.")
                    Text("Tap + to create one.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(presentations, key = { it.id }) { presentation ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deletePresentation(presentation)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        enableDismissFromStartToEnd = false
                    ) {
                        ListItem(
                            headlineContent = { Text(presentation.name) },
                            leadingContent = {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPresentation = presentation }
                        )
                    }
                }
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
                            viewModel.createPresentation(trimmed)
                        }
                        showNewDialog = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDialog = false }) { Text("Cancel") }
            }
        )
    }
}

package com.bensbible.app.ui.memorize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.MemorizedVerseEntity
import com.bensbible.app.viewmodel.MemorizeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorizeScreen(
    viewModel: MemorizeViewModel,
    modifier: Modifier = Modifier
) {
    val allVerses by viewModel.allVerses.collectAsState()
    val dueVerses by viewModel.dueVerses.collectAsState()

    if (viewModel.sessionActive) {
        MemorizeSessionScreen(viewModel = viewModel, modifier = modifier)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Memorize") })
        },
        floatingActionButton = {
            if (dueVerses.isNotEmpty()) {
                FloatingActionButton(onClick = { viewModel.startSession(dueVerses) }) {
                    Icon(Icons.Default.Psychology, contentDescription = "Practice")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (allVerses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "No verses yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Add verses from the Reader to start memorizing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    StatsCard(
                        totalVerses = allVerses.size,
                        dueCount = dueVerses.size
                    )
                }
                items(allVerses, key = { it.id }) { verse ->
                    SwipeToDismissVerseItem(
                        verse = verse,
                        onDismiss = { viewModel.deleteVerse(verse) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCard(totalVerses: Int, dueCount: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$totalVerses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("verses", style = MaterialTheme.typography.labelMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$dueCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (dueCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text("due today", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissVerseItem(
    verse: MemorizedVerseEntity,
    onDismiss: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    ) {
        VerseMemoryCard(verse = verse)
    }
}

@Composable
private fun VerseMemoryCard(verse: MemorizedVerseEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = verse.reference,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IntervalBadge(verse = verse)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = verse.verseText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun IntervalBadge(verse: MemorizedVerseEntity) {
    val (label, color) = if (verse.isDue) {
        "Due" to MaterialTheme.colorScheme.error
    } else {
        "In ${verse.intervalDays}d" to Color(0xFF4CAF50)
    }
    androidx.compose.material3.SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        )
    )
}

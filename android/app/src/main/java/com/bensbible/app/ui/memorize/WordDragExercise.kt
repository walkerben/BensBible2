package com.bensbible.app.ui.memorize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.MemorizedVerseEntity

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WordDragExercise(
    verse: MemorizedVerseEntity,
    shuffledWords: List<String>,
    correctOrder: List<String>,
    onSubmit: (Int) -> Unit
) {
    val arranged = remember { mutableStateListOf<String>() }
    val bank = remember { mutableStateListOf(*shuffledWords.toTypedArray()) }
    var submitted by remember { mutableStateOf(false) }
    var rearrangeCount by remember { mutableIntStateOf(0) }

    val isCorrect = arranged.toList() == correctOrder

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = verse.reference,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Arrange the words",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Answer zone
        Text(
            text = "Your answer:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (arranged.isEmpty()) {
            Text(
                text = "Tap words below to place them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                arranged.forEachIndexed { i, word ->
                    val chipColor = when {
                        !submitted -> MaterialTheme.colorScheme.primaryContainer
                        i < correctOrder.size && word == correctOrder[i] -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                    FilterChip(
                        selected = true,
                        onClick = {
                            if (!submitted) {
                                bank.add(word)
                                arranged.removeAt(i)
                                rearrangeCount++
                            }
                        },
                        label = { Text(word) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        // Word bank
        Text(
            text = "Word bank:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            bank.forEachIndexed { i, word ->
                ElevatedFilterChip(
                    selected = false,
                    onClick = {
                        if (!submitted) {
                            arranged.add(word)
                            bank.removeAt(i)
                        }
                    },
                    label = { Text(word) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (submitted && !isCorrect) {
            Text(
                text = "Correct: ${correctOrder.joinToString(" ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!submitted && arranged.isNotEmpty()) {
                TextButton(onClick = {
                    bank.clear()
                    bank.addAll(shuffledWords)
                    arranged.clear()
                    rearrangeCount++
                }) { Text("Clear") }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (!submitted) {
                Button(
                    onClick = {
                        submitted = true
                        val quality = if (isCorrect) (if (rearrangeCount == 0) 5 else 4) else 1
                        onSubmit(quality)
                    },
                    enabled = arranged.size == correctOrder.size
                ) { Text("Check") }
            }
        }
    }
}

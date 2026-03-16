package com.bensbible.app.ui.memorize

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.MemorizedVerseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceExercise(
    verse: MemorizedVerseEntity,
    options: List<String>,
    onSubmit: (Int) -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

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
            text = "Which verse matches this reference?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { option ->
            val isCorrectOption = option == verse.verseText
            val isSelected = option == selected

            val (containerColor, borderColor, iconColor) = when {
                !submitted -> Triple(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
                isCorrectOption -> Triple(
                    Color(0xFF4CAF50).copy(alpha = 0.15f),
                    Color(0xFF4CAF50),
                    Color(0xFF4CAF50)
                )
                isSelected -> Triple(
                    MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.error
                )
                else -> Triple(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                onClick = {
                    if (!submitted) {
                        selected = option
                        submitted = true
                        val quality = if (isCorrectOption) 5 else 1
                        onSubmit(quality)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(if (submitted && (isCorrectOption || isSelected)) 2.dp else 1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = when {
                            !submitted -> Icons.Default.RadioButtonUnchecked
                            isCorrectOption -> Icons.Default.CheckCircle
                            isSelected -> Icons.Default.Cancel
                            else -> Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                    )
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

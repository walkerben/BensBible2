package com.bensbible.app.ui.memorize

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.MemorizedVerseEntity
import com.bensbible.app.model.FillBlankSegment

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FillBlankExercise(
    verse: MemorizedVerseEntity,
    segments: List<FillBlankSegment>,
    onSubmit: (Int) -> Unit
) {
    val blankIndices = segments.indices.filter { segments[it] is FillBlankSegment.Blank }
    val answers = remember { mutableStateListOf(*Array(blankIndices.size) { "" }) }
    var submitted by remember { mutableStateOf(false) }
    var hintUsed by remember { mutableStateOf(false) }

    fun isBlankCorrect(blankPos: Int): Boolean {
        val segIdx = blankIndices[blankPos]
        val answer = (segments[segIdx] as FillBlankSegment.Blank).answer
        val given = answers[blankPos].trim()
        return given.equals(answer.filter { it.isLetterOrDigit() || it == '\'' }, ignoreCase = true) ||
               given.equals(answer, ignoreCase = true)
    }

    val allCorrect = blankIndices.indices.all { isBlankCorrect(it) }
    val allFilled = answers.all { it.isNotEmpty() }

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
            text = "Fill in the blank",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        var blankCounter = 0
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            segments.forEach { segment ->
                when (segment) {
                    is FillBlankSegment.Word -> {
                        Text(
                            text = segment.text + " ",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    is FillBlankSegment.Blank -> {
                        val bIdx = blankCounter++
                        val borderColor = when {
                            !submitted -> MaterialTheme.colorScheme.outline
                            isBlankCorrect(bIdx) -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.error
                        }
                        OutlinedTextField(
                            value = answers[bIdx],
                            onValueChange = { if (!submitted) answers[bIdx] = it },
                            modifier = Modifier.width(120.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = borderColor,
                                unfocusedBorderColor = borderColor
                            )
                        )
                        Text(" ", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (submitted && !allCorrect) {
            Text(
                text = "Correct answers:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            blankIndices.forEachIndexed { bi, si ->
                val answer = (segments[si] as FillBlankSegment.Blank).answer
                Text(
                    text = "• $answer",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!submitted && !hintUsed) {
                TextButton(onClick = {
                    hintUsed = true
                    blankIndices.forEachIndexed { bi, si ->
                        val answer = (segments[si] as FillBlankSegment.Blank).answer
                        answers[bi] = answer.take(2) + "…"
                    }
                }) { Text("Hint") }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (!submitted) {
                Button(
                    onClick = {
                        submitted = true
                        val quality = if (allCorrect) (if (hintUsed) 3 else 5) else 1
                        onSubmit(quality)
                    },
                    enabled = allFilled
                ) { Text("Check") }
            }
        }
    }
}

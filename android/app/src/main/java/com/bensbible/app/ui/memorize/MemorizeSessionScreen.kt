package com.bensbible.app.ui.memorize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.bensbible.app.model.ExerciseState
import com.bensbible.app.viewmodel.MemorizeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorizeSessionScreen(
    viewModel: MemorizeViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practice") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.endSession() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.skipVerse() }) {
                        Text("Skip")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Progress bar
                val total = viewModel.sessionQueueSize.takeIf { it > 0 } ?: 1
                val progress = viewModel.sessionIndex.toFloat() / total
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                when {
                    viewModel.sessionComplete -> {
                        SessionCompleteScreen(
                            correct = viewModel.sessionCorrect,
                            total = viewModel.sessionTotal,
                            onDone = { viewModel.endSession() }
                        )
                    }
                    viewModel.currentExercise != null -> {
                        when (val exercise = viewModel.currentExercise!!) {
                            is ExerciseState.FillBlank -> FillBlankExercise(
                                verse = exercise.verse,
                                segments = exercise.segments,
                                onSubmit = { quality -> viewModel.submitAnswer(quality) }
                            )
                            is ExerciseState.WordDrag -> WordDragExercise(
                                verse = exercise.verse,
                                shuffledWords = exercise.shuffledWords,
                                correctOrder = exercise.correctOrder,
                                onSubmit = { quality -> viewModel.submitAnswer(quality) }
                            )
                            is ExerciseState.MultipleChoice -> MultipleChoiceExercise(
                                verse = exercise.verse,
                                options = exercise.options,
                                onSubmit = { quality -> viewModel.submitAnswer(quality) }
                            )
                        }
                    }
                }
            }

            // Result overlay
            AnimatedVisibility(
                visible = viewModel.showingResult,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    color = if (viewModel.lastResultWasCorrect)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (viewModel.lastResultWasCorrect) "Correct!" else "Keep practicing",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (viewModel.lastResultWasCorrect)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCompleteScreen(
    correct: Int,
    total: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text("Session Complete!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "$correct / $total correct",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (total > 0) {
            val pct = (correct.toFloat() / total * 100).toInt()
            val color = when {
                pct >= 80 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                pct >= 50 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("$pct% accuracy", style = MaterialTheme.typography.titleMedium, color = color)
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}

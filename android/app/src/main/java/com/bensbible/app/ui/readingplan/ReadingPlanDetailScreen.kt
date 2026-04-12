package com.bensbible.app.ui.readingplan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.data.MemorizeRepository
import com.bensbible.app.data.PresentationRepository
import com.bensbible.app.data.ReadingPlan
import com.bensbible.app.data.ReadingPlanDay
import com.bensbible.app.viewmodel.ReadingPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPlanDetailScreen(
    plan: ReadingPlan,
    viewModel: ReadingPlanViewModel,
    bibleDataService: BibleDataService,
    annotationRepository: AnnotationRepository,
    presentationRepository: PresentationRepository,
    memorizeRepository: MemorizeRepository,
    onBack: () -> Unit,
    onDaySelected: (ReadingPlanDay) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.progressList.collectAsState()
    val progress = progressList.find { it.planId == plan.id }
    val completedDays = progress?.completedDays ?: emptySet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Description
            item {
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Completed banner
            if (progress?.isCompleted == true) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Plan Complete!",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }

            // Start button if not started
            if (progress == null) {
                item {
                    Button(
                        onClick = { viewModel.startPlan(plan.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Start Plan")
                    }
                }
            }

            item { HorizontalDivider() }

            // Day list
            items(plan.days, key = { it.dayNumber }) { day ->
                val isComplete = day.dayNumber in completedDays
                ListItem(
                    headlineContent = { Text("Day ${day.dayNumber}") },
                    supportingContent = { Text(day.referenceText) },
                    trailingContent = {
                        if (isComplete) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Complete",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = progress != null) { onDaySelected(day) }
                )
                HorizontalDivider()
            }
        }
    }
}

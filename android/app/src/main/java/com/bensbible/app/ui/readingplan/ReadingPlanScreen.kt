package com.bensbible.app.ui.readingplan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun ReadingPlanScreen(
    viewModel: ReadingPlanViewModel,
    bibleDataService: BibleDataService,
    annotationRepository: AnnotationRepository,
    presentationRepository: PresentationRepository,
    memorizeRepository: MemorizeRepository,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.progressList.collectAsState()
    var selectedPlan by remember { mutableStateOf<ReadingPlan?>(null) }
    var selectedDay by remember { mutableStateOf<Pair<ReadingPlan, ReadingPlanDay>?>(null) }

    selectedDay?.let { (plan, day) ->
        ReadingDayScreen(
            plan = plan,
            day = day,
            viewModel = viewModel,
            bibleDataService = bibleDataService,
            annotationRepository = annotationRepository,
            presentationRepository = presentationRepository,
            memorizeRepository = memorizeRepository,
            onBack = { selectedDay = null }
        )
        return
    }

    selectedPlan?.let { plan ->
        ReadingPlanDetailScreen(
            plan = plan,
            viewModel = viewModel,
            bibleDataService = bibleDataService,
            annotationRepository = annotationRepository,
            presentationRepository = presentationRepository,
            memorizeRepository = memorizeRepository,
            onBack = { selectedPlan = null },
            onDaySelected = { day -> selectedDay = plan to day }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reading Plans") })
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            val groupedPlans = viewModel.allPlans.groupBy { it.category }
            val categoryOrder = listOf("Bible in a Year", "New Testament", "Old Testament")

            categoryOrder.forEach { category ->
                val plansInCategory = groupedPlans[category] ?: return@forEach
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(plansInCategory, key = { it.id }) { plan ->
                    val progress = progressList.find { it.planId == plan.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { selectedPlan = plan }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = plan.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (progress?.isCompleted == true) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = "Completed",
                                        tint = Color(0xFFFFD700)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = plan.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (progress != null) {
                                LinearProgressIndicator(
                                    progress = { progress.completedCount.toFloat() / plan.totalDays },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${progress.completedCount} / ${plan.totalDays} days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (progress.isCompleted) {
                                    OutlinedButton(
                                        onClick = { selectedPlan = plan },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("View") }
                                } else {
                                    Button(
                                        onClick = { selectedPlan = plan },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Continue") }
                                }
                            } else {
                                Text(
                                    text = "Not started",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { selectedPlan = plan },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Start") }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

package com.bensbible.app.ui.readingplan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.data.ReadingPlan
import com.bensbible.app.data.ReadingPlanDay
import com.bensbible.app.model.Chapter
import com.bensbible.app.viewmodel.ReadingPlanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingDayScreen(
    plan: ReadingPlan,
    day: ReadingPlanDay,
    viewModel: ReadingPlanViewModel,
    bibleDataService: BibleDataService,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.progressList.collectAsState()
    val progress = progressList.find { it.planId == plan.id }

    var isComplete by remember(day.dayNumber, progress) {
        mutableStateOf(progress?.completedDays?.contains(day.dayNumber) ?: false)
    }

    // List of (bookName, chapterNumber, Chapter)
    var chapterTexts by remember { mutableStateOf<List<Triple<String, Int, Chapter>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(day.dayNumber) {
        isLoading = true
        chapterTexts = withContext(Dispatchers.IO) {
            day.readings.map { entry ->
                val chapter = bibleDataService.loadChapter(entry.bookName, entry.chapter)
                Triple(entry.bookName, entry.chapter, chapter)
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Day ${day.dayNumber}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    chapterTexts.forEach { (bookName, chapterNum, chapter) ->
                        item(key = "$bookName-$chapterNum-header") {
                            Text(
                                text = "$bookName $chapterNum",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        items(chapter.verses, key = { "$bookName-$chapterNum-${it.verse}" }) { verse ->
                            Text(
                                text = "${verse.number}  ${verse.text}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        item(key = "$bookName-$chapterNum-divider") {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        viewModel.markDayComplete(plan.id, day.dayNumber, plan.totalDays)
                        isComplete = true
                        onBack()
                    },
                    enabled = !isComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isComplete) "Completed" else "Mark Complete")
                }
            }
        }
    }
}

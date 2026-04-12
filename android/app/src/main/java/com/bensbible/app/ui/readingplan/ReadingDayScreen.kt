package com.bensbible.app.ui.readingplan

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.data.MemorizeRepository
import com.bensbible.app.data.PresentationRepository
import com.bensbible.app.data.ReadingPlan
import com.bensbible.app.data.ReadingPlanDay
import com.bensbible.app.data.VerseAnnotationEntity
import com.bensbible.app.model.VerseID
import com.bensbible.app.model.Chapter
import com.bensbible.app.ui.memorize.AddToMemorizeSheet
import com.bensbible.app.ui.memorize.MemorizeVerseEntry
import com.bensbible.app.ui.presentations.AddToPresentationSheet
import com.bensbible.app.ui.presentations.VerseEntry
import com.bensbible.app.ui.reader.HighlightColorPicker
import com.bensbible.app.ui.reader.NoteEditorSheet
import com.bensbible.app.ui.reader.VerseActionBar
import com.bensbible.app.ui.reader.VerseRow
import com.bensbible.app.viewmodel.ReadingPlanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingDayScreen(
    plan: ReadingPlan,
    day: ReadingPlanDay,
    viewModel: ReadingPlanViewModel,
    bibleDataService: BibleDataService,
    annotationRepository: AnnotationRepository,
    presentationRepository: PresentationRepository,
    memorizeRepository: MemorizeRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val progressList by viewModel.progressList.collectAsState()
    val progress = progressList.find { it.planId == plan.id }
    var isComplete by remember(day.dayNumber, progress) {
        mutableStateOf(progress?.completedDays?.contains(day.dayNumber) ?: false)
    }

    var chapterTexts by remember { mutableStateOf<List<Triple<String, Int, Chapter>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var annotations by remember { mutableStateOf<Map<String, VerseAnnotationEntity>>(emptyMap()) }
    var selectedVerseIDs by remember { mutableStateOf(emptySet<VerseID>()) }

    // Sheet visibility states
    var isHighlightPickerPresented by remember { mutableStateOf(false) }
    var isNoteEditorPresented by remember { mutableStateOf(false) }
    var noteEditingVerseID by remember { mutableStateOf<VerseID?>(null) }
    var noteEditingText by remember { mutableStateOf("") }
    var isAddToPresentationPresented by remember { mutableStateOf(false) }
    var isAddToMemorizePresented by remember { mutableStateOf(false) }

    fun loadAnnotations() {
        scope.launch {
            val all = mutableMapOf<String, VerseAnnotationEntity>()
            day.readings.forEach { entry ->
                all.putAll(annotationRepository.fetchAnnotations(entry.bookName, entry.chapter))
            }
            annotations = all
        }
    }

    LaunchedEffect(day.dayNumber) {
        isLoading = true
        chapterTexts = withContext(Dispatchers.IO) {
            day.readings.map { entry ->
                Triple(entry.bookName, entry.chapter, bibleDataService.loadChapter(entry.bookName, entry.chapter))
            }
        }
        isLoading = false
        loadAnnotations()
    }

    fun selectedVerseEntries(): List<VerseEntry> = selectedVerseIDs.sorted().mapNotNull { id ->
        val chapter = chapterTexts.find { it.first == id.book && it.second == id.chapter }?.third
            ?: return@mapNotNull null
        val verse = chapter.verses.find { it.number == id.verse } ?: return@mapNotNull null
        VerseEntry(id.book, id.chapter, id.verse, verse.text)
    }

    fun selectedMemorizeEntries(): List<MemorizeVerseEntry> = selectedVerseIDs.sorted().mapNotNull { id ->
        val chapter = chapterTexts.find { it.first == id.book && it.second == id.chapter }?.third
            ?: return@mapNotNull null
        val verse = chapter.verses.find { it.number == id.verse } ?: return@mapNotNull null
        MemorizeVerseEntry(id.book, id.chapter, id.verse, verse.text)
    }

    fun clipboardText(): String {
        val entries = selectedVerseEntries()
        val lines = if (entries.size == 1) entries[0].text
        else entries.joinToString("\n") { "${it.verseNumber} ${it.text}" }
        return "$lines\n\n— ${VerseID.displayRange(selectedVerseIDs)}"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                ) {
                    chapterTexts.forEach { (bookName, chapterNum, chapter) ->
                        item(key = "$bookName-$chapterNum-header") {
                            Text(
                                text = "$bookName $chapterNum",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        items(chapter.verses, key = { "$bookName-$chapterNum-${it.verse}" }) { verse ->
                            val verseID = VerseID(bookName, chapterNum, verse.number)
                            VerseRow(
                                verse = verse,
                                isSelected = verseID in selectedVerseIDs,
                                annotation = annotations[verseID.key],
                                onTap = {
                                    selectedVerseIDs = if (verseID in selectedVerseIDs)
                                        selectedVerseIDs - verseID
                                    else
                                        selectedVerseIDs + verseID
                                },
                                onNoteTap = {
                                    noteEditingVerseID = verseID
                                    noteEditingText = annotations[verseID.key]?.noteText ?: ""
                                    isNoteEditorPresented = true
                                }
                            )
                        }
                        item(key = "$bookName-$chapterNum-divider") {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            // Mark Complete button — hidden while verses are selected
            AnimatedVisibility(
                visible = selectedVerseIDs.isEmpty() && !isLoading,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
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

            // Verse action bar — slides up when verses are selected
            AnimatedVisibility(
                visible = selectedVerseIDs.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                VerseActionBar(
                    selectedCount = selectedVerseIDs.size,
                    onHighlight = { isHighlightPickerPresented = true },
                    onNote = {
                        val firstID = selectedVerseIDs.minOrNull() ?: return@VerseActionBar
                        noteEditingVerseID = firstID
                        noteEditingText = annotations[firstID.key]?.noteText ?: ""
                        isNoteEditorPresented = true
                    },
                    onBookmark = {
                        val ids = selectedVerseIDs.toList()
                        scope.launch {
                            annotationRepository.toggleBookmark(ids)
                            loadAnnotations()
                        }
                        selectedVerseIDs = emptySet()
                    },
                    onAddToPresentation = { isAddToPresentationPresented = true },
                    onAddToMemorize = { isAddToMemorizePresented = true },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(clipboardText()))
                        selectedVerseIDs = emptySet()
                    },
                    onShare = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, clipboardText())
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                        selectedVerseIDs = emptySet()
                    },
                    onDeselectAll = { selectedVerseIDs = emptySet() }
                )
            }
        }
    }

    // Highlight color picker
    if (isHighlightPickerPresented) {
        ModalBottomSheet(
            onDismissRequest = { isHighlightPickerPresented = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            HighlightColorPicker(onSelect = { color ->
                val ids = selectedVerseIDs.toList()
                scope.launch {
                    annotationRepository.setHighlight(color, ids)
                    loadAnnotations()
                }
                selectedVerseIDs = emptySet()
                isHighlightPickerPresented = false
            })
        }
    }

    // Note editor
    if (isNoteEditorPresented) {
        ModalBottomSheet(
            onDismissRequest = {
                isNoteEditorPresented = false
                noteEditingVerseID = null
                noteEditingText = ""
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            NoteEditorSheet(
                verseReference = noteEditingVerseID?.displayReference ?: "",
                text = noteEditingText,
                onTextChange = { noteEditingText = it },
                onSave = {
                    val id = noteEditingVerseID ?: return@NoteEditorSheet
                    val text = noteEditingText
                    scope.launch {
                        annotationRepository.saveNote(text, id)
                        loadAnnotations()
                    }
                    isNoteEditorPresented = false
                    noteEditingVerseID = null
                    noteEditingText = ""
                    selectedVerseIDs = emptySet()
                },
                onCancel = {
                    isNoteEditorPresented = false
                    noteEditingVerseID = null
                    noteEditingText = ""
                }
            )
        }
    }

    // Add to presentation
    if (isAddToPresentationPresented) {
        ModalBottomSheet(
            onDismissRequest = { isAddToPresentationPresented = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            AddToPresentationSheet(
                verses = selectedVerseEntries(),
                repository = presentationRepository,
                onDone = {
                    isAddToPresentationPresented = false
                    selectedVerseIDs = emptySet()
                }
            )
        }
    }

    // Add to memorize
    if (isAddToMemorizePresented) {
        ModalBottomSheet(
            onDismissRequest = { isAddToMemorizePresented = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            AddToMemorizeSheet(
                verses = selectedMemorizeEntries(),
                repository = memorizeRepository,
                onDone = {
                    isAddToMemorizePresented = false
                    selectedVerseIDs = emptySet()
                }
            )
        }
    }
}

package com.bensbible.app.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bensbible.app.viewmodel.NavigationCoordinator
import com.bensbible.app.viewmodel.ReaderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    coordinator: NavigationCoordinator,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }

    // Handle cross-tab navigation
    LaunchedEffect(coordinator.pendingNavigation) {
        coordinator.pendingNavigation?.let { location ->
            viewModel.navigateTo(location.bookName, location.chapterNumber, location.verseNumber)
            coordinator.pendingNavigation = null
        }
    }

    // Handle scroll-to-verse
    LaunchedEffect(viewModel.scrollToVerseID) {
        viewModel.scrollToVerseID?.let { verseID ->
            // Small delay to let the chapter content render
            delay(100)
            val chapter = viewModel.currentChapter ?: return@let
            val index = chapter.verses.indexOfFirst { it.verse == verseID }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
            viewModel.scrollToVerseID = null
        }
    }

    // Clear highlight after delay
    LaunchedEffect(viewModel.highlightedVerseID) {
        if (viewModel.highlightedVerseID != null) {
            delay(1500)
            viewModel.highlightedVerseID = null
        }
    }

    // Scroll to top on chapter change (skip if navigating to a specific verse)
    LaunchedEffect(viewModel.currentLocation) {
        if (viewModel.scrollToVerseID == null) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TextButton(onClick = { viewModel.isPickerPresented = true }) {
                        Text(
                            text = viewModel.currentLocation.displayTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Pick chapter",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.previousChapter() },
                        enabled = viewModel.canGoPrevious
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous chapter")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.nextChapter() },
                        enabled = viewModel.canGoNext
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next chapter")
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
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = if (viewModel.hasSelection) 78.dp else 8.dp
                )
            ) {
                val chapter = viewModel.currentChapter
                if (chapter != null) {
                    items(chapter.verses, key = { it.verse }) { verse ->
                        VerseRow(
                            verse = verse,
                            isSelected = viewModel.isSelected(verse),
                            annotation = viewModel.annotation(verse),
                            isHighlighted = viewModel.highlightedVerseID == verse.verse,
                            onTap = { viewModel.toggleVerseSelection(verse) }
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = viewModel.hasSelection,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                VerseActionBar(
                    selectedCount = viewModel.selectedCount,
                    onHighlight = { viewModel.isHighlightPickerPresented = true },
                    onNote = { viewModel.beginNoteEditing() },
                    onBookmark = { viewModel.bookmarkSelectedVerses() },
                    onShare = { viewModel.isShareSheetPresented = true },
                    onDeselectAll = { viewModel.deselectAll() }
                )
            }
        }
    }

    // Book/Chapter picker
    if (viewModel.isPickerPresented) {
        BookChapterPickerDialog(
            bookNames = viewModel.bookNames,
            currentLocation = viewModel.currentLocation,
            chapterCount = { viewModel.chapterCount(it) },
            onSelect = { book, chapter ->
                viewModel.navigateTo(book, chapter)
                viewModel.isPickerPresented = false
            },
            onDismiss = { viewModel.isPickerPresented = false }
        )
    }

    // Highlight color picker
    if (viewModel.isHighlightPickerPresented) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.isHighlightPickerPresented = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            HighlightColorPicker(
                onSelect = { color ->
                    viewModel.applyHighlight(color)
                    viewModel.isHighlightPickerPresented = false
                }
            )
        }
    }

    // Note editor
    if (viewModel.isNoteEditorPresented) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.cancelNoteEditing()
                viewModel.isNoteEditorPresented = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            NoteEditorSheet(
                verseReference = viewModel.noteEditingVerseID?.displayReference ?: "",
                text = viewModel.noteEditingText,
                onTextChange = { viewModel.noteEditingText = it },
                onSave = {
                    viewModel.saveNote()
                    viewModel.isNoteEditorPresented = false
                },
                onCancel = {
                    viewModel.cancelNoteEditing()
                    viewModel.isNoteEditorPresented = false
                }
            )
        }
    }

    // Share sheet
    if (viewModel.isShareSheetPresented) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.isShareSheetPresented = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            VerseShareSheet(
                verses = viewModel.selectedVerseTexts,
                reference = viewModel.selectedVerseReference,
                onDismiss = { viewModel.isShareSheetPresented = false }
            )
        }
    }
}

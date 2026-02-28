package com.bensbible.app.ui.presentations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bensbible.app.data.PresentationEntity
import com.bensbible.app.data.PresentationRepository
import com.bensbible.app.viewmodel.PresentationDetailViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresentationDetailScreen(
    presentation: PresentationEntity,
    repository: PresentationRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { PresentationDetailViewModel(repository, presentation.id) }
    val slides by viewModel.slides.collectAsState()
    var showSlideshow by remember { mutableStateOf(false) }

    if (showSlideshow) {
        Dialog(
            onDismissRequest = { showSlideshow = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            PresentationSlideshowScreen(
                slides = slides,
                onDismiss = { showSlideshow = false }
            )
        }
    }

    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        viewModel.reorderSlide(from.index, to.index, slides)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(presentation.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSlideshow = true },
                        enabled = slides.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Present")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            itemsIndexed(slides, key = { _, slide -> slide.id }) { index, slide ->
                ReorderableItem(reorderState, key = slide.id) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteSlide(slide)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        enableDismissFromStartToEnd = false
                    ) {
                        ListItem(
                            headlineContent = { Text(slide.reference) },
                            supportingContent = {
                                Text(
                                    slide.verseText,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    modifier = Modifier.draggableHandle(),
                                    onClick = {}
                                ) {
                                    Icon(Icons.Default.DragHandle, contentDescription = "Drag to reorder")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

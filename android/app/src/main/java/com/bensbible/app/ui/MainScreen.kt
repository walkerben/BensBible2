package com.bensbible.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.model.AppTab
import com.bensbible.app.ui.bookmarks.BookmarksScreen
import com.bensbible.app.ui.notes.NotesScreen
import com.bensbible.app.ui.reader.ReaderScreen
import com.bensbible.app.viewmodel.BookmarksViewModel
import com.bensbible.app.viewmodel.NavigationCoordinator
import com.bensbible.app.viewmodel.NotesViewModel
import com.bensbible.app.viewmodel.ReaderViewModel

@Composable
fun MainScreen(
    bibleDataService: BibleDataService,
    annotationRepository: AnnotationRepository
) {
    val coordinator = remember { NavigationCoordinator() }
    val readerViewModel = remember { ReaderViewModel(bibleDataService, annotationRepository) }
    val bookmarksViewModel = remember { BookmarksViewModel(annotationRepository) }
    val notesViewModel = remember { NotesViewModel(annotationRepository) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = coordinator.selectedTab == AppTab.READ,
                    onClick = { coordinator.selectedTab = AppTab.READ },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Read") },
                    label = { Text("Read") }
                )
                NavigationBarItem(
                    selected = coordinator.selectedTab == AppTab.BOOKMARKS,
                    onClick = { coordinator.selectedTab = AppTab.BOOKMARKS },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                    label = { Text("Bookmarks") }
                )
                NavigationBarItem(
                    selected = coordinator.selectedTab == AppTab.NOTES,
                    onClick = { coordinator.selectedTab = AppTab.NOTES },
                    icon = { Icon(Icons.Default.Notes, contentDescription = "Notes") },
                    label = { Text("Notes") }
                )
            }
        }
    ) { innerPadding ->
        when (coordinator.selectedTab) {
            AppTab.READ -> ReaderScreen(
                viewModel = readerViewModel,
                coordinator = coordinator,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.BOOKMARKS -> BookmarksScreen(
                viewModel = bookmarksViewModel,
                coordinator = coordinator,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.NOTES -> NotesScreen(
                viewModel = notesViewModel,
                coordinator = coordinator,
                annotationRepository = annotationRepository,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

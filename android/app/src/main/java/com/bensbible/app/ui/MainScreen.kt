package com.bensbible.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.data.LocationPreferences
import com.bensbible.app.data.MemorizeRepository
import com.bensbible.app.data.PresentationRepository
import com.bensbible.app.model.AppTab
import com.bensbible.app.ui.bookmarks.BookmarksScreen
import com.bensbible.app.ui.memorize.MemorizeScreen
import com.bensbible.app.ui.notes.NotesScreen
import com.bensbible.app.ui.presentations.PresentationsScreen
import com.bensbible.app.ui.reader.ReaderScreen
import com.bensbible.app.ui.search.SearchScreen
import com.bensbible.app.viewmodel.BookmarksViewModel
import com.bensbible.app.viewmodel.MemorizeViewModel
import com.bensbible.app.viewmodel.NavigationCoordinator
import com.bensbible.app.viewmodel.NotesViewModel
import com.bensbible.app.viewmodel.PresentationsViewModel
import com.bensbible.app.viewmodel.ReaderViewModel
import com.bensbible.app.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    bibleDataService: BibleDataService,
    annotationRepository: AnnotationRepository,
    presentationRepository: PresentationRepository,
    memorizeRepository: MemorizeRepository,
    locationPreferences: LocationPreferences
) {
    val coordinator = remember { NavigationCoordinator() }
    val readerViewModel = remember { ReaderViewModel(bibleDataService, annotationRepository, locationPreferences) }
    val searchViewModel = remember { SearchViewModel(bibleDataService) }
    val bookmarksViewModel = remember { BookmarksViewModel(annotationRepository) }
    val notesViewModel = remember { NotesViewModel(annotationRepository) }
    val presentationsViewModel = remember { PresentationsViewModel(presentationRepository) }
    val memorizeViewModel = remember { MemorizeViewModel(memorizeRepository) }

    val overflowTabs = setOf(AppTab.PRESENT, AppTab.MEMORIZE)
    val isMoreSelected = coordinator.selectedTab in overflowTabs
    var showMoreSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState
        ) {
            NavigationDrawerItem(
                label = { Text("Present") },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Present") },
                selected = coordinator.selectedTab == AppTab.PRESENT,
                onClick = {
                    coordinator.selectedTab = AppTab.PRESENT
                    showMoreSheet = false
                }
            )
            NavigationDrawerItem(
                label = { Text("Memorize") },
                icon = { Icon(Icons.Default.Psychology, contentDescription = "Memorize") },
                selected = coordinator.selectedTab == AppTab.MEMORIZE,
                onClick = {
                    coordinator.selectedTab = AppTab.MEMORIZE
                    showMoreSheet = false
                }
            )
        }
    }

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
                    selected = coordinator.selectedTab == AppTab.SEARCH,
                    onClick = { coordinator.selectedTab = AppTab.SEARCH },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
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
                NavigationBarItem(
                    selected = isMoreSelected,
                    onClick = { showMoreSheet = true },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("More") }
                )
            }
        }
    ) { innerPadding ->
        when (coordinator.selectedTab) {
            AppTab.READ -> ReaderScreen(
                viewModel = readerViewModel,
                coordinator = coordinator,
                presentationRepository = presentationRepository,
                memorizeRepository = memorizeRepository,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.SEARCH -> SearchScreen(
                viewModel = searchViewModel,
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
            AppTab.PRESENT -> PresentationsScreen(
                viewModel = presentationsViewModel,
                repository = presentationRepository,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.MEMORIZE -> MemorizeScreen(
                viewModel = memorizeViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

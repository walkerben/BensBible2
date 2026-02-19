package com.bensbible.app.ui.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bensbible.app.model.BibleLocation
import com.bensbible.app.model.BookGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookChapterPickerDialog(
    bookNames: List<String>,
    currentLocation: BibleLocation,
    chapterCount: (String) -> Int,
    onSelect: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBook by remember { mutableStateOf<String?>(null) }
    var selectedGroup by remember { mutableStateOf(BookGroup.ALL) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(selectedBook ?: "Books")
                    },
                    navigationIcon = {
                        if (selectedBook != null) {
                            IconButton(onClick = { selectedBook = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            val book = selectedBook
            if (book != null) {
                ChapterGrid(
                    bookName = book,
                    count = chapterCount(book),
                    currentLocation = currentLocation,
                    onSelect = { chapter -> onSelect(book, chapter) },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                BookList(
                    bookNames = bookNames,
                    currentBookName = currentLocation.bookName,
                    selectedGroup = selectedGroup,
                    onGroupChange = { selectedGroup = it },
                    onSelect = { selectedBook = it },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun BookList(
    bookNames: List<String>,
    currentBookName: String,
    selectedGroup: BookGroup,
    onGroupChange: (BookGroup) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            BookGroup.searchFilters.forEach { group ->
                FilterChip(
                    selected = selectedGroup == group,
                    onClick = { onGroupChange(group) },
                    label = { Text(group.displayName) }
                )
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (selectedGroup == BookGroup.ALL) {
                BookGroup.pickerSections.forEach { group ->
                    val groupBooks = group.filterBooks(bookNames)
                    item {
                        SectionHeader(group.displayName)
                    }
                    items(groupBooks) { name ->
                        BookRow(
                            name = name,
                            isCurrent = name == currentBookName,
                            onClick = { onSelect(name) }
                        )
                    }
                }
            } else {
                items(selectedGroup.filterBooks(bookNames)) { name ->
                    BookRow(
                        name = name,
                        isCurrent = name == currentBookName,
                        onClick = { onSelect(name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun BookRow(
    name: String,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isCurrent) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Current book",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChapterGrid(
    bookName: String,
    count: Int,
    currentLocation: BibleLocation,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(16.dp)
    ) {
        items((1..count).toList()) { number ->
            val isCurrent = currentLocation.bookName == bookName &&
                    currentLocation.chapterNumber == number

            Button(
                onClick = { onSelect(number) },
                shape = RoundedCornerShape(8.dp),
                colors = if (isCurrent) {
                    ButtonDefaults.buttonColors()
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            ) {
                Text("$number")
            }
        }
    }
}

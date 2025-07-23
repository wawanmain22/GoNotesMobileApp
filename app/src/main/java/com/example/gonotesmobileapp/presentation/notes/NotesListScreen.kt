package com.example.gonotesmobileapp.presentation.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gonotesmobileapp.domain.model.Note
import com.example.gonotesmobileapp.presentation.components.NoteCard
import com.example.gonotesmobileapp.presentation.components.SearchBar
import com.example.gonotesmobileapp.presentation.components.SearchFilterChips
import com.example.gonotesmobileapp.presentation.components.NoteVisibilityFilter
import com.example.gonotesmobileapp.ui.theme.GoNotesMobileAppTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNoteClick: (String) -> Unit,
    onAddNoteClick: () -> Unit,
    onEditNote: (String) -> Unit,
    onMenuClick: () -> Unit,
    viewModel: NotesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(uiState.isRefreshing)

    // Handle infinite scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                if (lastVisibleItemIndex != null && 
                    lastVisibleItemIndex >= uiState.notes.size - 2 && 
                    uiState.pagination?.hasNext == true &&
                    !uiState.isLoadingMore) {
                    viewModel.loadMoreNotes()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Notes") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Home"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Note"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onFilterClick = viewModel::toggleFilters,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filters
            if (uiState.showFilters) {
                SearchFilterChips(
                    visibilityFilter = when {
                        uiState.showPublicOnly == true -> NoteVisibilityFilter.PUBLIC_ONLY
                        uiState.showNonPublicOnly == true -> NoteVisibilityFilter.NON_PUBLIC_ONLY
                        else -> NoteVisibilityFilter.ALL
                    },
                    onVisibilityFilterChange = { filter ->
                        when (filter) {
                            NoteVisibilityFilter.ALL -> {
                                viewModel.onShowPublicOnlyChange(null)
                            }
                            NoteVisibilityFilter.PUBLIC_ONLY -> {
                                viewModel.onShowPublicOnlyChange(true)
                            }
                            NoteVisibilityFilter.NON_PUBLIC_ONLY -> {
                                viewModel.onShowPublicOnlyChange(false)
                            }
                        }
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Content
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.loadNotes(isRefresh = true) }
            ) {
                when {
                    uiState.isLoading && uiState.notes.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.notes.isEmpty() && !uiState.isLoading -> {
                        EmptyNotesState(
                            onAddNoteClick = onAddNoteClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.notes,
                                key = { note -> note.id }
                            ) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = onNoteClick,
                                    onEdit = onEditNote,
                                    onDelete = viewModel::deleteNote
                                )
                            }

                            // Loading more indicator
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // Add bottom padding
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
}

@Composable
fun EmptyNotesState(
    onAddNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📝",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Notes Yet",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start capturing your thoughts and ideas by creating your first note.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddNoteClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Note")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesListScreenPreview() {
    GoNotesMobileAppTheme {
        // Preview would need mock data
        EmptyNotesState(onAddNoteClick = {})
    }
} 
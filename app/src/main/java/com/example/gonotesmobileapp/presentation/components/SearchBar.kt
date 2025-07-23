package com.example.gonotesmobileapp.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gonotesmobileapp.ui.theme.GoNotesMobileAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    placeholder: String = "Search notes...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            Row {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(
                    onClick = onFilterClick
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    )
}

enum class NoteVisibilityFilter {
    ALL,
    PUBLIC_ONLY,
    NON_PUBLIC_ONLY
}

@Composable
fun SearchFilterChips(
    visibilityFilter: NoteVisibilityFilter,
    onVisibilityFilterChange: (NoteVisibilityFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Filter by Visibility:",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                onClick = { 
                    onVisibilityFilterChange(
                        if (visibilityFilter == NoteVisibilityFilter.PUBLIC_ONLY) 
                            NoteVisibilityFilter.ALL 
                        else 
                            NoteVisibilityFilter.PUBLIC_ONLY
                    )
                },
                label = { Text("Public Notes Only") },
                selected = visibilityFilter == NoteVisibilityFilter.PUBLIC_ONLY
            )
            
            FilterChip(
                onClick = { 
                    onVisibilityFilterChange(
                        if (visibilityFilter == NoteVisibilityFilter.NON_PUBLIC_ONLY) 
                            NoteVisibilityFilter.ALL 
                        else 
                            NoteVisibilityFilter.NON_PUBLIC_ONLY
                    )
                },
                label = { Text("Non-Public Notes Only") },
                selected = visibilityFilter == NoteVisibilityFilter.NON_PUBLIC_ONLY
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    GoNotesMobileAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SearchBar(
                searchQuery = "",
                onSearchQueryChange = {},
                onFilterClick = {},
                placeholder = "Search notes..."
            )

            SearchBar(
                searchQuery = "meeting notes",
                onSearchQueryChange = {},
                onFilterClick = {},
                placeholder = "Search notes..."
            )

            SearchFilterChips(
                visibilityFilter = NoteVisibilityFilter.ALL,
                onVisibilityFilterChange = {}
            )
        }
    }
} 
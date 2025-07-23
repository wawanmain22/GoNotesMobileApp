package com.example.gonotesmobileapp.presentation.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonotesmobileapp.domain.model.Note
import com.example.gonotesmobileapp.domain.model.Pagination
import com.example.gonotesmobileapp.domain.repository.NotesRepository
import com.example.gonotesmobileapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val pagination: Pagination? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val searchQuery: String = "",
    val selectedTags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val showPublicOnly: Boolean? = null,
    val showNonPublicOnly: Boolean? = null,
    val showFilters: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesListUiState())
    val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private val pageSize = 10

    init {
        Log.d("NotesListViewModel", "Initializing - loading notes")
        loadNotes()
    }

    // Method to refresh data - can be called from other screens
    fun refreshNotes() {
        Log.d("NotesListViewModel", "🔄 refreshNotes called - forcing refresh from external trigger")
        loadNotes(isRefresh = true)
    }

    fun loadNotes(isRefresh: Boolean = false) {
        Log.d("NotesListViewModel", "📥 loadNotes called - isRefresh: $isRefresh")
        
        if (isRefresh) {
            currentPage = 1
            Log.d("NotesListViewModel", "🔄 Refreshing notes (page reset to 1)")
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
        } else {
            Log.d("NotesListViewModel", "📥 Loading notes (page: $currentPage)")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            val result = if (_uiState.value.searchQuery.isNotEmpty() || 
                            _uiState.value.selectedTags.isNotEmpty() || 
                            _uiState.value.showPublicOnly != null) {
                Log.d("NotesListViewModel", "🔍 Using search with filters")
                searchNotes(currentPage)
            } else {
                Log.d("NotesListViewModel", "📋 Getting regular notes - page: $currentPage, size: $pageSize")
                notesRepository.getNotes(currentPage, pageSize)
            }

            Log.d("NotesListViewModel", "📊 Notes result: $result")

            when (result) {
                is Resource.Success -> {
                    val notesPage = result.data!!
                    val newNotes = if (isRefresh) {
                        Log.d("NotesListViewModel", "🔄 Replacing notes list with fresh data")
                        notesPage.notes
                    } else {
                        Log.d("NotesListViewModel", "➕ Appending notes to existing list")
                        (_uiState.value.notes + notesPage.notes).distinctBy { it.id }
                    }

                    // Extract all unique tags from notes
                    val allTags = newNotes.flatMap { it.tags }.distinct().sorted()

                    Log.d("NotesListViewModel", "✅ Success: ${newNotes.size} notes, ${allTags.size} tags")

                    _uiState.value = _uiState.value.copy(
                        notes = newNotes,
                        pagination = notesPage.pagination,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        availableTags = allTags,
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.e("NotesListViewModel", "❌ Error loading notes: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    Log.d("NotesListViewModel", "⏳ Loading state")
                    // Already handled above
                }
            }
        }
    }

    fun loadMoreNotes() {
        val pagination = _uiState.value.pagination
        if (pagination?.hasNext == true && !_uiState.value.isLoadingMore) {
            currentPage++
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            loadNotes()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        currentPage = 1
        loadNotes(isRefresh = true)
    }

    fun onTagSelect(tag: String) {
        val selectedTags = _uiState.value.selectedTags + tag
        _uiState.value = _uiState.value.copy(selectedTags = selectedTags)
        currentPage = 1
        loadNotes(isRefresh = true)
    }

    fun onTagDeselect(tag: String) {
        val selectedTags = _uiState.value.selectedTags - tag
        _uiState.value = _uiState.value.copy(selectedTags = selectedTags)
        currentPage = 1
        loadNotes(isRefresh = true)
    }

    fun onShowPublicOnlyChange(showPublic: Boolean?) {
        _uiState.value = _uiState.value.copy(
            showPublicOnly = showPublic,
            showNonPublicOnly = if (showPublic == false) true else null
        )
        currentPage = 1
        loadNotes(isRefresh = true)
    }

    fun toggleFilters() {
        _uiState.value = _uiState.value.copy(showFilters = !_uiState.value.showFilters)
    }

    fun deleteNote(noteId: String) {
        Log.d("NotesListViewModel", "deleteNote called for ID: $noteId")
        viewModelScope.launch {
            try {
                val result = notesRepository.deleteNote(noteId)
                Log.d("NotesListViewModel", "Delete result: $result")
                
                when (result) {
                    is Resource.Success -> {
                        Log.d("NotesListViewModel", "Delete successful - removing from list")
                        // Remove the note from current list
                        val updatedNotes = _uiState.value.notes.filter { it.id != noteId }
                        _uiState.value = _uiState.value.copy(
                            notes = updatedNotes,
                            error = null
                        )
                        Log.d("NotesListViewModel", "Note removed from list. Remaining notes: ${updatedNotes.size}")
                    }
                    is Resource.Error -> {
                        Log.e("NotesListViewModel", "Delete failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {
                        Log.d("NotesListViewModel", "Delete loading...")
                        // Handle loading if needed
                    }
                }
            } catch (e: Exception) {
                Log.e("NotesListViewModel", "Exception during delete", e)
                _uiState.value = _uiState.value.copy(error = "Failed to delete note: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private suspend fun searchNotes(page: Int): Resource<com.example.gonotesmobileapp.domain.model.NotesPage> {
        return notesRepository.searchNotes(
            query = _uiState.value.searchQuery.takeIf { it.isNotEmpty() },
            tags = _uiState.value.selectedTags,
            isPublic = _uiState.value.showPublicOnly,
            page = page,
            limit = pageSize
        )
    }
} 
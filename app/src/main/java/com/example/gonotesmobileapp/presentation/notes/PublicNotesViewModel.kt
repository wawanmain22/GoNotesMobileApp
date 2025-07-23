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

data class PublicNotesUiState(
    val notes: List<Note> = emptyList(),
    val pagination: Pagination? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PublicNotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicNotesUiState())
    val uiState: StateFlow<PublicNotesUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private val pageSize = 10

    init {
        Log.d("PublicNotesViewModel", "Initializing - loading public notes")
        loadPublicNotes()
    }

    fun loadPublicNotes(isRefresh: Boolean = false) {
        Log.d("PublicNotesViewModel", "📥 loadPublicNotes called - isRefresh: $isRefresh")
        
        if (isRefresh) {
            currentPage = 1
            Log.d("PublicNotesViewModel", "🔄 Refreshing public notes (page reset to 1)")
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
        } else {
            Log.d("PublicNotesViewModel", "📥 Loading public notes (page: $currentPage)")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            Log.d("PublicNotesViewModel", "🌍 Getting public notes - page: $currentPage, size: $pageSize")
            val result = notesRepository.getPublicNotes(currentPage, pageSize)

            Log.d("PublicNotesViewModel", "📊 Public notes result: $result")

            when (result) {
                is Resource.Success -> {
                    val notesPage = result.data!!
                    val newNotes = if (isRefresh) {
                        Log.d("PublicNotesViewModel", "🔄 Replacing notes list with fresh data")
                        notesPage.notes
                    } else {
                        Log.d("PublicNotesViewModel", "➕ Appending notes to existing list")
                        _uiState.value.notes + notesPage.notes
                    }

                    Log.d("PublicNotesViewModel", "✅ Success: ${newNotes.size} public notes")

                    _uiState.value = _uiState.value.copy(
                        notes = newNotes,
                        pagination = notesPage.pagination,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.e("PublicNotesViewModel", "❌ Error loading public notes: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    Log.d("PublicNotesViewModel", "⏳ Loading state")
                    // Already handled above
                }
            }
        }
    }

    fun loadMorePublicNotes() {
        val pagination = _uiState.value.pagination
        if (pagination?.hasNext == true && !_uiState.value.isLoadingMore) {
            currentPage++
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            loadPublicNotes()
        }
    }

    fun refresh() {
        loadPublicNotes(isRefresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 
package com.example.gonotesmobileapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonotesmobileapp.data.local.TokenManager
import com.example.gonotesmobileapp.domain.model.Note
import com.example.gonotesmobileapp.domain.model.User
import com.example.gonotesmobileapp.domain.repository.NotesRepository
import com.example.gonotesmobileapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentUser: User? = null,
    val recentNotes: List<Note> = emptyList(),
    val totalNotes: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            tokenManager.currentUserFlow.collect { currentUser ->
                Log.d("HomeViewModel", "Current user updated: ${currentUser?.fullName}")
                _uiState.value = _uiState.value.copy(currentUser = currentUser)
            }
        }
    }

    fun loadRecentNotes() {
        Log.d("HomeViewModel", "Starting to load recent notes")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = notesRepository.getNotes(page = 1, limit = 5)
            Log.d("HomeViewModel", "Notes result: $result")
            
            when (result) {
                is Resource.Success -> {
                    val notes = result.data?.notes ?: emptyList()
                    val total = result.data?.pagination?.total ?: 0
                    Log.d("HomeViewModel", "Success: Got ${notes.size} notes, total: $total")
                    _uiState.value = _uiState.value.copy(
                        recentNotes = notes,
                        totalNotes = total,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.e("HomeViewModel", "Error loading notes: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    Log.d("HomeViewModel", "Loading state")
                    // Already handled above
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 
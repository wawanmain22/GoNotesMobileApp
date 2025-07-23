package com.example.gonotesmobileapp.presentation.notes.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonotesmobileapp.domain.model.Note
import com.example.gonotesmobileapp.domain.repository.NotesRepository
import com.example.gonotesmobileapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    fun loadNote(noteId: String) {
        Log.d("NoteDetailViewModel", "🔄 Loading note with ID: $noteId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = notesRepository.getNoteById(noteId)
            Log.d("NoteDetailViewModel", "📥 Note load result: $result")
            
            when (result) {
                is Resource.Success -> {
                    Log.d("NoteDetailViewModel", "✅ Note loaded successfully: ${result.data?.title}")
                    _uiState.value = _uiState.value.copy(
                        note = result.data,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    Log.e("NoteDetailViewModel", "❌ Failed to load note: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    Log.d("NoteDetailViewModel", "⏳ Loading note...")
                    // Already handled above
                }
            }
        }
    }

    fun deleteNote(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val noteId = _uiState.value.note?.id ?: return
        Log.d("NoteDetailViewModel", "🗑️ Deleting note with ID: $noteId")

        viewModelScope.launch {
            val result = notesRepository.deleteNote(noteId)
            when (result) {
                is Resource.Success -> {
                    Log.d("NoteDetailViewModel", "✅ Note deleted successfully")
                    onSuccess()
                }
                is Resource.Error -> {
                    Log.e("NoteDetailViewModel", "❌ Delete failed: ${result.message}")
                    onError(result.message ?: "Failed to delete note")
                }
                is Resource.Loading -> {
                    Log.d("NoteDetailViewModel", "⏳ Deleting note...")
                    // Handle loading if needed
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 
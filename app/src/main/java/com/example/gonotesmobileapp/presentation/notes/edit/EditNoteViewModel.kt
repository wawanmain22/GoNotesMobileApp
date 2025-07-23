package com.example.gonotesmobileapp.presentation.notes.edit

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

data class EditNoteUiState(
    val originalNote: Note? = null,
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val isPublic: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingNote: Boolean = false,
    val titleError: String? = null,
    val contentError: String? = null,
    val error: String? = null
)

@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditNoteUiState())
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingNote = true, error = null)

            val result = notesRepository.getNoteById(noteId)
            when (result) {
                is Resource.Success -> {
                    val note = result.data!!
                    _uiState.value = _uiState.value.copy(
                        originalNote = note,
                        title = note.title,
                        content = note.content,
                        tags = note.tags,
                        isPublic = note.isPublic,
                        isLoadingNote = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingNote = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = null,
            error = null
        )
    }

    fun onContentChange(content: String) {
        _uiState.value = _uiState.value.copy(
            content = content,
            contentError = null,
            error = null
        )
    }

    fun onTagInputChange(tagInput: String) {
        _uiState.value = _uiState.value.copy(tagInput = tagInput)
    }

    fun addTag() {
        val tagInput = _uiState.value.tagInput.trim()
        if (tagInput.isNotEmpty() && tagInput !in _uiState.value.tags) {
            val newTags = _uiState.value.tags + tagInput
            _uiState.value = _uiState.value.copy(
                tags = newTags,
                tagInput = ""
            )
        }
    }

    fun removeTag(tag: String) {
        val newTags = _uiState.value.tags - tag
        _uiState.value = _uiState.value.copy(tags = newTags)
    }

    fun onPublicToggle(isPublic: Boolean) {
        _uiState.value = _uiState.value.copy(isPublic = isPublic)
    }

    fun updateNote(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!validateInput()) return

        val noteId = _uiState.value.originalNote?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = notesRepository.updateNote(
                noteId = noteId,
                title = _uiState.value.title.trim(),
                content = _uiState.value.content.trim(),
                tags = _uiState.value.tags,
                isPublic = _uiState.value.isPublic
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    onError(result.message ?: "Failed to update note")
                }
                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun hasChanges(): Boolean {
        val currentState = _uiState.value
        val originalNote = currentState.originalNote ?: return false

        return currentState.title.trim() != originalNote.title ||
                currentState.content.trim() != originalNote.content ||
                currentState.tags != originalNote.tags ||
                currentState.isPublic != originalNote.isPublic
    }

    private fun validateInput(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        // Title validation
        if (currentState.title.isBlank()) {
            _uiState.value = _uiState.value.copy(titleError = "Title is required")
            isValid = false
        } else if (currentState.title.length < 3) {
            _uiState.value = _uiState.value.copy(titleError = "Title must be at least 3 characters")
            isValid = false
        }

        // Content validation
        if (currentState.content.isBlank()) {
            _uiState.value = _uiState.value.copy(contentError = "Content is required")
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 
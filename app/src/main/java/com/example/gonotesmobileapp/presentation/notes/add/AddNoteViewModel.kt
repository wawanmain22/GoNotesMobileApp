package com.example.gonotesmobileapp.presentation.notes.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonotesmobileapp.domain.repository.NotesRepository
import com.example.gonotesmobileapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddNoteUiState(
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val isPublic: Boolean = false,
    val isLoading: Boolean = false,
    val titleError: String? = null,
    val contentError: String? = null,
    val error: String? = null
)

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

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

    fun saveNote(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = notesRepository.createNote(
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
                    onError(result.message ?: "Failed to create note")
                }
                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
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
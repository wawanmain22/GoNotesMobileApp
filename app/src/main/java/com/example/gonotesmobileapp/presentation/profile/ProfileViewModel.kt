package com.example.gonotesmobileapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonotesmobileapp.domain.model.User
import com.example.gonotesmobileapp.domain.repository.UserRepository
import com.example.gonotesmobileapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val fullName: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val fullNameError: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = userRepository.getProfile()) {
                is Resource.Success -> {
                    val user = result.data!!
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        fullName = user.fullName,
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun onFullNameChange(fullName: String) {
        _uiState.value = _uiState.value.copy(
            fullName = fullName,
            fullNameError = null,
            error = null,
            successMessage = null
        )
    }

    fun updateProfile() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null, successMessage = null)

            when (val result = userRepository.updateProfile(_uiState.value.fullName.trim())) {
                is Resource.Success -> {
                    val updatedUser = result.data!!
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        fullName = updatedUser.fullName,
                        isSaving = false,
                        error = null,
                        successMessage = "Profile updated successfully!"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = result.message
                    )
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

        // Full name validation
        if (currentState.fullName.isBlank()) {
            _uiState.value = _uiState.value.copy(fullNameError = "Full name is required")
            isValid = false
        } else if (currentState.fullName.length < 2) {
            _uiState.value = _uiState.value.copy(fullNameError = "Full name must be at least 2 characters")
            isValid = false
        }

        return isValid
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
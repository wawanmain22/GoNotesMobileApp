package com.example.gonotesmobileapp.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gonotesmobileapp.data.local.TokenManager
import com.example.gonotesmobileapp.domain.repository.AuthRepository
import com.example.gonotesmobileapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        checkAuthStatus()
        startTokenMonitoring()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _isLoggedIn.value = authRepository.isLoggedIn()
            
            // Try to refresh token if logged in
            if (_isLoggedIn.value) {
                tryRefreshToken()
            }
        }
    }

    private fun startTokenMonitoring() {
        viewModelScope.launch {
            // Monitor token changes from AuthInterceptor clearing tokens
            tokenManager.tokensFlow.collectLatest { tokens ->
                if (tokens == null && _isLoggedIn.value) {
                    Log.w("AuthViewModel", "Tokens cleared, forcing logout...")
                    _isLoggedIn.value = false
                }
            }
        }
        
        // Start background token refresh monitoring
        viewModelScope.launch {
            while (true) {
                delay(30000) // Check every 30 seconds
                
                if (_isLoggedIn.value) {
                    val shouldRefresh = tokenManager.isAccessTokenNearExpiry(120) // Refresh 2 minutes before expiry
                    
                    if (shouldRefresh) {
                        Log.d("AuthViewModel", "Token near expiry, attempting background refresh...")
                        val result = authRepository.refreshToken()
                        when (result) {
                            is Resource.Success -> {
                                Log.d("AuthViewModel", "Background token refresh successful")
                                tokenManager.saveTokens(result.data!!)
                            }
                            is Resource.Error -> {
                                Log.e("AuthViewModel", "Background token refresh failed: ${result.message}")
                                logout()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun tryRefreshToken() {
        viewModelScope.launch {
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrEmpty()) {
                val result = authRepository.refreshToken()
                when (result) {
                    is Resource.Success -> {
                        Log.d("AuthViewModel", "Initial token refresh successful")
                        _isLoggedIn.value = true
                        tokenManager.saveTokens(result.data!!)
                    }
                    is Resource.Error -> {
                        Log.e("AuthViewModel", "Initial token refresh failed: ${result.message}")
                        // Refresh failed, user needs to login again
                        logout()
                    }
                    is Resource.Loading -> {
                        // Handle loading if needed
                    }
                }
            } else {
                // No refresh token available
                logout()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Performing logout...")
            authRepository.logout()
            _isLoggedIn.value = false
        }
    }

    fun onLoginSuccess() {
        Log.d("AuthViewModel", "Login successful")
        _isLoggedIn.value = true
    }
} 
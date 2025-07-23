package com.example.gonotesmobileapp.data.remote

import android.util.Log
import com.example.gonotesmobileapp.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for auth endpoints
        val url = originalRequest.url.toString()
        if (url.contains("/auth/login") || url.contains("/auth/register") || url.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            tokenManager.getAccessToken()
        }

        val authenticatedRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)
        
        // Handle 401/403 responses - clear tokens to force logout
        if (response.code == 401 || response.code == 403) {
            Log.w("AuthInterceptor", "Received ${response.code} response, clearing tokens to force logout...")
            runBlocking { tokenManager.clearTokens() }
        }

        return response
    }
} 
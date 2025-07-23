package com.example.gonotesmobileapp.data.remote

import com.example.gonotesmobileapp.data.remote.dto.ApiResponse
import com.example.gonotesmobileapp.data.remote.dto.auth.*
import com.example.gonotesmobileapp.utils.Config
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    
    @POST("${Config.AUTH_ENDPOINT}/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<RegisterResponse>>

    @POST("${Config.AUTH_ENDPOINT}/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @POST("${Config.AUTH_ENDPOINT}/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<RefreshTokenResponse>>

    @POST("${Config.AUTH_ENDPOINT}/logout")
    suspend fun logout(
        @Body request: LogoutRequest
    ): Response<ApiResponse<Unit>>
} 
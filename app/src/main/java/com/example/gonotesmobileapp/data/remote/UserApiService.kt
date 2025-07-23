package com.example.gonotesmobileapp.data.remote

import com.example.gonotesmobileapp.data.remote.dto.ApiResponse
import com.example.gonotesmobileapp.data.remote.dto.user.UpdateProfileRequest
import com.example.gonotesmobileapp.data.remote.dto.user.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/v1/user/profile")
    suspend fun getProfile(): Response<ApiResponse<UserProfileResponse>>
    
    @PUT("api/v1/user/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserProfileResponse>>
}
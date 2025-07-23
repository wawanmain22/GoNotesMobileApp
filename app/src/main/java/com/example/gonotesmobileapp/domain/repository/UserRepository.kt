package com.example.gonotesmobileapp.domain.repository

import com.example.gonotesmobileapp.domain.model.User
import com.example.gonotesmobileapp.utils.Resource

interface UserRepository {
    suspend fun getProfile(): Resource<User>
    suspend fun updateProfile(fullName: String): Resource<User>
}
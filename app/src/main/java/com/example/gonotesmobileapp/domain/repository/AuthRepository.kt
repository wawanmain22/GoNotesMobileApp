package com.example.gonotesmobileapp.domain.repository

import com.example.gonotesmobileapp.domain.model.AuthTokens
import com.example.gonotesmobileapp.domain.model.Session
import com.example.gonotesmobileapp.domain.model.User
import com.example.gonotesmobileapp.utils.Resource

interface AuthRepository {
    suspend fun register(fullName: String, email: String, password: String): Resource<User>
    suspend fun login(email: String, password: String): Resource<Session>
    suspend fun refreshToken(): Resource<AuthTokens>
    suspend fun logout(): Resource<Unit>
    fun isLoggedIn(): Boolean
} 
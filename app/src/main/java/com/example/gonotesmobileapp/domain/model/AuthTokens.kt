package com.example.gonotesmobileapp.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int, // dalam detik (900 = 15 menit)
    val issuedAt: Long = System.currentTimeMillis() // timestamp when token was issued
) {
    fun isAccessTokenExpired(): Boolean {
        val expirationTime = issuedAt + (expiresIn * 1000L)
        return System.currentTimeMillis() >= expirationTime
    }
    
    fun isAccessTokenNearExpiry(bufferSeconds: Int = 60): Boolean {
        val expirationTime = issuedAt + (expiresIn * 1000L) - (bufferSeconds * 1000L)
        return System.currentTimeMillis() >= expirationTime
    }
} 
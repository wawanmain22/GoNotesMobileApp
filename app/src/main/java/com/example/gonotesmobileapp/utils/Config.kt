package com.example.gonotesmobileapp.utils

import com.example.gonotesmobileapp.BuildConfig

object Config {
    const val BASE_URL = BuildConfig.BASE_URL
    
    const val DEBUG = true
    const val CONNECTION_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    
    // API Endpoints
    const val API_VERSION = "api/v1"
    const val AUTH_ENDPOINT = "$API_VERSION/auth"
    const val USER_ENDPOINT = "$API_VERSION/user"
    const val NOTES_ENDPOINT = "$API_VERSION/notes"
    
    // Token Configuration
    const val ACCESS_TOKEN_KEY = "access_token"
    const val REFRESH_TOKEN_KEY = "refresh_token"
    const val TOKEN_EXPIRES_IN_KEY = "token_expires_in"
} 
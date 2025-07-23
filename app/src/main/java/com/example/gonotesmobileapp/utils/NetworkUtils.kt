package com.example.gonotesmobileapp.utils

import org.json.JSONObject
import retrofit2.Response

object NetworkUtils {
    
    fun <T> parseApiError(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val jsonObject = JSONObject(errorBody)
                jsonObject.optString("message", "Unknown error occurred")
            } else {
                "Unknown error occurred"
            }
        } catch (e: Exception) {
            "Unknown error occurred"
        }
    }
    
    fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is java.net.UnknownHostException ||
                throwable is java.net.SocketTimeoutException ||
                throwable is java.net.ConnectException
    }
    
    fun getErrorMessage(throwable: Throwable): String {
        return when {
            isNetworkError(throwable) -> "Please check your internet connection"
            throwable is retrofit2.HttpException -> {
                when (throwable.code()) {
                    400 -> "Bad request"
                    401 -> "Unauthorized access"
                    403 -> "Access forbidden"
                    404 -> "Resource not found"
                    500 -> "Server error"
                    else -> "Network error: ${throwable.message}"
                }
            }
            else -> throwable.message ?: "Unknown error occurred"
        }
    }
} 
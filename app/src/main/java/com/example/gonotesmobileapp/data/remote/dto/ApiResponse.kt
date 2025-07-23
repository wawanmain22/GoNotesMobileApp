package com.example.gonotesmobileapp.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status")
    val status: String,
    @Json(name = "code")
    val code: Int,
    @Json(name = "message")
    val message: String,
    @Json(name = "data")
    val data: T? = null,
    @Json(name = "error")
    val error: String? = null
) 
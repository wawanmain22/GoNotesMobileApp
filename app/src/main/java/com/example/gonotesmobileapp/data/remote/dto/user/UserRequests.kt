package com.example.gonotesmobileapp.data.remote.dto.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "email")
    val email: String,
    @Json(name = "full_name")
    val fullName: String
)
package com.example.gonotesmobileapp.data.remote.dto.notes

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotesResponse(
    @Json(name = "notes")
    val notes: List<NoteDto>,
    @Json(name = "total")
    val total: Int,
    @Json(name = "page")
    val page: Int,
    @Json(name = "page_size")
    val pageSize: Int,
    @Json(name = "total_pages")
    val totalPages: Int,
    @Json(name = "has_next")
    val hasNext: Boolean,
    @Json(name = "has_prev")
    val hasPrev: Boolean
)

@JsonClass(generateAdapter = true)
data class NoteDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "content")
    val content: String? = null,
    @Json(name = "preview")
    val preview: String? = null,
    @Json(name = "status")
    val status: String? = null,
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    @Json(name = "is_public")
    val isPublic: Boolean = false,
    @Json(name = "view_count")
    val viewCount: Int = 0,
    @Json(name = "user_id")
    val userId: String? = null,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "updated_at")
    val updatedAt: String
) {
    fun getContentText(): String = content ?: preview ?: ""
} 
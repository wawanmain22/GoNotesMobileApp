package com.example.gonotesmobileapp.data.remote.dto.notes

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateNoteRequest(
    @Json(name = "title")
    val title: String,
    @Json(name = "content")
    val content: String,
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    @Json(name = "is_public")
    val isPublic: Boolean = false
)

@JsonClass(generateAdapter = true)
data class UpdateNoteRequest(
    @Json(name = "title")
    val title: String,
    @Json(name = "content")
    val content: String,
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    @Json(name = "is_public")
    val isPublic: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SearchNotesRequest(
    @Json(name = "query")
    val query: String? = null,
    @Json(name = "tags")
    val tags: List<String> = emptyList(),
    @Json(name = "is_public")
    val isPublic: Boolean? = null,
    @Json(name = "page")
    val page: Int = 1,
    @Json(name = "page_size")
    val pageSize: Int = 20
) 
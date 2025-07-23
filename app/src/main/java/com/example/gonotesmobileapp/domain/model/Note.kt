package com.example.gonotesmobileapp.domain.model

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = false,
    val userId: String,
    val createdAt: String,
    val updatedAt: String
) 
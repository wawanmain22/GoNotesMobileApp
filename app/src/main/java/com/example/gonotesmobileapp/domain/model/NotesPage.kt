package com.example.gonotesmobileapp.domain.model

data class NotesPage(
    val notes: List<Note>,
    val pagination: Pagination
) 
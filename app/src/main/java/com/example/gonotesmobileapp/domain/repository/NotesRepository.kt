package com.example.gonotesmobileapp.domain.repository

import com.example.gonotesmobileapp.domain.model.Note
import com.example.gonotesmobileapp.domain.model.NotesPage
import com.example.gonotesmobileapp.utils.Resource

interface NotesRepository {
    suspend fun getNotes(page: Int = 1, limit: Int = 10): Resource<NotesPage>
    suspend fun createNote(
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        isPublic: Boolean = false
    ): Resource<Note>
    suspend fun getNoteById(noteId: String): Resource<Note>
    suspend fun updateNote(
        noteId: String,
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        isPublic: Boolean = false
    ): Resource<Note>
    suspend fun deleteNote(noteId: String): Resource<Unit>
    suspend fun searchNotes(
        query: String? = null,
        tags: List<String> = emptyList(),
        isPublic: Boolean? = null,
        page: Int = 1,
        limit: Int = 10
    ): Resource<NotesPage>
    suspend fun getPublicNotes(page: Int = 1, limit: Int = 10): Resource<NotesPage>
} 
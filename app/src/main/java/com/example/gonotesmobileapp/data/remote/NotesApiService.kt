package com.example.gonotesmobileapp.data.remote

import com.example.gonotesmobileapp.data.remote.dto.ApiResponse
import com.example.gonotesmobileapp.data.remote.dto.notes.*
import com.example.gonotesmobileapp.utils.Config
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

interface NotesApiService {
    
    @GET("${Config.NOTES_ENDPOINT}")
    suspend fun getNotes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<NotesResponse>>

    @POST("${Config.NOTES_ENDPOINT}")
    suspend fun createNote(
        @Body request: CreateNoteRequest
    ): Response<ApiResponse<NoteDto>>

    @GET("${Config.NOTES_ENDPOINT}/{note_id}")
    suspend fun getNoteById(
        @Path("note_id") noteId: String
    ): Response<ApiResponse<NoteDto>>

    @PUT("${Config.NOTES_ENDPOINT}/{note_id}")
    suspend fun updateNote(
        @Path("note_id") noteId: String,
        @Body request: UpdateNoteRequest
    ): Response<ApiResponse<NoteDto>>

    @DELETE("${Config.NOTES_ENDPOINT}/{note_id}")
    suspend fun deleteNote(
        @Path("note_id") noteId: String
    ): Response<ResponseBody>

    @POST("${Config.NOTES_ENDPOINT}/search")
    suspend fun searchNotes(
        @Body request: SearchNotesRequest
    ): Response<ApiResponse<NotesResponse>>

    @GET("${Config.NOTES_ENDPOINT}/public")
    suspend fun getPublicNotes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<NotesResponse>>
} 
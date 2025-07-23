package com.example.gonotesmobileapp.data.repository

import android.util.Log
import com.example.gonotesmobileapp.data.remote.NotesApiService
import com.example.gonotesmobileapp.data.remote.dto.notes.CreateNoteRequest
import com.example.gonotesmobileapp.data.remote.dto.notes.SearchNotesRequest
import com.example.gonotesmobileapp.data.remote.dto.notes.UpdateNoteRequest
import com.example.gonotesmobileapp.domain.model.Note
import com.example.gonotesmobileapp.domain.model.NotesPage
import com.example.gonotesmobileapp.domain.model.Pagination
import com.example.gonotesmobileapp.domain.repository.NotesRepository
import com.example.gonotesmobileapp.utils.NetworkUtils
import com.example.gonotesmobileapp.utils.Resource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepositoryImpl @Inject constructor(
    private val notesApiService: NotesApiService
) : NotesRepository {

    override suspend fun getNotes(page: Int, limit: Int): Resource<NotesPage> {
        return try {
            Log.d("NotesRepository", "Getting notes - page: $page, limit: $limit")
            val response = notesApiService.getNotes(page, limit)
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d("NotesRepository", "API Response: $apiResponse")
                
                val notesResponse = apiResponse?.data
                if (notesResponse != null) {
                    Log.d("NotesRepository", "Notes count: ${notesResponse.notes.size}")
                    
                    val notesPage = NotesPage(
                        notes = notesResponse.notes.map { dto ->
                            Log.d("NotesRepository", "Mapping note: ${dto.id} - ${dto.title}")
                            Note(
                                id = dto.id,
                                title = dto.title,
                                content = dto.getContentText(),
                                tags = dto.tags,
                                isPublic = dto.isPublic,
                                userId = dto.userId ?: "",
                                createdAt = dto.createdAt,
                                updatedAt = dto.updatedAt
                            )
                        },
                        pagination = Pagination(
                            page = notesResponse.page,
                            limit = notesResponse.pageSize,
                            total = notesResponse.total,
                            totalPages = notesResponse.totalPages,
                            hasNext = notesResponse.hasNext,
                            hasPrev = notesResponse.hasPrev
                        )
                    )
                    
                    Log.d("NotesRepository", "Successfully mapped ${notesPage.notes.size} notes")
                    Resource.Success(notesPage)
                } else {
                    Log.e("NotesRepository", "Notes response data is null")
                    Resource.Error("Invalid response format")
                }
            } else {
                val errorMessage = NetworkUtils.parseApiError(response)
                Log.e("NotesRepository", "API error: $errorMessage")
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            Log.e("NotesRepository", "HTTP exception", e)
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: IOException) {
            Log.e("NotesRepository", "IO exception", e)
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: Exception) {
            Log.e("NotesRepository", "Unexpected exception", e)
            Resource.Error(NetworkUtils.getErrorMessage(e))
        }
    }

    override suspend fun createNote(
        title: String,
        content: String,
        tags: List<String>,
        isPublic: Boolean
    ): Resource<Note> {
        return try {
            val request = CreateNoteRequest(title, content, tags, isPublic)
            val response = notesApiService.createNote(request)
            if (response.isSuccessful) {
                val noteDto = response.body()?.data
                if (noteDto != null) {
                    val note = Note(
                        id = noteDto.id,
                        title = noteDto.title,
                        content = noteDto.getContentText(),
                        tags = noteDto.tags,
                        isPublic = noteDto.isPublic,
                        userId = noteDto.userId ?: "",
                        createdAt = noteDto.createdAt,
                        updatedAt = noteDto.updatedAt
                    )
                    Resource.Success(note)
                } else {
                    Resource.Error("Invalid response format")
                }
            } else {
                val errorMessage = NetworkUtils.parseApiError(response)
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: IOException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: Exception) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        }
    }

    override suspend fun getNoteById(noteId: String): Resource<Note> {
        return try {
            val response = notesApiService.getNoteById(noteId)
            if (response.isSuccessful) {
                val noteDto = response.body()?.data
                if (noteDto != null) {
                    val note = Note(
                        id = noteDto.id,
                        title = noteDto.title,
                        content = noteDto.getContentText(),
                        tags = noteDto.tags,
                        isPublic = noteDto.isPublic,
                        userId = noteDto.userId ?: "",
                        createdAt = noteDto.createdAt,
                        updatedAt = noteDto.updatedAt
                    )
                    Resource.Success(note)
                } else {
                    Resource.Error("Invalid response format")
                }
            } else {
                val errorMessage = NetworkUtils.parseApiError(response)
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: IOException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: Exception) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        }
    }

    override suspend fun updateNote(
        noteId: String,
        title: String,
        content: String,
        tags: List<String>,
        isPublic: Boolean
    ): Resource<Note> {
        return try {
            val request = UpdateNoteRequest(title, content, tags, isPublic)
            val response = notesApiService.updateNote(noteId, request)
            if (response.isSuccessful) {
                val noteDto = response.body()?.data
                if (noteDto != null) {
                    val note = Note(
                        id = noteDto.id,
                        title = noteDto.title,
                        content = noteDto.getContentText(),
                        tags = noteDto.tags,
                        isPublic = noteDto.isPublic,
                        userId = noteDto.userId ?: "",
                        createdAt = noteDto.createdAt,
                        updatedAt = noteDto.updatedAt
                    )
                    Resource.Success(note)
                } else {
                    Resource.Error("Invalid response format")
                }
            } else {
                val errorMessage = NetworkUtils.parseApiError(response)
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: IOException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: Exception) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        }
    }

    override suspend fun deleteNote(noteId: String): Resource<Unit> {
        return try {
            Log.d("NotesRepository", "Deleting note with ID: $noteId")
            val response = notesApiService.deleteNote(noteId)
            Log.d("NotesRepository", "Delete response: code=${response.code()}, success=${response.isSuccessful}")
            
            if (response.isSuccessful) {
                Log.d("NotesRepository", "Note deleted successfully")
                Resource.Success(Unit)
            } else {
                // Parse error from response body if available
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Delete failed"
                } catch (e: Exception) {
                    "Delete failed"
                }
                Log.e("NotesRepository", "Delete failed: $errorMessage")
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            Log.e("NotesRepository", "HTTP exception during delete", e)
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: IOException) {
            Log.e("NotesRepository", "IO exception during delete", e)
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: Exception) {
            Log.e("NotesRepository", "Unexpected exception during delete", e)
            Resource.Error(NetworkUtils.getErrorMessage(e))
        }
    }

    override suspend fun searchNotes(
        query: String?,
        tags: List<String>,
        isPublic: Boolean?,
        page: Int,
        limit: Int
    ): Resource<NotesPage> {
        return try {
            val request = SearchNotesRequest(
                query = query,
                tags = tags, 
                isPublic = isPublic,
                page = page,
                pageSize = limit
            )
            val response = notesApiService.searchNotes(request)
            if (response.isSuccessful) {
                val notesResponse = response.body()?.data
                if (notesResponse != null) {
                    val notesPage = NotesPage(
                        notes = notesResponse.notes.map { dto ->
                            Note(
                                id = dto.id,
                                title = dto.title,
                                content = dto.getContentText(),
                                tags = dto.tags,
                                isPublic = dto.isPublic,
                                userId = dto.userId ?: "",
                                createdAt = dto.createdAt,
                                updatedAt = dto.updatedAt
                            )
                        },
                        pagination = Pagination(
                            page = notesResponse.page,
                            limit = notesResponse.pageSize,
                            total = notesResponse.total,
                            totalPages = notesResponse.totalPages,
                            hasNext = notesResponse.hasNext,
                            hasPrev = notesResponse.hasPrev
                        )
                    )
                    Resource.Success(notesPage)
                } else {
                    Resource.Error("Invalid response format")
                }
            } else {
                val errorMessage = NetworkUtils.parseApiError(response)
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: IOException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: Exception) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        }
    }

    override suspend fun getPublicNotes(page: Int, limit: Int): Resource<NotesPage> {
        return try {
            val response = notesApiService.getPublicNotes(page, limit)
            if (response.isSuccessful) {
                val notesResponse = response.body()?.data
                if (notesResponse != null) {
                    val notesPage = NotesPage(
                        notes = notesResponse.notes.map { dto ->
                            Note(
                                id = dto.id,
                                title = dto.title,
                                content = dto.getContentText(),
                                tags = dto.tags,
                                isPublic = dto.isPublic,
                                userId = dto.userId ?: "",
                                createdAt = dto.createdAt,
                                updatedAt = dto.updatedAt
                            )
                        },
                        pagination = Pagination(
                            page = notesResponse.page,
                            limit = notesResponse.pageSize,
                            total = notesResponse.total,
                            totalPages = notesResponse.totalPages,
                            hasNext = notesResponse.hasNext,
                            hasPrev = notesResponse.hasPrev
                        )
                    )
                    Resource.Success(notesPage)
                } else {
                    Resource.Error("Invalid response format")
                }
            } else {
                val errorMessage = NetworkUtils.parseApiError(response)
                Resource.Error(errorMessage)
            }
        } catch (e: HttpException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: IOException) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        } catch (e: Exception) {
            Resource.Error(NetworkUtils.getErrorMessage(e))
        }
    }
} 
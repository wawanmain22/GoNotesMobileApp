package com.example.gonotesmobileapp.data.repository

import com.example.gonotesmobileapp.data.local.TokenManager
import com.example.gonotesmobileapp.data.remote.UserApiService
import com.example.gonotesmobileapp.data.remote.dto.user.UpdateProfileRequest
import com.example.gonotesmobileapp.domain.model.User
import com.example.gonotesmobileapp.domain.repository.UserRepository
import com.example.gonotesmobileapp.utils.NetworkUtils
import com.example.gonotesmobileapp.utils.Resource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager
) : UserRepository {

    override suspend fun getProfile(): Resource<User> {
        return try {
            val response = userApiService.getProfile()
            
            if (response.isSuccessful) {
                val profileResponse = response.body()?.data
                if (profileResponse != null) {
                    val user = User(
                        id = profileResponse.id,
                        email = profileResponse.email,
                        fullName = profileResponse.fullName,
                        createdAt = profileResponse.createdAt,
                        updatedAt = profileResponse.updatedAt
                    )
                    Resource.Success(user)
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

    override suspend fun updateProfile(fullName: String): Resource<User> {
        return try {
            // Get current user to get email
            val currentUser = tokenManager.getCurrentUser()
            if (currentUser == null) {
                return Resource.Error("User not found")
            }

            val request = UpdateProfileRequest(
                email = currentUser.email,
                fullName = fullName
            )
            
            val response = userApiService.updateProfile(request)
            
            if (response.isSuccessful) {
                val profileResponse = response.body()?.data
                if (profileResponse != null) {
                    val updatedUser = User(
                        id = profileResponse.id,
                        email = profileResponse.email,
                        fullName = profileResponse.fullName,
                        createdAt = profileResponse.createdAt,
                        updatedAt = profileResponse.updatedAt
                    )
                    
                    // Update user in token manager
                    val currentSession = tokenManager.getCurrentSession()
                    if (currentSession != null) {
                        val updatedSession = currentSession.copy(user = updatedUser)
                        tokenManager.saveSession(updatedSession)
                    }
                    
                    Resource.Success(updatedUser)
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
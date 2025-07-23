package com.example.gonotesmobileapp.data.repository

import com.example.gonotesmobileapp.data.local.TokenManager
import com.example.gonotesmobileapp.data.remote.AuthApiService
import com.example.gonotesmobileapp.data.remote.dto.auth.LoginRequest
import com.example.gonotesmobileapp.data.remote.dto.auth.LogoutRequest
import com.example.gonotesmobileapp.data.remote.dto.auth.RefreshTokenRequest
import com.example.gonotesmobileapp.data.remote.dto.auth.RegisterRequest
import com.example.gonotesmobileapp.domain.model.AuthTokens
import com.example.gonotesmobileapp.domain.model.Session
import com.example.gonotesmobileapp.domain.model.User
import com.example.gonotesmobileapp.domain.repository.AuthRepository
import com.example.gonotesmobileapp.utils.NetworkUtils
import com.example.gonotesmobileapp.utils.Resource
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(fullName: String, email: String, password: String): Resource<User> {
        return try {
            val request = RegisterRequest(email, password, fullName)
            val response = authApiService.register(request)
            
            if (response.isSuccessful) {
                val registerResponse = response.body()?.data
                if (registerResponse != null) {
                    val user = User(
                        id = registerResponse.id,
                        email = registerResponse.email,
                        fullName = registerResponse.fullName,
                        createdAt = registerResponse.createdAt,
                        updatedAt = registerResponse.updatedAt
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

    override suspend fun login(email: String, password: String): Resource<Session> {
        return try {
            val request = LoginRequest(email, password)
            val response = authApiService.login(request)
            
            if (response.isSuccessful) {
                val loginResponse = response.body()?.data
                if (loginResponse != null) {
                    val user = User(
                        id = loginResponse.user.id,
                        email = loginResponse.user.email,
                        fullName = loginResponse.user.fullName,
                        createdAt = loginResponse.user.createdAt,
                        updatedAt = loginResponse.user.updatedAt
                    )
                    
                    val authTokens = AuthTokens(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        expiresIn = loginResponse.expiresIn
                    )
                    
                    val session = Session(
                        user = user,
                        authTokens = authTokens
                    )
                    
                    // Save complete session (tokens + user info)
                    tokenManager.saveSession(session)
                    
                    Resource.Success(session)
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

    override suspend fun refreshToken(): Resource<AuthTokens> {
        return try {
            val currentRefreshToken = tokenManager.getRefreshToken()
            if (currentRefreshToken.isNullOrEmpty()) {
                return Resource.Error("No refresh token available")
            }

            val request = RefreshTokenRequest(currentRefreshToken)
            val response = authApiService.refreshToken(request)
            
            if (response.isSuccessful) {
                val refreshResponse = response.body()?.data
                if (refreshResponse != null) {
                    val authTokens = AuthTokens(
                        accessToken = refreshResponse.accessToken,
                        refreshToken = refreshResponse.refreshToken,
                        expiresIn = refreshResponse.expiresIn
                    )
                    
                    // Save new tokens
                    tokenManager.saveTokens(authTokens)
                    
                    Resource.Success(authTokens)
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

    override suspend fun logout(): Resource<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrEmpty()) {
                val request = LogoutRequest(refreshToken)
                val response = authApiService.logout(request)
                if (response.isSuccessful) {
                    tokenManager.clearTokens()
                    Resource.Success(Unit)
                } else {
                    // Even if logout fails on server, clear local tokens
                    tokenManager.clearTokens()
                    Resource.Success(Unit)
                }
            } else {
                // No refresh token, just clear local storage
                tokenManager.clearTokens()
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            // Even if logout fails, clear local tokens
            tokenManager.clearTokens()
            Resource.Success(Unit)
        }
    }

    override fun isLoggedIn(): Boolean {
        return runBlocking {
            val token = tokenManager.getAccessToken()
            !token.isNullOrEmpty()
        }
    }
} 
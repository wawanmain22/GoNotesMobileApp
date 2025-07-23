package com.example.gonotesmobileapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gonotesmobileapp.domain.model.AuthTokens
import com.example.gonotesmobileapp.domain.model.User
import com.example.gonotesmobileapp.domain.model.Session
import com.example.gonotesmobileapp.utils.Config
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessTokenKey = stringPreferencesKey(Config.ACCESS_TOKEN_KEY)
    private val refreshTokenKey = stringPreferencesKey(Config.REFRESH_TOKEN_KEY)
    private val expiresInKey = intPreferencesKey(Config.TOKEN_EXPIRES_IN_KEY)
    private val issuedAtKey = stringPreferencesKey("token_issued_at")
    
    // User info keys
    private val userIdKey = stringPreferencesKey("user_id")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userFullNameKey = stringPreferencesKey("user_full_name")
    private val userCreatedAtKey = stringPreferencesKey("user_created_at")
    private val userUpdatedAtKey = stringPreferencesKey("user_updated_at")

    suspend fun saveSession(session: Session) {
        context.dataStore.edit { preferences ->
            // Save tokens
            preferences[accessTokenKey] = session.authTokens.accessToken
            preferences[refreshTokenKey] = session.authTokens.refreshToken
            preferences[expiresInKey] = session.authTokens.expiresIn
            preferences[issuedAtKey] = session.authTokens.issuedAt.toString()
            
            // Save user info
            preferences[userIdKey] = session.user.id
            preferences[userEmailKey] = session.user.email
            preferences[userFullNameKey] = session.user.fullName
            preferences[userCreatedAtKey] = session.user.createdAt
            preferences[userUpdatedAtKey] = session.user.updatedAt
        }
    }

    suspend fun saveTokens(tokens: AuthTokens) {
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = tokens.accessToken
            preferences[refreshTokenKey] = tokens.refreshToken
            preferences[expiresInKey] = tokens.expiresIn
            preferences[issuedAtKey] = tokens.issuedAt.toString()
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[accessTokenKey]
        }.first()
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[refreshTokenKey]
        }.first()
    }

    suspend fun getTokens(): AuthTokens? {
        return context.dataStore.data.map { preferences ->
            val accessToken = preferences[accessTokenKey]
            val refreshToken = preferences[refreshTokenKey]
            val expiresIn = preferences[expiresInKey]
            
            if (accessToken != null && refreshToken != null && expiresIn != null) {
                AuthTokens(accessToken, refreshToken, expiresIn)
            } else {
                null
            }
        }.first()
    }

    suspend fun getCurrentUser(): User? {
        return context.dataStore.data.map { preferences ->
            val userId = preferences[userIdKey]
            val userEmail = preferences[userEmailKey]
            val userFullName = preferences[userFullNameKey]
            val userCreatedAt = preferences[userCreatedAtKey]
            val userUpdatedAt = preferences[userUpdatedAtKey]
            
            if (userId != null && userEmail != null && userFullName != null && 
                userCreatedAt != null && userUpdatedAt != null) {
                User(
                    id = userId,
                    email = userEmail,
                    fullName = userFullName,
                    createdAt = userCreatedAt,
                    updatedAt = userUpdatedAt
                )
            } else {
                null
            }
        }.first()
    }

    suspend fun getCurrentSession(): Session? {
        return context.dataStore.data.map { preferences ->
            val tokens = getTokensFromPreferences(preferences)
            val user = getUserFromPreferences(preferences)
            
            if (tokens != null && user != null) {
                Session(user = user, authTokens = tokens)
            } else {
                null
            }
        }.first()
    }

    private fun getTokensFromPreferences(preferences: Preferences): AuthTokens? {
        val accessToken = preferences[accessTokenKey]
        val refreshToken = preferences[refreshTokenKey]
        val expiresIn = preferences[expiresInKey]
        val issuedAt = preferences[issuedAtKey]?.toLongOrNull()
        
        return if (accessToken != null && refreshToken != null && expiresIn != null && issuedAt != null) {
            AuthTokens(accessToken, refreshToken, expiresIn, issuedAt)
        } else {
            null
        }
    }

    private fun getUserFromPreferences(preferences: Preferences): User? {
        val userId = preferences[userIdKey]
        val userEmail = preferences[userEmailKey]
        val userFullName = preferences[userFullNameKey]
        val userCreatedAt = preferences[userCreatedAtKey]
        val userUpdatedAt = preferences[userUpdatedAtKey]
        
        return if (userId != null && userEmail != null && userFullName != null && 
                   userCreatedAt != null && userUpdatedAt != null) {
            User(
                id = userId,
                email = userEmail,
                fullName = userFullName,
                createdAt = userCreatedAt,
                updatedAt = userUpdatedAt
            )
        } else {
            null
        }
    }

    val tokensFlow: Flow<AuthTokens?> = context.dataStore.data.map { preferences ->
        getTokensFromPreferences(preferences)
    }

    val currentUserFlow: Flow<User?> = context.dataStore.data.map { preferences ->
        getUserFromPreferences(preferences)
    }

    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
            preferences.remove(refreshTokenKey)
            preferences.remove(expiresInKey)
            preferences.remove(issuedAtKey)
            preferences.remove(userIdKey)
            preferences.remove(userEmailKey)
            preferences.remove(userFullNameKey)
            preferences.remove(userCreatedAtKey)
            preferences.remove(userUpdatedAtKey)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
    
    suspend fun isAccessTokenExpired(): Boolean {
        val tokens = getTokens()
        return tokens?.isAccessTokenExpired() ?: true
    }
    
    suspend fun isAccessTokenNearExpiry(bufferSeconds: Int = 60): Boolean {
        val tokens = getTokens()
        return tokens?.isAccessTokenNearExpiry(bufferSeconds) ?: true
    }
} 
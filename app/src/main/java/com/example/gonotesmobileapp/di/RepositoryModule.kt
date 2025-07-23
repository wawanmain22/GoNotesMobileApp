package com.example.gonotesmobileapp.di

import com.example.gonotesmobileapp.data.repository.AuthRepositoryImpl
import com.example.gonotesmobileapp.data.repository.NotesRepositoryImpl
import com.example.gonotesmobileapp.data.repository.UserRepositoryImpl
import com.example.gonotesmobileapp.domain.repository.AuthRepository
import com.example.gonotesmobileapp.domain.repository.NotesRepository
import com.example.gonotesmobileapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        notesRepositoryImpl: NotesRepositoryImpl
    ): NotesRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
} 
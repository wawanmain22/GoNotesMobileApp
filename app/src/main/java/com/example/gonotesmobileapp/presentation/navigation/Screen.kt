package com.example.gonotesmobileapp.presentation.navigation

sealed class Screen(val route: String) {
    // Authentication
    object Login : Screen("login")
    object Register : Screen("register")
    
    // Main App
    object Home : Screen("home")
    
    // Notes
    object NotesList : Screen("notes")
    object AddNote : Screen("notes/add")
    object NoteDetail : Screen("notes/{noteId}?readOnly={readOnly}") {
        fun createRoute(noteId: String, readOnly: Boolean = false) = "notes/$noteId?readOnly=$readOnly"
    }
    object EditNote : Screen("notes/{noteId}/edit") {
        fun createRoute(noteId: String) = "notes/$noteId/edit"
    }
    object PublicNotes : Screen("notes/public")
    
    // User management
    object Profile : Screen("profile")
    object About : Screen("about")
} 
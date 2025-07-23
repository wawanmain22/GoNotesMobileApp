package com.example.gonotesmobileapp.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.gonotesmobileapp.presentation.auth.AuthViewModel
import com.example.gonotesmobileapp.presentation.auth.login.LoginScreen
import com.example.gonotesmobileapp.presentation.auth.register.RegisterScreen
import com.example.gonotesmobileapp.presentation.home.HomeScreen
import com.example.gonotesmobileapp.presentation.notes.NotesListScreen
import com.example.gonotesmobileapp.presentation.notes.NotesListViewModel
import com.example.gonotesmobileapp.presentation.notes.add.AddNoteScreen
import com.example.gonotesmobileapp.presentation.notes.detail.NoteDetailScreen
import com.example.gonotesmobileapp.presentation.notes.edit.EditNoteScreen
import com.example.gonotesmobileapp.presentation.notes.public.PublicNotesScreen
import com.example.gonotesmobileapp.presentation.profile.ProfileScreen
import com.example.gonotesmobileapp.presentation.about.AboutScreen

@Composable
fun GoNotesNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    ) {
        // Authentication screens
        composable(route = Screen.Login.route) {
            Log.d("Navigation", "📱 Current Page: LOGIN SCREEN")
            LoginScreen(
                onNavigateToRegister = {
                    Log.d("Navigation", "🔄 Navigation: Login → Register")
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    Log.d("Navigation", "🔄 Navigation: Login → Home (Success)")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            Log.d("Navigation", "📱 Current Page: REGISTER SCREEN")
            RegisterScreen(
                onNavigateToLogin = {
                    Log.d("Navigation", "🔄 Navigation: Register → Login")
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    Log.d("Navigation", "🔄 Navigation: Register → Login (Success)")
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Main app screens
        composable(route = Screen.Home.route) {
            Log.d("Navigation", "📱 Current Page: HOME SCREEN")
            HomeScreen(
                onNavigateToNotes = {
                    Log.d("Navigation", "🔄 Navigation: Home → My Notes")
                    navController.navigate(Screen.NotesList.route)
                },
                onNavigateToPublicNotes = {
                    Log.d("Navigation", "🔄 Navigation: Home → Public Notes")
                    navController.navigate(Screen.PublicNotes.route)
                },
                onNavigateToProfile = {
                    Log.d("Navigation", "🔄 Navigation: Home → Profile")
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToAbout = {
                    Log.d("Navigation", "🔄 Navigation: Home → About")
                    navController.navigate(Screen.About.route)
                },
                onLogout = {
                    Log.d("Navigation", "🔄 Navigation: Home → Login (Logout)")
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Notes list screen
        composable(route = Screen.NotesList.route) { backStackEntry ->
            Log.d("Navigation", "📱 Current Page: MY NOTES SCREEN")
            val notesListViewModel: NotesListViewModel = hiltViewModel()
            
            // Check if we're returning from other screens
            val savedStateHandle = backStackEntry.savedStateHandle
            val shouldRefresh = savedStateHandle.get<Boolean>("refresh_notes") ?: false
            
            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    Log.d("Navigation", "🔄 MY NOTES: Refreshing data due to navigation result")
                    notesListViewModel.refreshNotes()
                    savedStateHandle.remove<Boolean>("refresh_notes")
                }
            }
            
            NotesListScreen(
                onNoteClick = { noteId ->
                    Log.d("Navigation", "🔄 Navigation: My Notes → Note Detail ($noteId)")
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onAddNoteClick = {
                    Log.d("Navigation", "🔄 Navigation: My Notes → Add Note")
                    navController.navigate(Screen.AddNote.route)
                },
                onEditNote = { noteId ->
                    Log.d("Navigation", "🔄 Navigation: My Notes → Edit Note ($noteId)")
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                },
                onMenuClick = {
                    Log.d("Navigation", "🔄 Navigation: My Notes → Home (Back)")
                    navController.popBackStack()
                },
                viewModel = notesListViewModel
            )
        }

        // Add note screen
        composable(route = Screen.AddNote.route) {
            Log.d("Navigation", "📱 Current Page: ADD NOTE SCREEN")
            AddNoteScreen(
                onNavigateBack = {
                    Log.d("Navigation", "🔄 Navigation: Add Note → My Notes (Back)")
                    navController.popBackStack()
                },
                onNoteSaved = {
                    Log.d("Navigation", "🔄 Navigation: Add Note → My Notes (Saved)")
                    // Set refresh flag and navigate back
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_notes", true)
                    navController.popBackStack()
                }
            )
        }

        // Note detail screen
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType },
                navArgument("readOnly") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val isReadOnly = backStackEntry.arguments?.getBoolean("readOnly") ?: false
            Log.d("Navigation", "📱 Current Page: NOTE DETAIL SCREEN ($noteId) [readOnly: $isReadOnly]")
            
            // Check if we're returning from edit screen
            val savedStateHandle = backStackEntry.savedStateHandle
            val shouldRefresh = savedStateHandle.get<Boolean>("refresh_detail") ?: false
            
            LaunchedEffect(shouldRefresh) {
                if (shouldRefresh) {
                    Log.d("Navigation", "🔄 NOTE DETAIL: Refreshing data due to edit result")
                    savedStateHandle.remove<Boolean>("refresh_detail")
                }
            }
            
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    Log.d("Navigation", "🔄 Navigation: Note Detail → Previous Screen (Back)")
                    if (isReadOnly) {
                        // From public notes, don't set refresh flag
                        navController.popBackStack()
                    } else {
                        // From my notes, set refresh flag
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh_notes", true)
                        navController.popBackStack()
                    }
                },
                onEditNote = { noteId ->
                    Log.d("Navigation", "🔄 Navigation: Note Detail → Edit Note ($noteId)")
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                },
                onNoteDeleted = {
                    Log.d("Navigation", "🔄 Navigation: Note Detail → My Notes (Deleted)")
                    // Set refresh flag and navigate back
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_notes", true)
                    navController.popBackStack()
                },
                isReadOnly = isReadOnly
            )
        }

        // Edit note screen
        composable(
            route = Screen.EditNote.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            Log.d("Navigation", "📱 Current Page: EDIT NOTE SCREEN ($noteId)")
            
            EditNoteScreen(
                noteId = noteId,
                onNavigateBack = {
                    Log.d("Navigation", "🔄 Navigation: Edit Note → Previous Screen (Back)")
                    navController.popBackStack()
                },
                onNoteSaved = {
                    Log.d("Navigation", "🔄 Navigation: Edit Note → Previous Screen (Saved)")
                    // Set refresh flag for both detail and notes list
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_detail", true)
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_notes", true)
                    navController.popBackStack()
                }
            )
        }

        // Public notes screen
        composable(route = Screen.PublicNotes.route) {
            Log.d("Navigation", "📱 Current Page: PUBLIC NOTES SCREEN")
            PublicNotesScreen(
                onNoteClick = { noteId ->
                    Log.d("Navigation", "🔄 Navigation: Public Notes → Note Detail ($noteId) [READ-ONLY]")
                    navController.navigate(Screen.NoteDetail.createRoute(noteId, readOnly = true))
                },
                onNavigateBack = {
                    Log.d("Navigation", "🔄 Navigation: Public Notes → Home (Back)")
                    navController.popBackStack()
                }
            )
        }

        // Profile screen
        composable(route = Screen.Profile.route) {
            Log.d("Navigation", "📱 Current Page: PROFILE SCREEN")
            ProfileScreen(
                onNavigateBack = {
                    Log.d("Navigation", "🔄 Navigation: Profile → Home (Back)")
                    navController.popBackStack()
                }
            )
        }

        // About screen
        composable(route = Screen.About.route) {
            Log.d("Navigation", "📱 Current Page: ABOUT SCREEN")
            AboutScreen(
                onNavigateBack = {
                    Log.d("Navigation", "🔄 Navigation: About → Home (Back)")
                    navController.popBackStack()
                }
            )
        }
    }
}

 
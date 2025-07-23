package com.example.gonotesmobileapp.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gonotesmobileapp.presentation.components.NoteCard
import com.example.gonotesmobileapp.ui.theme.GoNotesMobileAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNotes: () -> Unit,
    onNavigateToPublicNotes: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Debug logging for UI state
    LaunchedEffect(uiState) {
        Log.d("HomeScreen", "🏠 UI State changed:")
        Log.d("HomeScreen", "   - isLoading: ${uiState.isLoading}")
        Log.d("HomeScreen", "   - recentNotes count: ${uiState.recentNotes.size}")
        Log.d("HomeScreen", "   - error: ${uiState.error}")
        if (uiState.recentNotes.isNotEmpty()) {
            uiState.recentNotes.forEachIndexed { index, note ->
                Log.d("HomeScreen", "   - Note $index: ${note.title}")
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "🏠 HomeScreen composed - loading recent notes")
        viewModel.loadRecentNotes()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentUser = uiState.currentUser,
                totalNotes = uiState.totalNotes,
                onNavigateToNotes = {
                    scope.launch { drawerState.close() }
                    onNavigateToNotes()
                },
                onNavigateToPublicNotes = {
                    scope.launch { drawerState.close() }
                    onNavigateToPublicNotes()
                },
                onNavigateToProfile = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                },
                onNavigateToAbout = {
                    scope.launch { drawerState.close() }
                    onNavigateToAbout()
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("GoNotes") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open drawer"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToNotes
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Note"
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            val currentUser = uiState.currentUser
                            Text(
                                text = if (currentUser != null) {
                                    "Welcome back, ${currentUser.fullName}!"
                                } else {
                                    "Welcome back!"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Ready to capture your thoughts?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Quick actions
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // My Notes button
                        Card(
                            onClick = onNavigateToNotes,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notes,
                                    contentDescription = "My Notes",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "My Notes",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Public Notes button
                        Card(
                            onClick = onNavigateToPublicNotes,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Public Notes",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Public Notes",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Recent notes section
                if (uiState.recentNotes.isNotEmpty()) {
                    Log.d("HomeScreen", "🏠 Rendering recent notes section - ${uiState.recentNotes.size} notes")
                    item {
                        Text(
                            text = "Recent Notes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(
                        items = uiState.recentNotes.take(3),
                        key = { note -> note.id }
                    ) { note ->
                        Log.d("HomeScreen", "🏠 Rendering note card: ${note.title}")
                        NoteCard(
                            note = note,
                            onClick = { /* Navigate to note detail */ },
                            showActions = false
                        )
                    }

                    if (uiState.recentNotes.size > 3) {
                        item {
                            TextButton(
                                onClick = onNavigateToNotes,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View All Notes")
                            }
                        }
                    }
                } else if (!uiState.isLoading) {
                    Log.d("HomeScreen", "🏠 Rendering empty state - no recent notes")
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📝",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No notes yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Start by creating your first note!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Loading state for recent notes
                if (uiState.isLoading) {
                    Log.d("HomeScreen", "🏠 Rendering loading state")
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    currentUser: com.example.gonotesmobileapp.domain.model.User?,
    totalNotes: Int,
    onNavigateToNotes: () -> Unit,
    onNavigateToPublicNotes: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Section with User Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Card(
                            modifier = Modifier.size(48.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.fullName?.take(1)?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // User Info
                        Column {
                            Text(
                                text = currentUser?.fullName ?: "User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Stats
                    Text(
                        text = "$totalNotes notes created",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigation Items
            DrawerMenuItem(
                icon = Icons.Default.Notes,
                title = "My Notes",
                subtitle = "View and manage your notes",
                onClick = onNavigateToNotes
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Public,
                title = "Public Notes",
                subtitle = "Explore community notes",
                onClick = onNavigateToPublicNotes
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Person,
                title = "Profile",
                subtitle = "Manage your account",
                onClick = onNavigateToProfile
            )
            
            DrawerMenuItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App information",
                onClick = onNavigateToAbout
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Logout Section
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            DrawerMenuItem(
                icon = Icons.Default.ExitToApp,
                title = "Logout",
                subtitle = "Sign out of your account",
                onClick = onLogout,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDestructive) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GoNotesMobileAppTheme {
        // Preview implementation
    }
}
package com.mynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mynotes.ui.navigation.Screen
import com.mynotes.ui.screens.NoteDetailScreen
import com.mynotes.ui.screens.NotesListScreen
import com.mynotes.ui.theme.NotesAppTheme
import com.mynotes.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NotesViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            NotesAppTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotesApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NotesApp(viewModel: NotesViewModel = viewModel()) {
    val navController = rememberNavController()
    val showArchived by viewModel.showArchived.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.NotesList.route
    ) {
        composable(Screen.NotesList.route) {
            NotesListScreen(
                viewModel = viewModel,
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetail.createRoute(noteId))
                },
                onAddClick = {
                    navController.navigate(Screen.NoteDetail.createRoute(null))
                },
                onArchivedClick = {
                    viewModel.toggleArchived()
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId")
            NoteDetailScreen(
                noteId = if (noteId == -1L) null else noteId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

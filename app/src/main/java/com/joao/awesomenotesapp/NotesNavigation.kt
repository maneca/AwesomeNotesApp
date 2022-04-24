package com.joao.awesomenotesapp

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.ui.*
import com.joao.awesomenotesapp.util.toJson
import com.joao.awesomenotesapp.viewmodel.*

@Composable
fun NotesNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {

        composable(Screen.MainScreen.route){
            BackHandler(true) {}
            val viewModel = hiltViewModel<MainViewModel>()
            MainScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route) },
                onNavigateToNotes = { navController.navigate(Screen.NotesScreen.withArgs(it)) }
            )
        }
        composable(Screen.LoginScreen.route) {
            BackHandler(true) {}
            val viewModel = hiltViewModel<LoginViewModel>()
            LoginScreen(
                viewModel = viewModel,
                navigateToRegister = {
                    navController.navigate(Screen.RegisterScreen.route)
                },
                onSubmit = {
                    navController.navigate(Screen.NotesScreen.withArgs(it))
                })
        }
        composable(Screen.RegisterScreen.route) {
            BackHandler(true) {}
            val viewModel = hiltViewModel<RegisterViewModel>()
            RegisterScreen(
                viewModel = viewModel,
                returnToLogin = {
                    navController.navigateUp()
                },
                onSubmit = {
                    navController.navigate(Screen.NotesScreen.withArgs(it))
                })
        }
        composable(
            route = Screen.NotesScreen.route + "/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            BackHandler(true) {}
            val viewModel = hiltViewModel<NotesViewModel>()
            entry.arguments?.getString("userId")?.let { id ->
                NotesScreen(
                    viewModel = viewModel,
                    navigateToLogin = {
                        navController.navigate(Screen.LoginScreen.route)
                    },
                    navigateToNote = { userId: String, note: Note ->
                        navController.navigate(
                            Screen.EditNotesScreen
                                .withArgs(
                                    userId,
                                    note.toJson()
                                )
                        )
                    },
                    navigateToDialog = { userId ->
                        navController.navigate(
                            Screen.LogoutDialog
                                .withArgs(userId)
                        )
                    },
                    userId = id
                )
            }
        }
        composable(
            route = Screen.EditNotesScreen.route + "/{userId}/{note}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("note") {
                    type = NavType.StringType
                    defaultValue = Note().toJson()
                }
            )
        ) { entry ->
            BackHandler(true) {}
            val viewModel = hiltViewModel<AddEditNotesViewModel>()
            AddEditNotesScreen(
                viewModel = viewModel,
                navigateBack = {
                    val notesScreen = Screen.NotesScreen.withArgs(entry.arguments?.getString("userId"))
                    navController.navigate(notesScreen){
                        popUpTo(notesScreen) { inclusive = true }
                    }
                }
            )
        }
        dialog(
            route = "logout/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            BackHandler(true) {}
            val viewModel = hiltViewModel<LogoutViewModel>()
            LogoutDialog(
                viewModel = viewModel,
                userId = entry.arguments?.getString("userId") ?: "",
                onDismiss = {
                    navController.navigateUp()
                },
                onConfirm = {
                    navController.navigate(Screen.LoginScreen.route)
                })
        }
    }
}

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object LoginScreen : Screen("login_screen")
    object RegisterScreen : Screen("register_screen")
    object NotesScreen : Screen("notes_screen")
    object EditNotesScreen : Screen("edit_notes_screen")
    object LogoutDialog : Screen("logout")

    fun withArgs(vararg args: String?): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
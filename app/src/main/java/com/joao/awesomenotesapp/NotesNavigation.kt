package com.joao.awesomenotesapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joao.awesomenotesapp.ui.AddEditNotesScreen
import com.joao.awesomenotesapp.ui.LoginScreen
import com.joao.awesomenotesapp.ui.NotesScreen

@Composable
fun NotesNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.LoginScreen.route){
        composable(Screen.LoginScreen.route){
            LoginScreen(navController = navController)
        }
        composable(
            route = Screen.NotesScreen.route + "/{userId}",
            arguments = listOf(
                navArgument("userId"){
                    type = NavType.StringType
                }
            )
        ){ entry->
            entry.arguments?.getString("userId")?.let { id ->
                NotesScreen(navController = navController, userId = id)
            }
        }
        composable(
            route = Screen.EditNotesScreen.route,
            /*arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    defaultValue = null
                    nullable = true
                }
            )*/
        ){ /*entry->
            entry.arguments?.getString("noteId")?.let { id ->
                AddEditNotesScreen(navController = navController, noteId = id)
            }*/
            AddEditNotesScreen(navController = navController, noteId = "")
        }
    }
}

sealed class Screen(val route: String){
    object LoginScreen : Screen("login_screen")
    object NotesScreen : Screen("notes_screen")
    object EditNotesScreen : Screen("edit_notes_screen")

    fun withArgs(vararg args: String) : String{
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
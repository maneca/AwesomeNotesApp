package com.joao.awesomenotesapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.Screen
import com.joao.awesomenotesapp.ui.components.NoteItem
import com.joao.awesomenotesapp.viewmodel.NotesViewModel
import kotlinx.coroutines.launch


@Composable
fun NotesScreen(navController: NavController, userId: String?) {
    val viewModel: NotesViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.app_name),
                        color = Color.White
                    )},
                backgroundColor = Color.Blue
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.EditNotesScreen.route)
                },
                backgroundColor = Color.Blue,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            )
        },
        scaffoldState = scaffoldState) {

        if(state.value.loading){
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }else{
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)) {
                items(state.value.notes.size) { note ->
                    NoteItem(
                        note = state.value.notes[note],
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                            },
                        onDeleteClick = {
                            scope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = "Note deleted",
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

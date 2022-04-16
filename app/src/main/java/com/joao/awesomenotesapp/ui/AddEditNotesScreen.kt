package com.joao.awesomenotesapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.util.ConnectionState
import com.joao.awesomenotesapp.util.UiEvent
import com.joao.awesomenotesapp.util.connectivityState
import com.joao.awesomenotesapp.viewmodel.AddEditNotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AddEditNotesScreen(navController: NavController, noteId: String?){
    val viewModel: AddEditNotesViewModel = hiltViewModel()
    val scaffoldState = rememberScaffoldState()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isTitleHintVisible by remember { mutableStateOf(true) }
    var isContentHintVisible by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val connection by connectivityState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is UiEvent.NoteSaved -> {
                    navController.navigateUp()
                }
                is UiEvent.Failed -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = context.getString(R.string.something_went_wrong),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_note), color = Color.White) },
                backgroundColor = Color.Blue,
                navigationIcon = if (navController.previousBackStackEntry != null) {
                    {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    null
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveNote(title = title, note = content, connection === ConnectionState.Available)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            TransparentHintTextField(
                text = title,
                hint = "Title",
                onValueChange = {
                    isTitleHintVisible = false
                    title = it
                },
                onFocusChange = {
                },
                isHintVisible = isTitleHintVisible,
                singleLine = true,
                textStyle = MaterialTheme.typography.h5
            )
            Spacer(modifier = Modifier.height(16.dp))
            TransparentHintTextField(
                text = content,
                hint = "Content",
                onValueChange = {
                    content = it
                    isContentHintVisible = false
                },
                onFocusChange = {
                },
                isHintVisible = isContentHintVisible,
                textStyle = MaterialTheme.typography.body1,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@Composable
fun TransparentHintTextField(
    text: String,
    hint: String,
    modifier: Modifier = Modifier,
    isHintVisible: Boolean = true,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle = TextStyle(),
    singleLine: Boolean = false,
    onFocusChange: (FocusState) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        BasicTextField(
            value = text,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    onFocusChange(it)
                }
        )
        if(isHintVisible) {
            Text(text = hint, style = textStyle, color = Color.DarkGray)
        }
    }
}

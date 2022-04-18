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
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.util.*
import com.joao.awesomenotesapp.viewmodel.AddEditNotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AddEditNotesScreen(
    viewModel: AddEditNotesViewModel,
    navigateBack: () -> Unit,
    userId: String,
    noteJson: String
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val connection by connectivityState()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.NoteSaved -> {
                    navigateBack()
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
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveNote(
                            userId = userId,
                            id = state.note.id,
                            title = state.note.title,
                            note = state.note.content,
                            hasInternetConnection = connection === ConnectionState.Available
                        )
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
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            TransparentHintTextField(
                text = state.note.title,
                hint = stringResource(id = R.string.title),
                onValueChange = {
                    viewModel.onUpdateTitle(it)
                },
                onFocusChange = {
                },
                isHintVisible = state.note.title == "",
                singleLine = true,
                textStyle = MaterialTheme.typography.h5
            )
            Spacer(modifier = Modifier.height(16.dp))
            TransparentHintTextField(
                text = state.note.content,
                hint = stringResource(id = R.string.content),
                onValueChange = {
                    viewModel.onUpdateContent(it)
                },
                onFocusChange = {
                },
                isHintVisible = state.note.content == "",
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
        if (isHintVisible) {
            Text(text = hint, style = textStyle, color = Color.DarkGray)
        }
    }
}

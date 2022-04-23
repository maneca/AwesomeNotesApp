package com.joao.awesomenotesapp.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.util.*
import com.joao.awesomenotesapp.viewmodel.AddEditNotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AddEditNotesScreen(
    viewModel: AddEditNotesViewModel,
    navigateBack: () -> Unit,
    userId: String
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val connection by connectivityState()
    val state by viewModel.uiState.collectAsState()
    var showLoading by remember { mutableStateOf(false) }

    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val launcher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.NoteSaved -> {
                    showLoading = false
                    navigateBack()
                }

                is UiEvent.ImageUrl ->
                    imageUri = event.uri

                is UiEvent.UploadingNote ->
                    showLoading = true

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
                    IconButton(onClick = { navigateBack() }, enabled = !showLoading) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (connection === ConnectionState.Available) {
                        IconButton(
                            onClick = { launcher.launch("image/*") },
                            enabled = !showLoading
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = {
                        viewModel.saveNote(
                            id = state.note.id,
                            title = state.note.title,
                            content = state.note.content,
                            imageUri = imageUri ?: Uri.EMPTY
                        )
                    }, enabled = !showLoading) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "",
                            tint = Color.White
                        )
                    }

                }
            )
        },
        scaffoldState = scaffoldState
    ) {
        if (showLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            imageUri?.let {
                if (Build.VERSION.SDK_INT < 28) {
                    bitmap.value = MediaStore.Images
                        .Media.getBitmap(context.contentResolver, it)

                } else {
                    val source = ImageDecoder
                        .createSource(context.contentResolver, it)
                    bitmap.value = ImageDecoder.decodeBitmap(source)
                }

                bitmap.value?.let { btm ->
                    Image(
                        bitmap = btm.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(200.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

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

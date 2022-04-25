package com.joao.awesomenotesapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.util.UiEvent
import com.joao.awesomenotesapp.util.collectAsStateLifecycleAware
import com.joao.awesomenotesapp.viewmodel.RegisterViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onSubmit: (String) -> Unit,
    returnToLogin: () -> Unit
) {
    val state = viewModel.state.collectAsStateLifecycleAware()
    val emailValue = remember { mutableStateOf("") }
    val passwordValue = remember { mutableStateOf("") }
    val confirmPasswordValue = remember { mutableStateOf("") }
    val buttonsEnabled = remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val passwordVisibility = remember { mutableStateOf(false) }
    val confirmPasswordVisibility = remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    LaunchedEffect(key1 = scaffoldState) {

        viewModel.errors.collect {
            buttonsEnabled.value = true
            scaffoldState.snackbarHostState.showSnackbar(
                message = it.asString(context),
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(key1 = true) {

        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.UserLoggedIn -> {
                    state.value.user?.let { onSubmit(it.uid) }
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .padding(10.dp)
        ) {
            val localFocusManager = LocalFocusManager.current
            val focusRequester = FocusRequester()

            Text(
                text = stringResource(id = R.string.register), fontSize = 30.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.padding(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = emailValue.value,
                    onValueChange = { emailValue.value = it },
                    label = { Text(text = stringResource(id = R.string.enter_email)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )

                OutlinedTextField(
                    value = passwordValue.value,
                    onValueChange = { passwordValue.value = it },
                    label = { Text(text = stringResource(id = R.string.enter_password)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    trailingIcon = {
                        IconButton(onClick = {
                            passwordVisibility.value = !passwordVisibility.value
                        }) {
                            Icon(
                                imageVector = if (!passwordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "",
                                tint = Color.Blue
                            )
                        }
                    },
                    visualTransformation = if (passwordVisibility.value) VisualTransformation.None else PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = confirmPasswordValue.value,
                    onValueChange = { confirmPasswordValue.value = it },
                    label = { Text(text = stringResource(id = R.string.confirm_password)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    trailingIcon = {
                        IconButton(onClick = {
                            confirmPasswordVisibility.value = !confirmPasswordVisibility.value
                        }) {
                            Icon(
                                imageVector = if (!confirmPasswordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "",
                                tint = Color.Blue
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisibility.value) VisualTransformation.None else PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.padding(10.dp))
                Button(
                    onClick = {
                        keyboardController?.hide()
                        localFocusManager.clearFocus()
                        viewModel.registerUser(
                            emailValue.value,
                            passwordValue.value,
                            confirmPasswordValue.value
                        )
                        buttonsEnabled.value = false
                    }, modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    enabled = buttonsEnabled.value
                ) {
                    Text(text = stringResource(id = R.string.register), fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.padding(20.dp))

                when {
                    state.value.loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    else -> {
                        Text(
                            text = stringResource(id = R.string.return_login),
                            modifier = Modifier.clickable(onClick = {
                                returnToLogin()
                            })
                        )
                    }
                }
            }


        }
    }
}
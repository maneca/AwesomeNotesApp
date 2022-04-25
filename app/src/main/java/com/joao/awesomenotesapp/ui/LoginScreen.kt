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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joao.awesomenotesapp.viewmodel.LoginViewModel
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.util.collectAsStateLifecycleAware


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navigateToRegister: () -> Unit,
    onSubmit: (String) -> Unit) {

    val state = viewModel.state.collectAsStateLifecycleAware()
    val scaffoldState = rememberScaffoldState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val buttonsEnabled = remember { mutableStateOf(true) }
    val passwordVisibility = remember { mutableStateOf(true) }
    val email = remember { mutableStateOf(TextFieldValue()) }
    val password = remember { mutableStateOf(TextFieldValue()) }

    val context = LocalContext.current
    LaunchedEffect(key1 = scaffoldState){

        viewModel.errors.collect{
            buttonsEnabled.value = true
            scaffoldState.snackbarHostState.showSnackbar(
                message = it.asString(context),
                duration = SnackbarDuration.Short
            )
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
                Text(
                    text = stringResource(id = R.string.login),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.padding(20.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val localFocusManager = LocalFocusManager.current
                    val focusRequester = FocusRequester()

                    OutlinedTextField(
                        value = email.value,
                        onValueChange = {
                            email.value = it
                        },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        label = {
                            Text(text = stringResource(R.string.enter_email))
                        },
                    )

                    OutlinedTextField(
                        value = password.value,
                        onValueChange = {
                            password.value = it
                        },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        label = {
                            Text(text = stringResource(R.string.enter_password))
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                passwordVisibility.value = !passwordVisibility.value
                            }) {
                                Icon(
                                    imageVector = if (passwordVisibility.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "",
                                    tint = Color.Blue
                                )
                            }
                        },
                        visualTransformation = if (passwordVisibility.value) PasswordVisualTransformation() else VisualTransformation.None
                    )

                    Spacer(modifier = Modifier.padding(10.dp))
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            localFocusManager.clearFocus()
                            viewModel.loginUser(email.value.text, password.value.text)
                            buttonsEnabled.value = false
                        },
                        enabled = buttonsEnabled.value,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(text = stringResource(R.string.login), fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.padding(20.dp))

                    when {
                        state.value.loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        state.value.user != null -> {
                            LaunchedEffect(Unit) {
                                onSubmit(state.value.user!!.uid)
                            }
                        }
                        else -> {
                            Text(
                                text = stringResource(id = R.string.create_account),
                                modifier = Modifier.clickable(onClick = {
                                    navigateToRegister()
                                })
                            )
                        }
                    }
                }
        }

    }
}
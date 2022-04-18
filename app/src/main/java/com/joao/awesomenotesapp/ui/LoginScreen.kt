package com.joao.awesomenotesapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.joao.awesomenotesapp.viewmodel.LoginRegisterViewModel
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.Screen


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginRegisterViewModel,
    onSubmit: (String) -> Unit) {

    val viewModel: LoginRegisterViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val buttonsEnabled = remember { mutableStateOf(true) }

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
        modifier = Modifier.fillMaxSize()) {

        val email = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }

        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email.value,
                onValueChange = {
                    email.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = stringResource(R.string.enter_email))
                },
            )
            Spacer(Modifier.size(16.dp))
            val passwordVisibility = remember { mutableStateOf(true) }
            OutlinedTextField(
                value = password.value,
                onValueChange = {
                    password.value = it
                },
                modifier = Modifier.fillMaxWidth(),
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
            Spacer(Modifier.size(16.dp))
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.loginUser(email.value.text, password.value.text)
                    buttonsEnabled.value = false
                },
                content = {
                    Text(text = stringResource(R.string.login), color = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = buttonsEnabled.value,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            )
            Spacer(Modifier.size(5.dp))
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.registerUser(email.value.text, password.value.text)
                    buttonsEnabled.value = false
                },
                content = {
                    Text(text = stringResource(R.string.register), color = Color.White)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = buttonsEnabled.value,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            )

            when {
                state.value.loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                state.value.user != null -> {
                    LaunchedEffect(Unit) {
                        onSubmit(state.value.user!!.uid)
                    }
                }
            }
        }
    }
}
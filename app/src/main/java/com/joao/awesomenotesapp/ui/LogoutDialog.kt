package com.joao.awesomenotesapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.util.ConnectionState
import com.joao.awesomenotesapp.util.UiEvent
import com.joao.awesomenotesapp.util.connectivityState
import com.joao.awesomenotesapp.viewmodel.LogoutViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun LogoutDialog(
    viewModel: LogoutViewModel,
    userId: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val connection by connectivityState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {

                is UiEvent.NoInternetConnection -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = context.getString(R.string.no_internet),
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEvent.UserLoggedOut -> {
                    onConfirm()
                }
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier.background(color = Color.White, shape = RoundedCornerShape(10.dp))
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Text(
                    fontSize = 20.sp,
                    modifier = Modifier.padding(4.dp),
                    text = stringResource(id = R.string.logout)
                )

                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = stringResource(id = R.string.logout_confirmation)
                )
                Spacer(modifier = Modifier.size(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        viewModel.logoutUser(userId = userId, hasInternetConnection = connection == ConnectionState.Available)
                    }, modifier = Modifier.wrapContentSize()) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            }
        }
    }
}
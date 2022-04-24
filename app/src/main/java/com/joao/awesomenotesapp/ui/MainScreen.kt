package com.joao.awesomenotesapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.util.collectAsStateLifecycleAware
import com.joao.awesomenotesapp.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToNotes: (String) -> Unit
) {
    val state = viewModel.firebaseUser.collectAsStateLifecycleAware()
    val isLoading = viewModel.isLoading.collectAsStateLifecycleAware()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading.value) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    elevation = 2.dp
                ) {
                    Image(
                        painterResource(R.drawable.ic_main_image),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.wrapContentSize()
                    )
                }

            }
        } else {
            LaunchedEffect(key1 = true) {
                if (state.value != null) {
                    state.value?.let { onNavigateToNotes(it.uid) }
                } else {
                    onNavigateToLogin()
                }
            }
        }
    }

}
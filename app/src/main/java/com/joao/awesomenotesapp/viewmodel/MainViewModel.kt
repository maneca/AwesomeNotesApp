package com.joao.awesomenotesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth) : ViewModel() {

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser = _firebaseUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        validateUser()
    }

    private fun validateUser() {
        viewModelScope.launch {
            if (firebaseAuth.currentUser != null) {
                _firebaseUser.value = firebaseAuth.currentUser
            }
            _isLoading.value = false
        }
    }
}
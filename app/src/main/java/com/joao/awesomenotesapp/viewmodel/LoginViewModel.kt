package com.joao.awesomenotesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.domain.repository.LoginRepository
import com.google.firebase.auth.FirebaseUser
import com.joao.awesomenotesapp.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dispatcher: DispatcherProvider,
    private val firebaseAuth: FirebaseAuth,
    private val repository: LoginRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state = _state.asStateFlow()

    private val validationFieldsChannel = Channel<UiText>()
    val errors = validationFieldsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            if(firebaseAuth.currentUser != null){
                _state.value = state.value.copy(
                    user = firebaseAuth.currentUser,
                    loading = false
                )
            }
        }
    }

    fun loginUser(email: String, password: String) {

        viewModelScope.launch {
            if(validateFields(email, password)){
                repository.loginUser(email, password)
                    .flowOn(dispatcher.io())
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = state.value.copy(
                                    user = result.data,
                                    loading = false
                                )
                            }
                            is Resource.Error -> {
                                _state.value = state.value.copy(
                                    user = result.data,
                                    loading = false
                                )

                                if (result.exception is CustomExceptions.ConflictException){
                                    validationFieldsChannel.send(UiText.DynamicString(result.exception.message!!))
                                }else{
                                    validationFieldsChannel.send(UiText.StringResource(R.string.something_went_wrong))
                                }
                            }
                            is Resource.Loading -> {
                                _state.value = state.value.copy(
                                    user = result.data,
                                    loading = true
                                )
                            }
                        }
                    }
            }
        }
    }

    private suspend fun validateFields(email: String, password: String): Boolean{
        when {
            email.isEmpty() -> {
                validationFieldsChannel.send(UiText.StringResource(R.string.email_mandatory))
                return false
            }
            password.isEmpty() -> {
                validationFieldsChannel.send(UiText.StringResource(R.string.password_mandatory))
                return false
            }
            email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()-> {
                validationFieldsChannel.send(UiText.StringResource(R.string.email_invalid))
                return false
            }
            password.isNotEmpty() && password.length < 6 -> {
                validationFieldsChannel.send(UiText.StringResource(R.string.password_short))
                return false
            }
        }
        return true
    }

    data class UserState(
        val user: FirebaseUser? = null,
        val loading: Boolean = false
    )
}


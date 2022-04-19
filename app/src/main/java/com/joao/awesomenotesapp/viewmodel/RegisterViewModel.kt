package com.joao.awesomenotesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.domain.repository.LoginRepository
import com.joao.awesomenotesapp.domain.repository.RegisterRepository
import com.joao.awesomenotesapp.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val dispatcher: DispatcherProvider,
    private val repository: RegisterRepository
): ViewModel() {
    private val _state = MutableStateFlow(LoginViewModel.UserState())
    val state = _state.asStateFlow()

    private val validationFieldsChannel = Channel<UiText>()
    val errors = validationFieldsChannel.receiveAsFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun registerUser(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if(validateFields(email, password, confirmPassword)){
                repository.registerUser(email, password)
                    .flowOn(dispatcher.io())
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = state.value.copy(
                                    user = result.data,
                                    loading = false
                                )
                                _eventFlow.emit(UiEvent.UserLoggedIn)
                            }
                            is Resource.Error -> {
                                _state.value = state.value.copy(
                                    user = result.data,
                                    loading = false
                                )

                                if (result.exception is CustomExceptions.ConflictException) {
                                    validationFieldsChannel.send(UiText.DynamicString(result.exception.message!!))
                                } else {
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

    private suspend fun validateFields(email: String, password: String, confirmPassword: String): Boolean{
        when {
            email.isEmpty() -> {
                validationFieldsChannel.send(UiText.StringResource(R.string.email_mandatory))
                return false
            }
            password.isEmpty() -> {
                validationFieldsChannel.send(UiText.StringResource(R.string.password_mandatory))
                return false
            }
            password != confirmPassword -> {
                validationFieldsChannel.send(UiText.StringResource(R.string.password_dont_match))
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
}
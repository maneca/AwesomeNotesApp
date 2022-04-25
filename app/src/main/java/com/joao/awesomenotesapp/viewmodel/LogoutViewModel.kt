package com.joao.awesomenotesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.domain.repository.LogoutRepository
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.DispatcherProvider
import com.joao.awesomenotesapp.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val dispatcher: DispatcherProvider,
    private val notesRepo: NotesRepository,
    private val logoutRepository: LogoutRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun logoutUser(userId: String, hasInternetConnection: Boolean){
        viewModelScope.launch(SupervisorJob()) {
            if(hasInternetConnection){
                notesRepo
                    .syncNotesToBackend(userId)

                logoutRepository
                    .logout(userId)
                    .flowOn(dispatcher.io())
                    .collect { result ->
                        if (result) {
                            _eventFlow.emit(UiEvent.UserLoggedOut)
                        } else {
                            _eventFlow.emit(UiEvent.Failed)
                        }
                    }
            }else{
                _eventFlow.emit(UiEvent.NoInternetConnection)
            }

        }
    }
}
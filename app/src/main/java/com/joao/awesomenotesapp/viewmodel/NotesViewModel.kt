package com.joao.awesomenotesapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val dispatcher: DispatcherProvider,
    private val repository: NotesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state = _state.asStateFlow()

    private val validationFieldsChannel = Channel<UiText>()
    val errors = validationFieldsChannel.receiveAsFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var getNotesJob: Job? = null

    init {
        getNotes("eERu49JLMLZcdLADMyygSnHo7Pm1")
    }

    private fun getNotes(userId: String) {
        getNotesJob?.cancel()

        getNotesJob = viewModelScope.launch {
            repository
                .getNotes(userId)
                .onEach {result ->
                    when(result){
                        is Resource.Success ->{
                            _state.value = state.value.copy(
                                notes = result.data ?: emptyList(),
                                loading = false
                            )
                        }
                        is Resource.Error -> {
                            _state.value = state.value.copy(
                                notes = listOf(),
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
                                notes = emptyList(),
                                loading = true
                            )
                        }
                    }
                }
                .flowOn(dispatcher.io())
                .launchIn(this)
        }
    }

    fun deleteNote(userId: String, noteId: String){
        viewModelScope.launch {
            repository
                .deleteNote(userId, noteId)
                .onEach { result ->
                    if (result) {
                        _eventFlow.emit(UiEvent.NoteDeleted)
                    } else {
                        _eventFlow.emit(UiEvent.Failed)
                    }
                }
                .flowOn(dispatcher.io())
                .launchIn(this)
        }
    }

    fun logoutUser(userId: String){
        viewModelScope.launch {
            repository
                .logout(userId)
                .onEach { result ->
                    if (result) {
                        _eventFlow.emit(UiEvent.UserLoggedOut)
                    } else {
                        _eventFlow.emit(UiEvent.Failed)
                    }
                }
                .flowOn(dispatcher.io())
                .launchIn(this)
        }
    }

    data class NotesState(
        val notes: List<Note> = emptyList(),
        val loading : Boolean = false
    )
}


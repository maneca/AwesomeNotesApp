package com.joao.awesomenotesapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcher: DispatcherProvider,
    private val repository: NotesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state = _state.asStateFlow()

    private val validationFieldsChannel = Channel<UiText>()
    val errors = validationFieldsChannel.receiveAsFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init{
        savedStateHandle.get<String>("userId")?.let { getNotes(userId = it) }
    }

    fun getNotes(userId: String) {

        viewModelScope.launch {
            repository
                .getNotes(userId)
                .flowOn(dispatcher.io())
                .collect {result ->
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
                                notes = result.data ?: emptyList(),
                                loading = true
                            )
                        }
                    }
                }
        }
    }

    fun deleteNote(userId: String, noteId: String){
        viewModelScope.launch {
            repository
                .deleteNote(noteId)
                .flowOn(dispatcher.io())
                .collect { result ->
                    if (result) {
                        _eventFlow.emit(UiEvent.NoteDeleted)
                        getNotes(userId)
                    } else {
                        _eventFlow.emit(UiEvent.Failed)
                    }
                }
        }

    }

    data class NotesState(
        val notes: List<Note> = emptyList(),
        val loading : Boolean = false
    )
}


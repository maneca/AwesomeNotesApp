package com.joao.awesomenotesapp.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.DispatcherProvider
import com.joao.awesomenotesapp.util.Resource
import com.joao.awesomenotesapp.util.UiEvent
import com.joao.awesomenotesapp.util.fromJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcher: DispatcherProvider,
    private val repository: NotesRepository
) : ViewModel(){

    private var _uiState: MutableStateFlow<NoteAddUiState> = MutableStateFlow(NoteAddUiState(
        note = savedStateHandle.get<String>("note")?.fromJson() ?: Note())
    )
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun saveNote(userId: String, id: String, title: String, content: String, imageUri: Uri, hasInternetConnection: Boolean) {
        viewModelScope.launch(SupervisorJob()) {
            repository
                .saveNote(id, title, content, imageUri, System.currentTimeMillis())
                .flowOn(dispatcher.io())
                .collect { result ->
                    if (result) {
                        _eventFlow.emit(UiEvent.NoteSaved)
                    } else {
                        _eventFlow.emit(UiEvent.Failed)
                    }
                }
            repository
                .syncNotesToBackend(userId, hasInternetConnection)
                .flowOn(dispatcher.io())
                .collect { result ->
                    when(result){
                        is Resource.Success -> _eventFlow.emit(UiEvent.SyncSuccessful)
                        is Resource.Error -> _eventFlow.emit(UiEvent.Failed)
                    }
                }
        }
    }

    fun onUpdateTitle(title: String){
        _uiState.value = _uiState.value.copy(note = uiState.value.note.copy(title = title))
    }

    fun onUpdateContent(content: String){
        _uiState.value = _uiState.value.copy(note = uiState.value.note.copy(content = content))
    }
}

data class NoteAddUiState(
    val note: Note,
)
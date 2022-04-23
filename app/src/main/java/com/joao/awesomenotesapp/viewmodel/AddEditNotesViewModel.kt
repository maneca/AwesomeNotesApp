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

    init {
        if(_uiState.value.note.id != "")
            getImageUrlForNote(_uiState.value.note.id)
    }

    fun saveNote(id: String, title: String, content: String, imageUri: Uri) {
        viewModelScope.launch {
            repository
                .saveNote(id, title, content, imageUri, System.currentTimeMillis())
                .flowOn(dispatcher.io())
                .collect { result ->
                    when(result){
                        is Resource.Loading ->
                            _eventFlow.emit(UiEvent.UploadingNote)
                        is Resource.Success ->
                            _eventFlow.emit(UiEvent.NoteSaved)
                        else ->
                            _eventFlow.emit(UiEvent.Failed)
                    }
                }
        }
    }

    private fun getImageUrlForNote(noteId: String){
        viewModelScope.launch {
            repository
                .getImageUrlForNote(noteId)
                .flowOn(dispatcher.io())
                .collect { result ->
                    when(result){
                        is Resource.Success ->
                            _eventFlow.emit(UiEvent.ImageUrl(result.data!!))
                        is Resource.Loading ->
                            _eventFlow.emit(UiEvent.UploadingNote)
                        else ->
                            _eventFlow.emit(UiEvent.Failed)
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
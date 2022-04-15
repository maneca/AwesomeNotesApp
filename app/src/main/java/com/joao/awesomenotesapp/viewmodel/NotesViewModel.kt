package com.joao.awesomenotesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val dispatcher: DispatcherProvider,
    private val repository: NotesRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun saveNote(title: String, note: String){
        viewModelScope.launch {
            repository
                .saveNote("aaa", title, note, System.currentTimeMillis())
                .onEach { result ->
                    if(result){
                        _eventFlow.emit(UiEvent.NoteSaved)
                    }else{
                        _eventFlow.emit(UiEvent.Failed)
                    }
                }
                .flowOn(dispatcher.io())
                .launchIn(this)
        }
    }

    sealed class UiEvent {
        object NoteSaved: UiEvent()
        object Failed: UiEvent()
    }

}


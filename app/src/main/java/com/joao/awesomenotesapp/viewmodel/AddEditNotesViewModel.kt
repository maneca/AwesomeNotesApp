package com.joao.awesomenotesapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.DispatcherProvider
import com.joao.awesomenotesapp.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditNotesViewModel @Inject constructor(
    app: Application,
    private val dispatcher: DispatcherProvider,
    private val repository: NotesRepository
) : AndroidViewModel(app){

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun saveNote(title: String, note: String, hasInternetConnection: Boolean) {
        viewModelScope.launch {
            repository
                .saveNote("eERu49JLMLZcdLADMyygSnHo7Pm1", title, note, System.currentTimeMillis(), hasInternetConnection)
                .onEach { result ->
                    if (result) {
                        _eventFlow.emit(UiEvent.NoteSaved)
                    } else {
                        _eventFlow.emit(UiEvent.Failed)
                    }
                }
                .flowOn(dispatcher.io())
                .launchIn(this)
        }
    }

}
package com.joao.awesomenotesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class SyncViewModel @Inject constructor(
    private val repository: NotesRepository
): ViewModel() {
    fun syncToBackend(userId: String){
        viewModelScope.launch{
            repository.syncNotesToBackend(userId)
        }
    }
}
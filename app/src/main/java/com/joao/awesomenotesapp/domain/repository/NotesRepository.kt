package com.joao.awesomenotesapp.domain.repository

import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    fun saveNote(userId: String, title: String, content: String, timestamp: Long, hasInternetConnection: Boolean): Flow<Boolean>

    fun editNote(title: String, content: String): Flow<Boolean>

    fun deleteNote(userId: String, noteId: String, hasInternetConnection: Boolean): Flow<Boolean>

    fun getNotes(userId: String, hasInternetConnection: Boolean): Flow<Resource<List<Note>>>

    fun logout(userId: String): Flow<Boolean>
}
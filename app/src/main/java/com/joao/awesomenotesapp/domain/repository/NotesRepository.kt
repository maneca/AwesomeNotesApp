package com.joao.awesomenotesapp.domain.repository

import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    fun saveNote(userId: String, title: String, content: String, timestamp: Long): Flow<Boolean>

    fun editNote(title: String, content: String): Flow<Boolean>

    fun deleteNote(noteId: String): Flow<Boolean>

    fun getNotes(userId: String): Flow<Resource<List<Note>>>
}
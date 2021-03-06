package com.joao.awesomenotesapp.domain.repository

import android.net.Uri
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    fun saveNote(userId: String, noteId: String, title: String, content: String, imageUri: Uri, timestamp: Long): Flow<Resource<Boolean>>

    fun deleteNote(noteId: String): Flow<Boolean>

    fun getNotes(userId: String): Flow<Resource<List<Note>>>

    suspend fun syncNotesToBackend(userId: String)

    fun getImageUrlForNote(noteId: String) : Flow<Resource<Uri>>
}
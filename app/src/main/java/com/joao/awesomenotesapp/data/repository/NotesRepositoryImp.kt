package com.joao.awesomenotesapp.data.repository

import android.net.Uri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import com.joao.awesomenotesapp.data.local.NoteDao
import com.joao.awesomenotesapp.data.local.entity.NoteEntity
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.io.File

class NotesRepositoryImp(
    private val firebaseDatabase: DatabaseReference,
    private val firebaseStorage: StorageReference,
    private val dao: NoteDao
) : NotesRepository {

    override fun saveNote(
        noteId: String,
        title: String,
        content: String,
        imageUri: Uri,
        timestamp: Long
    ): Flow<Boolean> = flow {
        val note = imageUri.path?.let { imageUri ->
            NoteEntity(
                timestamp.toString(),
                title,
                content,
                imageUri,
                timestamp
            )
        }
        note?.let { dao.insertNote(it) }
        emit(true)

    }

    override fun deleteNote(
        noteId: String
    ): Flow<Boolean> = flow {
        dao.deleteNote(noteId)
        emit(true)
    }

    override fun syncNotesToBackend(
        userId: String,
        hasInternetConnection: Boolean
    ):Flow<Resource<Boolean>> = callbackFlow {
        if(hasInternetConnection){
            val notes = dao.getNotes().map { it.toNote() }
            val ref = firebaseDatabase.database.getReference("users")
            ref.child(userId).child("notes").setValue("")
            for (note in notes) {
                    ref.child(userId).child("notes").child(note.id).setValue(note)
                        .addOnSuccessListener {
                            if(note.imagePath != ""){
                                val imageUri = Uri.fromFile(File(note.imagePath))
                                val imageRef = firebaseStorage.child("${imageUri.lastPathSegment}")
                                imageRef
                                    .putFile(imageUri)
                                    .addOnSuccessListener {
                                        trySend(Resource.Success(true))
                                    }
                                    .addOnFailureListener{
                                        trySend(Resource.Error(exception = CustomExceptions.UnknownException))
                                    }
                            }
                        }
                        .addOnFailureListener {
                            trySend(Resource.Error(exception = CustomExceptions.UnknownException))
                        }
            }
        }else{
            trySend(Resource.Error(exception = CustomExceptions.ApiNotResponding))
        }
        awaitClose()
    }

    override fun getNotes(
        userId: String
    ): Flow<Resource<List<Note>>> = flow {

        emit(Resource.Loading())
        val notes = dao.getNotes().map { it.toNote() }

        emit(Resource.Success(notes))
    }
}
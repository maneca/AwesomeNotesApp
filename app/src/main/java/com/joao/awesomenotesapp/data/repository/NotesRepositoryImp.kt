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
import kotlinx.coroutines.tasks.await
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
    ): Flow<Resource<Boolean>> = flow {

        emit(Resource.Loading())
        val note = NoteEntity(
                timestamp.toString(),
                title,
                content,
                timestamp
            )

        dao.insertNote(note)
        if(imageUri != Uri.EMPTY){
            val imageRef = firebaseStorage.child(note.id)
            imageRef.putFile(imageUri).await()

        }
        emit(Resource.Success(true))
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
                            trySend(Resource.Success(true))
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
    ): Flow<Resource<List<Note>>> = callbackFlow {

        trySend(Resource.Loading())
        val notes = dao.getNotes().map { it.toNote() }

        if(notes.isEmpty()){
            val ref = firebaseDatabase.database.getReference("users")

            ref.child(userId).child("notes").get()
                .addOnSuccessListener {
                    val remoteNotes = mutableListOf<Note>()
                    for (dataValues in it.children) {
                        val note: Note? = dataValues.getValue(Note::class.java)
                        remoteNotes.add(note!!)
                    }
                    trySend(Resource.Success(remoteNotes))
                }
                .addOnFailureListener {
                    trySend(Resource.Error(exception = CustomExceptions.UnknownException))
                }
        }else{
            trySend(Resource.Success(notes))
        }
        awaitClose()
    }

    override fun getImageUrlForNote(noteId: String) : Flow<Resource<Uri>> = callbackFlow{

        trySend(Resource.Loading())
        val imageRef = firebaseStorage.child(noteId)

        val localFile = File.createTempFile(noteId, "jpg")

        imageRef.getFile(localFile).addOnSuccessListener {
            trySend(Resource.Success(Uri.fromFile(localFile)))
        }.addOnFailureListener {
            trySend(Resource.Error(exception = CustomExceptions.UnknownException))
        }

        awaitClose()
    }

}
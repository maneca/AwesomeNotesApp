package com.joao.awesomenotesapp.data.repository

import android.net.Uri
import com.google.firebase.FirebaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageException
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
        userId: String,
        noteId: String,
        title: String,
        content: String,
        imageUri: Uri,
        timestamp: Long
    ): Flow<Resource<Boolean>> = flow {

        emit(Resource.Loading())
        val note = NoteEntity(
            timestamp.toString(),
            userId,
            title,
            content,
            timestamp
        )

        dao.insertNote(note)
        if (imageUri != Uri.EMPTY) {
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

    override suspend fun syncNotesToBackend(userId: String) {
        val notes = dao.getNotes(userId).map { it.toNote() }
        val ref = firebaseDatabase.database.getReference("users")
        ref.child(userId).child("notes").setValue("")
        for (note in notes) {
            ref.child(userId).child("notes").child(note.id).setValue(note)
        }
    }

    override fun getNotes(
        userId: String
    ): Flow<Resource<List<Note>>> = flow {

        emit(Resource.Loading())
        val notes = dao.getNotes(userId).map { it.toNote() }

        if (notes.isEmpty()) {
            dao.deleteNotes()
            val ref = firebaseDatabase.database.getReference("users")
            try {
                val result = ref.child(userId).child("notes").get().await()
                val remoteNotes = mutableListOf<Note>()
                for (dataValues in result.children) {
                    val note: Note? = dataValues.getValue(Note::class.java)
                    remoteNotes.add(note!!)
                }
                dao.insertNotes(remoteNotes.map{ NoteEntity(it.id, userId, it.title, it.content, it.timestamp)})
                emit(Resource.Success(remoteNotes))
            }catch (exception: FirebaseException){
                emit(Resource.Error(exception = CustomExceptions.UnknownException))
            }
        } else {
            emit(Resource.Success(notes))
        }
    }

    override fun getImageUrlForNote(noteId: String): Flow<Resource<Uri>> = callbackFlow {

        trySend(Resource.Loading())
        val imageRef = firebaseStorage.child(noteId)

        val localFile = File.createTempFile(noteId, "jpg")

        imageRef.getFile(localFile).addOnSuccessListener {
            trySend(Resource.Success(Uri.fromFile(localFile)))
        }.addOnFailureListener {
            if(it !is StorageException)
                trySend(Resource.Error(exception = CustomExceptions.UnknownException))
        }

        awaitClose()
    }

}
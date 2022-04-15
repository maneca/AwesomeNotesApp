package com.joao.awesomenotesapp.data.repository

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseReference
import com.joao.awesomenotesapp.data.local.NoteDao
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class NotesRepositoryImp(
    private val firebaseDatabase: DatabaseReference,
    private val firebaseAuth: FirebaseAuth,
    private val dao: NoteDao
) : NotesRepository {
    override fun saveNote(
        userId: String,
        title: String,
        content: String,
        timestamp: Long
    ): Flow<Boolean> {
        return callbackFlow {
            val ref = firebaseDatabase.database.getReference("users")
            val key = ref.child(userId).child("notes").push().key
            if (key != null) {
                val note = Note(key, title, content, timestamp)
                ref.child(userId).child("notes").child(key).setValue(note)
                    .addOnSuccessListener {
                        trySend(true)
                    }
                    .addOnFailureListener {
                        trySend(false)
                    }
            }

            awaitClose()
        }
    }

    override fun editNote(title: String, content: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteNote(userId: String, noteId: String): Flow<Boolean> = flow {
            val ref = firebaseDatabase.database.getReference("users")

            try {
                ref.child(userId).child("notes").child(noteId).removeValue().await()
                emit(true)
            }
            catch (_: Exception) {
                emit(false)
            }
            catch (_: FirebaseException) {
                emit(false)
            }
    }

    override fun getNotes(userId: String): Flow<Resource<List<Note>>> = flow {

        emit(Resource.Loading())

        val notes = dao.getNotes().map { it.toNote() }
        emit(Resource.Loading(notes))

        try {
            val ref = firebaseDatabase.database.getReference("users")
            val result = ref.child(userId).child("notes").get().await()

            dao.deleteNotes()

            val remoteNotes = mutableListOf<Note>()
            for (dataValues in result.children) {
                val note: Note? = dataValues.getValue(Note::class.java)
                remoteNotes.add(note!!)
            }

            dao.insertNotes(remoteNotes.map { it.toNoteEntity() })
            emit(Resource.Success(remoteNotes))
        }
        catch (exception : FirebaseAuthException){
            emit(
                Resource.Error(
                    exception = exception.localizedMessage?.let { it ->
                        CustomExceptions.ConflictException(it)
                    })
            )
        }
        catch (exception: Exception) {
            emit(Resource.Error(exception = CustomExceptions.UnknownException))
        }
    }

    override fun logout(userId: String): Flow<Boolean> = flow {
        firebaseAuth.signOut()
        emit(true)
    }
}
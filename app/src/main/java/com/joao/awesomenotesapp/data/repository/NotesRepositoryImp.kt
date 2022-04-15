package com.joao.awesomenotesapp.data.repository

import com.google.firebase.FirebaseException
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

class NotesRepositoryImp(
    private val firebaseDatabase: DatabaseReference,
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

    override fun deleteNote(userId: String, noteId: String): Flow<Boolean> {
        return callbackFlow {
            val ref = firebaseDatabase.database.getReference("users")
            ref.child(userId).child("notes").child(noteId).removeValue()
                .addOnSuccessListener {
                    trySend(true)
                }
                .addOnFailureListener {
                    trySend(false)
                }

            awaitClose()
        }
    }

    override fun getNotes(userId: String): Flow<Resource<List<Note>>> {
        return callbackFlow {
            trySend(Resource.Loading())

            val ref = firebaseDatabase.database.getReference("users")
            ref.child(userId).child("notes").get()
                .addOnCompleteListener { it ->
                    if (it.isSuccessful) {
                        val notes = mutableListOf<Note>()

                        for (dataValues in it.result.children) {
                            val restaurantModel: Note? = dataValues.getValue(Note::class.java)
                            notes.add(restaurantModel!!)
                        }

                        trySend(Resource.Success(notes))
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception is FirebaseException) {
                        trySend(Resource.Error(exception = CustomExceptions.ApiNotResponding))
                    } else {
                        trySend(
                            Resource.Error(
                                exception = (exception as FirebaseAuthException).localizedMessage?.let { it ->
                                    CustomExceptions.ConflictException(it)
                                })
                        )
                    }

                }

            awaitClose()
        }
    }
}
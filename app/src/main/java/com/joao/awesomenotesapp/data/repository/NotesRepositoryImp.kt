package com.joao.awesomenotesapp.data.repository

import com.joao.awesomenotesapp.data.local.NoteDao
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.Resource
import com.google.firebase.database.DatabaseReference
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
            val note = Note(title, content, timestamp)
            val ref = firebaseDatabase.database.getReference("users")
            val ref2 = firebaseDatabase.database.reference
            val key: String? = ref.push().key
            if (key != null) {
                ref.child(userId).setValue(note)
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

    override fun deleteNote(noteId: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getNotes(): Flow<Resource<List<Note>>> {
        return callbackFlow {
            trySend(Resource.Loading())

            val notes = dao.getNotes().map { it.toNote() }
            trySend(Resource.Loading(notes))


            awaitClose()
        }
    }
}
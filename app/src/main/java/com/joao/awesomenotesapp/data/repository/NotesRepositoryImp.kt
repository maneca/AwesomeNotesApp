package com.joao.awesomenotesapp.data.repository

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseReference
import com.joao.awesomenotesapp.data.local.NoteDao
import com.joao.awesomenotesapp.data.local.entity.NoteEntity
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.flow.Flow
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
        timestamp: Long,
        hasInternetConnection: Boolean
    ): Flow<Boolean>  = flow{
        if(hasInternetConnection){
            val ref = firebaseDatabase.database.getReference("users")
            val key = ref.child(userId).child("notes").push().key
            if (key != null) {
                val note = Note(key, title, content, timestamp)
                try{
                    ref.child(userId).child("notes").child(key).setValue(note).await()
                    emit(true)
                }catch (exception : FirebaseAuthException){
                    emit(false)
                }
                catch (exception: Exception) {
                    emit(false)
                }
            }
        }else{
            val note = NoteEntity(timestamp.toString(), title, content, timestamp)
            dao.insertNote(note)
            emit(true)
        }

    }

    override fun editNote(title: String, content: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override fun deleteNote(userId: String, noteId: String, hasInternetConnection: Boolean): Flow<Boolean> = flow {
        val ref = firebaseDatabase.database.getReference("users")

        if(hasInternetConnection){
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
        }else{
            dao.deleteNote(noteId)
            emit(true)
        }
    }

    override fun getNotes(userId: String, hasInternetConnection: Boolean): Flow<Resource<List<Note>>> = flow {

        emit(Resource.Loading())
        val notes = dao.getNotes().map { it.toNote() }

        if(hasInternetConnection){
            try {
                val ref = firebaseDatabase.database.getReference("users")
                val result = ref.child(userId).child("notes").get().await()

                val remoteNotes = mutableListOf<Note>()
                for (dataValues in result.children) {
                    val note: Note? = dataValues.getValue(Note::class.java)
                    remoteNotes.add(note!!)
                }

                if(remoteNotes.size > notes.size ||
                        remoteNotes.maxByOrNull { it.timestamp }!!.timestamp > notes.maxByOrNull { it.timestamp }!!.timestamp){
                    dao.deleteNotes()

                    dao.insertNotes(remoteNotes.map { it.toNoteEntity() })
                    emit(Resource.Success(remoteNotes))
                }else{
                    for(note in notes){
                        try{
                            ref.child(userId).child("notes").child(note.id).setValue(note).await()
                        }catch (exception : FirebaseAuthException){
                            emit(Resource.Error(exception = CustomExceptions.UnknownException))
                        }catch (exception: Exception) {
                            emit(Resource.Error(exception = CustomExceptions.UnknownException))
                        }
                    }

                    emit(Resource.Success(notes))
                }

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
        }else{
            emit(Resource.Success(notes))
        }
    }

    override fun logout(userId: String): Flow<Boolean> = flow {
        firebaseAuth.signOut()
        emit(true)
    }
}
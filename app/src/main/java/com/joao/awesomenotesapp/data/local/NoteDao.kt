package com.joao.awesomenotesapp.data.local

import androidx.room.*
import com.joao.awesomenotesapp.data.local.entity.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteNotes()

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("SELECT * FROM notes WHERE userId = :userId")
    suspend fun getNotes(userId: String): List<NoteEntity>

}
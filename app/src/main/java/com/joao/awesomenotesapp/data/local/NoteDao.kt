package com.joao.awesomenotesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.joao.awesomenotesapp.data.local.entity.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteNotes()

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("SELECT * FROM notes")
    suspend fun getNotes(): List<NoteEntity>

}
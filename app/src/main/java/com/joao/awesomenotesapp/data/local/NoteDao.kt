package com.joao.awesomenotesapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.joao.awesomenotesapp.data.local.entity.NoteEntity

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotes(notes: List<NoteEntity>)

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    fun deleteNotes()

    @Query("SELECT * FROM notes")
    suspend fun getNotes(): List<NoteEntity>

}
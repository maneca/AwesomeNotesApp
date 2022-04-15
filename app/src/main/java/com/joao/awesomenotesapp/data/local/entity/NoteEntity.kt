package com.joao.awesomenotesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.joao.awesomenotesapp.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id : Int,
    val title: String,
    val message: String,
    val timestamp: Long
) {
    fun toNote(): Note {
        return Note(
            title,
            message,
            timestamp,
        )
    }
}
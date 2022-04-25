package com.joao.awesomenotesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.joao.awesomenotesapp.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id : String,
    val userId: String,
    val title: String,
    val content: String,
    val timestamp: Long
) {
    fun toNote(): Note {
        return Note(
            id,
            title,
            content,
            timestamp,
        )
    }
}
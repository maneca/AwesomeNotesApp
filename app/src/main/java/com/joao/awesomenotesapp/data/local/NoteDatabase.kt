package com.joao.awesomenotesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.joao.awesomenotesapp.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1
)
abstract class NoteDatabase: RoomDatabase() {

    abstract val noteDao: NoteDao
}
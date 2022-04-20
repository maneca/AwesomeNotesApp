package com.joao.awesomenotesapp.domain.model

import com.google.firebase.database.IgnoreExtraProperties
import com.joao.awesomenotesapp.data.local.entity.NoteEntity

@IgnoreExtraProperties
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val imagePath: String = "",
    val timestamp: Long = 0
)

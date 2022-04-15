package com.joao.awesomenotesapp.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0
)

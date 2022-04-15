package com.joao.awesomenotesapp.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Note(
    val title: String,
    val message: String,
    val timestamp: Long
)

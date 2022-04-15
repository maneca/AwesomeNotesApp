package com.joao.awesomenotesapp.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val userId: String,
    val avatar: String,
    val notes: List<Note>
)

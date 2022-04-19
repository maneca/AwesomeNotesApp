package com.joao.awesomenotesapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface RegisterRepository {
    fun registerUser(email: String, password: String) : Flow<Resource<FirebaseUser?>>
}
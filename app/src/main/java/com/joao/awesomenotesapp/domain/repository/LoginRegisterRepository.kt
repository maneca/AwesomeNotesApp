package com.joao.awesomenotesapp.domain.repository

import com.joao.awesomenotesapp.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface LoginRegisterRepository {

    fun loginUser(email: String, password: String) : Flow<Resource<FirebaseUser?>>

    fun registerUser(email: String, password: String) : Flow<Resource<FirebaseUser?>>
}
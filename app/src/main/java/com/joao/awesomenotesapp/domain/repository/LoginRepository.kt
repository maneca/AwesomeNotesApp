package com.joao.awesomenotesapp.domain.repository

import com.joao.awesomenotesapp.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface LoginRepository {

    fun loginUser(email: String, password: String) : Flow<Resource<FirebaseUser?>>
}
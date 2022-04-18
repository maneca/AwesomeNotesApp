package com.joao.awesomenotesapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.joao.awesomenotesapp.domain.repository.LogoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LogoutRepositoryImp(
    private val firebaseAuth: FirebaseAuth
) : LogoutRepository {
    override fun logout(userId: String): Flow<Boolean> = flow {
        firebaseAuth.signOut()
        emit(true)
    }
}
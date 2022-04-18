package com.joao.awesomenotesapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface LogoutRepository {

    fun logout(userId: String): Flow<Boolean>
}
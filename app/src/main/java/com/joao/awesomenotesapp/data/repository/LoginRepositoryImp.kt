package com.joao.awesomenotesapp.data.repository

import com.joao.awesomenotesapp.domain.repository.LoginRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.Resource
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class LoginRepositoryImp(
    private val firebaseAuth: FirebaseAuth
) : LoginRepository {
    override fun loginUser(email: String, password: String): Flow<Resource<FirebaseUser?>> = flow {

        emit(Resource.Loading())
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit(Resource.Success(result.user))
        } catch (exception: FirebaseAuthException) {
            emit(
                Resource.Error(
                    exception = exception.localizedMessage?.let { it ->
                        CustomExceptions.ConflictException(it)
                    })
            )
        } catch (exception: FirebaseAuthInvalidUserException) {
            emit(
                Resource.Error(
                    exception = exception.localizedMessage?.let { it ->
                        CustomExceptions.ConflictException(it)
                    })
            )
        }catch (exception: FirebaseException) {
            emit(Resource.Error(exception = CustomExceptions.ApiNotResponding))
        }
    }
}
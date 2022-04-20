package com.joao.awesomenotesapp.data.repository

import com.joao.awesomenotesapp.domain.repository.LoginRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.Resource
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LoginRepositoryImp(
    private val firebaseAuth: FirebaseAuth
) : LoginRepository {
    override fun loginUser(email: String, password: String): Flow<Resource<FirebaseUser?>> =
        callbackFlow {
            trySend(Resource.Loading())
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    trySend(Resource.Success(it.user))
                }
                .addOnFailureListener { exception ->
                    when (exception) {
                        is FirebaseAuthException ->
                            trySend(
                                Resource.Error(
                                    exception = exception.localizedMessage?.let { it ->
                                        CustomExceptions.ConflictException(it)
                                    })
                            )
                        is FirebaseAuthInvalidUserException ->
                            trySend(
                                Resource.Error(
                                    exception = exception.localizedMessage?.let { it ->
                                        CustomExceptions.ConflictException(it)
                                    })
                            )
                        is FirebaseException ->
                            trySend(Resource.Error(exception = CustomExceptions.ApiNotResponding))
                    }

                }
            awaitClose()
        }
}
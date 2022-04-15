package com.joao.awesomenotesapp.data.repository

import com.joao.awesomenotesapp.domain.repository.LoginRegisterRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.Resource
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class LoginRegisterRepositoryImp(
    private val firebaseAuth: FirebaseAuth
): LoginRegisterRepository {
    override fun loginUser(email: String, password: String): Flow<Resource<FirebaseUser?>> {
        return callbackFlow {
            trySend(Resource.Loading())
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        trySend(Resource.Success(firebaseAuth.currentUser))
                    }
                }
                .addOnFailureListener { exception ->
                    if(exception is FirebaseException){
                        trySend(Resource.Error(exception = CustomExceptions.ApiNotResponding))
                    }else{
                        trySend(Resource.Error(
                            exception = (exception as FirebaseAuthException).localizedMessage?.let { it ->
                                CustomExceptions.ConflictException(it)
                            }))
                    }

                }
            awaitClose()
        }
    }

    override fun registerUser(email: String, password: String): Flow<Resource<FirebaseUser?>> {
        return callbackFlow {
            trySend(Resource.Loading())
            firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        trySend(Resource.Success(firebaseAuth.currentUser))
                    }
                }
                .addOnFailureListener { exception ->
                    if(exception is FirebaseException){
                        trySend(Resource.Error(exception = CustomExceptions.ApiNotResponding))
                    }else{
                        trySend(Resource.Error(
                            exception = (exception as FirebaseAuthException).localizedMessage?.let { it ->
                                CustomExceptions.ConflictException(it)
                            }))
                    }
                }
            awaitClose()
        }
    }
}
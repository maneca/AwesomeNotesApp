package com.joao.awesomenotesapp.data.repository

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.RegisterRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class RegisterRepositoryImp(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: DatabaseReference
) : RegisterRepository {

    override fun registerUser(email: String, password: String): Flow<Resource<FirebaseUser?>> =
        callbackFlow {
            trySend(Resource.Loading())
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    result.user?.uid?.let { userId ->
                        firebaseDatabase.database.getReference("users").child(userId).child("notes")
                            .setValue("")
                            .addOnSuccessListener {
                                trySend(Resource.Success(result.user))
                            }
                            .addOnFailureListener { exception ->
                                when (exception) {
                                    is FirebaseAuthException -> trySend(
                                        Resource.Error(
                                            exception = exception.localizedMessage?.let { it ->
                                                CustomExceptions.ConflictException(it)
                                            })
                                    )
                                    is FirebaseAuthInvalidUserException -> trySend(
                                        Resource.Error(
                                            exception = exception.localizedMessage?.let { it ->
                                                CustomExceptions.ConflictException(it)
                                            })
                                    )
                                    is FirebaseException -> trySend(Resource.Error(exception = CustomExceptions.ApiNotResponding))
                                }
                            }
                    }

                }
                .addOnFailureListener { exception ->
                    when (exception) {
                        is FirebaseAuthException -> trySend(
                            Resource.Error(
                                exception = exception.localizedMessage?.let { it ->
                                    CustomExceptions.ConflictException(it)
                                })
                        )
                        is FirebaseAuthInvalidUserException -> trySend(
                            Resource.Error(
                                exception = exception.localizedMessage?.let { it ->
                                    CustomExceptions.ConflictException(it)
                                })
                        )
                        is FirebaseException -> trySend(Resource.Error(exception = CustomExceptions.ApiNotResponding))
                    }
                }
            awaitClose()
        }
}
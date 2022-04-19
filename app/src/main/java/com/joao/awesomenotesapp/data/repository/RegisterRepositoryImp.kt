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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class RegisterRepositoryImp(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: DatabaseReference
) : RegisterRepository {

    override fun registerUser(email: String, password: String): Flow<Resource<FirebaseUser?>> = flow {
        emit(Resource.Loading())
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid?.let {
                firebaseDatabase.database.getReference("users").child(it).child("notes").setValue("").await()
            }
            emit(Resource.Success(result.user))
        }catch (exception: FirebaseAuthException) {
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
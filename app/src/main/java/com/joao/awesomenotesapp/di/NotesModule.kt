package com.joao.awesomenotesapp.di

import android.app.Application
import androidx.room.Room
import com.joao.awesomenotesapp.data.local.NoteDatabase
import com.joao.awesomenotesapp.data.repository.LoginRepositoryImp
import com.joao.awesomenotesapp.data.repository.NotesRepositoryImp
import com.joao.awesomenotesapp.domain.repository.LoginRepository
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.DefaultDispatcherProvider
import com.joao.awesomenotesapp.util.DispatcherProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.joao.awesomenotesapp.data.repository.LogoutRepositoryImp
import com.joao.awesomenotesapp.data.repository.RegisterRepositoryImp
import com.joao.awesomenotesapp.domain.repository.LogoutRepository
import com.joao.awesomenotesapp.domain.repository.RegisterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NotesModule {

    @Provides
    @Singleton
    fun provideLoginRepository(): LoginRepository{
        return LoginRepositoryImp(
            FirebaseAuth.getInstance()
        )
    }

    @Provides
    @Singleton
    fun provideRegisterRepository(): RegisterRepository{
        return RegisterRepositoryImp(
            FirebaseAuth.getInstance(),
            Firebase.database.reference
        )
    }

    @Provides
    @Singleton
    fun provideNotesRepository(
        database: NoteDatabase
    ): NotesRepository {
        return NotesRepositoryImp(Firebase.database.reference, database.noteDao)
    }

    @Provides
    @Singleton
    fun provideLogoutRepository(): LogoutRepository {
        return LogoutRepositoryImp(FirebaseAuth.getInstance())
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideNotesDatabase(app: Application): NoteDatabase{
        return Room.databaseBuilder(
            app, NoteDatabase::class.java, "notes_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providesDispatcher(): DispatcherProvider = DefaultDispatcherProvider()
}
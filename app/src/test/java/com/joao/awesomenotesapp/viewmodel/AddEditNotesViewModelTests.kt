package com.joao.awesomenotesapp.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joao.awesomenotesapp.MainCoroutineRule
import com.joao.awesomenotesapp.NotesApplication
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.*
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File


@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class AddEditNotesViewModelTests {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var mockRepo: NotesRepository

    @MockK
    private lateinit var mockUri: Uri

    @MockK
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @MockK
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var viewModel: AddEditNotesViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatcherProvider = object : DispatcherProvider {
        override fun default(): CoroutineDispatcher = testDispatcher
        override fun io(): CoroutineDispatcher = testDispatcher
        override fun main(): CoroutineDispatcher = testDispatcher
        override fun unconfined(): CoroutineDispatcher = testDispatcher

    }

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `save note, successful`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()
        mockkStatic(Uri::class)
        coEvery { mockRepo.saveNote(any(), any(), any(), any(), any(), any() ) } returns flowOf(Resource.Success(true))
        every { Uri.fromFile(File("")) } returns mockUri
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "userA"

        val savedStateHandle = SavedStateHandle().apply {
            set("note", Note().toJson())
        }
        viewModel = AddEditNotesViewModel(savedStateHandle, mockFirebaseAuth, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.saveNote("aaa", "111", "title", Uri.fromFile(File("")))
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.NoteSaved)
        }
    }

    @Test
    fun `save note, fail`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()
        mockkStatic(Uri::class)
        coEvery { mockRepo.saveNote(any(), any(), any(), any(), any(),any() ) } returns flowOf(Resource.Error(exception = CustomExceptions.UnknownException))
        every { Uri.fromFile(File("")) } returns mockUri
        coEvery { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { mockFirebaseUser.uid } returns "userA"

        val savedStateHandle = SavedStateHandle().apply {
            set("note", Note().toJson())
        }
        viewModel = AddEditNotesViewModel(savedStateHandle, mockFirebaseAuth, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.saveNote("aaa", "111", "title", Uri.fromFile(File("")))
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.Failed)
        }
    }

}
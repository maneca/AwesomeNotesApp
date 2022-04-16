package com.joao.awesomenotesapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.joao.awesomenotesapp.MainCoroutineRule
import com.joao.awesomenotesapp.NotesApplication
import com.joao.awesomenotesapp.R
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.*
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
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

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class NotesViewModelTests {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var mockRepo: NotesRepository

    @MockK
    private lateinit var mockApplication: NotesApplication

    private lateinit var viewModel: NotesViewModel

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
    fun `user logout, successful`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.logout(any() ) } returns flowOf(true)

        viewModel = NotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.logoutUser("111",true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.UserLoggedOut)
        }
    }

    @Test
    fun `user logout, fail`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.logout(any()) } returns flowOf(false)

        viewModel = NotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.logoutUser("111", true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.Failed)
        }
    }

    @Test
    fun `user logout, no internet`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.logout(any()) } returns flowOf(false)

        viewModel = NotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.logoutUser("111", false)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.NoInternetConnection)
        }
    }

    @Test
    fun `delete note, successful`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.deleteNote(any(), any(), any() ) } returns flowOf(true)

        viewModel = NotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.deleteNote("111", "223",true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.NoteDeleted)
        }
    }

    @Test
    fun `delete note, fail`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.deleteNote(any(), any(), any() ) } returns flowOf(false)

        viewModel = NotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.deleteNote("111", "223",true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.Failed)
        }
    }

    @Test
    fun `get notes, successful`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.getNotes(any(), any() ) } returns flowOf(Resource.Success(listOf()))

        viewModel = NotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.state.test {
            viewModel.getNotes("111",true)
            val emission = awaitItem()
            Truth.assertThat(emission.notes.size).isEqualTo(0)
            Truth.assertThat(emission.loading).isEqualTo(false)
        }
    }
}
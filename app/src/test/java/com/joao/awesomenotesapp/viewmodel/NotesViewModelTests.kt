package com.joao.awesomenotesapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.joao.awesomenotesapp.MainCoroutineRule
import com.joao.awesomenotesapp.domain.model.Note
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
    fun `delete note, successful`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.deleteNote(any() ) } returns flowOf(true)
        val savedStateHandle = SavedStateHandle().apply {
            set("note", Note().toJson())
        }

        viewModel = NotesViewModel(savedStateHandle, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.deleteNote("111", "223")
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.NoteDeleted)
        }
    }

    @Test
    fun `delete note, fail`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.deleteNote(any()) } returns flowOf(false)

        val savedStateHandle = SavedStateHandle().apply {
            set("note", Note().toJson())
        }
        viewModel = NotesViewModel(savedStateHandle, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.deleteNote("111", "223")
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.Failed)
        }
    }

    @Test
    fun `get notes, successful`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.getNotes(any()) } returns flowOf(Resource.Success(listOf()))
        val savedStateHandle = SavedStateHandle().apply {
            set("note", Note().toJson())
        }
        viewModel = NotesViewModel(savedStateHandle, testDispatcherProvider, mockRepo)
        viewModel.state.test {
            viewModel.getNotes("111")
            val emission = awaitItem()
            Truth.assertThat(emission.notes.size).isEqualTo(0)
            Truth.assertThat(emission.loading).isEqualTo(false)
        }
    }
}
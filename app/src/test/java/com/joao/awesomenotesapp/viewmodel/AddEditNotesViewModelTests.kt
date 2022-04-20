package com.joao.awesomenotesapp.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.joao.awesomenotesapp.MainCoroutineRule
import com.joao.awesomenotesapp.NotesApplication
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.DispatcherProvider
import com.joao.awesomenotesapp.util.UiEvent
import com.joao.awesomenotesapp.util.toJson
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
        coEvery { mockRepo.saveNote(any(), any(), any(), any(), any() ) } returns flowOf(true)
        every { Uri.fromFile(File("")) } returns mockUri

        val savedStateHandle = SavedStateHandle().apply {
            set("note", Note().toJson())
        }
        viewModel = AddEditNotesViewModel(savedStateHandle, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.saveNote("aaa", "111", "title", "note", Uri.fromFile(File("")), true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.NoteSaved)
        }
    }

    @Test
    fun `save note, fail`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()
        mockkStatic(Uri::class)
        coEvery { mockRepo.saveNote(any(), any(), any(), any(),any() ) } returns flowOf(false)
        every { Uri.fromFile(File("")) } returns mockUri

        val savedStateHandle = SavedStateHandle().apply {
            set("note", Note().toJson())
        }
        viewModel = AddEditNotesViewModel(savedStateHandle, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.saveNote("aaa", "111", "title", "note", Uri.fromFile(File("")), true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.Failed)
        }
    }

}
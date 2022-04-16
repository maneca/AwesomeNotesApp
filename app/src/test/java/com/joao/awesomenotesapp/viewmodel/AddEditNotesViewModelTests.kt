package com.joao.awesomenotesapp.viewmodel

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.joao.awesomenotesapp.MainCoroutineRule
import com.joao.awesomenotesapp.NotesApplication
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.DispatcherProvider
import com.joao.awesomenotesapp.util.UiEvent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
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
import org.mockito.Mock
import org.mockito.Mockito.`when`


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
    private lateinit var mockApplication: NotesApplication

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

        coEvery { mockRepo.saveNote(any(), any(), any(), any(), true ) } returns flowOf(true)

        viewModel = AddEditNotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.saveNote("111", "2323", true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.NoteSaved)
        }
    }

    @Test
    fun `save note, fail`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()

        coEvery { mockRepo.saveNote(any(), any(), any(), any(), true ) } returns flowOf(false)

        viewModel = AddEditNotesViewModel(mockApplication, testDispatcherProvider, mockRepo)
        viewModel.eventFlow.test {
            viewModel.saveNote("111", "2323", true)
            val emission = awaitItem()
            Truth.assertThat(emission).isEqualTo(UiEvent.Failed)
        }
    }

}
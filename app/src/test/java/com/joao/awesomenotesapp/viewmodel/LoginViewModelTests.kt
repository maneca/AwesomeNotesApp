package com.joao.awesomenotesapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.Modifier.Companion.any
import androidx.core.util.PatternsCompat
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joao.awesomenotesapp.MainCoroutineRule
import com.joao.awesomenotesapp.domain.repository.LoginRepository
import com.joao.awesomenotesapp.domain.repository.LogoutRepository
import com.joao.awesomenotesapp.util.CustomExceptions
import com.joao.awesomenotesapp.util.DispatcherProvider
import com.joao.awesomenotesapp.util.Resource
import com.joao.awesomenotesapp.util.UiEvent
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
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
class LoginViewModelTests {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var mockRepo: LoginRepository

    @MockK
    private lateinit var mockFirebaseUser: FirebaseUser

    @MockK
    private lateinit var mockFirebaseAuth: FirebaseAuth

    private lateinit var viewModel: LoginViewModel

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
    fun `user login, successful`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()
        mockkObject(PatternsCompat.EMAIL_ADDRESS)
        coEvery { mockRepo.loginUser(any(), any() ) } returns flowOf(Resource.Success(mockFirebaseUser))
        coEvery { mockFirebaseAuth.currentUser } returns mockFirebaseUser
        every { PatternsCompat.EMAIL_ADDRESS.matcher(any()).matches() } returns true

        viewModel = LoginViewModel(testDispatcherProvider, mockFirebaseAuth, mockRepo)
        viewModel.state.test {
            viewModel.loginUser("111","jhsdbfksd")
            val emission = awaitItem()
            Truth.assertThat(emission.user).isNotNull()
        }
    }

    @Test
    fun `user login, failed`() = runTest (testDispatcher){

        Truth.assertThat(mockRepo).isNotNull()
        mockkObject(PatternsCompat.EMAIL_ADDRESS)
        coEvery { mockRepo.loginUser(any(), any() ) } returns flowOf(Resource.Success(null))
        coEvery { mockFirebaseAuth.currentUser } returns null
        every { PatternsCompat.EMAIL_ADDRESS.matcher(any()).matches() } returns true

        viewModel = LoginViewModel(testDispatcherProvider, mockFirebaseAuth, mockRepo)
        viewModel.state.test {
            viewModel.loginUser("111","jhsdbfksd")
            val emission = awaitItem()
            Truth.assertThat(emission.user).isNull()
        }
    }

}
package com.joao.awesomenotesapp.repository

import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.joao.awesomenotesapp.data.local.NoteDao
import com.joao.awesomenotesapp.data.local.entity.NoteEntity
import com.joao.awesomenotesapp.data.repository.NotesRepositoryImp
import com.joao.awesomenotesapp.domain.model.Note
import com.joao.awesomenotesapp.domain.repository.NotesRepository
import com.joao.awesomenotesapp.util.Resource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class NotesRepositoryTests {
    @MockK(relaxUnitFun = true)
    private lateinit var mockDao: NoteDao

    @MockK
    private lateinit var mockDatabaseReference: DatabaseReference

    @MockK(relaxUnitFun = true)
    private lateinit var mockFirebaseAuth: FirebaseAuth

    private lateinit var repository: NotesRepository

    companion object {
        val noteA = NoteEntity(
            id = "sjdnkdgneri94j",
            title = "Lista de compras",
            content = "Batatas, Cebolas, Fruta",
            timestamp = 2334535345)
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = NotesRepositoryImp(mockDatabaseReference, mockFirebaseAuth, mockDao)
    }

    @Test
    fun getNotesFromLocalDb() = runBlocking {
        Assert.assertNotNull(mockDao)

        coEvery { mockDao.getNotes() } coAnswers { listOf(noteA) }

        val list: ArrayList<Resource<List<Note>>> = ArrayList()
        repository.getNotes("1", false).collect {
            list.add(it)
        }

        Truth.assertThat(list.size).isEqualTo(2)
        Truth.assertThat(list[0] is Resource.Loading).isTrue()
        Truth.assertThat((list[1] as Resource.Success).data?.size).isEqualTo(1)
    }

    @Test
    fun getNotesFromNetwork() = runTest {
        Assert.assertNotNull(mockDao)
        Assert.assertNotNull(mockDatabaseReference)

        val mockDataSnapshot = mockk<DataSnapshot>()
        val task: Task<DataSnapshot> = mockk(relaxed = true)

        coEvery { mockDao.getNotes() } coAnswers { listOf() }
        every { mockDatabaseReference.database.getReference(any()) } returns  mockDatabaseReference
        coEvery { mockDatabaseReference.child("1").child("notes").get() } returns task
        //every { mockDataSnapshot.children }

        val list: ArrayList<Resource<List<Note>>> = ArrayList()
        repository.getNotes("1", true).collect {
            list.add(it)
        }

        Truth.assertThat(list.size).isEqualTo(2)
        Truth.assertThat(list[0] is Resource.Loading).isTrue()
        Truth.assertThat((list[1] as Resource.Success).data?.size).isEqualTo(1)
    }

}
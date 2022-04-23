package com.joao.awesomenotesapp

import androidx.room.Room
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.joao.awesomenotesapp.data.local.NoteDao
import com.joao.awesomenotesapp.data.local.NoteDatabase
import com.joao.awesomenotesapp.data.local.entity.NoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@SmallTest
class NoteDaoTests {
    private lateinit var database: NoteDatabase
    private lateinit var dao: NoteDao

    companion object {
        val noteA = NoteEntity(
            id = "sjdnkdgneri94j",
            title = "Lista de compras",
            content = "Batatas, Cebolas, Fruta",
            timestamp = 2334535345)

        val noteB = NoteEntity(
            id = "jn5tu5jnerooier",
            title = "Ligar para a sra. Maria",
            content =  "961234567",
            timestamp = 2334455345)
    }

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            NoteDatabase::class.java)
            .build()

        dao = database.noteDao
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getNotes() = runBlocking{
        dao.insertNotes(listOf(noteA, noteB))

        val notes = dao.getNotes()
        Assert.assertEquals(2, notes.size)
        Assert.assertEquals(noteA.title, notes[0].title)
        Assert.assertEquals(noteB.title, notes[1].title)
    }

    @Test
    fun deleteNews() = runBlocking{
        dao.insertNotes(listOf(noteA, noteB))

        var notes = dao.getNotes()
        Assert.assertEquals(2, notes.size)

        dao.deleteNotes()
        notes = dao.getNotes()

        Assert.assertEquals(0, notes.size)
    }

    @Test
    fun insertNote() = runBlocking{
        dao.insertNote(noteA)

        val notes = dao.getNotes()
        Assert.assertEquals(1, notes.size)

        Assert.assertEquals(noteA.title, notes[0].title)
        Assert.assertEquals(noteA.content, notes[0].content)
        Assert.assertEquals(noteA.timestamp, notes[0].timestamp)
    }

    @Test
    fun deleteNote() = runBlocking{
        dao.insertNotes(listOf(noteA, noteB))

        var notes = dao.getNotes()
        Assert.assertEquals(2, notes.size)

        dao.deleteNote(noteB.id)
        notes = dao.getNotes()

        Assert.assertEquals(1, notes.size)
    }
}
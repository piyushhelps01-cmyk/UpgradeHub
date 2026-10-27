package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.NcertChapterEntity
import com.example.data.local.NcertDao
import com.example.data.repository.JeeData
import com.example.data.repository.NcertRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NcertDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var ncertDao: NcertDao
    private lateinit var repository: NcertRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        ncertDao = database.ncertDao()
        repository = NcertRepository(ncertDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testEnsureSeededAndToggleStep() = runBlocking {
        // Seed database
        repository.ensureSeeded(JeeData.CHAPTERS)
        val initialList = repository.allChapters.first()
        assertEquals(JeeData.CHAPTERS.size, initialList.size)

        // Initial chapter 0 should have theoryRead == false
        val chapter0 = initialList.first { it.chapterId == 0 }
        assertFalse(chapter0.theoryRead)
        assertEquals(0, chapter0.completedStepsCount)

        // Toggle theoryRead
        repository.toggleStep(0, "read")
        val updated0 = ncertDao.getNcertChapterDirect(0)
        assertTrue(updated0 != null && updated0.theoryRead)
        assertEquals(1, updated0?.completedStepsCount)

        // Update notes
        val testNote = "Crucial note: Cl > F electron gain enthalpy exception"
        repository.updateNotes(0, testNote)
        val noteUpdated = ncertDao.getNcertChapterDirect(0)
        assertEquals(testNote, noteUpdated?.notes)

        // Mark all 7 steps done
        repository.markAllSteps(0, true)
        val allDone = ncertDao.getNcertChapterDirect(0)
        assertTrue(allDone != null && allDone.isCompleted)
        assertEquals(7, allDone?.completedStepsCount)
        assertEquals(100, allDone?.completionPercentage)
    }
}

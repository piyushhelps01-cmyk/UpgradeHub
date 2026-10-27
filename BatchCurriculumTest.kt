package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DailyGoalDao
import com.example.data.model.AutoPlanConfiguration
import com.example.data.repository.BatchCurriculumRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BatchCurriculumTest {

    private lateinit var database: AppDatabase
    private lateinit var dailyGoalDao: DailyGoalDao
    private lateinit var repository: BatchCurriculumRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dailyGoalDao = database.dailyGoalDao()
        repository = BatchCurriculumRepository(context, dailyGoalDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPresetBatchesContainClassWiseAndCompetitiveExams() {
        val presets = BatchCurriculumRepository.PRESET_BATCHES
        assertTrue("Presets should have multiple batches", presets.size >= 4)

        // Verify Class 12th JEE batch
        val lakshya = presets.find { it.id == "jee_class_12" }
        assertNotNull(lakshya)
        assertEquals("Class 12th", lakshya?.classGrade)
        assertEquals("JEE Main & Advanced", lakshya?.examType)
        assertTrue((lakshya?.totalLectures ?: 0) > 200)

        // Verify Class 11th JEE batch
        val arjuna = presets.find { it.id == "jee_class_11" }
        assertNotNull(arjuna)
        assertEquals("Class 11th", arjuna?.classGrade)

        // Verify Dropper batch
        val dropper = presets.find { it.id == "jee_dropper_turbo" }
        assertNotNull(dropper)
        assertEquals("Dropper / 12th Pass", dropper?.classGrade)

        // Verify NEET Competitive exam batch
        val neet = presets.find { it.id == "neet_yakeen" }
        assertNotNull(neet)
        assertEquals("NEET (Medical)", neet?.examType)
    }

    @Test
    fun testParseBatchFromUrl() {
        // Test custom URL parsing
        val testUrl = "https://pw.live/batches/lakshya-jee-2026-chapterwise"
        val batch = repository.parseBatchFromUrl(testUrl)
        assertNotNull(batch)
        assertTrue(batch.chapters.isNotEmpty())
        assertTrue(batch.totalLectures > 0)
        assertEquals(testUrl, batch.sourceUrl)

        // Test NEET URL parsing
        val neetUrl = "https://pw.live/batches/neet-yakeen-class12-curriculum"
        val neetBatch = repository.parseBatchFromUrl(neetUrl)
        assertEquals("NEET (Medical)", neetBatch.examType)
    }

    @Test
    fun testAutoPlanGenerationAndSettlement() = runBlocking {
        val batch = BatchCurriculumRepository.PRESET_BATCHES.first() // Lakshya JEE
        val config = AutoPlanConfiguration(
            targetDays = 45,
            dailyWorkingHours = 12f,
            selectedBatchId = batch.id
        )

        val result = repository.generateAndSettleAutoPlan(batch, config)

        assertNotNull(result)
        assertEquals(45, result.targetDays)
        assertEquals(12f, result.dailyWorkingHours, 0.01f)
        assertTrue(result.totalGeneratedGoals > 0)
        assertTrue(result.previewGoalsForDay1.isNotEmpty())

        // Verify goals were settled in Room Database for day 1
        val day1Goals = dailyGoalDao.getGoalsForDay(1).first()
        assertTrue("Goals should be persisted in Room DB", day1Goals.isNotEmpty())
        assertEquals(4, day1Goals.size) // 4 slots for 12 hours
    }
}

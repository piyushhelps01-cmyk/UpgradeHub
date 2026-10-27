package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DailyGoalDao
import com.example.data.repository.DailyGoalRepository
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
class DailyGoalDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dailyGoalDao: DailyGoalDao
    private lateinit var repository: DailyGoalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dailyGoalDao = database.dailyGoalDao()
        repository = DailyGoalRepository(dailyGoalDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testEnsureGoalsForDaySeedsTasks() = runBlocking {
        // Day 1 has 4 scheduled focus slots (Tuesday)
        val initialGoals = repository.ensureGoalsForDay(1)
        assertTrue(initialGoals.isNotEmpty())
        assertEquals(4, initialGoals.size)

        // All initial goals should be incomplete
        assertTrue(initialGoals.none { it.isCompleted })

        // Check categories exist
        val categories = initialGoals.map { it.category }
        assertTrue(categories.contains("Physics"))
    }

    @Test
    fun testToggleGoalCompletion() = runBlocking {
        repository.ensureGoalsForDay(1)
        val goals = repository.getGoalsForDay(1).first()
        val firstGoal = goals.first()
        assertFalse(firstGoal.isCompleted)

        // Toggle completed
        repository.toggleGoalCompleted(firstGoal.id, firstGoal.isCompleted)
        val updatedGoals = repository.getGoalsForDay(1).first()
        val updatedFirst = updatedGoals.first { it.id == firstGoal.id }
        assertTrue(updatedFirst.isCompleted)

        // Toggle back
        repository.toggleGoalCompleted(updatedFirst.id, updatedFirst.isCompleted)
        val revertedGoals = repository.getGoalsForDay(1).first()
        val revertedFirst = revertedGoals.first { it.id == firstGoal.id }
        assertFalse(revertedFirst.isCompleted)
    }

    @Test
    fun testAddCustomGoalAndDelete() = runBlocking {
        repository.ensureGoalsForDay(1)
        val initialCount = repository.getGoalsForDay(1).first().size

        // Add custom goal
        val customId = repository.addCustomGoal(
            day = 1,
            title = "Solve 25 Electrostatics PYQs",
            category = "Physics",
            targetHours = 2.0f,
            priority = "HIGH"
        )
        assertTrue(customId > 0)

        val withCustom = repository.getGoalsForDay(1).first()
        assertEquals(initialCount + 1, withCustom.size)
        val foundCustom = withCustom.first { it.id == customId }
        assertEquals("Solve 25 Electrostatics PYQs", foundCustom.title)
        assertEquals(-1, foundCustom.slotIndex)

        // Delete custom goal
        repository.deleteGoal(customId)
        val afterDelete = repository.getGoalsForDay(1).first()
        assertEquals(initialCount, afterDelete.size)
    }

    @Test
    fun testSetAllGoalsCompletedAndReset() = runBlocking {
        repository.ensureGoalsForDay(1)

        // Mark all complete
        repository.setAllGoalsCompleted(1, true)
        val allDone = repository.getGoalsForDay(1).first()
        assertTrue(allDone.all { it.isCompleted })

        // Reset day goals
        repository.resetGoalsForDay(1)
        val resetGoals = repository.getGoalsForDay(1).first()
        assertTrue(resetGoals.all { !it.isCompleted })
    }

    @Test
    fun testEnsureAll60DaysGoalsSeededAndCompletionCalculation() = runBlocking {
        repository.ensureAll60DaysGoalsSeeded()
        val allGoals = repository.getAllGoals().first()
        // 60 days * 4 slots = 240 tasks
        assertEquals(240, allGoals.size)

        val totalCount = repository.totalGoalsCount.first()
        assertEquals(240, totalCount)

        val completedBefore = repository.completedGoalsCount.first()
        assertEquals(0, completedBefore)

        // Complete 12 tasks (e.g. 3 full days of 4 tasks each)
        for (d in 1..3) {
            repository.setAllGoalsCompleted(d, true)
        }

        val completedAfter = repository.completedGoalsCount.first()
        assertEquals(12, completedAfter)

        // Calculate percentage: 12 / 240 * 100 = 5.0%
        val completionPct = (completedAfter.toFloat() / totalCount.toFloat()) * 100f
        assertEquals(5.0f, completionPct, 0.01f)
    }

    @Test
    fun testSetGoalDoneBySlotSync() = runBlocking {
        repository.ensureGoalsForDay(1)
        repository.setGoalDoneBySlot(1, 0, true)
        val goals = repository.getGoalsForDay(1).first()
        val slot0Goal = goals.first { it.slotIndex == 0 }
        assertTrue(slot0Goal.isCompleted)

        repository.setGoalDoneBySlot(1, 0, false)
        val goalsAfter = repository.getGoalsForDay(1).first()
        val slot0GoalAfter = goalsAfter.first { it.slotIndex == 0 }
        assertFalse(slot0GoalAfter.isCompleted)
    }
}

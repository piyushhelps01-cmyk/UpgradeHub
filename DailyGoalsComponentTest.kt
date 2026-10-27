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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for the Daily Goals component's data fetching and completion toggling
 * using Room Database.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyGoalsComponentTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: DailyGoalDao
    private lateinit var repository: DailyGoalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dailyGoalDao()
        repository = DailyGoalRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testFetchCurrentDayTasksFromRoomDatabase() = runBlocking {
        // Ensure tasks are seeded in Room for Day 1
        val seeded = repository.ensureGoalsForDay(1)
        assertNotNull(seeded)
        assertTrue(seeded.isNotEmpty())

        // Fetch current day tasks via Flow
        val currentDayTasks = repository.getGoalsForDay(1).first()
        assertEquals(seeded.size, currentDayTasks.size)

        // Verify tasks belong to day 1 and default to incomplete
        assertTrue(currentDayTasks.all { it.dayNumber == 1 })
        assertTrue(currentDayTasks.none { it.isCompleted })
    }

    @Test
    fun testMarkTaskAsCompleteAndIncomplete() = runBlocking {
        // Seed Day 2
        repository.ensureGoalsForDay(2)
        val initialTasks = repository.getGoalsForDay(2).first()
        val targetTask = initialTasks.first()
        assertFalse(targetTask.isCompleted)

        // Mark complete
        repository.toggleGoalCompleted(targetTask.id, targetTask.isCompleted)

        val completedTasks = repository.getGoalsForDay(2).first()
        val updatedTask = completedTasks.first { it.id == targetTask.id }
        assertTrue("Task should be marked complete in Room DB", updatedTask.isCompleted)

        // Mark incomplete
        repository.toggleGoalCompleted(updatedTask.id, updatedTask.isCompleted)

        val revertedTasks = repository.getGoalsForDay(2).first()
        val revertedTask = revertedTasks.first { it.id == targetTask.id }
        assertFalse("Task should be reverted to incomplete in Room DB", revertedTask.isCompleted)
    }

    @Test
    fun testAddCustomTaskToRoomDatabaseAndToggle() = runBlocking {
        val currentDay = 5
        repository.ensureGoalsForDay(currentDay)
        val baseCount = repository.getGoalsForDay(currentDay).first().size

        // Add custom task
        val customId = repository.addCustomGoal(
            day = currentDay,
            title = "Revise Thermo Short Notes & Formula Sheet",
            category = "Physics",
            targetHours = 2.0f,
            priority = "HIGH"
        )
        assertTrue(customId > 0)

        val afterAdd = repository.getGoalsForDay(currentDay).first()
        assertEquals(baseCount + 1, afterAdd.size)
        val addedTask = afterAdd.first { it.id == customId }
        assertEquals("Revise Thermo Short Notes & Formula Sheet", addedTask.title)
        assertFalse(addedTask.isCompleted)

        // Mark custom task complete
        repository.toggleGoalCompleted(customId, false)
        val updatedAfterAdd = repository.getGoalsForDay(currentDay).first()
        assertTrue(updatedAfterAdd.first { it.id == customId }.isCompleted)
    }

    @Test
    fun testMarkAllTasksCompleteForDay() = runBlocking {
        val day = 10
        repository.ensureGoalsForDay(day)

        // Mark all complete
        repository.setAllGoalsCompleted(day, true)
        val allTasks = repository.getGoalsForDay(day).first()
        assertTrue(allTasks.all { it.isCompleted })

        // Reset
        repository.setAllGoalsCompleted(day, false)
        val resetTasks = repository.getGoalsForDay(day).first()
        assertTrue(resetTasks.none { it.isCompleted })
    }
}

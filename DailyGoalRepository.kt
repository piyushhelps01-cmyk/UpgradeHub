package com.example.data.repository

import com.example.data.local.DailyGoalDao
import com.example.data.local.DailyGoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class DailyGoalRepository(private val dao: DailyGoalDao) {

    fun getGoalsForDay(day: Int): Flow<List<DailyGoalEntity>> {
        return dao.getGoalsForDay(day)
    }

    fun getAllGoals(): Flow<List<DailyGoalEntity>> {
        return dao.getAllGoals()
    }

    val totalGoalsCount: Flow<Int> = dao.getTotalGoalsCount()
    val completedGoalsCount: Flow<Int> = dao.getCompletedGoalsCount()

    suspend fun getGoalById(id: Long): DailyGoalEntity? = withContext(Dispatchers.IO) {
        dao.getGoalById(id)
    }

    suspend fun setGoalDoneBySlot(day: Int, slotIndex: Int, isDone: Boolean) = withContext(Dispatchers.IO) {
        dao.setGoalCompletedBySlot(day, slotIndex, isDone)
    }

    suspend fun ensureAll60DaysGoalsSeeded() = withContext(Dispatchers.IO) {
        val existingCount = dao.getAllGoalsSync().size
        if (existingCount < 240) {
            val allToInsert = mutableListOf<DailyGoalEntity>()
            for (day in 1..60) {
                val existing = dao.getGoalsForDaySync(day)
                if (existing.isEmpty()) {
                    allToInsert.addAll(generateDefaultGoalsForDay(day))
                }
            }
            if (allToInsert.isNotEmpty()) {
                dao.insertGoals(allToInsert)
            }
        }
    }

    suspend fun ensureGoalsForDay(day: Int): List<DailyGoalEntity> = withContext(Dispatchers.IO) {
        val existing = dao.getGoalsForDaySync(day)
        if (existing.isNotEmpty()) {
            return@withContext existing
        }

        // Generate default scheduled tasks for this sprint day
        val defaultGoals = generateDefaultGoalsForDay(day)
        dao.insertGoals(defaultGoals)
        dao.getGoalsForDaySync(day)
    }

    suspend fun toggleGoalCompleted(id: Long, currentVal: Boolean) = withContext(Dispatchers.IO) {
        dao.setGoalCompleted(id, !currentVal)
    }

    suspend fun addCustomGoal(
        day: Int,
        title: String,
        category: String,
        targetHours: Float = 1.5f,
        priority: String = "NORMAL",
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val goal = DailyGoalEntity(
            dayNumber = day,
            title = title.trim(),
            category = category,
            targetHours = targetHours,
            isCompleted = false,
            priority = priority,
            slotIndex = -1,
            notes = notes
        )
        dao.insertGoal(goal)
    }

    suspend fun deleteGoal(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteGoalById(id)
    }

    suspend fun setAllGoalsCompleted(day: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        dao.setAllGoalsForDayCompleted(day, isCompleted)
    }

    suspend fun resetGoalsForDay(day: Int) = withContext(Dispatchers.IO) {
        dao.clearGoalsForDay(day)
        val defaultGoals = generateDefaultGoalsForDay(day)
        dao.insertGoals(defaultGoals)
    }

    private fun generateDefaultGoalsForDay(day: Int): List<DailyGoalEntity> {
        val dayIndex = (day - 1).coerceIn(0, 59)
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
            add(Calendar.DAY_OF_YEAR, dayIndex)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun..6=Sat
        val slotTitles = (JeeData.WEEKDAY_SLOTS[dayOfWeek]
            ?: listOf("12 Physics", "12 Chemistry", "12 Maths", "11 Physics")).take(4)
        val daySchedule = JeeData.DAY_SCHEDULES[day]

        return slotTitles.mapIndexed { idx, slotTitle ->
            val (category, specificTarget) = categorizeSlot(slotTitle, daySchedule)
            val fullTitle = "$slotTitle: $specificTarget"

            DailyGoalEntity(
                dayNumber = day,
                title = fullTitle,
                category = category,
                targetHours = 3.0f,
                isCompleted = false,
                priority = "HIGH",
                slotIndex = idx,
                notes = "Scheduled Slot ${idx + 1} (3 Hours)"
            )
        }
    }

    private fun categorizeSlot(
        slotTitle: String,
        daySchedule: JeeData.DayScheduleInfo?
    ): Pair<String, String> {
        return when {
            slotTitle.contains("Physics", ignoreCase = true) -> {
                val target = daySchedule?.physics ?: "Physics Lecture & DPP"
                "Physics" to target
            }
            slotTitle.contains("Chem", ignoreCase = true) -> {
                val target = daySchedule?.chem ?: "Chemistry Lecture & NCERT"
                "Chemistry" to target
            }
            slotTitle.contains("Maths", ignoreCase = true) -> {
                val target = daySchedule?.maths ?: "Maths Lecture & Practice"
                "Maths" to target
            }
            slotTitle.contains("Test", ignoreCase = true) || slotTitle.contains("Mock", ignoreCase = true) -> {
                val target = daySchedule?.backlogsOrTest ?: "Mock Test & Error Analysis"
                "Mock Test" to target
            }
            else -> {
                val target = daySchedule?.backlogsOrTest ?: "Formula Review & PYQ Sprint"
                "Revision" to target
            }
        }
    }
}

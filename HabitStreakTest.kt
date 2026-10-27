package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DailyGoalEntity
import com.example.data.model.HabitDayIndicator
import com.example.data.model.HabitStreakData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HabitStreakTest {

    @Test
    fun testHabitStreakDataStructure() {
        val days = listOf(
            HabitDayIndicator(
                dayNumber = 1,
                dateLabel = "Sep 1",
                dayOfWeek = "Tue",
                completedHours = 12.0f,
                targetHours = 12.0f,
                isGoalMet = true,
                isToday = false,
                isFuture = false,
                completedGoalsCount = 4,
                totalGoalsCount = 4
            ),
            HabitDayIndicator(
                dayNumber = 2,
                dateLabel = "Sep 2",
                dayOfWeek = "Wed",
                completedHours = 13.5f,
                targetHours = 12.0f,
                isGoalMet = true,
                isToday = false,
                isFuture = false,
                completedGoalsCount = 4,
                totalGoalsCount = 4
            ),
            HabitDayIndicator(
                dayNumber = 3,
                dateLabel = "Sep 3",
                dayOfWeek = "Thu",
                completedHours = 11.0f,
                targetHours = 12.0f,
                isGoalMet = true,
                isToday = false,
                isFuture = false,
                completedGoalsCount = 3,
                totalGoalsCount = 4
            ),
            HabitDayIndicator(
                dayNumber = 4,
                dateLabel = "Sep 4",
                dayOfWeek = "Fri",
                completedHours = 12.5f,
                targetHours = 12.0f,
                isGoalMet = true,
                isToday = true,
                isFuture = false,
                completedGoalsCount = 4,
                totalGoalsCount = 4
            )
        )

        val streakData = HabitStreakData(
            currentStreak = 4,
            bestStreak = 4,
            isTodayGoalMet = true,
            todayCompletedHours = 12.5f,
            todayTargetHours = 12.0f,
            todayProgressPct = 100,
            currentMilestoneName = "Spark",
            nextMilestoneName = "Iron Habit",
            nextMilestoneDays = 7,
            daysUntilNextMilestone = 3,
            milestoneProgressPct = 57,
            consecutiveDays = days,
            motivationalMessage = "Great momentum! Keep pushing towards Iron Habit!",
            streakRating = "Consistent 🌟"
        )

        assertEquals(4, streakData.currentStreak)
        assertTrue(streakData.isTodayGoalMet)
        assertEquals(3, streakData.daysUntilNextMilestone)
        assertEquals("Iron Habit", streakData.nextMilestoneName)
        assertEquals(4, streakData.consecutiveDays.size)
        assertTrue(streakData.consecutiveDays.all { it.isGoalMet })
    }
}

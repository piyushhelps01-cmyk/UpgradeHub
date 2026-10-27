package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_study_schedules",
    primaryKeys = ["dayNumber", "plannerDurationDays"]
)
data class DailyStudyScheduleEntity(
    val dayNumber: Int, // 1..150
    val plannerDurationDays: Int = 60, // 30, 45, 60, 75, 90, 120, 150
    val dateText: String = "",
    val dayOfWeek: String = "",
    val slot1Title: String = "Slot 1 (Physics)",
    val slot1Target: String = "",
    val slot1Done: Boolean = false,
    val slot1Hours: Float = 3.0f,
    val slot2Title: String = "Slot 2 (Mathematics / Biology)",
    val slot2Target: String = "",
    val slot2Done: Boolean = false,
    val slot2Hours: Float = 3.0f,
    val slot3Title: String = "Slot 3 (Chemistry)",
    val slot3Target: String = "",
    val slot3Done: Boolean = false,
    val slot3Hours: Float = 3.0f,
    val slot4Title: String = "Slot 4 (Revision / DPP / Backlog)",
    val slot4Target: String = "",
    val slot4Done: Boolean = false,
    val slot4Hours: Float = 3.0f,
    val actualHours: Float = 0f,
    val targetHours: Float = 12f,
    val dayScore: Int = 0, // 0..10
    val weakTopics: String = "",
    val nextDayPlan: String = "",
    val isCompleted: Boolean = false,
    val curriculumMilestone: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

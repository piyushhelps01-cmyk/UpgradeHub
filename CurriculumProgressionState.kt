package com.example.data.model

import com.example.data.local.DailyStudyScheduleEntity

data class CurriculumProgressionState(
    val currentDay: Int = 1,
    val totalDays: Int = 60,
    val completedDays: Int = 0,
    val progressPercentage: Float = 0f,
    val totalStudyHoursLogged: Float = 0f,
    val targetTotalHours: Float = 720f,
    val currentStreak: Int = 0,
    val averageDayScore: Float = 0f,
    val currentMilestone: String = "Sprint 1: Foundation & High-Weightage Kickoff",
    val pacingStatus: String = "On Track", // "On Track", "Behind Pace", "Pacing Ahead", "Sprint Mode"
    val activeSchedule: DailyStudyScheduleEntity? = null,
    val allSchedules: List<DailyStudyScheduleEntity> = emptyList(),
    val totalTasksCount: Int = 240,
    val completedTasksCount: Int = 0,
    val overallTaskCompletionPct: Float = 0f
)

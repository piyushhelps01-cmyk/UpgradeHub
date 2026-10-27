package com.example.data.model

data class HabitDayIndicator(
    val dayNumber: Int,
    val dateLabel: String,
    val dayOfWeek: String,
    val completedHours: Float,
    val targetHours: Float,
    val isGoalMet: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
    val completedGoalsCount: Int,
    val totalGoalsCount: Int
)

data class HabitStreakData(
    val currentStreak: Int,
    val bestStreak: Int,
    val isTodayGoalMet: Boolean,
    val todayCompletedHours: Float,
    val todayTargetHours: Float,
    val todayProgressPct: Int,
    val currentMilestoneName: String,
    val nextMilestoneName: String,
    val nextMilestoneDays: Int,
    val daysUntilNextMilestone: Int,
    val milestoneProgressPct: Int,
    val consecutiveDays: List<HabitDayIndicator>,
    val motivationalMessage: String,
    val streakRating: String,
    val todayCompletedSessions: Int = 0,
    val todayTotalSessions: Int = 4
)

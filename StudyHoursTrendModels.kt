package com.example.data.model

data class DailyStudyHoursPoint(
    val dayNumber: Int,
    val dateLabel: String,         // e.g. "01 Sep"
    val dayOfWeek: String,         // e.g. "Tue"
    val completedHours: Float,     // e.g. 12.0f
    val targetHours: Float,        // e.g. 12.0f or 15.0f
    val isTargetMet: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class ConsistencySummary(
    val totalHours: Float,
    val targetHours: Float,
    val averageHoursPerDay: Float,
    val consistencyScore: Int,       // 0-100%
    val daysCompletedTarget: Int,    // e.g. 5 of 7
    val totalDays: Int = 7,
    val currentStreak: Int,          // consecutive active days
    val bestDayNumber: Int,
    val bestDayHours: Float,
    val consistencyRating: String,   // "Exceptional Consistency", "Strong Momentum", etc.
    val ratingColorHex: Long,        // Color for rating badge
    val summaryNarrative: String     // Actionable advice/insight
)

data class SevenDayTrendData(
    val points: List<DailyStudyHoursPoint>,
    val consistencySummary: ConsistencySummary,
    val maxScaleHours: Float = 16f,
    val startDay: Int,
    val endDay: Int
)

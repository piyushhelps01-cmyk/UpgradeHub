package com.example.data.model

/**
 * 60-Day Student Study Hours & Syllabus Coverage Progress Models
 * Designed for D3.js and Recharts data visualization.
 */
data class SixtyDayProgressPoint(
    val dayNumber: Int,                  // 1..60
    val dateLabel: String,               // e.g. "Sep 01"
    val dayOfWeek: String,               // e.g. "Tue"
    val studyHours: Float,               // Daily focused study hours
    val cumulativeHours: Float,          // Running sum of study hours up to this day
    val targetHours: Float,              // Target for the day (12f or 15f)
    val cumulativeTargetHours: Float,    // Running sum of targets
    val syllabusCoveragePct: Float,      // 0f..100f percentage of syllabus covered
    val chaptersCompleted: Int,          // Cumulative chapters completed up to this day
    val movingAverageHours: Float,       // 7-day rolling average study hours
    val isTargetMet: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean,
    val isSelected: Boolean = false
)

data class SixtyDayAnalyticsSummary(
    val totalLoggedHours: Float,
    val totalTargetHours: Float,
    val currentCoveragePct: Float,
    val averageDailyHours: Float,
    val targetMetDaysCount: Int,
    val totalDays: Int = 60,
    val bestDayNumber: Int,
    val bestDayHours: Float,
    val projectedCompletionDay: Int,
    val paceStatus: String,              // "Ahead of Pace", "On Track", "Sprint Needed"
    val paceColorHex: Long,
    val summaryInsight: String
)

data class SixtyDayProgressData(
    val points: List<SixtyDayProgressPoint>,
    val summary: SixtyDayAnalyticsSummary,
    val maxStudyHours: Float = 18f,
    val maxCumulativeHours: Float = 750f
)

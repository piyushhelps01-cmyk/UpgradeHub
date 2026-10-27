package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity storing completed practice sessions, question drills, and accuracy telemetry
 * to support JEE 30, 45, 60, 75, 90, 120, and 150-day planner reviews.
 */
@Entity(tableName = "practice_session_history")
data class PracticeSessionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionTitle: String,
    val sessionType: String = "PYQ Drill", // "PYQ Drill", "DPP Solving", "NCERT Line-by-Line", "Formula Sprint", "Full Mock"
    val subject: String, // "Physics", "Chemistry", "Mathematics", "Biology"
    val chapterId: Int = 0,
    val chapterTitle: String = "",
    val questionsAttempted: Int = 0,
    val questionsCorrect: Int = 0,
    val accuracyPercentage: Float = 0f,
    val timeSpentMinutes: Int = 0,
    val plannerDurationDays: Int = 60, // 30, 45, 60, 75, 90, 120, 150
    val dayNumber: Int = 1,
    val moodRating: String = "CONFIDENT", // "CONFIDENT", "BALANCED", "NEEDS_REVISION"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

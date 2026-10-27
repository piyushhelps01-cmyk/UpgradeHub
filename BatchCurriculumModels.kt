package com.example.data.model

import com.example.data.local.DailyGoalEntity
import com.example.data.model.FacultyStack

data class BatchChapterPlan(
    val subject: String,
    val chapterName: String,
    val classGrade: String,
    val totalLectures: Int,
    val estimatedHours: Float,
    val priority: String = "High Weightage"
)

data class BatchCurriculum(
    val id: String,
    val title: String,
    val examType: String,
    val classGrade: String,
    val sourceUrl: String,
    val description: String,
    val totalLectures: Int,
    val totalHours: Float,
    val chapters: List<BatchChapterPlan>,
    val platform: String = "PW",
    val faculty: List<FacultyStack> = emptyList()
)

data class AutoPlanConfiguration(
    val targetDays: Int = 60, // Supports 30..150 day planners
    val dailyWorkingHours: Float = 12f,
    val lectureDurationHours: Float = 1.5f,
    val practiceRatio: Float = 1.0f, // 1h practice per 1.5h lecture
    val selectedBatchId: String = "jee_class_12",
    val parallelBatchIds: List<String> = listOf("jee_class_12") // Supports 2-3 parallel batches
)

data class AutoPlanResult(
    val batchTitle: String,
    val examType: String,
    val classGrade: String,
    val totalLectures: Int,
    val totalChapters: Int,
    val targetDays: Int,
    val dailyWorkingHours: Float,
    val lecturesPerDay: Float,
    val totalGeneratedGoals: Int,
    val previewGoalsForDay1: List<DailyGoalEntity>,
    val parallelBatchesCount: Int = 1,
    val parallelBatchTitles: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class SubjectBatchProgress(
    val subject: String,
    val completedChapters: Int,
    val totalChapters: Int,
    val completedLectures: Int,
    val totalLectures: Int,
    val percentage: Int
)

data class BatchCompletionStats(
    val batchId: String,
    val totalChapters: Int,
    val completedChapters: Int,
    val totalLectures: Int,
    val completedLectures: Int,
    val overallPercentage: Int,
    val subjectProgress: List<SubjectBatchProgress>
)

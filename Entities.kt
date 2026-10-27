package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Float,
    val timeSpentSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val questionId: String,
    val categoryName: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int,
    val explanation: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_progress")
data class StudyProgressEntity(
    @PrimaryKey
    val key: String,
    val value: String
)

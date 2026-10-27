package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing an NCERT Chapter and its 7-stage completion checklist,
 * notes on high-yield exceptions/tables, and update timestamps.
 */
@Entity(tableName = "ncert_chapters")
data class NcertChapterEntity(
    @PrimaryKey
    val chapterId: Int,
    val chapterTitle: String,
    val subject: String,
    val priority: String = "HIGH", // "HIGH", "MED"
    val theoryRead: Boolean = false, // 📖 NCERT Line-by-Line Reading & Margin Notes
    val inTextExamples: Boolean = false, // ✍️ In-Text Solved Examples
    val backExercises: Boolean = false, // 📝 Back Exercises & Numericals
    val markedLines: Boolean = false, // 🔍 High-Yield Lines, Reaction Tables & Exceptions
    val exemplarDone: Boolean = false, // 🎯 NCERT Exemplar Problems
    val revision1: Boolean = false, // 🔄 1st Active Recall Revision
    val revision2: Boolean = false, // 🌟 2nd Pre-Mock Polish
    val notes: String = "", // Student's personal notes on reactions/exceptions
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val completedStepsCount: Int
        get() = (if (theoryRead) 1 else 0) +
                (if (inTextExamples) 1 else 0) +
                (if (backExercises) 1 else 0) +
                (if (markedLines) 1 else 0) +
                (if (exemplarDone) 1 else 0) +
                (if (revision1) 1 else 0) +
                (if (revision2) 1 else 0)

    val totalStepsCount: Int get() = 7

    val isCompleted: Boolean get() = completedStepsCount == totalStepsCount

    val completionPercentage: Int
        get() = if (totalStepsCount > 0) (completedStepsCount * 100) / totalStepsCount else 0
}

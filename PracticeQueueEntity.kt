package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an item in the user's state-managed Practice Stack queue.
 * Allows students to prioritize pending chapters and specific focus areas for their next study session.
 */
@Entity(tableName = "practice_queue")
data class PracticeQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chapterId: Int,
    val chapterTitle: String,
    val subject: String,
    val priority: String = "HIGH", // "URGENT", "HIGH", "MEDIUM", "LOW"
    val focusArea: String = "PYQs & Question Practice", // "PYQs (30 Qs)", "DPP & Modules", "Prabal & Prakshit", "Formula & Notes Revision", "NCERT Line-by-Line"
    val targetMinutes: Int = 90,
    val sessionSlot: String = "Next Session", // "Next Session", "Morning Slot", "Afternoon Slot", "Night Slot"
    val orderIndex: Int = 0,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

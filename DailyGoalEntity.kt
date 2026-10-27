package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayNumber: Int, // 1 to 60
    val title: String,
    val category: String = "General", // "Physics", "Maths", "Chemistry", "Revision", "Mock Test", "General"
    val targetHours: Float = 3.0f,
    val isCompleted: Boolean = false,
    val priority: String = "HIGH", // "HIGH", "NORMAL", "LOW"
    val slotIndex: Int = -1, // 0..4 for scheduled slots, -1 for custom user goal
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

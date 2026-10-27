package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Eisenhower Matrix & Time-blocking Task Entity
 * Quadrants:
 * - DO_FIRST: Urgent & Important (High-yield chapter problem sets, Immediate test review)
 * - SCHEDULE: Not Urgent & Important (Deep conceptual study, NCERT line-by-line reading, Formula sheet synthesis)
 * - DELEGATE: Urgent & Not Important (Batch material printing, Library book return)
 * - ELIMINATE: Neither (Excessive social media, mindless scrolling)
 */
@Entity(tableName = "productivity_tasks")
data class ProductivityTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val quadrant: String = "DO_FIRST", // DO_FIRST, SCHEDULE, DELEGATE, ELIMINATE
    val priority: String = "P1", // P1 (Crucial), P2 (Medium), P3 (Low)
    val estimatedPomodoros: Int = 2,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val category: String = "Study", // Study, Exam, Routine, Personal
    val dueDate: String = "Today",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Student Financial Ledger & Expense/Subscription Tracker
 * Helps competitive exam aspirants balance coaching fees, test series renewals,
 * hostel/mess costs, and study material without stress.
 */
@Entity(tableName = "student_expenses")
data class StudentExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String = "BOOKS", // COACHING, TEST_SERIES, BOOKS, FOOD_MESS, TECH_STATIONERY, MISC
    val isSubscription: Boolean = false,
    val billingCycle: String = "ONE_TIME", // ONE_TIME, MONTHLY, QUARTERLY, ANNUAL
    val dateEpoch: Long = System.currentTimeMillis(),
    val notes: String = ""
)

/**
 * Atomic Habits & Daily Student Rituals
 * Morning routines, active recall sessions, screen-free wind-downs, and hydration.
 */
@Entity(tableName = "atomic_habits")
data class AtomicHabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val iconName: String = "Bolt",
    val category: String = "STUDY_FOCUS", // MORNING_ROUTINE, STUDY_FOCUS, PHYSICAL_WELLNESS, EVENING_WINDDOWN
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isCompletedToday: Boolean = false,
    val lastCompletedEpoch: Long = 0L,
    val reminderTime: String = "07:00 AM"
)

/**
 * Daily Holistic Wellness Log
 * Sleep hours, water hydration, mental stress/anxiety rating, and mindful breath resets.
 */
@Entity(tableName = "wellness_daily_logs")
data class WellnessDailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // e.g. "2026-10-07"
    val sleepHours: Float = 7.0f,
    val sleepQualityRating: Int = 4, // 1 to 5
    val waterIntakeMl: Int = 2000,
    val targetWaterMl: Int = 3000,
    val stressRating: Int = 3, // 1 to 10 (1 = Calm, 10 = Severe Burnout)
    val mindfulMinutes: Int = 10,
    val notes: String = "",
    val loggedAtEpoch: Long = System.currentTimeMillis()
)

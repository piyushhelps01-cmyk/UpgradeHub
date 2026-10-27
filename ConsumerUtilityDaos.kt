package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductivityTaskDao {
    @Query("SELECT * FROM productivity_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<ProductivityTaskEntity>>

    @Query("SELECT * FROM productivity_tasks WHERE quadrant = :quadrant ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksByQuadrant(quadrant: String): Flow<List<ProductivityTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ProductivityTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ProductivityTaskEntity>)

    @Update
    suspend fun updateTask(task: ProductivityTaskEntity)

    @Delete
    suspend fun deleteTask(task: ProductivityTaskEntity)

    @Query("UPDATE productivity_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun toggleTaskCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM productivity_tasks WHERE isCompleted = 1")
    suspend fun clearCompletedTasks()

    @Query("SELECT COUNT(*) FROM productivity_tasks")
    suspend fun getTaskCount(): Int
}

@Dao
interface StudentExpenseDao {
    @Query("SELECT * FROM student_expenses ORDER BY dateEpoch DESC")
    fun getAllExpenses(): Flow<List<StudentExpenseEntity>>

    @Query("SELECT * FROM student_expenses WHERE isSubscription = 1 ORDER BY dateEpoch DESC")
    fun getSubscriptions(): Flow<List<StudentExpenseEntity>>

    @Query("SELECT SUM(amount) FROM student_expenses")
    fun getTotalSpent(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: StudentExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<StudentExpenseEntity>)

    @Delete
    suspend fun deleteExpense(expense: StudentExpenseEntity)

    @Query("SELECT COUNT(*) FROM student_expenses")
    suspend fun getExpenseCount(): Int
}

@Dao
interface AtomicHabitDao {
    @Query("SELECT * FROM atomic_habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<AtomicHabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: AtomicHabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<AtomicHabitEntity>)

    @Update
    suspend fun updateHabit(habit: AtomicHabitEntity)

    @Delete
    suspend fun deleteHabit(habit: AtomicHabitEntity)

    @Query("UPDATE atomic_habits SET isCompletedToday = :completed, currentStreak = :streak, bestStreak = :bestStreak, lastCompletedEpoch = :epoch WHERE id = :id")
    suspend fun updateHabitProgress(id: Long, completed: Boolean, streak: Int, bestStreak: Int, epoch: Long)

    @Query("SELECT COUNT(*) FROM atomic_habits")
    suspend fun getHabitCount(): Int
}

@Dao
interface WellnessDailyLogDao {
    @Query("SELECT * FROM wellness_daily_logs ORDER BY dateString DESC")
    fun getAllLogs(): Flow<List<WellnessDailyLogEntity>>

    @Query("SELECT * FROM wellness_daily_logs WHERE dateString = :date LIMIT 1")
    suspend fun getLogForDate(date: String): WellnessDailyLogEntity?

    @Query("SELECT * FROM wellness_daily_logs ORDER BY dateString DESC LIMIT 1")
    fun getLatestLog(): Flow<WellnessDailyLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: WellnessDailyLogEntity): Long

    @Query("UPDATE wellness_daily_logs SET waterIntakeMl = :waterMl WHERE dateString = :date")
    suspend fun updateWaterForDate(date: String, waterMl: Int)

    @Query("SELECT COUNT(*) FROM wellness_daily_logs")
    suspend fun getLogCount(): Int
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {

    @Query("SELECT * FROM daily_goals WHERE dayNumber = :day ORDER BY slotIndex ASC, id ASC")
    fun getGoalsForDay(day: Int): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals WHERE dayNumber = :day ORDER BY slotIndex ASC, id ASC")
    suspend fun getGoalsForDaySync(day: Int): List<DailyGoalEntity>

    @Query("SELECT * FROM daily_goals ORDER BY dayNumber ASC, id ASC")
    fun getAllGoals(): Flow<List<DailyGoalEntity>>

    @Query("SELECT * FROM daily_goals ORDER BY dayNumber ASC, id ASC")
    suspend fun getAllGoalsSync(): List<DailyGoalEntity>

    @Query("SELECT * FROM daily_goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): DailyGoalEntity?

    @Query("SELECT * FROM daily_goals WHERE dayNumber = :day AND slotIndex = :slotIndex LIMIT 1")
    suspend fun getGoalByDayAndSlot(day: Int, slotIndex: Int): DailyGoalEntity?

    @Query("UPDATE daily_goals SET isCompleted = :isCompleted WHERE dayNumber = :day AND slotIndex = :slotIndex")
    suspend fun setGoalCompletedBySlot(day: Int, slotIndex: Int, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM daily_goals")
    fun getTotalGoalsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_goals WHERE isCompleted = 1")
    fun getCompletedGoalsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: DailyGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<DailyGoalEntity>)

    @Update
    suspend fun updateGoal(goal: DailyGoalEntity)

    @Query("UPDATE daily_goals SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setGoalCompleted(id: Long, isCompleted: Boolean)

    @Delete
    suspend fun deleteGoal(goal: DailyGoalEntity)

    @Query("DELETE FROM daily_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("UPDATE daily_goals SET isCompleted = :isCompleted WHERE dayNumber = :day")
    suspend fun setAllGoalsForDayCompleted(day: Int, isCompleted: Boolean)

    @Query("DELETE FROM daily_goals WHERE dayNumber = :day")
    suspend fun clearGoalsForDay(day: Int)

    @Query("DELETE FROM daily_goals")
    suspend fun clearAllGoals()
}

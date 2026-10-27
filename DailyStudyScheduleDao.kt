package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStudyScheduleDao {

    @Query("SELECT * FROM daily_study_schedules WHERE dayNumber = :dayNumber ORDER BY lastUpdated DESC LIMIT 1")
    fun getScheduleForDay(dayNumber: Int): Flow<DailyStudyScheduleEntity?>

    @Query("SELECT * FROM daily_study_schedules WHERE dayNumber = :dayNumber AND plannerDurationDays = :plannerDurationDays LIMIT 1")
    fun getScheduleForDayAndDuration(dayNumber: Int, plannerDurationDays: Int): Flow<DailyStudyScheduleEntity?>

    @Query("SELECT * FROM daily_study_schedules WHERE dayNumber = :dayNumber AND plannerDurationDays = :plannerDurationDays LIMIT 1")
    suspend fun getScheduleForDayAndDurationSync(dayNumber: Int, plannerDurationDays: Int): DailyStudyScheduleEntity?

    @Query("SELECT * FROM daily_study_schedules WHERE dayNumber = :dayNumber ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getScheduleForDaySync(dayNumber: Int): DailyStudyScheduleEntity?

    @Query("SELECT * FROM daily_study_schedules WHERE plannerDurationDays = :plannerDurationDays ORDER BY dayNumber ASC")
    fun getSchedulesForDuration(plannerDurationDays: Int): Flow<List<DailyStudyScheduleEntity>>

    @Query("SELECT * FROM daily_study_schedules WHERE plannerDurationDays = :plannerDurationDays ORDER BY dayNumber ASC")
    suspend fun getSchedulesForDurationSync(plannerDurationDays: Int): List<DailyStudyScheduleEntity>

    @Query("SELECT * FROM daily_study_schedules ORDER BY dayNumber ASC")
    fun getAllSchedules(): Flow<List<DailyStudyScheduleEntity>>

    @Query("SELECT * FROM daily_study_schedules ORDER BY dayNumber ASC")
    suspend fun getAllSchedulesSync(): List<DailyStudyScheduleEntity>

    @Query("SELECT COUNT(*) FROM daily_study_schedules WHERE isCompleted = 1")
    fun getCompletedDaysCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_study_schedules WHERE plannerDurationDays = :plannerDurationDays AND isCompleted = 1")
    fun getCompletedDaysCountForDuration(plannerDurationDays: Int): Flow<Int>

    @Query("SELECT SUM(actualHours) FROM daily_study_schedules")
    fun getTotalActualHours(): Flow<Float?>

    @Query("SELECT SUM(actualHours) FROM daily_study_schedules WHERE plannerDurationDays = :plannerDurationDays")
    fun getTotalActualHoursForDuration(plannerDurationDays: Int): Flow<Float?>

    @Query("SELECT AVG(dayScore) FROM daily_study_schedules WHERE dayScore > 0")
    fun getAverageDayScore(): Flow<Float?>

    @Query("SELECT AVG(dayScore) FROM daily_study_schedules WHERE plannerDurationDays = :plannerDurationDays AND dayScore > 0")
    fun getAverageDayScoreForDuration(plannerDurationDays: Int): Flow<Float?>

    @Query("SELECT COUNT(*) FROM daily_study_schedules")
    suspend fun getScheduleCount(): Int

    @Query("SELECT COUNT(*) FROM daily_study_schedules WHERE plannerDurationDays = :plannerDurationDays")
    suspend fun getScheduleCountForDuration(plannerDurationDays: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: DailyStudyScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<DailyStudyScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: DailyStudyScheduleEntity)

    @Query("UPDATE daily_study_schedules SET isCompleted = :isCompleted, lastUpdated = :timestamp WHERE dayNumber = :dayNumber AND plannerDurationDays = :plannerDurationDays")
    suspend fun setDayCompletedForDuration(dayNumber: Int, plannerDurationDays: Int, isCompleted: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE daily_study_schedules SET isCompleted = :isCompleted, lastUpdated = :timestamp WHERE dayNumber = :dayNumber")
    suspend fun setDayCompleted(dayNumber: Int, isCompleted: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE daily_study_schedules SET actualHours = :actualHours, dayScore = :dayScore, weakTopics = :weakTopics, nextDayPlan = :nextDayPlan, lastUpdated = :timestamp WHERE dayNumber = :dayNumber AND plannerDurationDays = :plannerDurationDays")
    suspend fun updateDayMetricsForDuration(
        dayNumber: Int,
        plannerDurationDays: Int,
        actualHours: Float,
        dayScore: Int,
        weakTopics: String,
        nextDayPlan: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE daily_study_schedules SET actualHours = :actualHours, dayScore = :dayScore, weakTopics = :weakTopics, nextDayPlan = :nextDayPlan, lastUpdated = :timestamp WHERE dayNumber = :dayNumber")
    suspend fun updateDayMetrics(
        dayNumber: Int,
        actualHours: Float,
        dayScore: Int,
        weakTopics: String,
        nextDayPlan: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM daily_study_schedules WHERE dayNumber = :dayNumber AND plannerDurationDays = :plannerDurationDays")
    suspend fun deleteScheduleForDuration(dayNumber: Int, plannerDurationDays: Int)

    @Query("DELETE FROM daily_study_schedules WHERE dayNumber = :dayNumber")
    suspend fun deleteSchedule(dayNumber: Int)

    @Query("DELETE FROM daily_study_schedules WHERE plannerDurationDays = :plannerDurationDays")
    suspend fun clearSchedulesForDuration(plannerDurationDays: Int)

    @Query("DELETE FROM daily_study_schedules")
    suspend fun clearAllSchedules()
}

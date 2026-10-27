package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeSessionHistoryDao {

    @Query("SELECT * FROM practice_session_history ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PracticeSessionHistoryEntity>>

    @Query("SELECT * FROM practice_session_history ORDER BY timestamp DESC")
    suspend fun getAllSessionsSync(): List<PracticeSessionHistoryEntity>

    @Query("SELECT * FROM practice_session_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 20): Flow<List<PracticeSessionHistoryEntity>>

    @Query("SELECT * FROM practice_session_history WHERE plannerDurationDays = :plannerDurationDays ORDER BY timestamp DESC")
    fun getSessionsForPlan(plannerDurationDays: Int): Flow<List<PracticeSessionHistoryEntity>>

    @Query("SELECT * FROM practice_session_history WHERE plannerDurationDays = :plannerDurationDays AND dayNumber = :dayNumber ORDER BY timestamp DESC")
    fun getSessionsForDay(plannerDurationDays: Int, dayNumber: Int): Flow<List<PracticeSessionHistoryEntity>>

    @Query("SELECT * FROM practice_session_history WHERE subject LIKE '%' || :subject || '%' ORDER BY timestamp DESC")
    fun getSessionsBySubject(subject: String): Flow<List<PracticeSessionHistoryEntity>>

    @Query("SELECT SUM(questionsAttempted) FROM practice_session_history")
    fun getTotalQuestionsAttempted(): Flow<Int?>

    @Query("SELECT SUM(questionsCorrect) FROM practice_session_history")
    fun getTotalQuestionsCorrect(): Flow<Int?>

    @Query("SELECT AVG(accuracyPercentage) FROM practice_session_history WHERE questionsAttempted > 0")
    fun getAverageAccuracy(): Flow<Float?>

    @Query("SELECT SUM(timeSpentMinutes) FROM practice_session_history")
    fun getTotalPracticeMinutes(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSessionHistoryEntity): Long

    @Query("DELETE FROM practice_session_history WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM practice_session_history")
    suspend fun clearAllSessions()
}

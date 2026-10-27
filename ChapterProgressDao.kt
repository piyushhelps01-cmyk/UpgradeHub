package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterProgressDao {

    @Query("SELECT * FROM chapter_progress ORDER BY chapterId ASC")
    fun getAllProgress(): Flow<List<ChapterProgressEntity>>

    @Query("SELECT * FROM chapter_progress ORDER BY chapterId ASC")
    suspend fun getAllProgressSync(): List<ChapterProgressEntity>

    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId LIMIT 1")
    fun getProgressForChapter(chapterId: Int): Flow<ChapterProgressEntity?>

    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId LIMIT 1")
    suspend fun getProgressForChapterSync(chapterId: Int): ChapterProgressEntity?

    @Query("SELECT * FROM chapter_progress WHERE subject LIKE '%' || :subject || '%' ORDER BY chapterId ASC")
    fun getProgressBySubject(subject: String): Flow<List<ChapterProgressEntity>>

    @Query("SELECT COUNT(*) FROM chapter_progress WHERE isCompleted = 1")
    fun getCompletedChaptersCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: ChapterProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<ChapterProgressEntity>)

    @Update
    suspend fun update(progress: ChapterProgressEntity)

    @Query("UPDATE chapter_progress SET isCompleted = :isCompleted, lastUpdated = :timestamp WHERE chapterId = :chapterId")
    suspend fun setChapterCompleted(chapterId: Int, isCompleted: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE chapter_progress SET notesContent = :notes, weakConcepts = :weakConcepts, lastUpdated = :timestamp WHERE chapterId = :chapterId")
    suspend fun updateNotes(chapterId: Int, notes: String, weakConcepts: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM chapter_progress WHERE chapterId = :chapterId")
    suspend fun deleteForChapter(chapterId: Int)

    @Query("DELETE FROM chapter_progress")
    suspend fun clearAll()
}

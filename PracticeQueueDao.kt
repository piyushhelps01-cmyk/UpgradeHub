package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeQueueDao {

    @Query("SELECT * FROM practice_queue ORDER BY isCompleted ASC, orderIndex ASC, addedAt DESC")
    fun getAllQueueItems(): Flow<List<PracticeQueueEntity>>

    @Query("SELECT * FROM practice_queue WHERE isCompleted = 0 ORDER BY orderIndex ASC, addedAt DESC")
    fun getPendingQueueItems(): Flow<List<PracticeQueueEntity>>

    @Query("SELECT * FROM practice_queue WHERE chapterId = :chapterId AND isCompleted = 0 LIMIT 1")
    suspend fun getPendingItemForChapter(chapterId: Int): PracticeQueueEntity?

    @Query("SELECT * FROM practice_queue WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): PracticeQueueEntity?

    @Query("SELECT COUNT(*) FROM practice_queue")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM practice_queue WHERE isCompleted = 0")
    suspend fun getPendingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PracticeQueueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PracticeQueueEntity>)

    @Update
    suspend fun updateItem(item: PracticeQueueEntity)

    @Query("DELETE FROM practice_queue WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM practice_queue WHERE isCompleted = 1")
    suspend fun clearCompletedItems()

    @Query("UPDATE practice_queue SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setItemCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE practice_queue SET orderIndex = :newOrder WHERE id = :id")
    suspend fun updateOrderIndex(id: Long, newOrder: Int)

    @Query("DELETE FROM practice_queue")
    suspend fun deleteAll()
}

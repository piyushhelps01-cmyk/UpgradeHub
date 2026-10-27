package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NcertDao {
    @Query("SELECT * FROM ncert_chapters ORDER BY chapterId ASC")
    fun getAllNcertChapters(): Flow<List<NcertChapterEntity>>

    @Query("SELECT * FROM ncert_chapters WHERE subject = :subject ORDER BY chapterId ASC")
    fun getNcertChaptersBySubject(subject: String): Flow<List<NcertChapterEntity>>

    @Query("SELECT * FROM ncert_chapters WHERE chapterId = :chapterId LIMIT 1")
    fun getNcertChapterById(chapterId: Int): Flow<NcertChapterEntity?>

    @Query("SELECT * FROM ncert_chapters WHERE chapterId = :chapterId LIMIT 1")
    suspend fun getNcertChapterDirect(chapterId: Int): NcertChapterEntity?

    @Query("SELECT COUNT(*) FROM ncert_chapters")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: NcertChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<NcertChapterEntity>)

    @Update
    suspend fun update(entity: NcertChapterEntity)

    @Query("UPDATE ncert_chapters SET notes = :notes, lastUpdated = :lastUpdated WHERE chapterId = :chapterId")
    suspend fun updateNotes(chapterId: Int, notes: String, lastUpdated: Long = System.currentTimeMillis())

    @Query("UPDATE ncert_chapters SET theoryRead = :state, inTextExamples = :state, backExercises = :state, markedLines = :state, exemplarDone = :state, revision1 = :state, revision2 = :state, lastUpdated = :lastUpdated WHERE chapterId = :chapterId")
    suspend fun markAllSteps(chapterId: Int, state: Boolean, lastUpdated: Long = System.currentTimeMillis())

    @Query("DELETE FROM ncert_chapters")
    suspend fun clearAll()
}

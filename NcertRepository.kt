package com.example.data.repository

import com.example.data.local.NcertChapterEntity
import com.example.data.local.NcertDao
import com.example.data.model.ChapterItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class NcertRepository(private val ncertDao: NcertDao) {

    val allChapters: Flow<List<NcertChapterEntity>> = ncertDao.getAllNcertChapters()

    /**
     * Seeds database with master syllabus chapters (both JEE and NEET) if not already populated.
     */
    suspend fun ensureSeeded(chapters: List<ChapterItem>) = withContext(Dispatchers.IO) {
        val toInsert = mutableListOf<NcertChapterEntity>()
        for (item in chapters) {
            val existing = ncertDao.getNcertChapterDirect(item.id)
            if (existing == null) {
                toInsert.add(
                    NcertChapterEntity(
                        chapterId = item.id,
                        chapterTitle = item.title,
                        subject = item.subject,
                        priority = item.priority
                    )
                )
            }
        }
        if (toInsert.isNotEmpty()) {
            ncertDao.insertAll(toInsert)
        }
    }

    /**
     * Toggles an individual checklist step for a chapter.
     */
    suspend fun toggleStep(chapterId: Int, stepKey: String) = withContext(Dispatchers.IO) {
        val current = ncertDao.getNcertChapterDirect(chapterId) ?: return@withContext
        val updated = when (stepKey.lowercase()) {
            "read", "theoryread" -> current.copy(theoryRead = !current.theoryRead, lastUpdated = System.currentTimeMillis())
            "examples", "intextexamples" -> current.copy(inTextExamples = !current.inTextExamples, lastUpdated = System.currentTimeMillis())
            "backexercises", "exercises" -> current.copy(backExercises = !current.backExercises, lastUpdated = System.currentTimeMillis())
            "marked", "markedlines" -> current.copy(markedLines = !current.markedLines, lastUpdated = System.currentTimeMillis())
            "exemplar", "exemplardone" -> current.copy(exemplarDone = !current.exemplarDone, lastUpdated = System.currentTimeMillis())
            "revision", "revision1" -> current.copy(revision1 = !current.revision1, lastUpdated = System.currentTimeMillis())
            "secondrevision", "revision2" -> current.copy(revision2 = !current.revision2, lastUpdated = System.currentTimeMillis())
            else -> current
        }
        ncertDao.update(updated)
    }

    /**
     * Marks all 7 checklist steps as completed or uncompleted for a chapter.
     */
    suspend fun markAllSteps(chapterId: Int, completed: Boolean) = withContext(Dispatchers.IO) {
        ncertDao.markAllSteps(chapterId, completed)
    }

    /**
     * Saves notes, key reactions, exception lines for a chapter.
     */
    suspend fun updateNotes(chapterId: Int, notes: String) = withContext(Dispatchers.IO) {
        ncertDao.updateNotes(chapterId, notes)
    }

    /**
     * Resets all NCERT checklist items and notes.
     */
    suspend fun resetAll(chapters: List<ChapterItem>) = withContext(Dispatchers.IO) {
        ncertDao.clearAll()
        val entities = chapters.map { item ->
            NcertChapterEntity(
                chapterId = item.id,
                chapterTitle = item.title,
                subject = item.subject,
                priority = item.priority
            )
        }
        ncertDao.insertAll(entities)
    }

    /**
     * Exports all NCERT checklist records as a JSON string.
     */
    suspend fun exportNcertJson(chaptersList: List<NcertChapterEntity>): String = withContext(Dispatchers.Default) {
        val array = JSONArray()
        chaptersList.forEach { c ->
            val obj = JSONObject().apply {
                put("chapterId", c.chapterId)
                put("chapterTitle", c.chapterTitle)
                put("subject", c.subject)
                put("priority", c.priority)
                put("theoryRead", c.theoryRead)
                put("inTextExamples", c.inTextExamples)
                put("backExercises", c.backExercises)
                put("markedLines", c.markedLines)
                put("exemplarDone", c.exemplarDone)
                put("revision1", c.revision1)
                put("revision2", c.revision2)
                put("notes", c.notes)
                put("lastUpdated", c.lastUpdated)
            }
            array.put(obj)
        }
        array.toString(2)
    }

    /**
     * Imports and updates NCERT checklist records from a JSON array.
     */
    suspend fun importNcertJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val chapterId = obj.getInt("chapterId")
                val entity = NcertChapterEntity(
                    chapterId = chapterId,
                    chapterTitle = obj.optString("chapterTitle", ""),
                    subject = obj.optString("subject", ""),
                    priority = obj.optString("priority", "HIGH"),
                    theoryRead = obj.optBoolean("theoryRead", false),
                    inTextExamples = obj.optBoolean("inTextExamples", false),
                    backExercises = obj.optBoolean("backExercises", false),
                    markedLines = obj.optBoolean("markedLines", false),
                    exemplarDone = obj.optBoolean("exemplarDone", false),
                    revision1 = obj.optBoolean("revision1", false),
                    revision2 = obj.optBoolean("revision2", false),
                    notes = obj.optString("notes", ""),
                    lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                )
                ncertDao.insertOrUpdate(entity)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

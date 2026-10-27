package com.example.data.repository

import com.example.data.local.PracticeQueueDao
import com.example.data.local.PracticeQueueEntity
import com.example.data.model.ChapterItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository providing clean, reactive data access for the Practice Stack Queue.
 */
class PracticeQueueRepository(private val dao: PracticeQueueDao) {

    val allQueueItems: Flow<List<PracticeQueueEntity>> = dao.getAllQueueItems()
    val pendingQueueItems: Flow<List<PracticeQueueEntity>> = dao.getPendingQueueItems()

    suspend fun addToQueue(
        chapterId: Int,
        chapterTitle: String,
        subject: String,
        priority: String = "HIGH",
        focusArea: String = "PYQs & Question Practice",
        targetMinutes: Int = 90,
        sessionSlot: String = "Next Session",
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        val item = PracticeQueueEntity(
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            subject = subject,
            priority = priority,
            focusArea = focusArea,
            targetMinutes = targetMinutes,
            sessionSlot = sessionSlot,
            orderIndex = count,
            notes = notes
        )
        dao.insertItem(item)
    }

    suspend fun toggleCompleted(id: Long, currentCompleted: Boolean) = withContext(Dispatchers.IO) {
        val newCompleted = !currentCompleted
        val completedTime = if (newCompleted) System.currentTimeMillis() else null
        dao.setItemCompleted(id, newCompleted, completedTime)
    }

    suspend fun removeFromQueue(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteItemById(id)
    }

    suspend fun clearCompleted() = withContext(Dispatchers.IO) {
        dao.clearCompletedItems()
    }

    suspend fun moveUp(id: Long) = withContext(Dispatchers.IO) {
        val item = dao.getItemById(id) ?: return@withContext
        if (item.orderIndex > 0) {
            dao.updateOrderIndex(id, item.orderIndex - 1)
        }
    }

    suspend fun moveDown(id: Long) = withContext(Dispatchers.IO) {
        val item = dao.getItemById(id) ?: return@withContext
        dao.updateOrderIndex(id, item.orderIndex + 1)
    }

    suspend fun prioritizeTop(id: Long) = withContext(Dispatchers.IO) {
        dao.updateOrderIndex(id, -1)
    }

    suspend fun seedInitialQueueIfEmpty(activeChapters: List<ChapterItem>) = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0 && activeChapters.isNotEmpty()) {
            val highPriorityChaps = activeChapters.filter { it.priority == "HIGH" }.take(3)
            val seedItems = highPriorityChaps.mapIndexed { index, chap ->
                PracticeQueueEntity(
                    chapterId = chap.id,
                    chapterTitle = chap.title,
                    subject = chap.subject,
                    priority = "HIGH",
                    focusArea = if (index == 0) "PYQs & Question Practice" else if (index == 1) "DPP & Module Sets" else "Prabal Level 2 Problems",
                    targetMinutes = if (index == 0) 120 else 90,
                    sessionSlot = if (index == 0) "Next Session" else "Evening Slot",
                    orderIndex = index,
                    notes = "Prioritized for rapid question mastery & formula review"
                )
            }
            dao.insertAll(seedItems)
        }
    }
}

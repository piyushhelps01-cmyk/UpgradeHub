package com.example.data.repository

import com.example.data.local.ChapterProgressDao
import com.example.data.local.ChapterProgressEntity
import com.example.data.model.ChapterChain
import com.example.data.model.ChapterItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository mediating access to Room chapter progress storage.
 */
class ChapterProgressRepository(
    private val dao: ChapterProgressDao
) {
    val allProgress: Flow<List<ChapterProgressEntity>> = dao.getAllProgress()
    val completedChaptersCount: Flow<Int> = dao.getCompletedChaptersCount()

    fun getProgressForChapter(chapterId: Int): Flow<ChapterProgressEntity?> {
        return dao.getProgressForChapter(chapterId)
    }

    suspend fun getProgressForChapterSync(chapterId: Int): ChapterProgressEntity? {
        return dao.getProgressForChapterSync(chapterId)
    }

    fun getProgressBySubject(subject: String): Flow<List<ChapterProgressEntity>> {
        return dao.getProgressBySubject(subject)
    }

    suspend fun saveOrUpdate(progress: ChapterProgressEntity) {
        dao.insertOrUpdate(progress)
    }

    suspend fun seedInitialChaptersIfEmpty(chapters: List<ChapterItem>) {
        val existing = dao.getAllProgressSync()
        if (existing.isNotEmpty()) return

        val initialList = chapters.map { chap ->
            ChapterProgressEntity(
                chapterId = chap.id,
                chapterTitle = chap.title,
                subject = chap.subject,
                priority = chap.priority,
                lastUpdated = System.currentTimeMillis()
            )
        }
        dao.insertAll(initialList)
    }

    suspend fun updateChapterStep(
        chapterId: Int,
        chapterTitle: String,
        subject: String,
        stepKey: String,
        checked: Boolean
    ) {
        val current = dao.getProgressForChapterSync(chapterId)
        val chain = current?.toChapterChain() ?: ChapterChain()

        val updatedChain = when (stepKey) {
            "lecture" -> chain.copy(lecture = checked)
            "notes" -> chain.copy(notes = checked)
            "dpp" -> chain.copy(dpp = checked)
            "ncert" -> chain.copy(ncert = checked)
            "prarambh" -> chain.copy(prarambh = checked)
            "prabal" -> chain.copy(prabal = checked)
            "prakshit" -> chain.copy(prakshit = checked)
            "pyq" -> chain.copy(pyq = checked)
            "revision" -> chain.copy(revision = checked)
            "test" -> chain.copy(test = checked)
            else -> chain
        }

        val updatedEntity = ChapterProgressEntity.fromChain(
            chapterId = chapterId,
            chapterTitle = chapterTitle.ifBlank { current?.chapterTitle ?: "Chapter $chapterId" },
            subject = subject.ifBlank { current?.subject ?: "Physics" },
            chain = updatedChain,
            notesContent = current?.notesContent ?: "",
            weakConcepts = current?.weakConcepts ?: ""
        )
        dao.insertOrUpdate(updatedEntity)
    }

    suspend fun markFullChapterChain(
        chapterId: Int,
        chapterTitle: String,
        subject: String,
        complete: Boolean
    ) {
        val current = dao.getProgressForChapterSync(chapterId)
        val chain = ChapterChain(
            lecture = complete,
            notes = complete,
            dpp = complete,
            ncert = complete,
            prarambh = complete,
            prabal = complete,
            prakshit = complete,
            pyq = complete,
            revision = complete,
            test = complete
        )
        val entity = ChapterProgressEntity.fromChain(
            chapterId = chapterId,
            chapterTitle = chapterTitle.ifBlank { current?.chapterTitle ?: "Chapter $chapterId" },
            subject = subject.ifBlank { current?.subject ?: "Physics" },
            chain = chain,
            notesContent = current?.notesContent ?: "",
            weakConcepts = current?.weakConcepts ?: ""
        )
        dao.insertOrUpdate(entity)
    }

    suspend fun updateChapterNotes(chapterId: Int, notes: String, weakConcepts: String) {
        dao.updateNotes(chapterId, notes, weakConcepts)
    }
}

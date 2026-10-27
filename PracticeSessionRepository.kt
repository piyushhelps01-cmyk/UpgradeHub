package com.example.data.repository

import com.example.data.local.PracticeSessionHistoryDao
import com.example.data.local.PracticeSessionHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository managing practice session history and drill telemetry in Room.
 */
class PracticeSessionRepository(
    private val dao: PracticeSessionHistoryDao
) {
    val allSessions: Flow<List<PracticeSessionHistoryEntity>> = dao.getAllSessions()

    fun getRecentSessions(limit: Int = 20): Flow<List<PracticeSessionHistoryEntity>> {
        return dao.getRecentSessions(limit)
    }

    fun getSessionsForPlan(plannerDurationDays: Int): Flow<List<PracticeSessionHistoryEntity>> {
        return dao.getSessionsForPlan(plannerDurationDays)
    }

    fun getSessionsForDay(plannerDurationDays: Int, dayNumber: Int): Flow<List<PracticeSessionHistoryEntity>> {
        return dao.getSessionsForDay(plannerDurationDays, dayNumber)
    }

    fun getSessionsBySubject(subject: String): Flow<List<PracticeSessionHistoryEntity>> {
        return dao.getSessionsBySubject(subject)
    }

    val totalQuestionsSolved: Flow<Int> = dao.getTotalQuestionsAttempted().map { it ?: 0 }
    val totalQuestionsCorrect: Flow<Int> = dao.getTotalQuestionsCorrect().map { it ?: 0 }
    val averageAccuracy: Flow<Float> = dao.getAverageAccuracy().map { it ?: 0f }
    val totalPracticeMinutes: Flow<Int> = dao.getTotalPracticeMinutes().map { it ?: 0 }

    suspend fun logSession(
        sessionTitle: String,
        sessionType: String = "PYQ Drill",
        subject: String,
        chapterId: Int = 0,
        chapterTitle: String = "",
        questionsAttempted: Int,
        questionsCorrect: Int,
        timeSpentMinutes: Int,
        plannerDurationDays: Int = 60,
        dayNumber: Int = 1,
        moodRating: String = "CONFIDENT",
        notes: String = ""
    ): Long {
        val accuracy = if (questionsAttempted > 0) {
            (questionsCorrect.toFloat() / questionsAttempted) * 100f
        } else 0f

        val entity = PracticeSessionHistoryEntity(
            sessionTitle = sessionTitle,
            sessionType = sessionType,
            subject = subject,
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            questionsAttempted = questionsAttempted,
            questionsCorrect = questionsCorrect,
            accuracyPercentage = accuracy,
            timeSpentMinutes = timeSpentMinutes,
            plannerDurationDays = plannerDurationDays,
            dayNumber = dayNumber,
            moodRating = moodRating,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )
        return dao.insertSession(entity)
    }

    suspend fun deleteSession(id: Long) {
        dao.deleteSessionById(id)
    }

    suspend fun seedSampleSessionsIfEmpty() {
        val existing = dao.getAllSessionsSync()
        if (existing.isNotEmpty()) return

        val sampleSessions = listOf(
            PracticeSessionHistoryEntity(
                sessionTitle = "Electrostatics PYQ Sprint (2020-2025)",
                sessionType = "PYQ Drill",
                subject = "Physics",
                chapterId = 1,
                chapterTitle = "Electrostatics & Gauss Law",
                questionsAttempted = 35,
                questionsCorrect = 31,
                accuracyPercentage = 88.5f,
                timeSpentMinutes = 75,
                plannerDurationDays = 60,
                dayNumber = 1,
                moodRating = "CONFIDENT",
                notes = "Solid grip on Gauss law flux integration",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            PracticeSessionHistoryEntity(
                sessionTitle = "Matrices & Determinants DPP Set",
                sessionType = "DPP Solving",
                subject = "Mathematics",
                chapterId = 3,
                chapterTitle = "Matrices and Determinants",
                questionsAttempted = 30,
                questionsCorrect = 26,
                accuracyPercentage = 86.6f,
                timeSpentMinutes = 80,
                plannerDurationDays = 60,
                dayNumber = 1,
                moodRating = "BALANCED",
                notes = "Cramer's rule questions done in under 2 mins each",
                timestamp = System.currentTimeMillis() - 43200000L
            ),
            PracticeSessionHistoryEntity(
                sessionTitle = "Solutions & Colligative NCERT Exemplar",
                sessionType = "NCERT Line-by-Line",
                subject = "Chemistry",
                chapterId = 2,
                chapterTitle = "Solutions & Colligative Properties",
                questionsAttempted = 25,
                questionsCorrect = 23,
                accuracyPercentage = 92.0f,
                timeSpentMinutes = 60,
                plannerDurationDays = 60,
                dayNumber = 2,
                moodRating = "CONFIDENT",
                notes = "Van 't Hoff factor calculations revised",
                timestamp = System.currentTimeMillis() - 10800000L
            )
        )

        sampleSessions.forEach { dao.insertSession(it) }
    }
}

package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TopicStudyStatus(val label: String) {
    NOT_STARTED("Not Started"),
    LEARNING("Learning"),
    COMPLETED("Completed"),
    REVISED("Revised")
}

enum class AutoMistakeClassification(val label: String) {
    CONCEPTUAL("Conceptual mistake"),
    CALCULATION("Calculation mistake"),
    FORMULA("Formula mistake"),
    SILLY("Silly mistake"),
    MISREAD("Misread question"),
    TIME_MANAGEMENT("Time-management issue"),
    REPEATED("Repeated mistake")
}

enum class BacklogCategory(val label: String) {
    LECTURE("Lectures"),
    DPP("DPPs"),
    ASSIGNMENT("Assignments"),
    CHAPTER("Chapters"),
    PRACTICE("Practice"),
    REVISION("Revisions"),
    TEST("Tests")
}

enum class BacklogDueStatus(val label: String) {
    OVERDUE("Overdue"),
    DUE_TODAY("Due today"),
    UPCOMING("Scheduled"),
    COMPLETED("Completed")
}

data class SyllabusTopicItem(
    val id: String,
    val ownerEmail: String,
    val examClass: String,      // e.g. "JEE Main • Class 12", "NEET UG • Class 12", "Class 11"
    val subject: String,        // "Physics", "Chemistry", "Mathematics", "Biology"
    val chapterTitle: String,
    val topicTitle: String,
    val status: TopicStudyStatus,
    val progressPct: Int,
    val lastStudiedLabel: String,
    val lastStudiedTimestamp: Long,
    val pendingWork: List<String>, // e.g. ["DPP #2", "15 PYQs", "Revision"]
    val completionHistory: List<String>
)

data class TrackedLectureItem(
    val id: String,
    val ownerEmail: String,
    val batchName: String,
    val subject: String,
    val chapterTitle: String,
    val lectureTitle: String,
    val durationMinutes: Int,
    val isWatched: Boolean,
    val dppCompleted: Boolean,
    val assignmentCompleted: Boolean,
    val updatedAt: Long
)

data class BacklogTaskItem(
    val id: String,
    val ownerEmail: String,
    val title: String,
    val subject: String,
    val chapterTitle: String,
    val category: BacklogCategory,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val dueStatus: BacklogDueStatus,
    val scheduledForLabel: String,
    val estimatedMinutes: Int,
    val isCompleted: Boolean,
    val createdAt: Long
)

data class QuestionBankItem(
    val id: String,
    val exam: String,          // "JEE Main", "JEE Advanced", "NEET UG"
    val classLevel: String,    // "Class 11", "Class 12"
    val subject: String,       // "Physics", "Chemistry", "Mathematics", "Biology"
    val chapter: String,
    val topic: String,
    val difficulty: String,    // "Easy", "Medium", "Hard"
    val year: String,          // "2025", "2024", "2023", "2022"
    val source: String,        // "PYQ", "NCERT", "DPP", "Exemplar", "Mock"
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val idealTimeSeconds: Int,
    val formulaUsed: String,
    val involvesCalculation: Boolean,
    val explanation: String
)

data class UserQuestionAttemptStat(
    val questionId: String,
    val ownerEmail: String,
    val attemptsCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastTimeTakenSeconds: Int = 0,
    val isSaved: Boolean = false,
    val isMarkedForRevision: Boolean = false,
    val lastAttemptedAt: Long = 0L
)

data class SmartAutoMistakeEntry(
    val id: String,
    val ownerEmail: String,
    val questionId: String,
    val questionTitle: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val conceptTag: String,
    val sourceLabel: String, // "Question Bank", "CBT Mock Test", "Practice Sprint"
    val primaryClassification: AutoMistakeClassification,
    val isRepeatedMistake: Boolean,
    val repeatCount: Int,
    val timeTakenSeconds: Int,
    val correctiveAction: String,
    val isResolved: Boolean,
    val timestamp: Long,
    val createdWeekLabel: String
)

data class WeeklyMistakeReportModel(
    val id: String,
    val ownerEmail: String,
    val weekLabel: String,
    val totalMistakes: Int,
    val newMistakes: Int,
    val repeatedMistakes: Int,
    val mostCommonMistakeType: String,
    val weakSubjects: List<String>,
    val weakChapters: List<String>,
    val weakTopics: List<String>,
    val comparisonWithPreviousWeek: String,
    val hasEnoughData: Boolean,
    val generatedAt: Long
)

data class SmartRevisionPlanNode(
    val id: String,
    val ownerEmail: String,
    val weakTopic: String,
    val chapter: String,
    val subject: String,
    val reason: String,       // e.g. "Low accuracy (42%) + 3 repeated mistakes"
    val priority: String,     // "CRITICAL", "HIGH", "MEDIUM"
    val whatToRevise: String,
    val questionsToSolve: String,
    val miniTestTitle: String,
    val reTestTarget: String,
    val isCompleted: Boolean,
    val completedAtLabel: String? = null
)

data class WeaknessActionFixChain(
    val problem: String,
    val reason: String,
    val requiredRevision: String,
    val targetedPractice: String,
    val mistakeReview: String,
    val miniTest: String,
    val reTest: String
)

data class TestAutoAnalysisReport(
    val id: String,
    val ownerEmail: String,
    val testTitle: String,
    val examCategory: String,
    val score: Int,
    val maxScore: Int,
    val accuracyPct: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val timeUsageSeconds: Int,
    val subjectPerformance: Map<String, Int>,     // Subject -> Accuracy %
    val chapterPerformance: Map<String, Int>,     // Chapter -> Accuracy %
    val topicPerformance: Map<String, Int>,       // Topic -> Accuracy %
    val difficultyPerformance: Map<String, Int>,  // Easy/Medium/Hard -> Accuracy %
    val mistakeTypeBreakdown: Map<String, Int>,   // Mistake Type -> Count
    val detectedWeaknesses: List<String>,
    val howToFixChains: List<WeaknessActionFixChain>,
    val timestamp: Long
)

data class PersonalizedRecommendationItem(
    val id: String,
    val ownerEmail: String,
    val actionCategory: String, // "Study this chapter", "Revise this topic", "Solve these questions", etc.
    val title: String,
    val subject: String,
    val whyExplanation: String,
    val priority: String,
    val ctaLabel: String,
    val targetPageName: String
)

data class PrepVaultGridResource(
    val id: String,
    val category: String, // 11 categories from PDF
    val title: String,
    val classLevel: String,
    val exam: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val summary: String,
    val keyPointsOrFormulas: List<String>,
    val tagBadge: String
)

data class CentralNoticeRecord(
    val id: String,
    val title: String,
    val message: String,
    val noticeType: String, // "ADMIN_ANNOUNCEMENT", "PLATFORM_NOTICE", "PERSONAL_NOTIFICATION"
    val targetUserEmail: String, // "ALL" or specific user email
    val senderName: String,
    val senderRole: String,
    val createdAtTimestamp: Long,
    val createdAtLabel: String,
    val readByUserEmails: Set<String> = emptySet()
)

class StudyIntelligenceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("upgrade_hub_study_intelligence_v2", Context.MODE_PRIVATE)

    private val _activeUserEmail = MutableStateFlow("guest@upgradehub.local")
    val activeUserEmail: StateFlow<String> = _activeUserEmail.asStateFlow()

    private val _activeUserRole = MutableStateFlow("USER")
    val activeUserRole: StateFlow<String> = _activeUserRole.asStateFlow()

    // All records in memory (filtered by RLS per activeUserEmail when exposed)
    private val _allSyllabusTopics = MutableStateFlow<List<SyllabusTopicItem>>(emptyList())
    private val _allTrackedLectures = MutableStateFlow<List<TrackedLectureItem>>(emptyList())
    private val _allBacklogTasks = MutableStateFlow<List<BacklogTaskItem>>(emptyList())
    private val _allQuestionAttempts = MutableStateFlow<List<UserQuestionAttemptStat>>(emptyList())
    private val _allAutoMistakes = MutableStateFlow<List<SmartAutoMistakeEntry>>(emptyList())
    private val _allWeeklyMistakeReports = MutableStateFlow<List<WeeklyMistakeReportModel>>(emptyList())
    private val _allRevisionPlans = MutableStateFlow<List<SmartRevisionPlanNode>>(emptyList())
    private val _allTestAnalyses = MutableStateFlow<List<TestAutoAnalysisReport>>(emptyList())
    private val _allCentralNotices = MutableStateFlow<List<CentralNoticeRecord>>(emptyList())

    val centralNotices: StateFlow<List<CentralNoticeRecord>> = _allCentralNotices.asStateFlow()
    val questionBankCatalog: List<QuestionBankItem> = buildCentralQuestionBank()
    val prepVaultCatalog: List<PrepVaultGridResource> = buildPrepVaultGridCatalog()

    init {
        loadOrInitializeData()
    }

    /**
     * Updates the current authenticated user context and enforces Row-Level Security (RLS) isolation.
     */
    fun setAuthenticatedUserContext(email: String, role: String) {
        val cleanEmail = email.trim().lowercase().ifBlank { "guest@upgradehub.local" }
        val resolvedRole = if (role.equals("ADMIN", ignoreCase = true)) "ADMIN" else "USER"
        _activeUserEmail.value = cleanEmail
        _activeUserRole.value = resolvedRole
    }

    fun validateOwnership(recordOwnerEmail: String): Boolean {
        return recordOwnerEmail.equals(_activeUserEmail.value, ignoreCase = true)
    }

    fun requireAdminAuthorization(): Boolean {
        return _activeUserRole.value.equals("ADMIN", ignoreCase = true)
    }

    // =========================================================================
    // RLS-Filtered User-Specific Getters
    // =========================================================================
    fun getUserSyllabusTopics(userEmail: String = _activeUserEmail.value): List<SyllabusTopicItem> {
        return _allSyllabusTopics.value.filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
    }

    fun getUserTrackedLectures(userEmail: String = _activeUserEmail.value): List<TrackedLectureItem> {
        return _allTrackedLectures.value.filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
    }

    fun getUserBacklogTasks(userEmail: String = _activeUserEmail.value): List<BacklogTaskItem> {
        return _allBacklogTasks.value.filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
    }

    fun getUserQuestionAttempts(userEmail: String = _activeUserEmail.value): Map<String, UserQuestionAttemptStat> {
        return _allQuestionAttempts.value
            .filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
            .associateBy { it.questionId }
    }

    fun getUserAutoMistakes(userEmail: String = _activeUserEmail.value): List<SmartAutoMistakeEntry> {
        return _allAutoMistakes.value
            .filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
            .sortedByDescending { it.timestamp }
    }

    fun getUserWeeklyMistakeReports(userEmail: String = _activeUserEmail.value): List<WeeklyMistakeReportModel> {
        return _allWeeklyMistakeReports.value
            .filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
            .sortedByDescending { it.generatedAt }
    }

    fun getUserRevisionPlans(userEmail: String = _activeUserEmail.value): List<SmartRevisionPlanNode> {
        return _allRevisionPlans.value.filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
    }

    fun getUserTestAnalyses(userEmail: String = _activeUserEmail.value): List<TestAutoAnalysisReport> {
        return _allTestAnalyses.value
            .filter { it.ownerEmail.equals(userEmail, ignoreCase = true) }
            .sortedByDescending { it.timestamp }
    }

    fun getNoticesForCurrentUser(userEmail: String = _activeUserEmail.value): List<CentralNoticeRecord> {
        return _allCentralNotices.value.filter {
            it.targetUserEmail.equals("ALL", ignoreCase = true) ||
                it.targetUserEmail.equals(userEmail, ignoreCase = true)
        }.sortedByDescending { it.createdAtTimestamp }
    }

    // =========================================================================
    // 1. Admin Communication Center & Notice Operations
    // =========================================================================
    fun sendAdminCommunication(
        title: String,
        message: String,
        noticeType: String, // "ADMIN_ANNOUNCEMENT", "PLATFORM_NOTICE", "PERSONAL_NOTIFICATION"
        targetUserEmail: String // "ALL" or specific email
    ): Result<CentralNoticeRecord> {
        if (!requireAdminAuthorization()) {
            return Result.failure(SecurityException("Server Authorization Failed: Only ADMIN role can dispatch communications."))
        }
        if (title.isBlank() || message.isBlank()) {
            return Result.failure(IllegalArgumentException("Title and message are required."))
        }
        val now = System.currentTimeMillis()
        val formattedTime = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(now))
        val record = CentralNoticeRecord(
            id = "notice_$now",
            title = title.trim(),
            message = message.trim(),
            noticeType = noticeType,
            targetUserEmail = targetUserEmail.trim().ifBlank { "ALL" },
            senderName = "Admin Communication Center (${_activeUserEmail.value})",
            senderRole = "ADMIN",
            createdAtTimestamp = now,
            createdAtLabel = formattedTime,
            readByUserEmails = emptySet()
        )
        _allCentralNotices.update { listOf(record) + it }
        saveNoticesToPrefs()
        return Result.success(record)
    }

    fun markNoticeAsRead(noticeId: String, userEmail: String = _activeUserEmail.value) {
        _allCentralNotices.update { list ->
            list.map { item ->
                if (item.id == noticeId) {
                    item.copy(readByUserEmails = item.readByUserEmails + userEmail.lowercase())
                } else item
            }
        }
        saveNoticesToPrefs()
    }

    fun markAllNoticesReadForUser(userEmail: String = _activeUserEmail.value) {
        val clean = userEmail.lowercase()
        _allCentralNotices.update { list ->
            list.map { item ->
                if (item.targetUserEmail.equals("ALL", ignoreCase = true) || item.targetUserEmail.equals(clean, ignoreCase = true)) {
                    item.copy(readByUserEmails = item.readByUserEmails + clean)
                } else item
            }
        }
        saveNoticesToPrefs()
    }

    // =========================================================================
    // 2. Syllabus + Study Tracker (Class/Exam -> Subject -> Chapter -> Topic)
    // =========================================================================
    fun updateSyllabusTopicStatus(topicId: String, nextStatus: TopicStudyStatus) {
        val user = _activeUserEmail.value
        val now = System.currentTimeMillis()
        val timeLabel = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(now))
        _allSyllabusTopics.update { list ->
            list.map { item ->
                if (item.id == topicId && validateOwnership(item.ownerEmail)) {
                    val newPct = when (nextStatus) {
                        TopicStudyStatus.NOT_STARTED -> 0
                        TopicStudyStatus.LEARNING -> 45
                        TopicStudyStatus.COMPLETED -> 85
                        TopicStudyStatus.REVISED -> 100
                    }
                    val updatedPending = when (nextStatus) {
                        TopicStudyStatus.NOT_STARTED -> listOf("Watch Lecture", "Solve DPP", "15 PYQs", "Revision")
                        TopicStudyStatus.LEARNING -> listOf("Complete DPP", "15 PYQs", "Revision")
                        TopicStudyStatus.COMPLETED -> listOf("Spaced Revision", "Mini Test")
                        TopicStudyStatus.REVISED -> emptyList()
                    }
                    val historyEntry = "${nextStatus.label} on $timeLabel"
                    item.copy(
                        status = nextStatus,
                        progressPct = newPct,
                        lastStudiedLabel = timeLabel,
                        lastStudiedTimestamp = now,
                        pendingWork = updatedPending,
                        completionHistory = (listOf(historyEntry) + item.completionHistory).take(6)
                    )
                } else item
            }
        }
        recalculateDynamicRevisionAndBacklog(user)
    }

    // =========================================================================
    // 3. Lecture Tracker + DPP/Assignment Connection
    // =========================================================================
    fun toggleLectureWatched(lectureId: String) {
        _allTrackedLectures.update { list ->
            list.map { item ->
                if (item.id == lectureId && validateOwnership(item.ownerEmail)) {
                    item.copy(isWatched = !item.isWatched, updatedAt = System.currentTimeMillis())
                } else item
            }
        }
    }

    fun toggleLectureDppCompleted(lectureId: String) {
        _allTrackedLectures.update { list ->
            list.map { item ->
                if (item.id == lectureId && validateOwnership(item.ownerEmail)) {
                    item.copy(dppCompleted = !item.dppCompleted, updatedAt = System.currentTimeMillis())
                } else item
            }
        }
    }

    fun toggleLectureAssignmentCompleted(lectureId: String) {
        _allTrackedLectures.update { list ->
            list.map { item ->
                if (item.id == lectureId && validateOwnership(item.ownerEmail)) {
                    item.copy(assignmentCompleted = !item.assignmentCompleted, updatedAt = System.currentTimeMillis())
                } else item
            }
        }
    }

    // =========================================================================
    // 4. Backlog Manager (Prioritize, Reschedule, Complete)
    // =========================================================================
    fun prioritizeBacklogItem(backlogId: String, newPriority: String) {
        _allBacklogTasks.update { list ->
            list.map { item ->
                if (item.id == backlogId && validateOwnership(item.ownerEmail)) {
                    item.copy(priority = newPriority)
                } else item
            }
        }
    }

    fun rescheduleBacklogItem(backlogId: String, newScheduleLabel: String, newDueStatus: BacklogDueStatus) {
        _allBacklogTasks.update { list ->
            list.map { item ->
                if (item.id == backlogId && validateOwnership(item.ownerEmail)) {
                    item.copy(scheduledForLabel = newScheduleLabel, dueStatus = newDueStatus)
                } else item
            }
        }
    }

    fun toggleBacklogItemCompleted(backlogId: String) {
        _allBacklogTasks.update { list ->
            list.map { item ->
                if (item.id == backlogId && validateOwnership(item.ownerEmail)) {
                    val nextCompleted = !item.isCompleted
                    item.copy(
                        isCompleted = nextCompleted,
                        dueStatus = if (nextCompleted) BacklogDueStatus.COMPLETED else BacklogDueStatus.DUE_TODAY
                    )
                } else item
            }
        }
    }

    // =========================================================================
    // 5. Question Bank Attempt + SMART AUTO MISTAKE BOOK Trigger
    // =========================================================================
    fun recordQuestionBankAttempt(
        question: QuestionBankItem,
        selectedOptionIndex: Int,
        timeTakenSeconds: Int
    ): SmartAutoMistakeEntry? {
        val user = _activeUserEmail.value
        val isCorrect = selectedOptionIndex == question.correctIndex
        val now = System.currentTimeMillis()

        val existing = _allQuestionAttempts.value.find {
            it.questionId == question.id && it.ownerEmail.equals(user, ignoreCase = true)
        }
        val updatedStat = if (existing != null) {
            existing.copy(
                attemptsCount = existing.attemptsCount + 1,
                correctCount = existing.correctCount + (if (isCorrect) 1 else 0),
                incorrectCount = existing.incorrectCount + (if (!isCorrect) 1 else 0),
                lastTimeTakenSeconds = timeTakenSeconds,
                isMarkedForRevision = if (!isCorrect) true else existing.isMarkedForRevision,
                lastAttemptedAt = now
            )
        } else {
            UserQuestionAttemptStat(
                questionId = question.id,
                ownerEmail = user,
                attemptsCount = 1,
                correctCount = if (isCorrect) 1 else 0,
                incorrectCount = if (!isCorrect) 1 else 0,
                lastTimeTakenSeconds = timeTakenSeconds,
                isSaved = false,
                isMarkedForRevision = !isCorrect,
                lastAttemptedAt = now
            )
        }

        _allQuestionAttempts.update { list ->
            val filtered = list.filterNot { it.questionId == question.id && it.ownerEmail.equals(user, ignoreCase = true) }
            listOf(updatedStat) + filtered
        }

        // SMART AUTO MISTAKE BOOK: Automatically detect & classify mistake when answered incorrectly!
        if (!isCorrect) {
            return autoDetectAndSaveMistake(
                userEmail = user,
                questionId = question.id,
                questionTitle = question.questionText,
                subject = question.subject,
                chapter = question.chapter,
                topic = question.topic,
                conceptTag = question.formulaUsed.ifBlank { question.topic },
                sourceLabel = "${question.source} (${question.exam} ${question.year})",
                timeTakenSeconds = timeTakenSeconds,
                idealTimeSeconds = question.idealTimeSeconds,
                involvesCalculation = question.involvesCalculation,
                hasFormula = question.formulaUsed.isNotBlank(),
                difficulty = question.difficulty,
                explanation = question.explanation
            )
        }
        recalculateDynamicRevisionAndBacklog(user)
        return null
    }

    fun toggleSaveQuestion(questionId: String) {
        val user = _activeUserEmail.value
        val existing = _allQuestionAttempts.value.find {
            it.questionId == questionId && it.ownerEmail.equals(user, ignoreCase = true)
        }
        val updated = existing?.copy(isSaved = !existing.isSaved)
            ?: UserQuestionAttemptStat(questionId = questionId, ownerEmail = user, isSaved = true)
        _allQuestionAttempts.update { list ->
            listOf(updated) + list.filterNot { it.questionId == questionId && it.ownerEmail.equals(user, ignoreCase = true) }
        }
    }

    fun toggleRevisionQuestion(questionId: String) {
        val user = _activeUserEmail.value
        val existing = _allQuestionAttempts.value.find {
            it.questionId == questionId && it.ownerEmail.equals(user, ignoreCase = true)
        }
        val updated = existing?.copy(isMarkedForRevision = !existing.isMarkedForRevision)
            ?: UserQuestionAttemptStat(questionId = questionId, ownerEmail = user, isMarkedForRevision = true)
        _allQuestionAttempts.update { list ->
            listOf(updated) + list.filterNot { it.questionId == questionId && it.ownerEmail.equals(user, ignoreCase = true) }
        }
    }

    /**
     * SMART AUTO MISTAKE BOOK:
     * Automatically classifies mistakes into:
     * Conceptual, Calculation, Formula, Silly, Misread, Time-management, or Repeated mistake.
     */
    fun autoDetectAndSaveMistake(
        userEmail: String = _activeUserEmail.value,
        questionId: String,
        questionTitle: String,
        subject: String,
        chapter: String,
        topic: String,
        conceptTag: String,
        sourceLabel: String,
        timeTakenSeconds: Int,
        idealTimeSeconds: Int,
        involvesCalculation: Boolean,
        hasFormula: Boolean,
        difficulty: String,
        explanation: String
    ): SmartAutoMistakeEntry {
        val existingUserMistakes = getUserAutoMistakes(userEmail)
        val sameTopicOrConceptMistakes = existingUserMistakes.filter {
            it.topic.equals(topic, ignoreCase = true) ||
                it.chapter.equals(chapter, ignoreCase = true) ||
                it.questionId == questionId
        }
        val repeatCount = sameTopicOrConceptMistakes.size + 1
        val isRepeated = repeatCount >= 2

        val primaryClass = when {
            isRepeated -> AutoMistakeClassification.REPEATED
            timeTakenSeconds > idealTimeSeconds * 1.4 -> AutoMistakeClassification.TIME_MANAGEMENT
            timeTakenSeconds < 18 && difficulty.equals("Easy", ignoreCase = true) -> AutoMistakeClassification.MISREAD
            difficulty.equals("Easy", ignoreCase = true) && involvesCalculation -> AutoMistakeClassification.SILLY
            involvesCalculation && hasFormula -> AutoMistakeClassification.CALCULATION
            hasFormula -> AutoMistakeClassification.FORMULA
            else -> AutoMistakeClassification.CONCEPTUAL
        }

        val correctiveAction = "Revise $topic ($conceptTag) -> Solve 10 targeted PYQs -> Attempt 15-min Mini Test on $chapter"
        val entry = SmartAutoMistakeEntry(
            id = "auto_err_${System.currentTimeMillis()}_${questionId.hashCode()}",
            ownerEmail = userEmail,
            questionId = questionId,
            questionTitle = questionTitle,
            subject = subject,
            chapter = chapter,
            topic = topic,
            conceptTag = conceptTag,
            sourceLabel = sourceLabel,
            primaryClassification = primaryClass,
            isRepeatedMistake = isRepeated,
            repeatCount = repeatCount,
            timeTakenSeconds = timeTakenSeconds,
            correctiveAction = correctiveAction,
            isResolved = false,
            timestamp = System.currentTimeMillis(),
            createdWeekLabel = "Current Week"
        )

        _allAutoMistakes.update { current ->
            val withoutDuplicateQ = current.filterNot {
                it.ownerEmail.equals(userEmail, ignoreCase = true) && it.questionId == questionId
            }
            listOf(entry) + withoutDuplicateQ
        }

        regenerateWeeklyMistakeReportForUser(userEmail)
        recalculateDynamicRevisionAndBacklog(userEmail)
        return entry
    }

    fun toggleAutoMistakeResolved(mistakeId: String) {
        _allAutoMistakes.update { list ->
            list.map { item ->
                if (item.id == mistakeId && validateOwnership(item.ownerEmail)) {
                    item.copy(isResolved = !item.isResolved)
                } else item
            }
        }
        regenerateWeeklyMistakeReportForUser(_activeUserEmail.value)
    }

    // =========================================================================
    // 6. Test + Auto Analysis & "How to Fix the Problem" Engine
    // =========================================================================
    fun analyzeSubmittedTestAndSaveMistakes(
        testTitle: String,
        examCategory: String,
        questions: List<com.example.data.model.SelfStudyPyqQuestion>,
        userAnswers: Map<Int, Int>,
        timeSpentSeconds: Int
    ): TestAutoAnalysisReport {
        val user = _activeUserEmail.value
        var correct = 0
        var incorrect = 0
        var unattempted = 0

        val subjectCorrect = mutableMapOf<String, Int>()
        val subjectTotal = mutableMapOf<String, Int>()
        val chapterCorrect = mutableMapOf<String, Int>()
        val chapterTotal = mutableMapOf<String, Int>()
        val topicCorrect = mutableMapOf<String, Int>()
        val topicTotal = mutableMapOf<String, Int>()
        val diffCorrect = mutableMapOf("Easy" to 0, "Medium" to 0, "Hard" to 0)
        val diffTotal = mutableMapOf("Easy" to 0, "Medium" to 0, "Hard" to 0)
        val mistakeTypeCounts = mutableMapOf<String, Int>()

        questions.forEachIndexed { idx, q ->
            val subj = q.subject.ifBlank { "Physics" }
            val chap = when {
                q.questionText.contains("charge", true) || q.questionText.contains("dipole", true) || q.questionText.contains("flux", true) -> "Electrostatics"
                q.questionText.contains("current", true) || q.questionText.contains("resistor", true) -> "Current Electricity"
                q.questionText.contains("integral", true) || q.questionText.contains("derivative", true) -> "Calculus"
                q.questionText.contains("orbital", true) || q.questionText.contains("hybrid", true) -> "Chemical Bonding"
                else -> "$subj Core Concepts"
            }
            val top = "$chap — High-Yield Application"
            val diff = when (idx % 3) {
                0 -> "Easy"
                1 -> "Medium"
                else -> "Hard"
            }

            subjectTotal[subj] = (subjectTotal[subj] ?: 0) + 1
            chapterTotal[chap] = (chapterTotal[chap] ?: 0) + 1
            topicTotal[top] = (topicTotal[top] ?: 0) + 1
            diffTotal[diff] = (diffTotal[diff] ?: 0) + 1

            val ans = userAnswers[idx]
            if (ans == null) {
                unattempted++
            } else if (ans == q.correctOptionIndex) {
                correct++
                subjectCorrect[subj] = (subjectCorrect[subj] ?: 0) + 1
                chapterCorrect[chap] = (chapterCorrect[chap] ?: 0) + 1
                topicCorrect[top] = (topicCorrect[top] ?: 0) + 1
                diffCorrect[diff] = (diffCorrect[diff] ?: 0) + 1
            } else {
                incorrect++
                val autoMistake = autoDetectAndSaveMistake(
                    userEmail = user,
                    questionId = "${testTitle}_q_$idx",
                    questionTitle = q.questionText,
                    subject = subj,
                    chapter = chap,
                    topic = top,
                    conceptTag = chap,
                    sourceLabel = testTitle,
                    timeTakenSeconds = (timeSpentSeconds / questions.size.coerceAtLeast(1)).coerceAtLeast(35),
                    idealTimeSeconds = 90,
                    involvesCalculation = q.questionText.any { it.isDigit() },
                    hasFormula = true,
                    difficulty = diff,
                    explanation = q.stepByStepSolution
                )
                val label = autoMistake.primaryClassification.label
                mistakeTypeCounts[label] = (mistakeTypeCounts[label] ?: 0) + 1
            }
        }

        val attempted = correct + incorrect
        val accuracy = if (attempted > 0) (correct * 100) / attempted else 0
        val score = (correct * 4) - (incorrect * 1)
        val maxScore = questions.size * 4

        val subjPerf = subjectTotal.mapValues { (k, tot) ->
            if (tot > 0) ((subjectCorrect[k] ?: 0) * 100) / tot else 0
        }
        val chapPerf = chapterTotal.mapValues { (k, tot) ->
            if (tot > 0) ((chapterCorrect[k] ?: 0) * 100) / tot else 0
        }
        val topPerf = topicTotal.mapValues { (k, tot) ->
            if (tot > 0) ((topicCorrect[k] ?: 0) * 100) / tot else 0
        }
        val diffPerf = diffTotal.filterValues { it > 0 }.mapValues { (k, tot) ->
            ((diffCorrect[k] ?: 0) * 100) / tot
        }

        val weakChapters = chapPerf.filterValues { it < 70 }.keys.toList()
        val detectedWeaknesses = if (weakChapters.isNotEmpty()) {
            weakChapters.map { "$it (Accuracy ${chapPerf[it]}%)" }
        } else if (unattempted > questions.size / 2) {
            listOf("High Unattempted Ratio ($unattempted/${questions.size} skipped)")
        } else {
            listOf("Speed & Precision under Final-Hour Pressure")
        }

        val howToFixChains = (weakChapters.ifEmpty { chapPerf.keys.take(2) }).map { chap ->
            val acc = chapPerf[chap] ?: accuracy
            val topMistake = mistakeTypeCounts.maxByOrNull { it.value }?.key ?: "Conceptual / Formula application"
            WeaknessActionFixChain(
                problem = "$chap underperformance in $testTitle",
                reason = "Accuracy is $acc% with primary error pattern: $topMistake",
                requiredRevision = "Revise $chap summary notes & core formula derivations (25 mins)",
                targetedPractice = "Solve 15 targeted $chap PYQs in Question Bank",
                mistakeReview = "Review logged mistakes for $chap in Smart Auto Mistake Book",
                miniTest = "Attempt 10-question timed Topic Mini Test (15 mins)",
                reTest = "Re-attempt $testTitle after 48 hours aiming for >85% accuracy"
            )
        }

        val report = TestAutoAnalysisReport(
            id = "test_analysis_${System.currentTimeMillis()}",
            ownerEmail = user,
            testTitle = testTitle,
            examCategory = examCategory,
            score = score,
            maxScore = maxScore,
            accuracyPct = accuracy,
            correctCount = correct,
            incorrectCount = incorrect,
            unattemptedCount = unattempted,
            timeUsageSeconds = timeSpentSeconds,
            subjectPerformance = subjPerf,
            chapterPerformance = chapPerf,
            topicPerformance = topPerf,
            difficultyPerformance = diffPerf,
            mistakeTypeBreakdown = mistakeTypeCounts,
            detectedWeaknesses = detectedWeaknesses,
            howToFixChains = howToFixChains,
            timestamp = System.currentTimeMillis()
        )

        _allTestAnalyses.update { listOf(report) + it }
        regenerateWeeklyMistakeReportForUser(user)
        recalculateDynamicRevisionAndBacklog(user)
        return report
    }

    // =========================================================================
    // 7. Weekly Mistake Report Generator
    // =========================================================================
    private fun regenerateWeeklyMistakeReportForUser(userEmail: String) {
        val userMistakes = getUserAutoMistakes(userEmail)
        if (userMistakes.isEmpty()) {
            val emptyReport = WeeklyMistakeReportModel(
                id = "wk_empty_${userEmail}",
                ownerEmail = userEmail,
                weekLabel = "Current Week",
                totalMistakes = 0,
                newMistakes = 0,
                repeatedMistakes = 0,
                mostCommonMistakeType = "Not enough data yet.",
                weakSubjects = emptyList(),
                weakChapters = emptyList(),
                weakTopics = emptyList(),
                comparisonWithPreviousWeek = "Not enough data yet.",
                hasEnoughData = false,
                generatedAt = System.currentTimeMillis()
            )
            _allWeeklyMistakeReports.update { list ->
                listOf(emptyReport) + list.filterNot { it.ownerEmail.equals(userEmail, ignoreCase = true) && it.weekLabel == "Current Week" }
            }
            return
        }

        val unresolved = userMistakes.filter { !it.isResolved }
        val activePool = unresolved.ifEmpty { userMistakes }
        val repeatedCount = userMistakes.count { it.isRepeatedMistake }
        val mostCommonType = activePool
            .groupingBy { it.primaryClassification.label }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "Conceptual mistake"

        val weakSubjects = activePool.groupingBy { it.subject }.eachCount()
            .entries.sortedByDescending { it.value }.map { "${it.key} (${it.value})" }.take(3)
        val weakChapters = activePool.groupingBy { it.chapter }.eachCount()
            .entries.sortedByDescending { it.value }.map { it.key }.take(3)
        val weakTopics = activePool.groupingBy { it.topic }.eachCount()
            .entries.sortedByDescending { it.value }.map { it.key }.take(3)

        val resolvedCount = userMistakes.count { it.isResolved }
        val comparisonText = when {
            resolvedCount > unresolved.size -> "Improved by +${(resolvedCount * 15).coerceAtMost(45)}% vs previous week ($resolvedCount mistakes resolved)"
            repeatedCount >= 2 -> "Needs attention: $repeatedCount repeated mistakes detected this week vs last week"
            else -> "Steady progress: ${unresolved.size} active mistakes under review compared with previous week"
        }

        val currentReport = WeeklyMistakeReportModel(
            id = "wk_curr_${userEmail}",
            ownerEmail = userEmail,
            weekLabel = "Current Week (Active)",
            totalMistakes = userMistakes.size,
            newMistakes = userMistakes.count { it.createdWeekLabel == "Current Week" },
            repeatedMistakes = repeatedCount,
            mostCommonMistakeType = mostCommonType,
            weakSubjects = weakSubjects,
            weakChapters = weakChapters,
            weakTopics = weakTopics,
            comparisonWithPreviousWeek = comparisonText,
            hasEnoughData = true,
            generatedAt = System.currentTimeMillis()
        )

        _allWeeklyMistakeReports.update { list ->
            val others = list.filterNot { it.ownerEmail.equals(userEmail, ignoreCase = true) && it.weekLabel.startsWith("Current Week") }
            listOf(currentReport) + others
        }
    }

    // =========================================================================
    // 8. Smart Revision System + Weakness Detection + Personalized Recommendations
    // =========================================================================
    fun toggleRevisionPlanCompleted(planId: String) {
        val nowLabel = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
        _allRevisionPlans.update { list ->
            list.map { item ->
                if (item.id == planId && validateOwnership(item.ownerEmail)) {
                    val next = !item.isCompleted
                    item.copy(
                        isCompleted = next,
                        completedAtLabel = if (next) nowLabel else null
                    )
                } else item
            }
        }
    }

    private fun recalculateDynamicRevisionAndBacklog(userEmail: String) {
        val mistakes = getUserAutoMistakes(userEmail).filter { !it.isResolved }
        val existingPlans = getUserRevisionPlans(userEmail)
        val newPlans = mistakes.take(4).mapIndexed { index, m ->
            val existingMatch = existingPlans.find { it.weakTopic.equals(m.topic, ignoreCase = true) }
            SmartRevisionPlanNode(
                id = existingMatch?.id ?: "rev_auto_${userEmail}_$index",
                ownerEmail = userEmail,
                weakTopic = m.topic,
                chapter = m.chapter,
                subject = m.subject,
                reason = "${m.primaryClassification.label}${if (m.isRepeatedMistake) " (${m.repeatCount}x repeated)" else ""} in ${m.sourceLabel}",
                priority = if (m.isRepeatedMistake) "CRITICAL" else "HIGH",
                whatToRevise = "Revise ${m.conceptTag} formulas & NCERT illustrations for ${m.chapter}",
                questionsToSolve = "Solve 15 targeted ${m.chapter} PYQs (Medium-Hard)",
                miniTestTitle = "15-Min ${m.topic} Concept Drill",
                reTestTarget = "Score >85% in ${m.chapter} Re-Test",
                isCompleted = existingMatch?.isCompleted ?: false,
                completedAtLabel = existingMatch?.completedAtLabel
            )
        }
        if (newPlans.isNotEmpty()) {
            _allRevisionPlans.update { all ->
                val otherUsers = all.filterNot { it.ownerEmail.equals(userEmail, ignoreCase = true) }
                newPlans + otherUsers
            }
        }
    }

    /**
     * Personalized Recommendation System:
     * Generates recommendations separately for every user based on their actual data,
     * and explicitly explains WHY each recommendation was generated.
     * If there is not enough data, returns empty list so UI displays "Not enough data yet."
     */
    fun getPersonalizedRecommendationsForUser(userEmail: String = _activeUserEmail.value): List<PersonalizedRecommendationItem> {
        val mistakes = getUserAutoMistakes(userEmail).filter { !it.isResolved }
        val backlogs = getUserBacklogTasks(userEmail).filter { !it.isCompleted }
        val lectures = getUserTrackedLectures(userEmail)
        val topics = getUserSyllabusTopics(userEmail)
        val testReports = getUserTestAnalyses(userEmail)

        if (mistakes.isEmpty() && backlogs.isEmpty() && lectures.isEmpty() && topics.isEmpty() && testReports.isEmpty()) {
            return emptyList()
        }

        val recommendations = mutableListOf<PersonalizedRecommendationItem>()

        // 1. From Repeated / Active Mistakes
        mistakes.firstOrNull()?.let { topMistake ->
            val latestTestAcc = testReports.firstOrNull()?.accuracyPct ?: 52
            recommendations.add(
                PersonalizedRecommendationItem(
                    id = "rec_mistake_${topMistake.id}",
                    ownerEmail = userEmail,
                    actionCategory = "Revise this topic & Review mistakes",
                    title = "${topMistake.chapter}: ${topMistake.topic}",
                    subject = topMistake.subject,
                    whyExplanation = "${topMistake.chapter} (${topMistake.topic}) is flagged because your recent accuracy is $latestTestAcc% and you made ${topMistake.repeatCount} ${topMistake.primaryClassification.label.lowercase()}(s) in ${topMistake.sourceLabel}. Revise the formulas, solve 15 targeted questions and attempt a mini test.",
                    priority = if (topMistake.isRepeatedMistake) "CRITICAL" else "HIGH",
                    ctaLabel = "Open Mistake Book & Fix",
                    targetPageName = "TESTS"
                )
            )
        }

        // 2. From Pending Lectures / DPPs
        lectures.firstOrNull { !it.isWatched || !it.dppCompleted }?.let { pendingLec ->
            val actionType = if (!pendingLec.isWatched) "Watch pending lecture" else "Complete this DPP"
            recommendations.add(
                PersonalizedRecommendationItem(
                    id = "rec_lec_${pendingLec.id}",
                    ownerEmail = userEmail,
                    actionCategory = actionType,
                    title = "${pendingLec.chapterTitle} — ${pendingLec.lectureTitle}",
                    subject = pendingLec.subject,
                    whyExplanation = "Recommended because ${if (!pendingLec.isWatched) "this ${pendingLec.durationMinutes}-min lecture in ${pendingLec.batchName} is still pending" else "you watched the lecture but have not completed the linked DPP yet"}, which blocks downstream PYQ mastery.",
                    priority = "HIGH",
                    ctaLabel = "Go to Daily Planner",
                    targetPageName = "DAILY_PLANNER"
                )
            )
        }

        // 3. From High-Priority / Overdue Backlog
        backlogs.firstOrNull { it.dueStatus == BacklogDueStatus.OVERDUE || it.priority == "HIGH" }?.let { item ->
            recommendations.add(
                PersonalizedRecommendationItem(
                    id = "rec_backlog_${item.id}",
                    ownerEmail = userEmail,
                    actionCategory = "Clear this backlog",
                    title = "${item.title} (${item.chapterTitle})",
                    subject = item.subject,
                    whyExplanation = "Generated because this ${item.category.label.lowercase()} item is marked ${item.dueStatus.label.uppercase()} with ${item.priority} priority (${item.estimatedMinutes} mins required).",
                    priority = item.priority,
                    ctaLabel = "Open Syllabus & Backlog",
                    targetPageName = "CHAPTERS"
                )
            )
        }

        // 4. From Syllabus Topic needing Revision / Mini Test
        topics.firstOrNull { it.status == TopicStudyStatus.LEARNING || it.status == TopicStudyStatus.COMPLETED }?.let { topic ->
            recommendations.add(
                PersonalizedRecommendationItem(
                    id = "rec_topic_${topic.id}",
                    ownerEmail = userEmail,
                    actionCategory = "Attempt a mini test & Solve questions",
                    title = "${topic.chapterTitle}: ${topic.topicTitle}",
                    subject = topic.subject,
                    whyExplanation = "Your topic status is '${topic.status.label}' (${topic.progressPct}% complete, last studied ${topic.lastStudiedLabel}) with pending work: ${topic.pendingWork.joinToString(", ")}.",
                    priority = "MEDIUM",
                    ctaLabel = "Practice Questions",
                    targetPageName = "PRACTICE"
                )
            )
        }

        return recommendations
    }

    // =========================================================================
    // Seed Initial Connected Data for Primary Authenticated User
    // =========================================================================
    private fun loadOrInitializeData() {
        seedDefaultNoticesIfNeeded()
    }

    private fun seedDefaultNoticesIfNeeded() {
        if (_allCentralNotices.value.isNotEmpty()) return
        val now = System.currentTimeMillis()
        _allCentralNotices.value = listOf(
            CentralNoticeRecord(
                id = "notice_seed_1",
                title = "🚨 NTA Full-Syllabus CBT Mock & Auto Mistake Book Active",
                message = "Every wrong answer in CBT Mock Tests and Practice Question Bank is now automatically classified and saved to your personal Mistake Book with an actionable improvement plan.",
                noticeType = "PLATFORM_NOTICE",
                targetUserEmail = "ALL",
                senderName = "Upgrade Hub Academic Desk",
                senderRole = "ADMIN",
                createdAtTimestamp = now - 3600_000L,
                createdAtLabel = "Today, 09:00 AM",
                readByUserEmails = emptySet()
            ),
            CentralNoticeRecord(
                id = "notice_seed_2",
                title = "🎯 Personal Study Intelligence Alert: Electrostatics & Calculus",
                message = "Your Weekly Mistake Report is ready. Review your 2 repeated formula/calculation traps in Electrostatics before attempting Mock #2.",
                noticeType = "PERSONAL_NOTIFICATION",
                targetUserEmail = "demo@upgradehub.local",
                senderName = "Study Intelligence Engine",
                senderRole = "ADMIN",
                createdAtTimestamp = now - 7200_000L,
                createdAtLabel = "Today, 08:15 AM",
                readByUserEmails = emptySet()
            )
        )
    }

    private fun ensureUserSeededIfPrimaryAccount(userEmail: String) {
        if (!userEmail.equals("demo@upgradehub.local", ignoreCase = true)) {
            // For newly registered secondary accounts, keep empty until they generate real activity
            // so the app genuinely shows "Not enough data yet." and strict user-wise RLS isolation!
            return
        }
        if (_allSyllabusTopics.value.any { it.ownerEmail.equals(userEmail, ignoreCase = true) }) return

        val now = System.currentTimeMillis()
        _allSyllabusTopics.value = listOf(
            SyllabusTopicItem(
                id = "top_phy_1",
                ownerEmail = userEmail,
                examClass = "JEE Main • Class 12",
                subject = "Physics",
                chapterTitle = "Electrostatics & Gauss Law",
                topicTitle = "Electric Dipole & Flux through Symmetrical Surfaces",
                status = TopicStudyStatus.COMPLETED,
                progressPct = 85,
                lastStudiedLabel = "Today, 7:30 AM",
                lastStudiedTimestamp = now - 7_200_000L,
                pendingWork = listOf("Spaced Revision", "Mini Test"),
                completionHistory = listOf("Completed on Today, 7:30 AM", "Learning started 2 days ago")
            ),
            SyllabusTopicItem(
                id = "top_phy_2",
                ownerEmail = userEmail,
                examClass = "JEE Main • Class 12",
                subject = "Physics",
                chapterTitle = "Current Electricity",
                topicTitle = "Kirchhoff's Laws, Meter Bridge & RC Circuits",
                status = TopicStudyStatus.LEARNING,
                progressPct = 48,
                lastStudiedLabel = "Yesterday",
                lastStudiedTimestamp = now - 86_400_000L,
                pendingWork = listOf("Complete DPP #3", "15 PYQs", "Formula Revision"),
                completionHistory = listOf("Learning started Yesterday")
            ),
            SyllabusTopicItem(
                id = "top_chem_1",
                ownerEmail = userEmail,
                examClass = "JEE Main • Class 12",
                subject = "Chemistry",
                chapterTitle = "Coordination Compounds",
                topicTitle = "Crystal Field Theory (CFT), Hybridization & Isomerism",
                status = TopicStudyStatus.REVISED,
                progressPct = 100,
                lastStudiedLabel = "2 days ago",
                lastStudiedTimestamp = now - 172_800_000L,
                pendingWork = emptyList(),
                completionHistory = listOf("Revised 2 days ago", "Completed 4 days ago")
            ),
            SyllabusTopicItem(
                id = "top_math_1",
                ownerEmail = userEmail,
                examClass = "JEE Main • Class 12",
                subject = "Mathematics",
                chapterTitle = "Definite Integration & Area",
                topicTitle = "King's Property, Leibniz Rule & Bounded Area",
                status = TopicStudyStatus.LEARNING,
                progressPct = 55,
                lastStudiedLabel = "3 days ago",
                lastStudiedTimestamp = now - 259_200_000L,
                pendingWork = listOf("Solve 20 Shift PYQs", "Review Mistake Book"),
                completionHistory = listOf("Learning started 3 days ago")
            ),
            SyllabusTopicItem(
                id = "top_bio_1",
                ownerEmail = userEmail,
                examClass = "NEET UG • Class 12",
                subject = "Biology",
                chapterTitle = "Molecular Basis of Inheritance",
                topicTitle = "DNA Replication, Transcription & Lac Operon",
                status = TopicStudyStatus.NOT_STARTED,
                progressPct = 0,
                lastStudiedLabel = "Not studied yet",
                lastStudiedTimestamp = 0L,
                pendingWork = listOf("Watch Lecture", "NCERT Line-by-Line", "DPP MCQs"),
                completionHistory = emptyList()
            )
        )

        _allTrackedLectures.value = listOf(
            TrackedLectureItem(
                id = "lec_1",
                ownerEmail = userEmail,
                batchName = "Lakshya JEE 2026",
                subject = "Physics",
                chapterTitle = "Current Electricity",
                lectureTitle = "L-04: RC Transient Circuits & Potentiometer",
                durationMinutes = 105,
                isWatched = true,
                dppCompleted = false,
                assignmentCompleted = false,
                updatedAt = now - 3600_000L
            ),
            TrackedLectureItem(
                id = "lec_2",
                ownerEmail = userEmail,
                batchName = "Lakshya JEE 2026",
                subject = "Chemistry",
                chapterTitle = "Electrochemistry",
                lectureTitle = "L-02: Nernst Equation & Conductance Cells",
                durationMinutes = 95,
                isWatched = false,
                dppCompleted = false,
                assignmentCompleted = false,
                updatedAt = now - 86_400_000L
            ),
            TrackedLectureItem(
                id = "lec_3",
                ownerEmail = userEmail,
                batchName = "Lakshya JEE 2026",
                subject = "Mathematics",
                chapterTitle = "Definite Integration",
                lectureTitle = "L-05: Definite Integral Properties & Reduction",
                durationMinutes = 110,
                isWatched = true,
                dppCompleted = true,
                assignmentCompleted = false,
                updatedAt = now - 120_000_000L
            )
        )

        _allBacklogTasks.value = listOf(
            BacklogTaskItem(
                id = "bl_1",
                ownerEmail = userEmail,
                title = "Current Electricity DPP #04 (RC Circuits)",
                subject = "Physics",
                chapterTitle = "Current Electricity",
                category = BacklogCategory.DPP,
                priority = "HIGH",
                dueStatus = BacklogDueStatus.OVERDUE,
                scheduledForLabel = "Overdue by 1 day",
                estimatedMinutes = 45,
                isCompleted = false,
                createdAt = now - 86_400_000L
            ),
            BacklogTaskItem(
                id = "bl_2",
                ownerEmail = userEmail,
                title = "Electrochemistry L-02: Nernst Equation Lecture",
                subject = "Chemistry",
                chapterTitle = "Electrochemistry",
                category = BacklogCategory.LECTURE,
                priority = "HIGH",
                dueStatus = BacklogDueStatus.DUE_TODAY,
                scheduledForLabel = "Due Today (Slot 2)",
                estimatedMinutes = 95,
                isCompleted = false,
                createdAt = now - 43_200_000L
            ),
            BacklogTaskItem(
                id = "bl_3",
                ownerEmail = userEmail,
                title = "Definite Integration Assignment Sheet #2",
                subject = "Mathematics",
                chapterTitle = "Definite Integration",
                category = BacklogCategory.ASSIGNMENT,
                priority = "MEDIUM",
                dueStatus = BacklogDueStatus.DUE_TODAY,
                scheduledForLabel = "Due Today (Slot 3)",
                estimatedMinutes = 60,
                isCompleted = false,
                createdAt = now - 30_000_000L
            ),
            BacklogTaskItem(
                id = "bl_4",
                ownerEmail = userEmail,
                title = "Electrostatics Gauss Law Revision & Mini-Test",
                subject = "Physics",
                chapterTitle = "Electrostatics",
                category = BacklogCategory.REVISION,
                priority = "HIGH",
                dueStatus = BacklogDueStatus.DUE_TODAY,
                scheduledForLabel = "Due Today (Slot 4)",
                estimatedMinutes = 40,
                isCompleted = false,
                createdAt = now - 20_000_000L
            )
        )

        _allAutoMistakes.value = listOf(
            SmartAutoMistakeEntry(
                id = "auto_seed_1",
                ownerEmail = userEmail,
                questionId = "qb_phy_2",
                questionTitle = "Time constant τ = R_eq × C in wheatstone-unbalanced RC bridge circuit",
                subject = "Physics",
                chapter = "Current Electricity",
                topic = "Kirchhoff's Laws, Meter Bridge & RC Circuits",
                conceptTag = "Thevenin Equivalent Resistance R_th across Capacitor",
                sourceLabel = "CBT Mock Test #1",
                primaryClassification = AutoMistakeClassification.REPEATED,
                isRepeatedMistake = true,
                repeatCount = 3,
                timeTakenSeconds = 145,
                correctiveAction = "Short voltage sources, open independent current sources, find R_eq across C terminals -> Solve 15 RC PYQs -> Attempt 15-min Mini Test",
                isResolved = false,
                timestamp = now - 14_400_000L,
                createdWeekLabel = "Current Week"
            ),
            SmartAutoMistakeEntry(
                id = "auto_seed_2",
                ownerEmail = userEmail,
                questionId = "qb_math_1",
                questionTitle = "Definite Integral ∫₀^(π/2) ln(sin x) dx sign & coefficient error",
                subject = "Mathematics",
                chapter = "Definite Integration",
                topic = "King's Property, Leibniz Rule & Bounded Area",
                conceptTag = "King's Property Standard Result: -(π/2) ln 2",
                sourceLabel = "PYQ Question Bank (2024 Shift 1)",
                primaryClassification = AutoMistakeClassification.FORMULA,
                isRepeatedMistake = false,
                repeatCount = 1,
                timeTakenSeconds = 110,
                correctiveAction = "Revise Definite Integral standard properties sheet -> Solve 10 King's Property PYQs",
                isResolved = false,
                timestamp = now - 48_000_000L,
                createdWeekLabel = "Current Week"
            ),
            SmartAutoMistakeEntry(
                id = "auto_seed_3",
                ownerEmail = userEmail,
                questionId = "qb_chem_1",
                questionTitle = "Nernst Equation log₁₀([Products]/[Reactants]) stoichiometry power omitted",
                subject = "Chemistry",
                chapter = "Electrochemistry",
                topic = "Nernst Equation & Cell EMF",
                conceptTag = "E_cell = E°_cell - (0.0591/n) log Q",
                sourceLabel = "DPP Practice Sprint",
                primaryClassification = AutoMistakeClassification.CALCULATION,
                isRepeatedMistake = true,
                repeatCount = 2,
                timeTakenSeconds = 132,
                correctiveAction = "Always raise ion concentration to stoichiometric coefficient power before taking log₁₀",
                isResolved = false,
                timestamp = now - 90_000_000L,
                createdWeekLabel = "Current Week"
            )
        )

        val prevWeekReport = WeeklyMistakeReportModel(
            id = "wk_prev_${userEmail}",
            ownerEmail = userEmail,
            weekLabel = "Previous Week (Archive)",
            totalMistakes = 5,
            newMistakes = 4,
            repeatedMistakes = 2,
            mostCommonMistakeType = "Calculation mistake",
            weakSubjects = listOf("Physics (3)", "Mathematics (2)"),
            weakChapters = listOf("Electrostatics", "Limits & Continuity"),
            weakTopics = listOf("Electric Dipole Torque", "1^∞ Indeterminate Forms"),
            comparisonWithPreviousWeek = "Baseline week established (5 total mistakes logged)",
            hasEnoughData = true,
            generatedAt = now - 604_800_000L
        )
        _allWeeklyMistakeReports.value = listOf(prevWeekReport)
        regenerateWeeklyMistakeReportForUser(userEmail)
        recalculateDynamicRevisionAndBacklog(userEmail)

        _allTestAnalyses.value = listOf(
            TestAutoAnalysisReport(
                id = "test_analysis_seed_1",
                ownerEmail = userEmail,
                testTitle = "All-India Major Mock Test #1 (NTA Pattern)",
                examCategory = "JEE Main",
                score = 212,
                maxScore = 300,
                accuracyPct = 82,
                correctCount = 56,
                incorrectCount = 12,
                unattemptedCount = 7,
                timeUsageSeconds = 148 * 60,
                subjectPerformance = mapOf("Physics" to 76, "Chemistry" to 88, "Mathematics" to 81),
                chapterPerformance = mapOf(
                    "Current Electricity" to 48,
                    "Electrostatics" to 85,
                    "Coordination Compounds" to 92,
                    "Definite Integration" to 64
                ),
                topicPerformance = mapOf(
                    "RC Transient Circuits" to 45,
                    "King's Property Integrals" to 62,
                    "Crystal Field Theory" to 94
                ),
                difficultyPerformance = mapOf("Easy" to 94, "Medium" to 82, "Hard" to 58),
                mistakeTypeBreakdown = mapOf(
                    "Repeated mistake" to 4,
                    "Calculation mistake" to 4,
                    "Formula mistake" to 2,
                    "Time-management issue" to 2
                ),
                detectedWeaknesses = listOf(
                    "Current Electricity (Accuracy 48% • 3 repeated mistakes)",
                    "Definite Integration (Accuracy 64% • Formula recall lag)"
                ),
                howToFixChains = listOf(
                    WeaknessActionFixChain(
                        problem = "Current Electricity accuracy dropped to 48% with 3 repeated mistakes",
                        reason = "Repeated errors in finding equivalent resistance R_th across capacitor in RC circuits & bridge networks",
                        requiredRevision = "Revise Thevenin Equivalent Resistance & Kirchhoff's Loop Law notes (25 mins)",
                        targetedPractice = "Solve 15 targeted Current Electricity PYQs (2022–2025) in Question Bank",
                        mistakeReview = "Review 3 RC Circuit mistakes in Smart Auto Mistake Book",
                        miniTest = "Attempt 15-Min Current Electricity Mini Test",
                        reTest = "Re-test Current Electricity unit mock aiming for >85% accuracy"
                    ),
                    WeaknessActionFixChain(
                        problem = "Definite Integration speed & formula recall (64% accuracy)",
                        reason = "Time-management & sign errors when applying King's and Queen's properties",
                        requiredRevision = "Revise Definite Integral Standard Properties Cheat Sheet in Prep Vault",
                        targetedPractice = "Solve 12 Definite Integration shift PYQs under 2-min timer",
                        mistakeReview = "Review King's Property mistake entry in Mistake Book",
                        miniTest = "Attempt 10-Question Calculus Sprint",
                        reTest = "Verify in next Full-Syllabus CBT Mock"
                    )
                ),
                timestamp = now - 172_800_000L
            )
        )
    }

    private fun saveNoticesToPrefs() {
        try {
            val arr = JSONArray()
            _allCentralNotices.value.forEach { n ->
                val obj = JSONObject()
                obj.put("id", n.id)
                obj.put("title", n.title)
                obj.put("message", n.message)
                obj.put("noticeType", n.noticeType)
                obj.put("targetUserEmail", n.targetUserEmail)
                obj.put("senderName", n.senderName)
                obj.put("senderRole", n.senderRole)
                obj.put("createdAtTimestamp", n.createdAtTimestamp)
                obj.put("createdAtLabel", n.createdAtLabel)
                arr.put(obj)
            }
            prefs.edit().putString("central_notices_json", arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun buildCentralQuestionBank(): List<QuestionBankItem> = listOf(
        QuestionBankItem(
            id = "qb_phy_1",
            exam = "JEE Main",
            classLevel = "Class 12",
            subject = "Physics",
            chapter = "Electrostatics",
            topic = "Electric Dipole & Flux",
            difficulty = "Medium",
            year = "2025",
            source = "PYQ",
            questionText = "An electric dipole of dipole moment p = 4 × 10⁻⁹ C·m is aligned at 30° with the direction of a uniform electric field of magnitude 5 × 10⁴ N/C. The magnitude of the torque acting on the dipole is:",
            options = listOf("1.0 × 10⁻⁴ N·m", "2.0 × 10⁻⁴ N·m", "1.73 × 10⁻⁴ N·m", "0.5 × 10⁻⁴ N·m"),
            correctIndex = 0,
            idealTimeSeconds = 75,
            formulaUsed = "τ = p E sin θ",
            involvesCalculation = true,
            explanation = "τ = p E sin(30°) = (4 × 10⁻⁹) × (5 × 10⁴) × 0.5 = 1.0 × 10⁻⁴ N·m."
        ),
        QuestionBankItem(
            id = "qb_phy_2",
            exam = "JEE Advanced",
            classLevel = "Class 12",
            subject = "Physics",
            chapter = "Current Electricity",
            topic = "RC Circuits & Bridge Networks",
            difficulty = "Hard",
            year = "2024",
            source = "PYQ",
            questionText = "In a steady-state RC circuit where a 6 μF capacitor is connected in parallel with a 10 Ω resistor and in series with a 20 Ω resistor across a 30 V ideal battery, the charge stored on the capacitor is:",
            options = listOf("60 μC", "120 μC", "180 μC", "90 μC"),
            correctIndex = 0,
            idealTimeSeconds = 110,
            formulaUsed = "Q = C · V_parallel, where V_parallel = E · R₁ / (R₁ + R₂)",
            involvesCalculation = true,
            explanation = "In steady state, capacitor acts as open circuit. Current I = 30 / (10 + 20) = 1 A. Voltage across 10 Ω resistor = 1 A × 10 Ω = 10 V. Charge Q = C × V = 6 μF × 10 V = 60 μC."
        ),
        QuestionBankItem(
            id = "qb_chem_1",
            exam = "JEE Main",
            classLevel = "Class 12",
            subject = "Chemistry",
            chapter = "Coordination Compounds",
            topic = "Crystal Field Theory (CFT)",
            difficulty = "Easy",
            year = "2025",
            source = "NCERT",
            questionText = "Which of the following octahedral complexes is diamagnetic and has the highest crystal field splitting energy (Δ₀)?",
            options = listOf("[Co(NH₃)₆]³⁺", "[CoF₆]³⁻", "[Fe(H₂O)₆]²⁺", "[Mn(H₂O)₆]²⁺"),
            correctIndex = 0,
            idealTimeSeconds = 60,
            formulaUsed = "Co³⁺ (3d⁶) with strong field ligand NH₃ → t₂g⁶ eg⁰ (Diamagnetic)",
            involvesCalculation = false,
            explanation = "In [Co(NH₃)₆]³⁺, Co³⁺ is a 3d⁶ system and NH₃ acts as a strong field ligand causing pairing of all 6 electrons in the t₂g orbitals (t₂g⁶ eg⁰), making it low-spin and diamagnetic."
        ),
        QuestionBankItem(
            id = "qb_math_1",
            exam = "JEE Main",
            classLevel = "Class 12",
            subject = "Mathematics",
            chapter = "Definite Integration",
            topic = "King's Property",
            difficulty = "Medium",
            year = "2024",
            source = "PYQ",
            questionText = "The value of the definite integral I = ∫₀^(π/2) [√(sin x) / (√(sin x) + √(cos x))] dx is equal to:",
            options = listOf("π / 4", "π / 2", "π / 8", "0"),
            correctIndex = 0,
            idealTimeSeconds = 65,
            formulaUsed = "∫₀ᵃ f(x) dx = ∫₀ᵃ f(a - x) dx ⇒ 2I = π/2 ⇒ I = π/4",
            involvesCalculation = true,
            explanation = "Applying King's property x → π/2 - x, the numerator becomes √(cos x) while the denominator remains unchanged. Adding both integrals gives 2I = ∫₀^(π/2) 1 dx = π/2, so I = π/4."
        ),
        QuestionBankItem(
            id = "qb_bio_1",
            exam = "NEET UG",
            classLevel = "Class 12",
            subject = "Biology",
            chapter = "Molecular Basis of Inheritance",
            topic = "Lac Operon & Gene Regulation",
            difficulty = "Medium",
            year = "2024",
            source = "PYQ",
            questionText = "In the E. coli lac operon, what is the specific function of the structural gene lacY?",
            options = listOf(
                "Codes for permease which increases cell permeability to β-galactosides",
                "Codes for β-galactosidase which hydrolyzes lactose",
                "Codes for transacetylase enzyme",
                "Codes for the repressor protein"
            ),
            correctIndex = 0,
            idealTimeSeconds = 50,
            formulaUsed = "lacZ = β-galactosidase, lacY = Permease, lacA = Transacetylase",
            involvesCalculation = false,
            explanation = "NCERT Line-by-Line: The z gene codes for beta-galactosidase, the y gene codes for permease (which increases permeability of the cell to beta-galactosides), and the a gene encodes a transacetylase."
        ),
        QuestionBankItem(
            id = "qb_phy_3",
            exam = "JEE Main",
            classLevel = "Class 11",
            subject = "Physics",
            chapter = "Rotational Motion",
            topic = "Moment of Inertia & Rolling",
            difficulty = "Medium",
            year = "2023",
            source = "DPP",
            questionText = "A solid sphere and a hollow sphere of the same mass M and radius R roll down an inclined plane from rest without slipping. The ratio of their translational accelerations (a_solid : a_hollow) is:",
            options = listOf("25 : 21", "15 : 14", "7 : 5", "5 : 3"),
            correctIndex = 0,
            idealTimeSeconds = 90,
            formulaUsed = "a = g sin θ / (1 + k²/R²)",
            involvesCalculation = true,
            explanation = "For solid sphere, 1 + k²/R² = 1 + 2/5 = 7/5. For hollow sphere, 1 + k²/R² = 1 + 2/3 = 5/3. Thus a_solid / a_hollow = (5/7) / (3/5) = 25 / 21."
        ),
        QuestionBankItem(
            id = "qb_chem_2",
            exam = "JEE Advanced",
            classLevel = "Class 11",
            subject = "Chemistry",
            chapter = "Chemical Thermodynamics",
            topic = "Gibbs Energy & Spontaneity",
            difficulty = "Hard",
            year = "2024",
            source = "PYQ",
            questionText = "For a chemical reaction at constant temperature and pressure, ΔH = -80 kJ/mol and ΔS = -200 J/(mol·K). At what maximum temperature will this reaction cease to be spontaneous?",
            options = listOf("400 K", "300 K", "500 K", "250 K"),
            correctIndex = 0,
            idealTimeSeconds = 80,
            formulaUsed = "ΔG = ΔH - TΔS < 0 for spontaneity",
            involvesCalculation = true,
            explanation = "Spontaneity requires ΔG < 0. Set ΔG = 0: T_eq = ΔH / ΔS = (-80,000 J/mol) / (-200 J/mol·K) = 400 K. For T > 400 K, ΔG becomes positive (non-spontaneous)."
        ),
        QuestionBankItem(
            id = "qb_math_2",
            exam = "JEE Advanced",
            classLevel = "Class 11",
            subject = "Mathematics",
            chapter = "Conic Sections",
            topic = "Parabola & Tangents",
            difficulty = "Hard",
            year = "2023",
            source = "PYQ",
            questionText = "The locus of the point of intersection of two perpendicular tangents to the parabola y² = 4ax is:",
            options = listOf("The directrix x = -a", "The latus rectum x = a", "The tangent at vertex x = 0", "The axis of parabola y = 0"),
            correctIndex = 0,
            idealTimeSeconds = 70,
            formulaUsed = "Director circle of parabola y² = 4ax is its directrix x + a = 0",
            involvesCalculation = false,
            explanation = "The locus of points from which perpendicular tangents can be drawn to a conic is its director circle. For a parabola, the director circle degenerates into the directrix x + a = 0."
        ),
        QuestionBankItem(
            id = "qb_bio_2",
            exam = "NEET UG",
            classLevel = "Class 11",
            subject = "Biology",
            chapter = "Cell: The Unit of Life",
            topic = "Ribosomes & Inclusion Bodies",
            difficulty = "Easy",
            year = "2025",
            source = "NCERT",
            questionText = "Which of the following cellular organelles is non-membrane bound and found in both prokaryotic as well as eukaryotic cells?",
            options = listOf("Ribosome", "Centrosome", "Peroxisome", "Lysosome"),
            correctIndex = 0,
            idealTimeSeconds = 40,
            formulaUsed = "Ribosomes are non-membrane bound ribonucleoprotein complexes present in both prokaryotes (70S) and eukaryotes (80S)",
            involvesCalculation = false,
            explanation = "NCERT Class 11: Ribosomes are non-membrane bound organelles found in all cells — both eukaryotic as well as prokaryotic."
        ),
        QuestionBankItem(
            id = "qb_phy_4",
            exam = "NEET UG",
            classLevel = "Class 11",
            subject = "Physics",
            chapter = "Gravitation",
            topic = "Escape Velocity & Orbital Speed",
            difficulty = "Easy",
            year = "2024",
            source = "NCERT",
            questionText = "The escape velocity from the surface of Earth is 11.2 km/s. If a planet has double the mass and double the radius of Earth, the escape velocity from its surface is:",
            options = listOf("11.2 km/s", "22.4 km/s", "5.6 km/s", "15.8 km/s"),
            correctIndex = 0,
            idealTimeSeconds = 55,
            formulaUsed = "v_e = √(2GM / R)",
            involvesCalculation = true,
            explanation = "v_e' = √(2G(2M) / (2R)) = √(2GM / R) = v_e = 11.2 km/s."
        ),
        QuestionBankItem(
            id = "qb_chem_3",
            exam = "JEE Main",
            classLevel = "Class 11",
            subject = "Chemistry",
            chapter = "Structure of Atom",
            topic = "Bohr Model & Rydberg Formula",
            difficulty = "Medium",
            year = "2023",
            source = "DPP",
            questionText = "The shortest wavelength transition in the Lyman series of atomic hydrogen (n = ∞ to n = 1) is 91.2 nm. The longest wavelength transition in the Balmer series (n = 3 to n = 2) is:",
            options = listOf("656.3 nm", "486.1 nm", "121.5 nm", "820.5 nm"),
            correctIndex = 0,
            idealTimeSeconds = 85,
            formulaUsed = "1/λ = R_H (1/n₁² - 1/n₂²)",
            involvesCalculation = true,
            explanation = "1/λ_Lyman_min = R_H (1 - 0) => λ_min = 1/R_H = 91.2 nm. For Balmer max: 1/λ_Balmer = R_H (1/4 - 1/9) = (5/36) R_H => λ = (36/5)(1/R_H) = 7.2 × 91.2 nm = 656.6 nm."
        ),
        QuestionBankItem(
            id = "qb_math_3",
            exam = "JEE Main",
            classLevel = "Class 11",
            subject = "Mathematics",
            chapter = "Complex Numbers",
            topic = "Cube Roots of Unity",
            difficulty = "Easy",
            year = "2025",
            source = "NCERT",
            questionText = "If ω is a non-real complex cube root of unity, then the value of (1 - ω + ω²)⁵ + (1 + ω - ω²)⁵ is:",
            options = listOf("32", "-32", "0", "16"),
            correctIndex = 0,
            idealTimeSeconds = 60,
            formulaUsed = "1 + ω + ω² = 0, ω³ = 1",
            involvesCalculation = true,
            explanation = "Since 1 + ω² = -ω, (1 - ω + ω²)⁵ = (-2ω)⁵ = -32ω⁵ = -32ω². Similarly, since 1 + ω = -ω², (1 + ω - ω²)⁵ = (-2ω²)⁵ = -32ω¹⁰ = -32ω. Sum = -32(ω² + ω) = -32(-1) = 32."
        )
    )

    private fun buildPrepVaultGridCatalog(): List<PrepVaultGridResource> = listOf(
        PrepVaultGridResource(
            id = "pv_1",
            category = "Formula",
            title = "Electrostatics & Current Electricity Master Formula Sheet",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Physics",
            chapter = "Current Electricity",
            topic = "RC Circuits & Bridge Laws",
            summary = "Complete vector forms of Coulomb's Law, Gauss's Law, Dipole Torque, Kirchhoff's Laws, and RC time constant τ = R_th·C.",
            keyPointsOrFormulas = listOf("Φ = q_in / ε₀", "τ = p × E", "V_C(t) = V₀(1 - e^(-t/RC))", "E_cell = V + I·r"),
            tagBadge = "HIGH YIELD"
        ),
        PrepVaultGridResource(
            id = "pv_2",
            category = "PYQ",
            title = "2019–2025 Shift-Wise Solved PYQ Compendium",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Mathematics",
            chapter = "Definite Integration",
            topic = "King's Property & Leibniz Rule",
            summary = "Top 50 shift questions from NTA 2019–2025 with shortcut reductions and area-under-curve templates.",
            keyPointsOrFormulas = listOf("∫₀^(π/2) ln(sin x) dx = -(π/2) ln 2", "d/dx ∫_{g(x)}^{h(x)} f(t) dt = f(h(x))h'(x) - f(g(x))g'(x)"),
            tagBadge = "2025 UPDATED"
        ),
        PrepVaultGridResource(
            id = "pv_3",
            category = "Sample Paper",
            title = "NTA Pattern Full-Length Sample Paper #1 (with Solutions)",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Physics",
            chapter = "Full Syllabus",
            topic = "75 Questions NTA Pattern",
            summary = "latest rationalised syllabus 300-mark sample paper with section-B numerical answer traps explained.",
            keyPointsOrFormulas = listOf("Section A: 20 MCQs (+4/-1)", "Section B: 5 Numericals (+4/-1)", "Time allocation: 50m Chem, 60m Phys, 70m Math"),
            tagBadge = "NTA OFFICIAL"
        ),
        PrepVaultGridResource(
            id = "pv_4",
            category = "Repeated Questions",
            title = "Most Repeated NTA Concept Archetypes (Asked 6+ Times)",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Chemistry",
            chapter = "Coordination Compounds",
            topic = "Magnetic Moment & Hybridization",
            summary = "Spin-only magnetic moment μ = √(n(n+2)) BM and spectrochemical series pairing exceptions asked in almost every shift.",
            keyPointsOrFormulas = listOf("μ = √(n(n+2)) BM", "[Ni(CN)₄]²⁻: dsp² Square Planar (μ = 0)", "[NiCl₄]²⁻: sp³ Tetrahedral (μ = 2.83 BM)"),
            tagBadge = "6X REPEATED"
        ),
        PrepVaultGridResource(
            id = "pv_5",
            category = "Important Questions",
            title = "Top 100 Rank-Decider Problems for 99.5+ Percentile",
            classLevel = "Class 11",
            exam = "JEE Advanced",
            subject = "Physics",
            chapter = "Rotational Motion",
            topic = "Angular Momentum Conservation",
            summary = "Carefully curated multi-concept problems combining impulse, pure rolling, and instantaneous axis of rotation.",
            keyPointsOrFormulas = listOf("L_P = L_cm + r_cm × M v_cm", "v_contact = v_cm - ωR = 0"),
            tagBadge = "99.5%ILE+"
        ),
        PrepVaultGridResource(
            id = "pv_6",
            category = "High-Weightage Topics",
            title = "80/20 Pareto High-Weightage Chapters Blueprint",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Physics",
            chapter = "Modern Physics & Current Electricity",
            topic = "Weightage Analysis",
            summary = "12 chapters that consistently contribute 62% of total marks in JEE Main & NEET UG papers.",
            keyPointsOrFormulas = listOf("Modern Physics: 3–4 Qs/shift", "Current Electricity: 3 Qs/shift", "Coordination Compounds: 3 Qs/shift", "3D Geometry & Vectors: 4 Qs/shift"),
            tagBadge = "62% MARKS"
        ),
        PrepVaultGridResource(
            id = "pv_7",
            category = "Notes",
            title = "Handwritten Topper Short Notes — Organic Named Reactions",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Chemistry",
            chapter = "Aldehydes, Ketones & Carboxylic Acids",
            topic = "Aldol, Cannizzaro & Reimer-Tiemann",
            summary = "Reagent, intermediate carbocation/carbanion, and stereochemistry summary for all 28 NCERT named reactions.",
            keyPointsOrFormulas = listOf("Reimer-Tiemann: CHCl₃ + aq. NaOH → :CCl₂ electrophile", "Cannizzaro: No α-H, conc. OH⁻, hydride shift RDS"),
            tagBadge = "TOPPER NOTES"
        ),
        PrepVaultGridResource(
            id = "pv_8",
            category = "Concept Revision",
            title = "15-Minute Rapid Concept Revision Flash-Summary",
            classLevel = "Class 12",
            exam = "NEET UG",
            subject = "Biology",
            chapter = "Molecular Basis of Inheritance",
            topic = "DNA Packaging, Replication & Operon",
            summary = "Line-by-line NCERT tables, scientist years, and enzyme roles for rapid pre-test active recall.",
            keyPointsOrFormulas = listOf("Histone octamer: +vely charged (Lysine & Arginine)", "Nucleosome: ~200 bp DNA helix", "DNA Polymerase III: 5' → 3' polymerization"),
            tagBadge = "NCERT CORE"
        ),
        PrepVaultGridResource(
            id = "pv_9",
            category = "Study Material",
            title = "Complete Chapter Module + Graded Exercise (Prarambh & Prabal)",
            classLevel = "Class 11",
            exam = "JEE Main",
            subject = "Chemistry",
            chapter = "Thermodynamics",
            topic = "First & Second Law, Gibbs Free Energy",
            summary = "Structured theory with 45 graded level-1 & level-2 exercises and sign convention table.",
            keyPointsOrFormulas = listOf("ΔG = ΔH - TΔS", "ΔG° = -2.303 RT log₁₀ K_eq", "w_rev = -2.303 nRT log₁₀(V₂/V₁)"),
            tagBadge = "MODULE"
        ),
        PrepVaultGridResource(
            id = "pv_10",
            category = "Mock Tests",
            title = "Chapter-Wise & Full-Syllabus Timed Mini-Mocks",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Mathematics",
            chapter = "Calculus & Coordinate Geometry",
            topic = "Timed Speed Drills",
            summary = "30-minute high-intensity speed drills connected directly to the CBT Exam Engine & Auto Mistake Book.",
            keyPointsOrFormulas = listOf("Target speed: 2.2 mins/question", "Auto-logs mistakes on submission"),
            tagBadge = "LIVE CBT"
        ),
        PrepVaultGridResource(
            id = "pv_11",
            category = "Cheat Sheets",
            title = "Negative-Marking Trap & Exception Cheat Sheet",
            classLevel = "Class 12",
            exam = "JEE Main",
            subject = "Chemistry",
            chapter = "p-Block & Periodicity",
            topic = "Inorganic Exceptions",
            summary = "All inorganic chemistry order exceptions (Ionization Enthalpy, Electron Gain Enthalpy, Bond Angle, Basicity).",
            keyPointsOrFormulas = listOf("Group 13 IE: B > Tl > Ga > Al > In", "Halogen EGE: Cl > F > Br > I", "Hydride Boiling Point: H₂O > SbH₃ > NH₃ > AsH₃ > PH₃"),
            tagBadge = "TRAP GUARD"
        )
    )
}

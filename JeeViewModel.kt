package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.PdfExportImportManager
import com.example.data.export.PdfImportOutcome
import com.example.data.local.AppDatabase
import com.example.data.local.DailyGoalEntity
import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardItemEntity
import com.example.data.local.NcertChapterEntity
import com.example.data.local.PracticeQueueEntity
import com.example.data.model.ChapterChain
import com.example.data.model.ChapterItem
import com.example.data.model.ConsistencySummary
import com.example.data.model.DailyRecordRow
import com.example.data.model.AutoPlanConfiguration
import com.example.data.model.AutoPlanResult
import com.example.data.model.BatchCurriculum
import com.example.data.model.DailySlot
import com.example.data.model.DailyStudyHoursPoint
import com.example.data.model.FlashcardDeckUiModel
import com.example.data.model.HabitDayIndicator
import com.example.data.model.HabitStreakData
import com.example.data.model.MotivationalQuote
import com.example.data.model.NcertProgress
import com.example.data.model.SevenDayTrendData
import com.example.data.model.SixtyDayProgressData
import com.example.data.model.SixtyDayProgressPoint
import com.example.data.model.SixtyDayAnalyticsSummary
import com.example.data.model.SlotProgress
import com.example.data.model.WeeklyReviewData
import com.example.data.repository.BatchCurriculumRepository
import com.example.data.repository.DailyGoalRepository
import com.example.data.repository.FlashcardRepository
import com.example.data.repository.JeeData
import com.example.data.repository.JeeRepository
import com.example.data.repository.MotivationalQuoteRepository
import com.example.data.repository.NcertRepository
import com.example.data.repository.PracticeQueueRepository
import com.example.data.repository.SelfStudyRepository
import com.example.data.model.SelfStudyMockTest
import com.example.data.model.SelfStudyTestSubmission
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import com.example.data.local.DailyStudyScheduleEntity
import com.example.data.local.ChapterProgressEntity
import com.example.data.local.PracticeSessionHistoryEntity
import com.example.data.local.UserAccountEntity
import com.example.data.model.BatchCompletionStats
import com.example.data.model.ConnectedFlowNode
import com.example.data.model.CurriculumProgressionState
import com.example.data.model.FacultyStack
import com.example.data.model.SubjectBatchProgress
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChapterProgressRepository
import com.example.data.repository.DailyScheduleRepository
import com.example.data.repository.PracticeSessionRepository
import com.example.data.model.DemandItem
import com.example.data.model.DemandStatus
import com.example.data.model.DemandCategory
import com.example.data.model.AdminAuditLog
import com.example.data.model.AdminPermissions
import com.example.data.model.AdminStudyMaterial
import com.example.data.model.StudentCohortProfile
import java.util.Locale

enum class JeePage(val title: String, val navLabel: String) {
    DASHBOARD("Home", "Home"),
    PRACTICE("Practice", "Practice"),
    CAMPUS_HUB("Campus Hub", "Campus Hub"),
    AI("AI Coach", "AI"),
    PROGRESS("Progress", "Progress"),
    PROFILE("Profile", "Profile"),

    // Direct and Hub modules
    LEADERBOARD("Monthly Leaderboard", "Leaderboard"),
    TESTS("Tests & Mocks", "Tests"),
    EVENTS("Live Events & Contests", "Events"),
    STREAK("Streak & Focus Studio", "Streak"),
    DEMAND_CENTRE("Demand Centre", "Demands"),
    DAILY_PLANNER("Today's Plan", "Daily"),
    CHAPTERS("Chapter Tracker", "Chapters"),
    NCERT("NCERT Master Checklist", "NCERT"),
    HOURS("Study Hours & Calc", "Hours"),
    WEEKLY("Weekly Review", "Weekly"),
    FLOW("Connected Flow & Stack", "Strategy"),
    SETTINGS("Data & Settings", "Settings"),
    BOOKMARKS("Prep Hub Vault", "Prep Vault"),
    MEMES_HUB("Memes Hub", "Memes Hub"),
    REFERRALS("Referral & XP", "Rewards"),
    ADMIN_PORTAL("Admin Control Portal", "Admin"),
    CONSUMER_UTILITY("Student Utilities OS", "Utilities")
}

data class SubjectProgressStat(
    val subject: String,
    val completed: Int,
    val total: Int,
    val percentage: Int
)

data class PracticeKeyStat(
    val key: String,
    val displayName: String,
    val completedCount: Int,
    val totalCount: Int,
    val percentage: Int
)

data class ActiveMockTestState(
    val activeTest: SelfStudyMockTest? = null,
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(),
    val markedForReview: Set<Int> = emptySet(),
    val isSubmitted: Boolean = false,
    val finalSubmission: SelfStudyTestSubmission? = null
)

data class JeeUiState(
    val currentPage: JeePage = JeePage.DASHBOARD,
    val selectedDay: Int = 1, // 1..60
    val selectedSubjectFilter: String = "All",
    val chapterSearchQuery: String = "",
    val totalFocusedHours: Float = 0f,
    val overallCompletionPct: Int = 0,
    val sprintDayCurrent: Int = 1,
    val todayTargetHours: Int = 12,
    val todayCommandSlots: List<DailySlot> = emptyList(),
    val subjectProgressStats: List<SubjectProgressStat> = emptyList(),
    val practiceStats: List<PracticeKeyStat> = emptyList(),
    val workloadHours: Float = 1347f,
    val dailyRate: Float = 12f,
    val calculatedDays: Float = 112.3f,
    val showPrintLayout: Boolean = false,
    val snackbarMessage: String? = null
)

class JeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = JeeRepository(application.applicationContext)
    private val ncertRepository = NcertRepository(AppDatabase.getDatabase(application).ncertDao())
    private val dailyGoalRepository = DailyGoalRepository(AppDatabase.getDatabase(application).dailyGoalDao())
    private val flashcardRepository = FlashcardRepository(AppDatabase.getDatabase(application).flashcardDao())
    private val quoteRepository = MotivationalQuoteRepository(application.applicationContext)
    private val batchRepository = BatchCurriculumRepository(
        application.applicationContext,
        AppDatabase.getDatabase(application).dailyGoalDao(),
        AppDatabase.getDatabase(application).dailyStudyScheduleDao()
    )
    private val authRepository = AuthRepository(
        application.applicationContext,
        AppDatabase.getDatabase(application).userAccountDao()
    )
    val dailyScheduleRepository = DailyScheduleRepository(
        AppDatabase.getDatabase(application).dailyStudyScheduleDao()
    )
    val practiceQueueRepository = PracticeQueueRepository(
        AppDatabase.getDatabase(application).practiceQueueDao()
    )
    val selfStudyRepository = SelfStudyRepository()
    val studyIntelligenceRepository = com.example.data.repository.StudyIntelligenceRepository(application.applicationContext)
    val leaderboardEventsRepository = com.example.data.repository.LeaderboardEventsRepository()
    val firestoreService = com.example.data.remote.FirebaseFirestoreService(application.applicationContext)
    val chapterProgressRepository = ChapterProgressRepository(
        AppDatabase.getDatabase(application).chapterProgressDao()
    )
    val practiceSessionRepository = PracticeSessionRepository(
        AppDatabase.getDatabase(application).practiceSessionHistoryDao()
    )

    val chapterProgressList: StateFlow<List<ChapterProgressEntity>> = chapterProgressRepository.allProgress
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val practiceSessionsHistory: StateFlow<List<PracticeSessionHistoryEntity>> = practiceSessionRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalPracticeQuestionsSolved: StateFlow<Int> = practiceSessionRepository.totalQuestionsSolved
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val averagePracticeAccuracy: StateFlow<Float> = practiceSessionRepository.averageAccuracy
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val totalPracticeMinutes: StateFlow<Int> = practiceSessionRepository.totalPracticeMinutes
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastCloudSyncTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastCloudSyncTimestamp: StateFlow<Long> = _lastCloudSyncTimestamp.asStateFlow()

    val consumerUtilityRepository = com.example.data.repository.ConsumerUtilityRepository(
        AppDatabase.getDatabase(application),
        viewModelScope
    )

    val utilityTasks = consumerUtilityRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val utilityExpenses = consumerUtilityRepository.allExpenses
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val utilitySubscriptions = consumerUtilityRepository.subscriptions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val utilityTotalSpent = consumerUtilityRepository.totalSpent
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    val utilityHabits = consumerUtilityRepository.allHabits
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val utilityLatestWellness = consumerUtilityRepository.latestWellnessLog
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun addProductivityTask(
        title: String,
        quadrant: String,
        priority: String,
        pomodoros: Int,
        category: String,
        dueDate: String
    ) {
        viewModelScope.launch {
            consumerUtilityRepository.addTask(title, quadrant, priority, pomodoros, category, dueDate)
            showSnackbar("Task added to Eisenhower $quadrant matrix")
        }
    }

    fun toggleProductivityTask(taskId: Long, completed: Boolean) {
        viewModelScope.launch {
            consumerUtilityRepository.toggleTaskCompleted(taskId, completed)
        }
    }

    fun deleteProductivityTask(task: com.example.data.local.ProductivityTaskEntity) {
        viewModelScope.launch {
            consumerUtilityRepository.deleteTask(task)
            showSnackbar("Task removed")
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            consumerUtilityRepository.clearCompletedTasks()
            showSnackbar("Completed tasks cleared")
        }
    }

    fun addStudentExpense(
        title: String,
        amount: Double,
        category: String,
        isSubscription: Boolean,
        cycle: String,
        notes: String
    ) {
        viewModelScope.launch {
            consumerUtilityRepository.addExpense(title, amount, category, isSubscription, cycle, notes)
            showSnackbar("Expense recorded: ₹${amount.toInt()}")
        }
    }

    fun deleteStudentExpense(expense: com.example.data.local.StudentExpenseEntity) {
        viewModelScope.launch {
            consumerUtilityRepository.deleteExpense(expense)
            showSnackbar("Expense entry removed")
        }
    }

    fun toggleAtomicHabitToday(habit: com.example.data.local.AtomicHabitEntity) {
        viewModelScope.launch {
            consumerUtilityRepository.toggleHabitCompletedToday(habit)
            val status = if (!habit.isCompletedToday) "completed! Streak: ${habit.currentStreak + 1} 🔥" else "unmarked"
            showSnackbar("Habit $status")
        }
    }

    fun addAtomicHabit(
        title: String,
        category: String,
        targetDays: Int,
        reminderTime: String
    ) {
        viewModelScope.launch {
            consumerUtilityRepository.addHabit(title, category, targetDays, reminderTime)
            showSnackbar("New Atomic Habit created!")
        }
    }

    fun deleteAtomicHabit(habit: com.example.data.local.AtomicHabitEntity) {
        viewModelScope.launch {
            consumerUtilityRepository.deleteHabit(habit)
            showSnackbar("Habit deleted")
        }
    }

    fun logStudentWellness(
        sleepHours: Float,
        sleepQuality: Int,
        waterIntakeMl: Int,
        stressRating: Int,
        mindfulMinutes: Int,
        notes: String
    ) {
        viewModelScope.launch {
            consumerUtilityRepository.logWellness(sleepHours, sleepQuality, waterIntakeMl, stressRating, mindfulMinutes, notes)
            showSnackbar("Daily Wellness Log updated! 🧘")
        }
    }

    fun addWaterIntakeQuick(amountMl: Int) {
        viewModelScope.launch {
            consumerUtilityRepository.addWaterIntake(amountMl)
            showSnackbar("Hydration updated (+${amountMl}ml) 💧")
        }
    }

    val leaderboardStudents = leaderboardEventsRepository.students
    val currentLeaderboardMonth = leaderboardEventsRepository.currentMonth
    val eventsList = leaderboardEventsRepository.events
    val hallOfFameWinners = leaderboardEventsRepository.hallOfFameWinners

    fun sendHighFive(studentName: String) {
        leaderboardEventsRepository.sendHighFive(studentName)
        showSnackbar("Sent high-five to $studentName! ✋🌟")
    }

    fun toggleEventRegistration(eventId: String) {
        leaderboardEventsRepository.toggleEventRegistration(eventId)
    }

    @Suppress("UNUSED_PARAMETER")
    fun boostUserScore(points: Int, hours: Float = 0f, problems: Int = 0) {
        // Legacy feature hooks remain for callers, but client events cannot mint authoritative XP.
    }

    private val _activeMockTestState = MutableStateFlow(ActiveMockTestState())
    val activeMockTestState: StateFlow<ActiveMockTestState> = _activeMockTestState.asStateFlow()

    fun startMockTest(test: SelfStudyMockTest) {
        _activeMockTestState.value = ActiveMockTestState(
            activeTest = test,
            currentQuestionIndex = 0,
            userAnswers = emptyMap(),
            markedForReview = emptySet(),
            isSubmitted = false,
            finalSubmission = null
        )
    }

    fun selectMockTestAnswer(questionIndex: Int, optionIndex: Int) {
        val currentAnswers = _activeMockTestState.value.userAnswers.toMutableMap()
        currentAnswers[questionIndex] = optionIndex
        _activeMockTestState.value = _activeMockTestState.value.copy(userAnswers = currentAnswers)
    }

    fun clearMockTestAnswer(questionIndex: Int) {
        val currentAnswers = _activeMockTestState.value.userAnswers.toMutableMap()
        currentAnswers.remove(questionIndex)
        _activeMockTestState.value = _activeMockTestState.value.copy(userAnswers = currentAnswers)
    }

    fun toggleMockTestMarkForReview(questionIndex: Int) {
        val currentMarks = _activeMockTestState.value.markedForReview.toMutableSet()
        if (currentMarks.contains(questionIndex)) {
            currentMarks.remove(questionIndex)
        } else {
            currentMarks.add(questionIndex)
        }
        _activeMockTestState.value = _activeMockTestState.value.copy(markedForReview = currentMarks)
    }

    fun jumpToMockTestQuestion(index: Int) {
        val test = _activeMockTestState.value.activeTest ?: return
        if (index in 0 until test.questions.size) {
            _activeMockTestState.value = _activeMockTestState.value.copy(currentQuestionIndex = index)
        }
    }

    fun submitMockTest() {
        val state = _activeMockTestState.value
        val test = state.activeTest ?: return
        var correct = 0
        var incorrect = 0
        var unattempted = 0
        test.questions.forEachIndexed { idx, q ->
            val userAns = state.userAnswers[idx]
            if (userAns == null) {
                unattempted++
            } else if (userAns == q.correctOptionIndex) {
                correct++
            } else {
                incorrect++
            }
        }
        val score = (correct * 4) - (incorrect * 1)
        val maxScore = test.questions.size * 4
        val submission = SelfStudyTestSubmission(
            testId = test.id,
            score = score,
            maxScore = maxScore,
            correctCount = correct,
            incorrectCount = incorrect,
            unattemptedCount = unattempted,
            timeSpentSeconds = 180,
            userAnswers = state.userAnswers
        )
        _activeMockTestState.value = state.copy(
            isSubmitted = true,
            finalSubmission = submission
        )
        // Automatically analyze test & save wrong answers into THAT USER's Smart Auto Mistake Book
        val report = studyIntelligenceRepository.analyzeSubmittedTestAndSaveMistakes(
            testTitle = test.title,
            examCategory = test.exam,
            questions = test.questions,
            userAnswers = state.userAnswers,
            timeSpentSeconds = 180
        )
        if (incorrect > 0) {
            showSnackbar("Auto Mistake Book: $incorrect mistake(s) automatically classified & saved for ${report.ownerEmail}!")
        }
        _version.update { it + 1 }
    }

    fun exitMockTest() {
        _activeMockTestState.value = ActiveMockTestState()
    }

    fun toggleSelfStudyBookmark(id: String) {
        selfStudyRepository.toggleBookmark(id)
    }

    val currentUser: StateFlow<UserAccountEntity?> = authRepository.currentUser
    val allUsers = authRepository.allUsers

    // Demand Centre state
    private val _demands = MutableStateFlow<List<DemandItem>>(
        listOf(
            DemandItem(
                id = "dem_1",
                title = "PW Prayas 2026 Batch Auto-Import Support",
                category = DemandCategory.BATCH,
                description = "Please add automatic syllabus and lecture schedule sync for PW Prayas Dropper 2026 batch.",
                status = DemandStatus.IN_PROGRESS,
                upvotes = 42,
                isUpvotedByMe = true,
                submittedBy = "Prince Kumar",
                createdAt = "15 Sep 2026",
                adminResponse = "Our curriculum parser for Prayas Dropper 2026 is currently in testing and will be available in the next sync update."
            ),
            DemandItem(
                id = "dem_2",
                title = "SSC CGL & CHSL 2026 Exam Structure",
                category = DemandCategory.EXAM,
                description = "Add universal syllabus tracking and previous year question sets for SSC CGL Tier 1 & 2 aspirants.",
                status = DemandStatus.PLANNED,
                upvotes = 35,
                isUpvotedByMe = false,
                submittedBy = "Aditya Verma",
                createdAt = "14 Sep 2026",
                adminResponse = "Planned for Q4 rollout as part of universal multi-exam platform expansion."
            ),
            DemandItem(
                id = "dem_3",
                title = "Organic Reaction Mechanism Step-by-Step AI Solver",
                category = DemandCategory.CONTENT,
                description = "AI solver that visualizes curly arrow mechanism and reagents for electrophilic addition and rearrangements.",
                status = DemandStatus.REVIEWING,
                upvotes = 28,
                isUpvotedByMe = false,
                submittedBy = "Sneha Roy",
                createdAt = "16 Sep 2026",
                adminResponse = null
            ),
            DemandItem(
                id = "dem_4",
                title = "Lock Screen Study Widget with Streak & Next Task",
                category = DemandCategory.FEATURE,
                description = "Android Glance widget showing today's focus task, hours completed, and streak freeze status on the home screen.",
                status = DemandStatus.NEW,
                upvotes = 19,
                isUpvotedByMe = false,
                submittedBy = "Rohan Sharma",
                createdAt = "17 Sep 2026",
                adminResponse = null
            ),
            DemandItem(
                id = "dem_5",
                title = "Offline CBT Mock Test Reattempt Mode",
                category = DemandCategory.IMPROVEMENT,
                description = "Allow full-length mock tests to be completed and auto-submitted safely even if internet fluctuates during exam simulation.",
                status = DemandStatus.COMPLETED,
                upvotes = 56,
                isUpvotedByMe = true,
                submittedBy = "Vikram S.",
                createdAt = "10 Sep 2026",
                adminResponse = "Fully supported! Test answers are saved locally in Room database and synced upon network reconnection."
            )
        )
    )
    val demands: StateFlow<List<DemandItem>> = _demands.asStateFlow()

    fun submitDemand(title: String, category: DemandCategory, description: String) {
        if (title.isBlank()) return
        val newDemand = DemandItem(
            id = "dem_${System.currentTimeMillis()}",
            title = title.trim(),
            category = category,
            description = description.trim(),
            status = DemandStatus.NEW,
            upvotes = 1,
            isUpvotedByMe = true,
            submittedBy = currentUser.value?.fullName ?: "You",
            createdAt = "Today",
            adminResponse = "Received and under initial review by our academic panel."
        )
        _demands.value = listOf(newDemand) + _demands.value
        showSnackbar("Demand submitted successfully! Our team will review it.")
    }

    fun upvoteDemand(id: String) {
        _demands.value = _demands.value.map { item ->
            if (item.id == id) {
                val newUpvoted = !item.isUpvotedByMe
                val newCount = if (newUpvoted) item.upvotes + 1 else (item.upvotes - 1).coerceAtLeast(0)
                item.copy(upvotes = newCount, isUpvotedByMe = newUpvoted)
            } else {
                item
            }
        }
    }

    private val _curriculumProgression = MutableStateFlow(CurriculumProgressionState())
    val curriculumProgression: StateFlow<CurriculumProgressionState> = _curriculumProgression.asStateFlow()

    private val _uiState = MutableStateFlow(JeeUiState())
    val uiState: StateFlow<JeeUiState> = _uiState.asStateFlow()

    private val _batchCurriculums = MutableStateFlow(BatchCurriculumRepository.PRESET_BATCHES)
    val batchCurriculums: StateFlow<List<BatchCurriculum>> = _batchCurriculums.asStateFlow()

    private val initialBatch = BatchCurriculumRepository.PRESET_BATCHES.find { it.id == batchRepository.getActiveBatchId() }
        ?: BatchCurriculumRepository.PRESET_BATCHES.first()

    private val _selectedBatch = MutableStateFlow(initialBatch)
    val selectedBatch: StateFlow<BatchCurriculum> = _selectedBatch.asStateFlow()

    val isNeetMode: StateFlow<Boolean> = _selectedBatch.map { batch ->
        batch.examType.contains("NEET", ignoreCase = true) ||
                batch.id.contains("neet", ignoreCase = true) ||
                batch.id.contains("yakeen", ignoreCase = true)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialBatch.examType.contains("NEET", ignoreCase = true))

    private val _activeChapters = MutableStateFlow(batchRepository.getBatchChaptersAsItems(initialBatch))
    val activeChapters: StateFlow<List<ChapterItem>> = _activeChapters.asStateFlow()

    val activeSubjects: StateFlow<List<String>> = _activeChapters.map { chapters ->
        val subjects = chapters.map { it.subject }.distinct()
        listOf("All") + subjects
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        listOf("All") + batchRepository.getBatchChaptersAsItems(initialBatch).map { it.subject }.distinct()
    )

    val activePracticeSubjects: StateFlow<List<String>> = _selectedBatch.map { batch ->
        val subs = batch.chapters.map { it.subject }.distinct()
        if (subs.isNotEmpty()) {
            listOf("All") + subs + "Formulas"
        } else if (batch.examType.contains("NEET", ignoreCase = true) || batch.id.contains("neet", ignoreCase = true) || batch.id.contains("yakeen", ignoreCase = true)) {
            listOf("All", "Biology", "Physics", "Chemistry", "Formulas")
        } else {
            listOf("All", "Physics", "Chemistry", "Mathematics", "Formulas")
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        listOf("All") + initialBatch.chapters.map { it.subject }.distinct() + "Formulas"
    )

    val activeFlows: StateFlow<List<ConnectedFlowNode>> = _selectedBatch.map { batch ->
        if (batch.examType.contains("NEET", ignoreCase = true) || batch.id.contains("neet", ignoreCase = true) || batch.id.contains("yakeen", ignoreCase = true)) {
            JeeData.NEET_CONNECTED_FLOWS
        } else {
            JeeData.CONNECTED_FLOWS
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, if (initialBatch.examType.contains("NEET", ignoreCase = true)) JeeData.NEET_CONNECTED_FLOWS else JeeData.CONNECTED_FLOWS)

    val activeFaculty: StateFlow<List<FacultyStack>> = _selectedBatch.map { batch ->
        if (batch.faculty.isNotEmpty()) {
            batch.faculty
        } else if (batch.examType.contains("NEET", ignoreCase = true) || batch.id.contains("neet", ignoreCase = true) || batch.id.contains("yakeen", ignoreCase = true)) {
            JeeData.NEET_FACULTY_STACK
        } else {
            JeeData.FACULTY_STACK
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialBatch.faculty.ifEmpty {
            if (initialBatch.examType.contains("NEET", ignoreCase = true)) JeeData.NEET_FACULTY_STACK else JeeData.FACULTY_STACK
        }
    )

    val activeWeeks: StateFlow<List<Triple<Int, String, String>>> = _selectedBatch.map { batch ->
        if (batch.examType.contains("NEET", ignoreCase = true) || batch.id.contains("neet", ignoreCase = true) || batch.id.contains("yakeen", ignoreCase = true)) {
            JeeData.NEET_WEEKS
        } else {
            JeeData.WEEKS
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, if (initialBatch.examType.contains("NEET", ignoreCase = true)) JeeData.NEET_WEEKS else JeeData.WEEKS)

    private val _batchCompletionStats = MutableStateFlow(batchRepository.getBatchCompletionStats(_selectedBatch.value))
    val batchCompletionStats: StateFlow<BatchCompletionStats> = _batchCompletionStats.asStateFlow()

    private val _parallelBatchIds = MutableStateFlow(batchRepository.getParallelBatchIds())
    val parallelBatchIds: StateFlow<List<String>> = _parallelBatchIds.asStateFlow()

    val parallelBatches: StateFlow<List<BatchCurriculum>> = combine(
        _batchCurriculums,
        _parallelBatchIds,
        _selectedBatch
    ) { allBatches, ids, primary ->
        val resolved = ids.mapNotNull { id -> allBatches.find { it.id == id } ?: BatchCurriculumRepository.PRESET_BATCHES.find { it.id == id } }
        if (resolved.isEmpty()) listOf(primary) else resolved.distinctBy { it.id }.take(3)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf(initialBatch))

    private val _selectedExamCategory = MutableStateFlow("All Exams")
    val selectedExamCategory: StateFlow<String> = _selectedExamCategory.asStateFlow()

    fun setSelectedExamCategory(category: String) {
        _selectedExamCategory.value = category
    }

    fun toggleParallelBatch(batchId: String) {
        val current = _parallelBatchIds.value.toMutableList()
        if (current.contains(batchId)) {
            if (current.size > 1) {
                current.remove(batchId)
                showSnackbar("Removed batch from parallel schedule (${current.size} active)")
            } else {
                showSnackbar("At least 1 primary batch must remain active")
                return
            }
        } else {
            if (current.size >= 3) {
                // Replace the 3rd parallel batch so user can keep 2-3 parallel batches active smoothly
                current.removeAt(current.lastIndex)
            }
            current.add(batchId)
            showSnackbar("Added to Parallel Batches (${current.size}/3 Parallel Batches Active)!")
        }
        _parallelBatchIds.value = current
        batchRepository.setParallelBatchIds(current)
        executeAutoPlan()
    }

    private val _batchTargetDays = MutableStateFlow(batchRepository.getActiveTargetDays())
    val batchTargetDays: StateFlow<Int> = _batchTargetDays.asStateFlow()

    private val _batchDailyHours = MutableStateFlow(batchRepository.getActiveDailyHours())
    val batchDailyHours: StateFlow<Float> = _batchDailyHours.asStateFlow()

    private val _batchUrlInput = MutableStateFlow("")
    val batchUrlInput: StateFlow<String> = _batchUrlInput.asStateFlow()

    private val _isAutoPlanning = MutableStateFlow(false)
    val isAutoPlanning: StateFlow<Boolean> = _isAutoPlanning.asStateFlow()

    private val _lastAutoPlanResult = MutableStateFlow<AutoPlanResult?>(null)
    val lastAutoPlanResult: StateFlow<AutoPlanResult?> = _lastAutoPlanResult.asStateFlow()

    private val _dailyQuote = MutableStateFlow(quoteRepository.getDailyQuote(1))
    val dailyQuote: StateFlow<MotivationalQuote> = _dailyQuote.asStateFlow()

    private val _isQuoteBookmarked = MutableStateFlow(false)
    val isQuoteBookmarked: StateFlow<Boolean> = _isQuoteBookmarked.asStateFlow()

    private val _isQuoteRefreshing = MutableStateFlow(false)
    val isQuoteRefreshing: StateFlow<Boolean> = _isQuoteRefreshing.asStateFlow()

    val flashcardDecks: StateFlow<List<FlashcardDeckUiModel>> = combine(
        flashcardRepository.getAllDecks(),
        flashcardRepository.getAllCards()
    ) { decks, allCards ->
        decks.map { deck ->
            val cardsForThisDeck = allCards.filter { it.deckId == deck.id }
            val count = cardsForThisDeck.size
            val mastered = cardsForThisDeck.count { it.isMastered }
            val pct = if (count > 0) ((mastered.toFloat() / count) * 100).toInt() else 0
            FlashcardDeckUiModel(
                deck = deck,
                cardCount = count,
                masteredCount = mastered,
                masteryPercentage = pct
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val ncertChapters: StateFlow<List<NcertChapterEntity>> = ncertRepository.allChapters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cache maps to trigger recomposition when items update
    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentDailyGoals: StateFlow<List<DailyGoalEntity>> = combine(
        _uiState.map { it.selectedDay }.distinctUntilChanged(),
        _version
    ) { day, _ -> day }
        .flatMapLatest { day ->
            dailyGoalRepository.getGoalsForDay(day)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val practiceQueue: StateFlow<List<PracticeQueueEntity>> = practiceQueueRepository.allQueueItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sevenDayTrendData: StateFlow<SevenDayTrendData> = combine(
        _uiState,
        dailyGoalRepository.getAllGoals(),
        _version
    ) { state, allGoals, _ ->
        computeSevenDayTrend(state, allGoals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = computeSevenDayTrend(_uiState.value, emptyList())
    )

    val sixtyDayProgressData: StateFlow<SixtyDayProgressData> = combine(
        _uiState,
        dailyGoalRepository.getAllGoals(),
        _activeChapters,
        _version
    ) { state, allGoals, chapters, _ ->
        computeSixtyDayProgress(state, allGoals, chapters)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = computeSixtyDayProgress(_uiState.value, emptyList(), _activeChapters.value)
    )

    val habitStreakData: StateFlow<HabitStreakData> = combine(
        _uiState,
        dailyGoalRepository.getAllGoals(),
        _version
    ) { state, allGoals, _ ->
        computeHabitStreak(state, allGoals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = computeHabitStreak(_uiState.value, emptyList())
    )

    init {
        computeInitialSprintDay()
        val currentSprint = _uiState.value.sprintDayCurrent.coerceIn(1, 60)
        // Seed realistic baseline study hours for past sprint days and today if empty
        val samplePattern = listOf("11.5", "12.5", "14.0", "12.0", "13.5", "15.0", "12.5")
        for (d in 1..currentSprint) {
            val dayIdx = d - 1
            if (repository.getDayRecord(dayIdx, "hours", "").isEmpty()) {
                val seededHours = if (d == currentSprint) "4.5" else samplePattern[dayIdx % samplePattern.size]
                repository.setDayRecord(dayIdx, "hours", seededHours)
            }
        }
        // Seed realistic initial chapter task milestones if none are checked yet so chapter progress indicators display rich remaining task counts
        val activeChaps = _activeChapters.value
        val hasAnyChapterProgress = activeChaps.any { repository.getChapterChain(it.id).completedCount() > 0 }
        if (!hasAnyChapterProgress && activeChaps.isNotEmpty()) {
            val allMilestoneKeys = listOf("lecture", "notes", "dpp", "ncert", "prarambh", "prabal", "prakshit", "pyq", "revision", "test")
            val seedCounts = listOf(10, 8, 6, 5, 3, 2)
            activeChaps.take(seedCounts.size).forEachIndexed { idx, chap ->
                val countToCheck = seedCounts[idx]
                allMilestoneKeys.take(countToCheck).forEach { key ->
                    repository.setChapterCheck(chap.id, key, true)
                }
            }
        }
        refreshAll()
        val currentDay = _uiState.value.sprintDayCurrent
        val initialQuote = quoteRepository.getDailyQuote(currentDay)
        _dailyQuote.value = initialQuote
        _isQuoteBookmarked.value = quoteRepository.isQuoteBookmarked(initialQuote.id)
        viewModelScope.launch {
            ncertRepository.ensureSeeded(_activeChapters.value)
            dailyGoalRepository.ensureAll60DaysGoalsSeeded()
            dailyGoalRepository.ensureGoalsForDay(_uiState.value.selectedDay)
            flashcardRepository.ensureDefaultDecksSeeded()
            val isNeet = initialBatch.examType.contains("NEET", ignoreCase = true)
            dailyScheduleRepository.seedInitialSchedulesIfEmpty(isNeet)
            practiceQueueRepository.seedInitialQueueIfEmpty(_activeChapters.value)
            chapterProgressRepository.seedInitialChaptersIfEmpty(_activeChapters.value)
            practiceSessionRepository.seedSampleSessionsIfEmpty()
        }

        viewModelScope.launch {
            combine(
                combine(
                    _uiState.map { it.selectedDay }.distinctUntilChanged(),
                    _batchTargetDays,
                    _batchDailyHours
                ) { day, targetDays, dailyHours -> Triple(day, targetDays, dailyHours) },
                dailyScheduleRepository.allSchedules,
                dailyScheduleRepository.completedDaysCount,
                dailyScheduleRepository.totalActualHours,
                dailyScheduleRepository.averageScore
            ) { (selectedDay, targetDays, dailyHours), schedules, completedCount, totalHours, avgScore ->
                val effectiveTotalDays = if (schedules.isNotEmpty()) schedules.size else targetDays
                val progressPct = if (effectiveTotalDays > 0) {
                    (completedCount.toFloat() / effectiveTotalDays) * 100f
                } else 0f

                val activeSched = schedules.find { it.dayNumber == selectedDay }

                val pacing = when {
                    completedCount >= selectedDay -> "Pacing Ahead (Outstanding!)"
                    completedCount == selectedDay - 1 -> "On Track (Target Achieved)"
                    completedCount == 0 && selectedDay == 1 -> "On Track (Sprint Kickoff)"
                    completedCount < selectedDay - 2 -> "Behind Pace (${selectedDay - completedCount} days lagging)"
                    else -> "On Track"
                }

                val milestone = activeSched?.curriculumMilestone?.ifEmpty { null }
                    ?: "Week ${((selectedDay - 1) / 7) + 1}: $effectiveTotalDays-Day High-Yield Progression"

                var streak = 0
                for (d in 1..effectiveTotalDays) {
                    val s = schedules.find { it.dayNumber == d }
                    if (s?.isCompleted == true) {
                        streak++
                    } else if (d < selectedDay) {
                        streak = 0
                    }
                }

                // Plan task calculation based on Room database task data
                val totalTasks = effectiveTotalDays * 4
                val completedTasks = schedules.sumOf { s ->
                    (if (s.slot1Done) 1 else 0) +
                    (if (s.slot2Done) 1 else 0) +
                    (if (s.slot3Done) 1 else 0) +
                    (if (s.slot4Done) 1 else 0)
                }
                val taskProgressPct = if (totalTasks > 0) {
                    (completedTasks.toFloat() / totalTasks.toFloat()) * 100f
                } else 0f

                CurriculumProgressionState(
                    currentDay = selectedDay,
                    totalDays = effectiveTotalDays,
                    completedDays = completedCount,
                    progressPercentage = progressPct,
                    totalStudyHoursLogged = totalHours,
                    targetTotalHours = effectiveTotalDays * dailyHours,
                    currentStreak = streak,
                    averageDayScore = avgScore,
                    currentMilestone = milestone,
                    pacingStatus = pacing,
                    activeSchedule = activeSched,
                    allSchedules = schedules,
                    totalTasksCount = totalTasks,
                    completedTasksCount = completedTasks,
                    overallTaskCompletionPct = taskProgressPct
                )
            }.collect { state ->
                _curriculumProgression.value = state
                _uiState.update { it.copy(overallCompletionPct = state.overallTaskCompletionPct.roundToInt()) }
            }
        }

        // Real-time Cloud Firestore synchronization
        viewModelScope.launch {
            try {
                firestoreService.observeMaterials().collect { remoteMaterials ->
                    if (remoteMaterials.isNotEmpty()) {
                        _uploadedMaterials.value = remoteMaterials
                    }
                }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            try {
                firestoreService.observeAnnouncements().collect { remoteAnnouncements ->
                    if (remoteAnnouncements.isNotEmpty()) {
                        _teamAnnouncements.value = remoteAnnouncements
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun computeInitialSprintDay() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
        }
        val now = Calendar.getInstance()
        val diffDays = ((now.timeInMillis - startCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
        val validDay = diffDays.coerceIn(1, _batchTargetDays.value)
        _uiState.update { it.copy(sprintDayCurrent = validDay, selectedDay = validDay) }
    }

    fun setPage(page: JeePage) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun selectDay(day: Int) {
        val maxDays = _batchTargetDays.value
        val bounded = day.coerceIn(1, maxDays)
        _uiState.update { it.copy(selectedDay = bounded) }
        val dayQuote = quoteRepository.getDailyQuote(bounded)
        _dailyQuote.value = dayQuote
        _isQuoteBookmarked.value = quoteRepository.isQuoteBookmarked(dayQuote.id)
        refreshDailyView()
        viewModelScope.launch {
            dailyGoalRepository.ensureGoalsForDay(bounded)
        }
    }

    fun nextDay() {
        selectDay(_uiState.value.selectedDay + 1)
    }

    fun prevDay() {
        selectDay(_uiState.value.selectedDay - 1)
    }

    fun setSubjectFilter(filter: String) {
        _uiState.update { it.copy(selectedSubjectFilter = filter) }
    }

    fun setChapterSearchQuery(query: String) {
        _uiState.update { it.copy(chapterSearchQuery = query) }
    }

    fun togglePrintLayout() {
        _uiState.update { it.copy(showPrintLayout = !it.showPrintLayout) }
    }

    fun showSnackbar(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // --- Daily Goal Dashboard & Streak Session Completion methods ---
    fun toggleDailyGoal(goalId: Long, currentVal: Boolean) {
        viewModelScope.launch {
            dailyGoalRepository.toggleGoalCompleted(goalId, currentVal)
            val goal = dailyGoalRepository.getGoalById(goalId)
            if (goal != null) {
                if (goal.slotIndex in 0..3) {
                    dailyScheduleRepository.setSlotDoneExplicit(goal.dayNumber, goal.slotIndex, !currentVal)
                }
                val dayGoals = dailyGoalRepository.getGoalsForDay(goal.dayNumber).first()
                val completedHoursFromSessions = dayGoals.filter { it.isCompleted }.sumOf { it.targetHours.toDouble() }.toFloat()
                repository.setDayRecord(
                    dayIndex = (goal.dayNumber - 1).coerceIn(0, 59),
                    key = "hours",
                    value = String.format(Locale.ENGLISH, "%.1f", completedHoursFromSessions)
                )
            }
            _version.update { it + 1 }
            refreshStats()
        }
    }

    fun completeNextStudySessionToday() {
        viewModelScope.launch {
            val todayDay = _uiState.value.sprintDayCurrent.coerceIn(1, 60)
            dailyGoalRepository.ensureGoalsForDay(todayDay)
            val todayGoals = dailyGoalRepository.getGoalsForDay(todayDay).first()
            val nextPending = todayGoals.firstOrNull { !it.isCompleted }
            if (nextPending != null) {
                dailyGoalRepository.toggleGoalCompleted(nextPending.id, false)
                if (nextPending.slotIndex in 0..3) {
                    dailyScheduleRepository.setSlotDoneExplicit(todayDay, nextPending.slotIndex, true)
                }
                val updatedGoals = dailyGoalRepository.getGoalsForDay(todayDay).first()
                val completedSessions = updatedGoals.count { it.isCompleted }
                val completedSessionHours = updatedGoals.filter { it.isCompleted }.sumOf { it.targetHours.toDouble() }.toFloat()
                val currentManual = repository.getDayRecord(todayDay - 1, "hours", "").toFloatOrNull() ?: 0f
                val newTotalHours = maxOf(completedSessionHours, (currentManual + nextPending.targetHours.coerceAtLeast(2.5f)).coerceAtMost(16f))
                repository.setDayRecord(
                    dayIndex = todayDay - 1,
                    key = "hours",
                    value = String.format(Locale.ENGLISH, "%.1f", newTotalHours)
                )
                _version.update { it + 1 }
                refreshStats()
                showSnackbar("🔥 Session Completed: \"${nextPending.title}\" ($completedSessions/${updatedGoals.size} sessions done today)!")
            } else {
                val currentManual = repository.getDayRecord(todayDay - 1, "hours", "").toFloatOrNull() ?: 12f
                val boosted = (currentManual + 1.5f).coerceAtMost(16f)
                repository.setDayRecord(
                    dayIndex = todayDay - 1,
                    key = "hours",
                    value = String.format(Locale.ENGLISH, "%.1f", boosted)
                )
                _version.update { it + 1 }
                refreshStats()
                showSnackbar("⚡ Bonus Study Session Logged (${String.format(Locale.ENGLISH, "%.1f", boosted)}h today)! Daily Streak Locked In! 🏆")
            }
        }
    }

    fun addCustomDailyGoal(title: String, category: String, targetHours: Float, priority: String) {
        viewModelScope.launch {
            dailyGoalRepository.addCustomGoal(
                day = _uiState.value.selectedDay,
                title = title,
                category = category,
                targetHours = targetHours,
                priority = priority
            )
            showSnackbar("Added custom goal for Day ${_uiState.value.selectedDay}")
            _version.update { it + 1 }
        }
    }

    fun deleteDailyGoal(goalId: Long) {
        viewModelScope.launch {
            dailyGoalRepository.deleteGoal(goalId)
            showSnackbar("Goal deleted")
            _version.update { it + 1 }
        }
    }

    fun setAllDailyGoalsCompleted(isCompleted: Boolean) {
        viewModelScope.launch {
            val day = _uiState.value.selectedDay
            dailyGoalRepository.setAllGoalsCompleted(day, isCompleted)
            dailyScheduleRepository.setDayCompleted(day, isCompleted)
            showSnackbar(if (isCompleted) "All Day $day goals marked complete! 🎯" else "Day $day goals reset")
            _version.update { it + 1 }
        }
    }

    fun resetDailyGoalsForCurrentDay() {
        viewModelScope.launch {
            val day = _uiState.value.selectedDay
            dailyGoalRepository.resetGoalsForDay(day)
            showSnackbar("Default scheduled tasks restored for Day $day")
            _version.update { it + 1 }
        }
    }

    // --- Room Database 60-Day Curriculum Progression Methods ---
    fun toggleStudySlotDone(dayNumber: Int, slotIndex: Int) {
        viewModelScope.launch {
            dailyScheduleRepository.toggleSlotDone(dayNumber, slotIndex)
            val schedule = dailyScheduleRepository.getScheduleForDaySync(dayNumber)
            val isDone = when (slotIndex) {
                0 -> schedule?.slot1Done ?: false
                1 -> schedule?.slot2Done ?: false
                2 -> schedule?.slot3Done ?: false
                3 -> schedule?.slot4Done ?: false
                else -> false
            }
            dailyGoalRepository.setGoalDoneBySlot(dayNumber, slotIndex, isDone)
            val dayIndex = dayNumber - 1
            val checkKeys = listOf("lec", "notes", "dpp", "pyq")
            checkKeys.forEach { k ->
                val curr = repository.getSlotCheck(dayIndex, slotIndex, k)
                repository.setSlotCheck(dayIndex, slotIndex, k, !curr)
            }
            _version.update { it + 1 }
            refreshDailyView()
            refreshStats()
        }
    }

    fun toggleStudyDayCompleted(dayNumber: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            dailyScheduleRepository.setDayCompleted(dayNumber, isCompleted)
            dailyGoalRepository.setAllGoalsCompleted(dayNumber, isCompleted)
            showSnackbar(if (isCompleted) "Day $dayNumber marked COMPLETE in 60-Day Curriculum! 🏆" else "Day $dayNumber marked in progress")
            _version.update { it + 1 }
        }
    }

    fun updateStudyDayAudit(
        dayNumber: Int,
        actualHours: Float,
        score: Int,
        weakTopics: String,
        nextDayPlan: String
    ) {
        viewModelScope.launch {
            dailyScheduleRepository.updateDayAudit(
                dayNumber = dayNumber,
                actualHours = actualHours,
                score = score,
                weakTopics = weakTopics,
                nextDayPlan = nextDayPlan
            )
            val dayIndex = dayNumber - 1
            repository.setDayRecord(dayIndex, "hours", actualHours.toString())
            repository.setDayRecord(dayIndex, "score", score.toString())
            repository.setDayRecord(dayIndex, "weakTopics", weakTopics)
            repository.setDayRecord(dayIndex, "notesPlan", nextDayPlan)
            _version.update { it + 1 }
            refreshStats()
            showSnackbar("Day $dayNumber study audit & metrics saved to Room Database! ✅")
        }
    }

    fun getDailyStudySchedule(dayNumber: Int): Flow<DailyStudyScheduleEntity?> {
        return dailyScheduleRepository.getScheduleForDay(dayNumber)
    }

    // --- Slot checks ---
    fun toggleSlotCheck(dayIndex: Int, slotIndex: Int, checkKey: String, currentVal: Boolean) {
        repository.setSlotCheck(dayIndex, slotIndex, checkKey, !currentVal)
        _version.update { it + 1 }
        refreshStats()
    }

    fun getSlotProgress(dayIndex: Int, slotIndex: Int): SlotProgress {
        return repository.getSlotProgress(dayIndex, slotIndex)
    }

    // --- Chapter chain checks ---
    fun toggleChapterCheck(chapterId: Int, checkKey: String, currentVal: Boolean) {
        val newVal = !currentVal
        repository.setChapterCheck(chapterId, checkKey, newVal)
        val chapterItem = _activeChapters.value.find { it.id == chapterId }
        viewModelScope.launch {
            chapterProgressRepository.updateChapterStep(
                chapterId = chapterId,
                chapterTitle = chapterItem?.title ?: "Chapter $chapterId",
                subject = chapterItem?.subject ?: "Physics",
                stepKey = checkKey,
                checked = newVal
            )
        }
        _version.update { it + 1 }
        refreshStats()
    }

    fun markFullChapterChain(chapterId: Int, complete: Boolean) {
        listOf("lecture", "notes", "dpp", "ncert", "prarambh", "prabal", "prakshit", "pyq", "revision", "test").forEach { k ->
            repository.setChapterCheck(chapterId, k, complete)
        }
        val chapterItem = _activeChapters.value.find { it.id == chapterId }
        viewModelScope.launch {
            chapterProgressRepository.markFullChapterChain(
                chapterId = chapterId,
                chapterTitle = chapterItem?.title ?: "Chapter $chapterId",
                subject = chapterItem?.subject ?: "Physics",
                complete = complete
            )
        }
        _version.update { it + 1 }
        refreshStats()
    }

    fun bulkMarkSubjectMilestone(chapterIds: List<Int>, milestoneKey: String, markCompleted: Boolean = true) {
        if (chapterIds.isEmpty()) return
        chapterIds.forEach { id ->
            repository.setChapterCheck(id, milestoneKey, markCompleted)
        }
        _version.update { it + 1 }
        refreshStats()
        showSnackbar("✅ Updated '$milestoneKey' across ${chapterIds.size} chapters!")
    }

    fun getChapterChain(chapterId: Int): ChapterChain {
        return repository.getChapterChain(chapterId)
    }

    // --- NCERT checks (Room Database Integrated + NCERT -> Syllabus -> Planner Workflow) ---
    fun toggleNcertStep(chapterId: Int, stepKey: String) {
        viewModelScope.launch {
            ncertRepository.toggleStep(chapterId, stepKey)
            // Synchronize with legacy repository key if applicable
            val currentVal = repository.getNcertCheck(chapterId, stepKey)
            repository.setNcertCheck(chapterId, stepKey, !currentVal)
            // Auto-sync with Syllabus Chapter Tracker 'ncert' step
            repository.setChapterCheck(chapterId, "ncert", true)
            _version.update { it + 1 }
            refreshStats()
        }
    }

    fun markAllNcertSteps(chapterId: Int, completed: Boolean) {
        viewModelScope.launch {
            ncertRepository.markAllSteps(chapterId, completed)
            listOf("read", "examples", "marked", "revision").forEach { k ->
                repository.setNcertCheck(chapterId, k, completed)
            }
            repository.setChapterCheck(chapterId, "ncert", completed)
            _version.update { it + 1 }
            refreshStats()
            showSnackbar(if (completed) "All NCERT milestones complete & synced to Syllabus Tracker!" else "NCERT checklist cleared.")
        }
    }

    /**
     * NCERT -> Syllabus -> Planner Connected Workflow:
     * 1. Marks NCERT reading & in-text examples in NCERT Command
     * 2. Syncs 'ncert' & 'notes' milestones in Syllabus Chapter Tracker
     * 3. Schedules a dedicated 3h block into Today's Daily Planner & Practice Stack
     */
    fun pushNcertChapterToSyllabusAndPlanner(
        chapterId: Int,
        chapterTitle: String,
        subject: String
    ) {
        viewModelScope.launch {
            // Step 1: NCERT Command
            val existingEntity = ncertChapters.value.find { it.chapterId == chapterId }
            if (existingEntity != null && !existingEntity.theoryRead) {
                ncertRepository.toggleStep(chapterId, "theoryRead")
            }
            // Step 2: Syllabus Chapter Tracker
            repository.setChapterCheck(chapterId, "ncert", true)
            repository.setChapterCheck(chapterId, "lecture", true)

            // Step 3: Daily Planner Slot
            val day = _uiState.value.selectedDay.coerceIn(1, _batchTargetDays.value.coerceAtLeast(60))
            dailyGoalRepository.addCustomGoal(
                day = day,
                title = "[NCERT → Syllabus → Planner] $subject: $chapterTitle (Line-by-Line + PYQs)",
                category = subject,
                targetHours = 3.0f,
                priority = "HIGH"
            )
            _version.update { it + 1 }
            refreshStats()
            showSnackbar("✅ Workflow Synced: '$chapterTitle' linked across NCERT → Syllabus → Day $day Planner!")
        }
    }

    /**
     * 1-Tap Full Pipeline Sync: Pushes top pending NCERT chapters into Syllabus Tracker & Daily Planner
     */
    fun syncAllPendingNcertToSyllabusAndPlanner() {
        viewModelScope.launch {
            val allChaps = _activeChapters.value
            val pendingChaps = allChaps.filter { !repository.getChapterChain(it.id).ncert }.take(3)
                .ifEmpty { allChaps.take(2) }
            val day = _uiState.value.selectedDay.coerceIn(1, _batchTargetDays.value.coerceAtLeast(60))

            pendingChaps.forEachIndexed { idx, chap ->
                repository.setChapterCheck(chap.id, "ncert", true)
                val targetDay = (day + idx).coerceAtMost(_batchTargetDays.value.coerceAtLeast(60))
                dailyGoalRepository.addCustomGoal(
                    day = targetDay,
                    title = "[NCERT → Syllabus → Planner] ${chap.subject}: ${chap.title}",
                    category = chap.subject,
                    targetHours = 3.0f,
                    priority = "HIGH"
                )
            }
            _version.update { it + 1 }
            refreshStats()
            showSnackbar("🚀 NCERT → Syllabus → Planner Workflow Executed! ${pendingChaps.size} chapters scheduled into Daily Planner.")
        }
    }

    fun updateNcertNotes(chapterId: Int, notes: String) {
        viewModelScope.launch {
            ncertRepository.updateNotes(chapterId, notes)
            showSnackbar("NCERT notes saved to database!")
        }
    }

    fun resetNcertProgress() {
        viewModelScope.launch {
            ncertRepository.resetAll(_activeChapters.value)
            _version.update { it + 1 }
            refreshStats()
            showSnackbar("All NCERT progress reset in Room.")
        }
    }

    fun exportNcertProgressJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = ncertRepository.exportNcertJson(ncertChapters.value)
            onResult(json)
        }
    }

    fun importNcertProgressJson(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = ncertRepository.importNcertJson(json)
            if (success) {
                _version.update { it + 1 }
                refreshStats()
                showSnackbar("NCERT progress restored from JSON!")
            } else {
                showSnackbar("Failed to parse NCERT JSON.")
            }
            onComplete(success)
        }
    }

    fun toggleNcertCheck(chapterId: Int, key: String, currentVal: Boolean) {
        toggleNcertStep(chapterId, key)
    }

    fun getNcertProgress(chapterId: Int): NcertProgress {
        val roomEntity = ncertChapters.value.find { it.chapterId == chapterId }
        return if (roomEntity != null) {
            NcertProgress(
                read = roomEntity.theoryRead,
                examples = roomEntity.inTextExamples,
                markedLines = roomEntity.markedLines,
                revision = roomEntity.revision1
            )
        } else {
            repository.getNcertProgress(chapterId)
        }
    }

    // --- Day record details ---
    fun getDayRecord(dayIndex: Int, key: String): String {
        return repository.getDayRecord(dayIndex, key, "")
    }

    fun updateDayRecord(dayIndex: Int, key: String, value: String) {
        repository.setDayRecord(dayIndex, key, value)
        _version.update { it + 1 }
        refreshStats()
    }

    // --- Teacher checklist ---
    fun getTeacherCheck(dayIndex: Int, teacherId: String, key: String): Boolean {
        return repository.getTeacherCheck(dayIndex, teacherId, key)
    }

    fun toggleTeacherCheck(dayIndex: Int, teacherId: String, key: String, currentVal: Boolean) {
        repository.setTeacherCheck(dayIndex, teacherId, key, !currentVal)
        _version.update { it + 1 }
    }

    fun getTeacherLecNo(dayIndex: Int, teacherId: String): String {
        return repository.getTeacherLecNo(dayIndex, teacherId)
    }

    fun updateTeacherLecNo(dayIndex: Int, teacherId: String, lecNo: String) {
        repository.setTeacherLecNo(dayIndex, teacherId, lecNo)
        _version.update { it + 1 }
    }

    // --- Weekly review ---
    fun getWeeklyReview(weekNumber: Int): WeeklyReviewData {
        return repository.getWeeklyReview(weekNumber)
    }

    fun toggleWeeklyFlag(weekNumber: Int, key: String, currentVal: Boolean) {
        repository.setWeeklyReviewFlag(weekNumber, key, !currentVal)
        _version.update { it + 1 }
    }

    fun updateWeeklyString(weekNumber: Int, key: String, value: String) {
        repository.setWeeklyReviewString(weekNumber, key, value)
        _version.update { it + 1 }
    }

    fun updateWeeklyScore(weekNumber: Int, score: Float) {
        repository.setWeeklyReviewScore(weekNumber, score)
        _version.update { it + 1 }
    }

    // --- Calculator ---
    fun setWorkloadHours(hours: Float) {
        repository.setWorkloadHours(hours)
        updateCalc()
    }

    fun setDailyRate(rate: Float) {
        repository.setDailyRate(rate)
        updateCalc()
    }

    private fun updateCalc() {
        val w = repository.getWorkloadHours()
        val r = repository.getDailyRate().coerceAtLeast(1f)
        val days = w / r
        _uiState.update {
            it.copy(
                workloadHours = w,
                dailyRate = r,
                calculatedDays = days
            )
        }
    }

    // --- Refresh Stats ---
    fun refreshAll() {
        refreshDailyView()
        refreshStats()
        updateCalc()
    }

    private fun refreshDailyView() {
        val selectedDay = _uiState.value.selectedDay
        val dayIndex = selectedDay - 1
        val isNeet = isNeetMode.value
        val dailyHours = _batchDailyHours.value
        val slotHours = (dailyHours / 4f).coerceAtLeast(1f)

        viewModelScope.launch(Dispatchers.IO) {
            val sched = dailyScheduleRepository.getScheduleForDaySync(selectedDay)
            val slots = if (sched != null) {
                listOf(
                    DailySlot(0, sched.slot1Title, "${sched.slot1Hours.toInt()} Hours", sched.slot1Target),
                    DailySlot(1, sched.slot2Title, "${sched.slot2Hours.toInt()} Hours", sched.slot2Target),
                    DailySlot(2, sched.slot3Title, "${sched.slot3Hours.toInt()} Hours", sched.slot3Target),
                    DailySlot(3, sched.slot4Title, "${sched.slot4Hours.toInt()} Hours", sched.slot4Target)
                )
            } else {
                val cal = Calendar.getInstance().apply {
                    set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
                    add(Calendar.DAY_OF_YEAR, dayIndex)
                }
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                val slotTitles = if (isNeet) {
                    when (dayOfWeek) {
                        0 -> listOf("NEET Full Mock Test", "Biology (Botany & Zoology)", "NEET Physics", "NEET Chemistry")
                        1 -> listOf("11 Biology (NCERT Deep Dive)", "NEET Physics", "NEET Chemistry", "12 Biology")
                        2 -> listOf("NEET Chemistry", "12 Biology", "NEET Physics", "11 Biology")
                        3 -> listOf("12 Biology", "NEET Physics", "NEET Chemistry", "Botany Backlog")
                        4 -> listOf("NEET Physics", "NEET Chemistry", "Biology (Botany & Zoology)", "11th Revision")
                        5 -> listOf("NEET Chemistry", "11 Biology", "12 Biology", "NEET Physics")
                        else -> listOf("12 Biology", "NEET Physics", "NEET Chemistry", "Zoology Revision")
                    }
                } else {
                    JeeData.WEEKDAY_SLOTS[dayOfWeek]?.take(4) ?: listOf("Slot 1 (Physics)", "Slot 2 (Maths)", "Slot 3 (Chemistry)", "Slot 4 (Revision)")
                }
                slotTitles.mapIndexed { idx, title ->
                    val suggested = getSuggestedTargetForSlot(title, dayIndex)
                    DailySlot(idx, title, "${slotHours.toInt()} Hours", suggested)
                }
            }

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        todayTargetHours = dailyHours.toInt(),
                        todayCommandSlots = slots
                    )
                }
            }
        }
    }

    private fun getSuggestedTargetForSlot(slotTitle: String, dayIndex: Int): String {
        val isNeet = isNeetMode.value
        if (isNeet) {
            return when {
                slotTitle.contains("Bio", ignoreCase = true) || slotTitle.contains("Botany", ignoreCase = true) || slotTitle.contains("Zoology", ignoreCase = true) ->
                    "NCERT Line-by-Line & 40 NCERT-based MCQs"
                slotTitle.contains("Physics", ignoreCase = true) -> "NEET Physics Formulas & 30 Numerical PYQs"
                slotTitle.contains("Chem", ignoreCase = true) -> "NCERT Chemistry Reactions, Tables & DPP"
                slotTitle.contains("PYQ", ignoreCase = true) || slotTitle.contains("Test", ignoreCase = true) -> "NEET AIPMT PYQs & Timed Sectional Test"
                else -> "High-Yield NCERT Diagram & Exception Review"
            }
        }
        val dayInfo = JeeData.DAY_SCHEDULES[dayIndex + 1]
        return when {
            slotTitle.contains("Physics", ignoreCase = true) -> dayInfo?.physics ?: "Physics Lecture & DPP"
            slotTitle.contains("Maths", ignoreCase = true) -> dayInfo?.maths ?: "Maths Lecture & Practice"
            slotTitle.contains("Chem", ignoreCase = true) -> dayInfo?.chem ?: "Chemistry Lecture & NCERT"
            slotTitle.contains("PYQ", ignoreCase = true) || slotTitle.contains("Test", ignoreCase = true) -> "Mixed Test & Analysis"
            else -> dayInfo?.backlogsOrTest ?: "Backlog Clearance / PYQ"
        }
    }

    private fun refreshStats() {
        // Total focused hours
        var totalHours = 0f
        for (i in 0 until 60) {
            val hStr = repository.getDayRecord(i, "hours", "")
            totalHours += hStr.toFloatOrNull() ?: 0f
        }

        // Chapters completion
        var completedChaps = 0
        val allChapters = _activeChapters.value
        allChapters.forEach { chap ->
            val chain = repository.getChapterChain(chap.id)
            if (chain.isComplete()) {
                completedChaps++
            }
        }
        val overallPct = if (allChapters.isNotEmpty()) (completedChaps * 100) / allChapters.size else 0

        // Subject breakdown
        val subjects = allChapters.map { it.subject }.distinct()
        val subjectStats = subjects.map { subj ->
            val chapsInSubj = allChapters.filter { it.subject == subj }
            val doneInSubj = chapsInSubj.count { repository.getChapterChain(it.id).isComplete() }
            val pct = if (chapsInSubj.isNotEmpty()) (doneInSubj * 100) / chapsInSubj.size else 0
            SubjectProgressStat(subj, doneInSubj, chapsInSubj.size, pct)
        }

        // Practice stack stats
        val practiceKeys = listOf(
            "dpp" to "DPP (Daily Problems)",
            "module" to "Module Sets",
            "prarambh" to "Prarambh (L1 Foundation)",
            "prabal" to "Prabal (L2 Intermediate)",
            "prakshit" to "Prakshit (L3 Advanced)",
            "pyq" to "PYQs (1800+ Questions)",
            "revision" to "Formula & Short Notes Revision",
            "test" to "Mock Tests & Analysis"
        )
        val pStats = practiceKeys.map { (k, name) ->
            val doneCount = allChapters.count { repository.getChapterCheck(it.id, k) }
            val pct = if (allChapters.isNotEmpty()) (doneCount * 100) / allChapters.size else 0
            PracticeKeyStat(k, name, doneCount, allChapters.size, pct)
        }

        _uiState.update {
            it.copy(
                totalFocusedHours = totalHours,
                overallCompletionPct = if (_curriculumProgression.value.totalTasksCount > 0) {
                    _curriculumProgression.value.overallTaskCompletionPct.roundToInt()
                } else overallPct,
                subjectProgressStats = subjectStats,
                practiceStats = pStats
            )
        }
    }

    // --- Export / Import / Reset ---
    private val pdfManager by lazy { PdfExportImportManager(getApplication<Application>().applicationContext) }

    private val _isPdfExporting = MutableStateFlow(false)
    val isPdfExporting: StateFlow<Boolean> = _isPdfExporting.asStateFlow()

    private val _lastExportedPdf = MutableStateFlow<File?>(null)
    val lastExportedPdf: StateFlow<File?> = _lastExportedPdf.asStateFlow()

    fun exportProgressJson(): String {
        return repository.exportAllJson()
    }

    fun importProgressJson(json: String): Boolean {
        val success = repository.importFromJson(json)
        if (success) {
            refreshAll()
            _version.update { it + 1 }
            showSnackbar("Progress snapshot successfully imported!")
        } else {
            showSnackbar("Failed to parse JSON snapshot.")
        }
        return success
    }

    fun exportProgressPdf(onPdfReady: (File) -> Unit = {}) {
        viewModelScope.launch {
            _isPdfExporting.value = true
            try {
                val state = _uiState.value
                val batch = _selectedBatch.value
                val targetDays = _batchTargetDays.value
                val dailyHours = _batchDailyHours.value
                val goals = dailyGoalRepository.getGoalsForDay(state.selectedDay).first()
                val snapshotJson = repository.exportAllJson()

                val pdfFile = pdfManager.exportPdfReport(
                    uiState = state,
                    activeBatch = batch,
                    targetDays = targetDays,
                    dailyHours = dailyHours,
                    goals = goals,
                    fullSnapshotJson = snapshotJson
                )
                _lastExportedPdf.value = pdfFile
                showSnackbar("Upgrade Hub PDF report generated successfully!")
                onPdfReady(pdfFile)
            } catch (e: Exception) {
                showSnackbar("PDF Export failed: ${e.localizedMessage}")
            } finally {
                _isPdfExporting.value = false
            }
        }
    }

    fun sharePdf(file: File) {
        try {
            val intent = pdfManager.createSharePdfIntent(file)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val chooser = Intent.createChooser(intent, "Share Upgrade Hub PDF Plan").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            getApplication<Application>().startActivity(chooser)
        } catch (e: Exception) {
            showSnackbar("Unable to share PDF: ${e.localizedMessage}")
        }
    }

    fun savePdfToLocalUri(destinationUri: Uri) {
        exportProgressPdf { generatedFile ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>().applicationContext
                    context.contentResolver.openOutputStream(destinationUri)?.use { outStream ->
                        generatedFile.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        showSnackbar("Study progress & planner PDF saved to device storage!")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showSnackbar("Failed to save PDF to device: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    fun importProgressPdf(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    showSnackbar("Could not open selected PDF.")
                    return@launch
                }
                when (val outcome = pdfManager.parsePdfStream(inputStream)) {
                    is PdfImportOutcome.SnapshotRestored -> {
                        val ok = repository.importFromJson(outcome.rawJson)
                        if (ok) {
                            refreshAll()
                            _version.update { it + 1 }
                            showSnackbar("PDF Snapshot Restored! All study records and progress restored.")
                        } else {
                            showSnackbar("Could not apply extracted PDF snapshot data.")
                        }
                    }
                    is PdfImportOutcome.SyllabusImported -> {
                        val customBatch = BatchCurriculum(
                            id = "pdf_batch_${System.currentTimeMillis()}",
                            title = outcome.title,
                            examType = "Custom Syllabus",
                            classGrade = "Imported Document",
                            sourceUrl = uri.toString(),
                            description = outcome.rawSummary,
                            totalLectures = outcome.totalLectures,
                            totalHours = outcome.totalLectures * 1.5f,
                            chapters = emptyList()
                        )
                        _batchCurriculums.update { listOf(customBatch) + it }
                        _selectedBatch.value = customBatch
                        showSnackbar("Syllabus imported from PDF: ${outcome.title} (~${outcome.totalLectures} lectures)")
                    }
                    is PdfImportOutcome.Error -> {
                        showSnackbar(outcome.message)
                    }
                }
            } catch (e: Exception) {
                showSnackbar("Error importing PDF: ${e.localizedMessage}")
            }
        }
    }

    fun resetAllProgress() {
        repository.resetAll()
        resetNcertProgress()
        viewModelScope.launch {
            dailyGoalRepository.resetGoalsForDay(_uiState.value.selectedDay)
        }
        refreshAll()
        _version.update { it + 1 }
        showSnackbar("All tracker, daily goals, and NCERT progress has been reset.")
    }

    private fun computeSevenDayTrend(
        state: JeeUiState,
        allGoals: List<DailyGoalEntity>
    ): SevenDayTrendData {
        val selectedDay = state.selectedDay
        val currentSprintDay = state.sprintDayCurrent

        // Standard 7-day rolling window ending at max(7, selectedDay)
        val endDay = maxOf(7, selectedDay).coerceIn(7, 60)
        val startDay = (endDay - 6).coerceAtLeast(1)

        val points = mutableListOf<DailyStudyHoursPoint>()
        val dayFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

        for (d in startDay..endDay) {
            val dayIndex = d - 1
            val cal = Calendar.getInstance().apply {
                set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
                add(Calendar.DAY_OF_YEAR, dayIndex)
            }
            val dateLabel = dayFormat.format(cal.time)
            val dayOfWeek = dayOfWeekFormat.format(cal.time)

            val isWeekendOrWed = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            val targetHours = if (isWeekendOrWed) 15f else 12f

            // 1. Check manual hours record
            val manualHours = repository.getDayRecord(dayIndex, "hours", "").toFloatOrNull() ?: 0f

            // 2. Check completed goals from daily goals
            val goalsForDay = allGoals.filter { it.dayNumber == d }
            val completedGoalsHours = goalsForDay
                .filter { it.isCompleted }
                .sumOf { it.targetHours.toDouble() }
                .toFloat()

            // Completed is highest of manual or completed tasks
            val completedHours = maxOf(manualHours, completedGoalsHours)
            val isMet = completedHours >= targetHours

            points.add(
                DailyStudyHoursPoint(
                    dayNumber = d,
                    dateLabel = dateLabel,
                    dayOfWeek = dayOfWeek,
                    completedHours = completedHours,
                    targetHours = targetHours,
                    isTargetMet = isMet,
                    isToday = (d == currentSprintDay),
                    isSelected = (d == selectedDay)
                )
            )
        }

        val totalHours = points.sumOf { it.completedHours.toDouble() }.toFloat()
        val targetHours = points.sumOf { it.targetHours.toDouble() }.toFloat()
        val averageHours = if (points.isNotEmpty()) totalHours / points.size else 0f
        val daysCompleted = points.count { it.isTargetMet }
        val consistencyScore = if (targetHours > 0f) {
            ((totalHours / targetHours) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // Calculate active streak (days with hours >= 6h moving backwards from today/latest)
        var streak = 0
        for (i in points.indices.reversed()) {
            if (points[i].completedHours >= 6f || points[i].isTargetMet) {
                streak++
            } else if (points[i].dayNumber <= currentSprintDay) {
                break
            }
        }

        val bestDay = points.maxByOrNull { it.completedHours }
        val bestDayNum = bestDay?.dayNumber ?: startDay
        val bestDayHrs = bestDay?.completedHours ?: 0f

        val (rating, colorHex) = when {
            consistencyScore >= 85 -> "Exceptional Consistency" to 0xFF00C853
            consistencyScore >= 70 -> "Strong Momentum" to 0xFF5266CF
            consistencyScore >= 50 -> "Steady Progress" to 0xFFFFA000
            else -> "Building Routine" to 0xFF6574FF
        }

        val narrative = when {
            consistencyScore >= 85 ->
                "Phenomenal discipline! You logged ${String.format(Locale.ENGLISH, "%.1f", totalHours)}h across 7 days (avg ${String.format(Locale.ENGLISH, "%.1f", averageHours)}h/day) and met targets on $daysCompleted of 7 days. Your marathon sprint pace is locked in."
            consistencyScore >= 70 ->
                "Solid study momentum! You logged ${String.format(Locale.ENGLISH, "%.1f", totalHours)}h with a ${String.format(Locale.ENGLISH, "%.1f", averageHours)}h daily average. Consistently clearing your daily command slots will boost your score above 85%."
            consistencyScore >= 50 ->
                "Steady progress. You logged ${String.format(Locale.ENGLISH, "%.1f", totalHours)}h across 7 days. Target completing your full 12h/15h slots to build a compound advantage."
            totalHours > 0f ->
                "Good start. You've logged ${String.format(Locale.ENGLISH, "%.1f", totalHours)}h so far. Complete each 3-hour focus slot today to establish your daily consistency streak."
            else ->
                "Begin logging your completed 3-hour study blocks or check off daily goals in the planner to visualize your 7-day study consistency and trend."
        }

        val summary = ConsistencySummary(
            totalHours = totalHours,
            targetHours = targetHours,
            averageHoursPerDay = averageHours,
            consistencyScore = consistencyScore,
            daysCompletedTarget = daysCompleted,
            totalDays = points.size,
            currentStreak = streak,
            bestDayNumber = bestDayNum,
            bestDayHours = bestDayHrs,
            consistencyRating = rating,
            ratingColorHex = colorHex,
            summaryNarrative = narrative
        )

        val maxVal = maxOf(
            16f,
            (points.maxOfOrNull { maxOf(it.completedHours, it.targetHours) } ?: 16f) + 1f
        )

        return SevenDayTrendData(
            points = points,
            consistencySummary = summary,
            maxScaleHours = maxVal,
            startDay = startDay,
            endDay = endDay
        )
    }

    private fun computeSixtyDayProgress(
        state: JeeUiState,
        allGoals: List<DailyGoalEntity>,
        activeChapters: List<ChapterItem>
    ): SixtyDayProgressData {
        val selectedDay = state.selectedDay
        val currentSprintDay = state.sprintDayCurrent.coerceIn(1, 60)
        val totalChaptersCount = activeChapters.size.coerceAtLeast(1)

        val dayFormat = SimpleDateFormat("dd MMM", Locale.ENGLISH)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

        val points = mutableListOf<SixtyDayProgressPoint>()
        var runningHours = 0f
        var runningTargetHours = 0f

        val completedChaptersSet = activeChapters.filter { repository.getChapterChain(it.id).isComplete() }.map { it.id }.toSet()
        val actualCompletedChaptersCount = completedChaptersSet.size

        val rawHoursList = mutableListOf<Float>()
        for (d in 1..60) {
            val dayIndex = d - 1
            val manualHours = repository.getDayRecord(dayIndex, "hours", "").toFloatOrNull() ?: 0f
            val goalsForDay = allGoals.filter { it.dayNumber == d }
            val completedGoalsHours = goalsForDay
                .filter { it.isCompleted }
                .sumOf { it.targetHours.toDouble() }
                .toFloat()
            val completedHours = maxOf(manualHours, completedGoalsHours)
            rawHoursList.add(completedHours)
        }

        for (d in 1..60) {
            val dayIndex = d - 1
            val cal = Calendar.getInstance().apply {
                set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
                add(Calendar.DAY_OF_YEAR, dayIndex)
            }
            val dateLabel = dayFormat.format(cal.time)
            val dayOfWeek = dayOfWeekFormat.format(cal.time)

            val isWeekendOrWed = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            val targetHours = if (isWeekendOrWed) 15f else 12f
            runningTargetHours += targetHours

            val dayHours = rawHoursList[dayIndex]
            val isFuture = (d > currentSprintDay)
            val isToday = (d == currentSprintDay)
            val isSelected = (d == selectedDay)
            val isMet = dayHours >= targetHours

            if (d <= currentSprintDay) {
                runningHours += dayHours
            }

            // 7-day rolling moving average
            val windowStart = maxOf(0, dayIndex - 6)
            val windowSlice = rawHoursList.subList(windowStart, dayIndex + 1)
            val movingAvg = if (windowSlice.isNotEmpty()) windowSlice.sum() / windowSlice.size else 0f

            // Syllabus coverage computation
            val actualOverallPct = (actualCompletedChaptersCount.toFloat() / totalChaptersCount) * 100f
            val coveragePct = if (d <= currentSprintDay) {
                val weight = d.toFloat() / currentSprintDay.toFloat()
                (actualOverallPct * weight).coerceIn(0f, 100f)
            } else {
                val remainingRatio = (d - currentSprintDay).toFloat() / (60 - currentSprintDay).coerceAtLeast(1).toFloat()
                (actualOverallPct + (100f - actualOverallPct) * remainingRatio).coerceIn(0f, 100f)
            }

            val estChapters = ((coveragePct / 100f) * totalChaptersCount).roundToInt().coerceIn(0, totalChaptersCount)

            points.add(
                SixtyDayProgressPoint(
                    dayNumber = d,
                    dateLabel = dateLabel,
                    dayOfWeek = dayOfWeek,
                    studyHours = dayHours,
                    cumulativeHours = runningHours,
                    targetHours = targetHours,
                    cumulativeTargetHours = runningTargetHours,
                    syllabusCoveragePct = coveragePct,
                    chaptersCompleted = estChapters,
                    movingAverageHours = movingAvg,
                    isTargetMet = isMet,
                    isToday = isToday,
                    isFuture = isFuture,
                    isSelected = isSelected
                )
            )
        }

        val totalLoggedHours = points.filter { !it.isFuture }.sumOf { it.studyHours.toDouble() }.toFloat()
        val totalTargetHours = points.sumOf { it.targetHours.toDouble() }.toFloat()
        val pastDaysCount = currentSprintDay.coerceAtLeast(1)
        val avgHours = if (pastDaysCount > 0) totalLoggedHours / pastDaysCount else 0f
        val targetMetDays = points.filter { !it.isFuture }.count { it.isTargetMet }

        val bestDay = points.filter { !it.isFuture }.maxByOrNull { it.studyHours }
        val bestDayNum = bestDay?.dayNumber ?: 1
        val bestDayHrs = bestDay?.studyHours ?: 0f

        val currentCoverage = points.getOrNull(currentSprintDay - 1)?.syllabusCoveragePct ?: 0f
        val expectedCoverageByNow = (currentSprintDay.toFloat() / 60f) * 100f

        val (paceStatus, paceColorHex, insight) = when {
            currentCoverage >= expectedCoverageByNow + 4f -> Triple(
                "Ahead of Schedule 🚀",
                0xFF00C853,
                "Outstanding study momentum! You have completed ${String.format(Locale.ENGLISH, "%.1f", currentCoverage)}% of the syllabus, exceeding the 60-day baseline target with a strong daily average of ${String.format(Locale.ENGLISH, "%.1f", avgHours)}h/day."
            )
            currentCoverage >= expectedCoverageByNow - 5f -> Triple(
                "On Track 🎯",
                0xFF0070F3,
                "Steady syllabus progression. You have logged ${String.format(Locale.ENGLISH, "%.1f", totalLoggedHours)}h across $pastDaysCount sprint days (${String.format(Locale.ENGLISH, "%.1f", avgHours)}h/day avg). Maintain 12h/day to complete 100% syllabus on schedule."
            )
            else -> Triple(
                "Sprint Acceleration Needed ⚡",
                0xFFFF9800,
                "Syllabus coverage is currently ${String.format(Locale.ENGLISH, "%.1f", currentCoverage)}% vs target ${String.format(Locale.ENGLISH, "%.1f", expectedCoverageByNow)}%. Clear all 4 daily command slots and high-yield PYQs to accelerate your completion pace."
            )
        }

        val projectedCompletionDay = if (avgHours >= 10f) {
            val remainingHours = (720f - totalLoggedHours).coerceAtLeast(0f)
            val daysNeeded = (remainingHours / avgHours).roundToInt()
            (currentSprintDay + daysNeeded).coerceIn(currentSprintDay, 60)
        } else {
            60
        }

        val summary = SixtyDayAnalyticsSummary(
            totalLoggedHours = totalLoggedHours,
            totalTargetHours = totalTargetHours,
            currentCoveragePct = currentCoverage,
            averageDailyHours = avgHours,
            targetMetDaysCount = targetMetDays,
            totalDays = 60,
            bestDayNumber = bestDayNum,
            bestDayHours = bestDayHrs,
            projectedCompletionDay = projectedCompletionDay,
            paceStatus = paceStatus,
            paceColorHex = paceColorHex,
            summaryInsight = insight
        )

        val maxValHours = maxOf(18f, (points.maxOfOrNull { it.studyHours } ?: 16f) + 2f)
        val maxValCumulative = maxOf(750f, points.maxOfOrNull { it.cumulativeTargetHours } ?: 750f)

        return SixtyDayProgressData(
            points = points,
            summary = summary,
            maxStudyHours = maxValHours,
            maxCumulativeHours = maxValCumulative
        )
    }

    private fun computeHabitStreak(
        state: JeeUiState,
        allGoals: List<DailyGoalEntity>
    ): HabitStreakData {
        val currentSprintDay = state.sprintDayCurrent.coerceIn(1, 60)

        val dateFormat = SimpleDateFormat("MMM d", Locale.ENGLISH)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
        val baseCal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
        }

        data class DayRecord(
            val dayNumber: Int,
            val dateLabel: String,
            val dayOfWeek: String,
            val completedHours: Float,
            val targetHours: Float,
            val isGoalMet: Boolean,
            val completedGoalsCount: Int,
            val totalGoalsCount: Int
        )

        val evaluatedDays = mutableListOf<DayRecord>()

        for (d in 1..currentSprintDay) {
            val cal = (baseCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, d - 1)
            }
            val dateLabel = dateFormat.format(cal.time)
            val dayOfWeek = dayOfWeekFormat.format(cal.time)

            val manualHours = repository.getDayRecord(d - 1, "hours", "").toFloatOrNull() ?: 0f
            val goalsForDay = allGoals.filter { it.dayNumber == d }
            val completedGoals = goalsForDay.count { it.isCompleted }
            val totalGoals = goalsForDay.size
            val completedGoalHours = goalsForDay.filter { it.isCompleted }.sumOf { it.targetHours.toDouble() }.toFloat()

            val completedHours = maxOf(manualHours, completedGoalHours)
            val targetHours = if (goalsForDay.isNotEmpty()) {
                goalsForDay.sumOf { it.targetHours.toDouble() }.toFloat()
            } else {
                12.0f
            }

            // Met threshold: at least 2 completed study sessions OR at least 6 focused hours OR >= 75% of target hours
            val isMet = completedGoals >= 2 || completedHours >= 6.0f || (targetHours > 0f && completedHours >= targetHours * 0.75f)

            evaluatedDays.add(
                DayRecord(
                    dayNumber = d,
                    dateLabel = dateLabel,
                    dayOfWeek = dayOfWeek,
                    completedHours = completedHours,
                    targetHours = targetHours,
                    isGoalMet = isMet,
                    completedGoalsCount = completedGoals,
                    totalGoalsCount = totalGoals
                )
            )
        }

        val todayEval = evaluatedDays.lastOrNull()
        val isTodayGoalMet = todayEval?.isGoalMet == true
        val todayCompletedHours = todayEval?.completedHours ?: 0f
        val todayTargetHours = todayEval?.targetHours ?: 12f
        val todayCompletedSessions = todayEval?.completedGoalsCount ?: 0
        val todayTotalSessions = (todayEval?.totalGoalsCount ?: 4).coerceAtLeast(4)
        val todayProgressPct = if (todayTargetHours > 0f) {
            ((todayCompletedHours / todayTargetHours) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // Current Streak calculation
        var currentStreak = 0
        if (isTodayGoalMet) {
            currentStreak = 1
            for (i in (evaluatedDays.size - 2) downTo 0) {
                if (evaluatedDays[i].isGoalMet) {
                    currentStreak++
                } else {
                    break
                }
            }
        } else {
            // Today in progress, count backwards from yesterday
            for (i in (evaluatedDays.size - 2) downTo 0) {
                if (evaluatedDays[i].isGoalMet) {
                    currentStreak++
                } else {
                    break
                }
            }
        }

        // Best Historical Streak
        var bestStreak = 0
        var tempStreak = 0
        for (day in evaluatedDays) {
            if (day.isGoalMet) {
                tempStreak++
                if (tempStreak > bestStreak) {
                    bestStreak = tempStreak
                }
            } else {
                tempStreak = 0
            }
        }
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        // Milestone Tiers
        val milestones = listOf(
            Triple("Spark", 3, "Ignited regular study routine"),
            Triple("Iron Habit", 7, "One full unbroken week of goals"),
            Triple("Sprint Titan", 14, "Two weeks of relentless discipline"),
            Triple("Unstoppable Master", 30, "One full month of peak exam prep"),
            Triple("Grand Top Ranker", 60, "Flawless sprint completion")
        )

        val currentMilestone = milestones.lastOrNull { currentStreak >= it.second }?.first ?: "Getting Started"
        val nextMilestoneTuple = milestones.firstOrNull { it.second > currentStreak } ?: milestones.last()
        val nextMilestoneName = nextMilestoneTuple.first
        val nextMilestoneDays = nextMilestoneTuple.second
        val daysUntilNext = maxOf(0, nextMilestoneDays - currentStreak)

        val prevMilestoneDays = milestones.lastOrNull { it.second <= currentStreak }?.second ?: 0
        val milestoneRange = nextMilestoneDays - prevMilestoneDays
        val progressInRange = currentStreak - prevMilestoneDays
        val milestoneProgressPct = if (milestoneRange > 0) {
            ((progressInRange.toFloat() / milestoneRange) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

        // Consecutive Days Window (7-8 days centered around today)
        val windowStart = maxOf(1, currentSprintDay - 5)
        val windowEnd = minOf(60, currentSprintDay + 1)
        val indicators = mutableListOf<HabitDayIndicator>()

        for (d in windowStart..windowEnd) {
            val cal = (baseCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, d - 1)
            }
            val dateLabel = dateFormat.format(cal.time)
            val dayOfWeek = dayOfWeekFormat.format(cal.time)
            val isToday = (d == currentSprintDay)
            val isFuture = (d > currentSprintDay)

            val eval = evaluatedDays.find { it.dayNumber == d }
            if (eval != null) {
                indicators.add(
                    HabitDayIndicator(
                        dayNumber = d,
                        dateLabel = dateLabel,
                        dayOfWeek = dayOfWeek,
                        completedHours = eval.completedHours,
                        targetHours = eval.targetHours,
                        isGoalMet = eval.isGoalMet,
                        isToday = isToday,
                        isFuture = false,
                        completedGoalsCount = eval.completedGoalsCount,
                        totalGoalsCount = eval.totalGoalsCount
                    )
                )
            } else {
                indicators.add(
                    HabitDayIndicator(
                        dayNumber = d,
                        dateLabel = dateLabel,
                        dayOfWeek = dayOfWeek,
                        completedHours = 0f,
                        targetHours = 12f,
                        isGoalMet = false,
                        isToday = false,
                        isFuture = true,
                        completedGoalsCount = 0,
                        totalGoalsCount = 4
                    )
                )
            }
        }

        val motivational = when {
            currentStreak >= 7 -> "🔥 UNSTOPPABLE MOMENTUM! $currentStreak days of unbroken focus. You're building exam-winning stamina!"
            currentStreak >= 3 -> "⚡ Outstanding discipline! $currentStreak consecutive days. Just $daysUntilNext days to unlock \"$nextMilestoneName\"!"
            currentStreak == 2 -> "💪 Great start! Two consecutive days locked in. Keep the flame alive today!"
            currentStreak == 1 -> "🌱 Streak started! Complete today's focus slots to reach a 2-day streak."
            else -> "🚀 Today is Day 1 of your new streak! Finish your study goals to ignite the flame."
        }

        val rating = when {
            currentStreak >= 14 -> "Legendary 🔥"
            currentStreak >= 7 -> "Elite ⚡"
            currentStreak >= 3 -> "Consistent 🌟"
            currentStreak >= 1 -> "Active 🌱"
            else -> "Ready to Ignite 🎯"
        }

        return HabitStreakData(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            isTodayGoalMet = isTodayGoalMet,
            todayCompletedHours = todayCompletedHours,
            todayTargetHours = todayTargetHours,
            todayProgressPct = todayProgressPct,
            currentMilestoneName = currentMilestone,
            nextMilestoneName = nextMilestoneName,
            nextMilestoneDays = nextMilestoneDays,
            daysUntilNextMilestone = daysUntilNext,
            milestoneProgressPct = milestoneProgressPct,
            consecutiveDays = indicators,
            motivationalMessage = motivational,
            streakRating = rating,
            todayCompletedSessions = todayCompletedSessions,
            todayTotalSessions = todayTotalSessions
        )
    }

    // Flashcard Module Operations
    fun getCardsForDeck(deckId: Long): kotlinx.coroutines.flow.Flow<List<FlashcardItemEntity>> {
        return flashcardRepository.getCardsForDeck(deckId)
    }

    fun createFlashcardDeck(
        title: String,
        subject: String = "Physics",
        description: String = "",
        colorHex: Long = 0xFF6574FF
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            flashcardRepository.createDeck(title, subject, description, colorHex)
            showSnackbar("Deck \"$title\" created!")
        }
    }

    fun updateFlashcardDeck(deck: FlashcardDeckEntity) {
        viewModelScope.launch {
            flashcardRepository.updateDeck(deck)
            showSnackbar("Deck updated")
        }
    }

    fun deleteFlashcardDeck(deckId: Long, deckTitle: String = "Deck") {
        viewModelScope.launch {
            flashcardRepository.deleteDeck(deckId)
            showSnackbar("$deckTitle deleted")
        }
    }

    fun addFlashcard(deckId: Long, front: String, back: String, tag: String = "Formula") {
        if (front.isBlank() || back.isBlank()) return
        viewModelScope.launch {
            flashcardRepository.addCard(deckId, front, back, tag)
            showSnackbar("Card added to deck")
        }
    }

    fun addFlashcard(
        subject: String,
        chapter: String,
        question: String,
        answer: String,
        difficulty: String = "Formula"
    ) {
        if (question.isBlank() || answer.isBlank()) return
        viewModelScope.launch {
            val existingDeck = flashcardDecks.value.firstOrNull {
                it.deck.subject.contains(subject, ignoreCase = true) ||
                    it.deck.title.contains(subject, ignoreCase = true)
            }?.deck
            val deckId = existingDeck?.id ?: flashcardRepository.createDeck(
                title = "$subject High-Yield Deck",
                subject = subject,
                description = "Auto-generated from $chapter",
                colorHex = 0xFF0070F3
            )
            flashcardRepository.addCard(deckId, "[$chapter] $question", answer, difficulty)
        }
    }

    fun updateFlashcard(card: FlashcardItemEntity) {
        viewModelScope.launch {
            flashcardRepository.updateCard(card)
            showSnackbar("Card updated")
        }
    }

    fun deleteFlashcard(cardId: Long) {
        viewModelScope.launch {
            flashcardRepository.deleteCard(cardId)
            showSnackbar("Card deleted")
        }
    }

    fun toggleCardMastered(cardId: Long, current: Boolean) {
        viewModelScope.launch {
            flashcardRepository.setCardMastered(cardId, !current)
        }
    }

    fun resetDeckProgress(deckId: Long, deckTitle: String = "Deck") {
        viewModelScope.launch {
            flashcardRepository.resetDeckMastery(deckId)
            showSnackbar("Progress reset for $deckTitle")
        }
    }

    // Motivational Quote Operations
    fun fetchNextQuote() {
        viewModelScope.launch {
            _isQuoteRefreshing.value = true
            val currentId = _dailyQuote.value.id
            val newQuote = quoteRepository.getRandomQuote(excludeId = currentId)
            _dailyQuote.value = newQuote
            _isQuoteBookmarked.value = quoteRepository.isQuoteBookmarked(newQuote.id)
            _isQuoteRefreshing.value = false
            showSnackbar("New motivation loaded")
        }
    }

    fun toggleBookmarkQuote() {
        val current = _dailyQuote.value
        val newState = !_isQuoteBookmarked.value
        quoteRepository.setQuoteBookmarked(current.id, newState)
        _isQuoteBookmarked.value = newState
        showSnackbar(if (newState) "Quote saved to bookmarks" else "Quote removed from bookmarks")
    }

    fun copyQuoteToClipboard(context: android.content.Context) {
        val quote = _dailyQuote.value
        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(
            "UpgradeHub Daily Quote",
            "\"${quote.text}\"\n— ${quote.author} (${quote.role})"
        )
        clipboard?.setPrimaryClip(clip)
        showSnackbar("Quote copied to clipboard")
    }

    // Batch Curriculum & Auto-Planner Operations
    fun selectBatch(batchId: String) {
        val found = _batchCurriculums.value.find { it.id == batchId }
            ?: BatchCurriculumRepository.PRESET_BATCHES.find { it.id == batchId }
        if (found != null) {
            val isNeet = found.examType.contains("NEET", ignoreCase = true) ||
                    found.id.contains("neet", ignoreCase = true) ||
                    found.id.contains("yakeen", ignoreCase = true)

            batchRepository.setActiveBatchId(found.id)
            _selectedBatch.value = found
            _batchUrlInput.value = found.sourceUrl
            _batchCompletionStats.value = batchRepository.getBatchCompletionStats(found)

            // Switch active chapters immediately to the batch's specific curriculum
            val batchChapters = batchRepository.getBatchChaptersAsItems(found)
            _activeChapters.value = batchChapters

            // Seed NCERT database with new chapters if missing
            viewModelScope.launch {
                ncertRepository.ensureSeeded(batchChapters)
            }

            // Sync user profile target exam if registered
            currentUser.value?.let { user ->
                val newTargetExam = if (isNeet) "NEET 2026 (Medical)" else "JEE 2026"
                val newTargetScore = if (isNeet) "680/720" else "260/300"
                if (!user.targetExam.contains(if (isNeet) "NEET" else "JEE", ignoreCase = true)) {
                    updateUserProfile(
                        fullName = user.fullName,
                        email = user.email,
                        targetExam = newTargetExam,
                        targetScore = newTargetScore
                    )
                }
            }

            // Reset to Day 1 and clear filters so the student starts cleanly with the new batch
            _uiState.update { it.copy(selectedDay = 1, selectedSubjectFilter = "All", chapterSearchQuery = "") }

            // Automatically execute auto-plan so the daily timetable, focus slots, Room goals, and schedules adapt immediately!
            executeAutoPlan()

            val examBadge = if (isNeet) "NEET Medical (Biology, Physics, Chemistry)" else "JEE Main & Advanced"
            showSnackbar("Switched to ${found.title} — $examBadge active!")
        }
    }

    fun isBatchChapterComplete(chapterName: String): Boolean {
        return batchRepository.isChapterComplete(_selectedBatch.value.id, chapterName)
    }

    fun toggleBatchChapterComplete(chapterName: String) {
        val currentBatch = _selectedBatch.value
        val newStatus = batchRepository.toggleChapterComplete(currentBatch.id, chapterName)
        _batchCompletionStats.value = batchRepository.getBatchCompletionStats(currentBatch)
        _version.update { it + 1 }
        showSnackbar(if (newStatus) "Marked '$chapterName' as Complete" else "Marked '$chapterName' as Incomplete")
    }

    fun refreshBatchCompletionStats() {
        _batchCompletionStats.value = batchRepository.getBatchCompletionStats(_selectedBatch.value)
    }

    // Authentication Operations
    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    studyIntelligenceRepository.setAuthenticatedUserContext(user.email, user.role)
                } else {
                    studyIntelligenceRepository.setAuthenticatedUserContext("guest@upgradehub.local", "USER")
                }
                _version.update { it + 1 }
            }
        }
    }

    fun signInWithGoogle(
        email: String,
        fullName: String,
        targetExam: String = "JEE 2026 (Lakshya Batch)",
        onResult: (Result<UserAccountEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(email, fullName, targetExam)
            onResult(result)
            if (result.isSuccess) {
                val u = result.getOrNull()
                if (u != null) {
                    studyIntelligenceRepository.setAuthenticatedUserContext(u.email, u.role)
                }
                _version.update { it + 1 }
                showSnackbar("Signed in with Google as ${u?.fullName} (${u?.role}) ✓")
            }
        }
    }

    /**
     * Interactive Google Sign-In with Credential Manager and Firebase Authentication.
     */
    fun signInWithGoogleCredential(
        activity: android.app.Activity,
        targetExam: String = "JEE 2026 (Lakshya Batch)",
        onResult: (Result<UserAccountEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleCredential(activity, targetExam)
            onResult(result)
            if (result.isSuccess) {
                val u = result.getOrNull()
                if (u != null) {
                    studyIntelligenceRepository.setAuthenticatedUserContext(u.email, u.role)
                }
                _version.update { it + 1 }
                showSnackbar("Signed in with Google & Firebase as ${u?.fullName} ✓")
                // Automatically synchronize local Room study progress with Cloud Firebase
                syncStudyProgressToCloud()
            } else {
                val ex = result.exceptionOrNull()
                if (ex !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                    showSnackbar("Google Sign-In note: ${ex?.message ?: "Sign-in dismissed"}")
                }
            }
        }
    }

    /**
     * Cross-Device Cloud Synchronization using Firebase Firestore.
     * Synchronizes Room Daily Schedules, Chapter Progress, and Practice Session History.
     */
    fun syncStudyProgressToCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                val user = authRepository.currentUser.value
                val email = user?.email ?: "guest_student"
                val userId = email.replace(".", "_").replace("@", "_")

                val currentDays = _batchTargetDays.value
                val completedChapters = chapterProgressRepository.completedChaptersCount.first()
                val totalPracticeSessions = practiceSessionRepository.allSessions.first().size
                val totalQuestionsAttempted = practiceSessionRepository.totalQuestionsSolved.first()
                val avgAccuracy = practiceSessionRepository.averageAccuracy.first()

                val syncPayload = mapOf(
                    "userId" to userId,
                    "email" to email,
                    "fullName" to (user?.fullName ?: "Student"),
                    "targetExam" to (user?.targetExam ?: "JEE 2026"),
                    "plannerDurationDays" to currentDays,
                    "overallCompletionPct" to _uiState.value.overallCompletionPct,
                    "totalFocusedHours" to _uiState.value.totalFocusedHours,
                    "completedChaptersCount" to completedChapters,
                    "totalPracticeSessions" to totalPracticeSessions,
                    "totalQuestionsAttempted" to totalQuestionsAttempted,
                    "averageAccuracy" to avgAccuracy,
                    "syncedAt" to System.currentTimeMillis()
                )

                val result = firestoreService.syncStudyProgressData(userId, syncPayload)
                if (result.isSuccess) {
                    _lastCloudSyncTimestamp.value = System.currentTimeMillis()
                    showSnackbar("☁️ Study progress & $currentDays-Day Plan synced across devices!")
                    onComplete(true, "Cloud sync successful!")
                } else {
                    showSnackbar("Sync notice: ${result.exceptionOrNull()?.localizedMessage ?: "Saved locally"}")
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Sync error")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Sync error")
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    /**
     * Restores synchronized study progress metadata from Firebase Firestore.
     */
    fun restoreStudyProgressFromCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                val user = authRepository.currentUser.value
                val email = user?.email ?: "guest_student"
                val userId = email.replace(".", "_").replace("@", "_")

                val result = firestoreService.fetchStudyProgressData(userId)
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null) {
                        val cloudDays = (data["plannerDurationDays"] as? Number)?.toInt() ?: 60
                        if (cloudDays in listOf(30, 45, 60, 75, 90, 120, 150)) {
                            setBatchTargetDays(cloudDays)
                        }
                        _lastCloudSyncTimestamp.value = System.currentTimeMillis()
                        showSnackbar("☁️ Restored study data from Cloud Firebase!")
                        onComplete(true, "Data restored successfully!")
                    } else {
                        showSnackbar("No existing cloud backup found. Your local progress is now the primary copy.")
                        syncStudyProgressToCloud()
                        onComplete(true, "Initial sync created")
                    }
                } else {
                    showSnackbar("Error fetching cloud data: ${result.exceptionOrNull()?.localizedMessage}")
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Error")
                }
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Error")
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    /**
     * Logs a completed practice session to Room PracticeSessionHistory.
     */
    fun logCustomPracticeSession(
        sessionTitle: String,
        sessionType: String,
        subject: String,
        chapterId: Int = 0,
        chapterTitle: String = "",
        questionsAttempted: Int,
        questionsCorrect: Int,
        timeSpentMinutes: Int,
        moodRating: String = "CONFIDENT",
        notes: String = ""
    ) {
        viewModelScope.launch {
            practiceSessionRepository.logSession(
                sessionTitle = sessionTitle,
                sessionType = sessionType,
                subject = subject,
                chapterId = chapterId,
                chapterTitle = chapterTitle,
                questionsAttempted = questionsAttempted,
                questionsCorrect = questionsCorrect,
                timeSpentMinutes = timeSpentMinutes,
                plannerDurationDays = _batchTargetDays.value,
                dayNumber = _uiState.value.selectedDay,
                moodRating = moodRating,
                notes = notes
            )
            showSnackbar("Recorded practice session: \"$sessionTitle\" ($questionsCorrect/$questionsAttempted correct)")
        }
    }

    fun deletePracticeSession(id: Long) {
        viewModelScope.launch {
            practiceSessionRepository.deleteSession(id)
            showSnackbar("Practice session entry deleted")
        }
    }

    fun isCurrentUserAdmin(): Boolean = authRepository.isCurrentUserAdmin()

    fun verifyAdminAccess(onResult: (Result<Boolean>) -> Unit = {}) {
        viewModelScope.launch {
            val result = authRepository.refreshAdminAuthorization()
            val authorized = result.getOrDefault(false)
            val user = authRepository.currentUser.value
            studyIntelligenceRepository.setAuthenticatedUserContext(
                email = user?.email ?: "guest@upgradehub.local",
                role = if (authorized) "ADMIN" else "USER"
            )
            if (!authorized) {
                showSnackbar("Admin access could not be verified for this session.")
            }
            onResult(result)
        }
    }

    fun registerUser(
        fullName: String,
        email: String,
        password: String,
        targetExam: String,
        targetScore: String,
        onResult: (Result<UserAccountEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password, targetExam, targetScore)
            onResult(result)
            if (result.isSuccess) {
                showSnackbar("Welcome to Upgrade Hub, ${result.getOrNull()?.fullName}!")
            }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (Result<UserAccountEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            onResult(result)
            if (result.isSuccess) {
                showSnackbar("Welcome back, ${result.getOrNull()?.fullName}!")
            }
        }
    }

    fun logoutUser() {
        authRepository.logout()
        showSnackbar("Logged out successfully")
    }

    fun updateUserProfile(
        fullName: String,
        email: String,
        targetExam: String,
        targetScore: String,
        targetCollege: String = "",
        bio: String = "",
        onResult: (Result<UserAccountEntity>) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.updateProfile(
                fullName = fullName,
                email = email,
                targetExam = targetExam,
                targetScore = targetScore,
                targetCollege = targetCollege,
                bio = bio
            )
            onResult(result)
            if (result.isSuccess) {
                showSnackbar("Profile updated successfully")
            } else {
                result.exceptionOrNull()?.message?.let { showSnackbar(it) }
            }
        }
    }

    fun setBatchTargetDays(days: Int) {
        val clamped = days.coerceIn(30, 150)
        _batchTargetDays.value = clamped
        batchRepository.setActiveTargetDays(clamped)
        executeAutoPlan()
    }

    fun setBatchDailyHours(hours: Float) {
        val clamped = hours.coerceIn(4f, 16f)
        _batchDailyHours.value = clamped
        batchRepository.setActiveDailyHours(clamped)
        executeAutoPlan()
    }

    fun setBatchUrlInput(url: String) {
        _batchUrlInput.value = url
    }

    fun parseAndApplyBatchUrl(url: String) {
        if (url.isBlank()) {
            showSnackbar("Please enter a valid batch link or URL (PW, Unacademy, KGS, etc.)")
            return
        }
        val parsed = batchRepository.parseBatchFromUrl(url)
        val existing = _batchCurriculums.value.filter { it.id != parsed.id }
        _batchCurriculums.value = listOf(parsed) + existing
        _selectedBatch.value = parsed
        _batchUrlInput.value = url
        // Also include in parallel batches if not present
        val updatedParallel = (listOf(parsed.id) + _parallelBatchIds.value).distinct().take(3)
        _parallelBatchIds.value = updatedParallel
        batchRepository.setParallelBatchIds(updatedParallel)
        executeAutoPlan()
        showSnackbar("Synced [${parsed.platform}] ${parsed.title} (${parsed.totalLectures} lectures)!")
    }

    fun executeAutoPlan() {
        viewModelScope.launch {
            _isAutoPlanning.value = true
            try {
                val batch = _selectedBatch.value
                val targetDays = _batchTargetDays.value.coerceIn(30, 150)
                val dailyHours = _batchDailyHours.value
                val activeParallel = parallelBatches.value
                val config = AutoPlanConfiguration(
                    targetDays = targetDays,
                    dailyWorkingHours = dailyHours,
                    selectedBatchId = batch.id,
                    parallelBatchIds = activeParallel.map { it.id }
                )
                val result = batchRepository.generateAndSettleAutoPlan(
                    curriculum = batch,
                    config = config,
                    parallelBatches = activeParallel
                )
                _lastAutoPlanResult.value = result

                // Update active chapters
                val batchChapters = batchRepository.getBatchChaptersAsItems(batch)
                _activeChapters.value = batchChapters

                // Ensure selected day is clamped within 1..targetDays
                val currentDay = _uiState.value.selectedDay.coerceIn(1, targetDays)

                // Update UI state with new target hours, days & workload
                _uiState.update { current ->
                    current.copy(
                        selectedDay = currentDay,
                        todayTargetHours = result.dailyWorkingHours.toInt(),
                        workloadHours = result.totalLectures * 1.5f + (result.totalChapters * 4f),
                        calculatedDays = result.targetDays.toFloat()
                    )
                }

                refreshDailyView()
                refreshStats()
                _version.update { it + 1 }
                showSnackbar("Auto-Plan Ready! ${result.parallelBatchesCount} Parallel Batch(es) across ${result.targetDays} Days")
            } catch (e: Exception) {
                showSnackbar("Error generating plan: ${e.localizedMessage}")
            } finally {
                _isAutoPlanning.value = false
            }
        }
    }

    // --- Practice Stack Queue Operations ---
    fun addToPracticeQueue(
        chapter: ChapterItem,
        priority: String = "HIGH",
        focusArea: String = "PYQs & Question Practice",
        targetMinutes: Int = 90,
        sessionSlot: String = "Next Session",
        notes: String = ""
    ) {
        viewModelScope.launch {
            practiceQueueRepository.addToQueue(
                chapterId = chapter.id,
                chapterTitle = chapter.title,
                subject = chapter.subject,
                priority = priority,
                focusArea = focusArea,
                targetMinutes = targetMinutes,
                sessionSlot = sessionSlot,
                notes = notes
            )
            showSnackbar("Added \"${chapter.title}\" to Practice Stack")
        }
    }

    fun togglePracticeQueueItem(id: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            practiceQueueRepository.toggleCompleted(id, currentCompleted)
            showSnackbar(if (!currentCompleted) "Chapter session marked completed!" else "Moved back to active queue")
        }
    }

    fun removeFromPracticeQueue(id: Long) {
        viewModelScope.launch {
            practiceQueueRepository.removeFromQueue(id)
            showSnackbar("Removed from Practice Stack")
        }
    }

    fun clearCompletedPracticeQueue() {
        viewModelScope.launch {
            practiceQueueRepository.clearCompleted()
            showSnackbar("Cleared completed practice chapters")
        }
    }

    fun movePracticeQueueItemUp(id: Long) {
        viewModelScope.launch {
            practiceQueueRepository.moveUp(id)
        }
    }

    fun movePracticeQueueItemDown(id: Long) {
        viewModelScope.launch {
            practiceQueueRepository.moveDown(id)
        }
    }

    fun prioritizePracticeQueueTop(id: Long) {
        viewModelScope.launch {
            practiceQueueRepository.prioritizeTop(id)
            showSnackbar("Prioritized as #1 for Next Session!")
        }
    }

    fun getPendingChaptersForQueue(): List<ChapterItem> {
        val all = _activeChapters.value
        val incomplete = all.filter { !repository.getChapterChain(it.id).isComplete() }
        return if (incomplete.isNotEmpty()) incomplete else all
    }

    // =========================================================================
    // BACKLOG RECOVERY & MISSED SLOT AUTO-RESCHEDULER ENGINE
    // =========================================================================
    private val _lastRescheduledSummary = MutableStateFlow<String?>(null)
    val lastRescheduledSummary: StateFlow<String?> = _lastRescheduledSummary.asStateFlow()

    fun getMissedSlotsCountForDay(day: Int): Int {
        val dayIdx = (day - 1).coerceAtLeast(0)
        var missedSlots = 0
        for (slotIdx in 0..3) {
            val p = repository.getSlotProgress(dayIdx, slotIdx)
            val doneCount = listOf(p.lecture, p.dpp, p.pyq, p.ncert, p.revision).count { it }
            if (doneCount < 2) {
                missedSlots++
            }
        }
        return missedSlots
    }

    fun rescheduleMissedSlotsToUpcomingDays(fromDay: Int, spreadDays: Int = 3) {
        viewModelScope.launch {
            val totalDays = _batchTargetDays.value.coerceIn(30, 150)
            val clampedSpread = spreadDays.coerceIn(2, 7)
            val missedSlotsCount = getMissedSlotsCountForDay(fromDay).coerceAtLeast(1)
            val slots = _uiState.value.todayCommandSlots
            val dayIdx = (fromDay - 1).coerceAtLeast(0)

            // Identify which slots on `fromDay` were incomplete
            val incompleteSlotTitles = slots.filter { slot ->
                val p = repository.getSlotProgress(dayIdx, slot.slotIndex)
                listOf(p.lecture, p.dpp, p.pyq, p.ncert, p.revision).count { it } < 2
            }.map { it.title }.ifEmpty {
                listOf("Physics Lecture & DPP Catch-Up", "Chemistry NCERT + PYQ Buffer")
            }

            // Distribute catch-up buffer notes into the upcoming days' audit/notesPlan records
            for (offset in 1..clampedSpread) {
                val targetDay = (fromDay + offset).coerceAtMost(totalDays)
                val targetDayIdx = targetDay - 1
                val assignedTopic = incompleteSlotTitles[(offset - 1) % incompleteSlotTitles.size]
                val existingPlan = repository.getDayRecord(targetDayIdx, "notesPlan", "")
                val catchUpNote = "[Catch-Up D$fromDay: $assignedTopic (+45m)]"
                val mergedPlan = if (existingPlan.contains(catchUpNote)) {
                    existingPlan
                } else if (existingPlan.isBlank()) {
                    catchUpNote
                } else {
                    "$existingPlan • $catchUpNote"
                }
                repository.setDayRecord(targetDayIdx, "notesPlan", mergedPlan)
            }

            // Also add a dedicated Backlog Catch-Up Goal in DailyGoalRepository so it's immediately actionable
            val primaryCatchUpTitle = "Backlog Catch-Up (D$fromDay): ${incompleteSlotTitles.first().take(34)}"
            dailyGoalRepository.addCustomGoal(
                day = fromDay,
                title = primaryCatchUpTitle,
                category = "Revision",
                targetHours = 1.5f,
                priority = "HIGH"
            )

            val summaryText = "Rescheduled $missedSlotsCount missed slot(s) from Day $fromDay across Day ${(fromDay + 1).coerceAtMost(totalDays)}–Day ${(fromDay + clampedSpread).coerceAtMost(totalDays)} (+45m/day buffer)"
            _lastRescheduledSummary.value = summaryText
            _version.update { it + 1 }
            refreshDailyView()
            showSnackbar("✅ $summaryText")
        }
    }

    // =========================================================================
    // MISTAKE NOTEBOOK / ERROR LOG (FOR CBT MOCK TESTS & PYQs)
    // =========================================================================
    private val _mistakeNotebookEntries = MutableStateFlow(
        listOf(
            MistakeNotebookEntry(
                id = "err_1",
                questionTitle = "Gauss Law electric flux through hemispherical shell with off-center dipole",
                subject = "Physics",
                chapterOrSource = "Electrostatics • JEE Main 2025 Jan Shift 1",
                errorType = "Concept Gap",
                takeawayNote = "Net flux depends ONLY on enclosed charge q_in / ε₀ = 0 for a complete dipole inside closed surface.",
                isResolved = false,
                createdAt = "Today"
            ),
            MistakeNotebookEntry(
                id = "err_2",
                questionTitle = "Order of Hydration Enthalpy & Ionic Mobility of Alkali Metal Ions in Aqueous Solution",
                subject = "Chemistry",
                chapterOrSource = "Periodic Table & s-Block • NTA Mock #2",
                errorType = "Silly Mistake",
                takeawayNote = "Marked gas-phase radius instead of hydrated radius: Li+(aq) is largest, so lowest ionic mobility in water.",
                isResolved = false,
                createdAt = "Yesterday"
            ),
            MistakeNotebookEntry(
                id = "err_3",
                questionTitle = "Definite Integral using King's Property ∫₀^(π/2) ln(sin x) dx = -π/2 ln 2",
                subject = "Maths",
                chapterOrSource = "Definite Integration • PYQ Sprint",
                errorType = "Formula Forgotten",
                takeawayNote = "Memorize standard result ∫₀^(π/2) ln(sin x) dx = ∫₀^(π/2) ln(cos x) dx = -(π/2) ln 2 to save 3 minutes.",
                isResolved = true,
                createdAt = "2d ago"
            )
        )
    )
    val mistakeNotebookEntries: StateFlow<List<MistakeNotebookEntry>> = _mistakeNotebookEntries.asStateFlow()

    fun addMistakeEntry(
        questionTitle: String,
        subject: String,
        chapterOrSource: String,
        errorType: String,
        takeawayNote: String
    ) {
        if (questionTitle.isBlank()) return
        val newEntry = MistakeNotebookEntry(
            id = "err_${System.currentTimeMillis()}",
            questionTitle = questionTitle.trim(),
            subject = subject.ifBlank { "Physics" },
            chapterOrSource = chapterOrSource.ifBlank { "CBT Mock / PYQ" },
            errorType = errorType.ifBlank { "Concept Gap" },
            takeawayNote = takeawayNote.ifBlank { "Review step-by-step derivation before next mock test." },
            isResolved = false,
            createdAt = "Just now"
        )
        _mistakeNotebookEntries.update { current ->
            if (current.any { it.questionTitle.equals(newEntry.questionTitle, ignoreCase = true) }) {
                current.map {
                    if (it.questionTitle.equals(newEntry.questionTitle, ignoreCase = true)) {
                        it.copy(errorType = errorType, takeawayNote = takeawayNote.ifBlank { it.takeawayNote }, isResolved = false)
                    } else it
                }
            } else {
                listOf(newEntry) + current
            }
        }
        showSnackbar("📓 Saved to Mistake Notebook [$errorType]!")
    }

    fun toggleMistakeResolved(id: String) {
        _mistakeNotebookEntries.update { list ->
            list.map { entry ->
                if (entry.id == id) entry.copy(isResolved = !entry.isResolved) else entry
            }
        }
    }

    fun deleteMistakeEntry(id: String) {
        _mistakeNotebookEntries.update { list ->
            list.filterNot { it.id == id }
        }
        showSnackbar("Removed entry from Mistake Notebook")
    }

    // =========================================================================
    // TEAM ANNOUNCEMENTS & IMPORTANT BROADCASTS ENGINE
    // =========================================================================
    private val _teamAnnouncements = MutableStateFlow(
        listOf(
            TeamAnnouncement(
                id = "ann_1",
                title = "🚨 NTA JEE Main 2026 Full-Syllabus CBT Mock #3 Live Now",
                message = "Sample announcement. Mock-test publishing and leaderboard rewards are not connected in this preview.",
                category = "URGENT",
                authorName = "Upgrade Exam Arena Desk",
                authorRole = "Official Broadcast",
                timestamp = "Today, 9:30 AM",
                isPinned = true,
                isImportant = true,
                isRead = false,
                actionLabel = "Open CBT Arena",
                actionTargetPage = JeePage.TESTS,
                acknowledgementCount = 246,
                isAcknowledgedByMe = false
            ),
            TeamAnnouncement(
                id = "ann_2",
                title = "📢 Parallel Batch Auto-Planner & Missed Slot Rescheduler Active",
                message = "Following student feedback in Demand Centre, the Curriculum Team has enabled 1-tap Missed Slot Auto-Rescheduling (+45m/day catch-up buffer) and Multi-Batch Sync for PW Lakshya, Prayas, Unacademy & KGS batches.",
                category = "BATCH UPDATE",
                authorName = "Upgrade Curriculum Team",
                authorRole = "Core Engineering",
                timestamp = "Yesterday",
                isPinned = true,
                isImportant = true,
                isRead = false,
                actionLabel = "Open Daily Planner",
                actionTargetPage = JeePage.DAILY_PLANNER,
                acknowledgementCount = 189,
                isAcknowledgedByMe = true
            ),
            TeamAnnouncement(
                id = "ann_3",
                title = "Leaderboard preview notice",
                message = "The sample leaderboard is not live. No cash reward campaign is active, and UpgradeHub will not request UPI details here.",
                category = "REWARD & CONTEST",
                authorName = "Mentor & Community Board",
                authorRole = "Student Welfare",
                timestamp = "2 days ago",
                isPinned = false,
                isImportant = false,
                isRead = true,
                actionLabel = "View Leaderboard",
                actionTargetPage = JeePage.LEADERBOARD,
                acknowledgementCount = 312,
                isAcknowledgedByMe = true
            ),
            TeamAnnouncement(
                id = "ann_4",
                title = "📘 Prep Hub Vault: 2025 Jan & April Shift PYQ PDFs Added",
                message = "High-yield chapter-wise formula sheets and 2025 shift-wise solved papers with step-by-step derivations are now available in the Prep Hub Vault.",
                category = "EXAM ALERT",
                authorName = "Academic Content Team",
                authorRole = "Senior Faculty",
                timestamp = "3 days ago",
                isPinned = false,
                isImportant = false,
                isRead = true,
                actionLabel = "Open Prep Vault",
                actionTargetPage = JeePage.BOOKMARKS,
                acknowledgementCount = 405,
                isAcknowledgedByMe = false
            )
        )
    )
    val teamAnnouncements: StateFlow<List<TeamAnnouncement>> = _teamAnnouncements.asStateFlow()

    fun postTeamAnnouncement(
        title: String,
        message: String,
        category: String = "URGENT",
        authorName: String = "Upgrade Academic Team",
        authorRole: String = "Official Broadcast",
        isPinned: Boolean = true,
        isImportant: Boolean = true,
        actionLabel: String? = null,
        actionTargetPage: JeePage? = null
    ) {
        if (title.isBlank() || message.isBlank()) {
            showSnackbar("Please enter both an announcement title and message")
            return
        }
        val newAnnouncement = TeamAnnouncement(
            id = "ann_${System.currentTimeMillis()}",
            title = title.trim(),
            message = message.trim(),
            category = category.ifBlank { "URGENT" },
            authorName = authorName.ifBlank { "Upgrade Academic Team" },
            authorRole = authorRole.ifBlank { "Official Broadcast" },
            timestamp = "Just now",
            isPinned = isPinned,
            isImportant = isImportant,
            isRead = false,
            actionLabel = actionLabel,
            actionTargetPage = actionTargetPage,
            acknowledgementCount = 1,
            isAcknowledgedByMe = true
        )
        _teamAnnouncements.update { current ->
            val updated = listOf(newAnnouncement) + current
            updated.sortedByDescending { it.isPinned }
        }
        showSnackbar("📢 Team Announcement broadcasted: \"${title.take(36)}\"")
    }

    fun togglePinAnnouncement(id: String) {
        _teamAnnouncements.update { current ->
            current.map { ann ->
                if (ann.id == id) ann.copy(isPinned = !ann.isPinned) else ann
            }.sortedByDescending { it.isPinned }
        }
    }

    fun acknowledgeAnnouncement(id: String) {
        _teamAnnouncements.update { current ->
            current.map { ann ->
                if (ann.id == id) {
                    val toggled = !ann.isAcknowledgedByMe
                    val newCount = if (toggled) ann.acknowledgementCount + 1 else (ann.acknowledgementCount - 1).coerceAtLeast(0)
                    ann.copy(
                        isAcknowledgedByMe = toggled,
                        acknowledgementCount = newCount,
                        isRead = true
                    )
                } else ann
            }
        }
    }

    fun markAllAnnouncementsRead() {
        _teamAnnouncements.update { current ->
            current.map { it.copy(isRead = true) }
        }
        showSnackbar("All team announcements marked as read")
    }

    fun deleteAnnouncement(id: String) {
        _teamAnnouncements.update { current ->
            current.filterNot { it.id == id }
        }
        showSnackbar("Announcement removed from board")
    }

    // =========================================================================
    // ADMIN PORTAL & ROLE GOVERNANCE ENGINE (STUDENT DATA, UPLOADS, BROADCASTS)
    // =========================================================================
    private val _adminPermissions = MutableStateFlow(AdminPermissions())
    val adminPermissions: StateFlow<AdminPermissions> = _adminPermissions.asStateFlow()

    private val _adminAuditLogs = MutableStateFlow(
        listOf(
            AdminAuditLog(
                id = "log_1",
                adminName = "Prince Kumar (Super Admin)",
                action = "ROLE_GOVERNANCE",
                details = "RBAC Policy initialized: Full Faculty & Super Admin privileges active",
                timestamp = "Today, 10:15 AM"
            ),
            AdminAuditLog(
                id = "log_2",
                adminName = "Upgrade Exam Arena Desk",
                action = "POST_ANNOUNCEMENT",
                details = "Broadcasted CBT Full-Syllabus Mock #3 alert to All Batches",
                timestamp = "Today, 09:30 AM"
            ),
            AdminAuditLog(
                id = "log_3",
                adminName = "Academic Content Team",
                action = "UPLOAD_MATERIAL",
                details = "Uploaded 'Electrostatics Advanced DPP #5' (2.4 MB)",
                timestamp = "Yesterday, 04:20 PM"
            ),
            AdminAuditLog(
                id = "log_4",
                adminName = "Curriculum Director",
                action = "EXPORT_DATA",
                details = "Generated Cohort Diagnostic Audit for Lakshya JEE 2026 Batch",
                timestamp = "2 days ago"
            )
        )
    )
    val adminAuditLogs: StateFlow<List<AdminAuditLog>> = _adminAuditLogs.asStateFlow()

    private val _uploadedMaterials = MutableStateFlow(
        listOf(
            AdminStudyMaterial(
                id = "mat_1",
                title = "Electrostatics & Capacitance Advanced DPP #5",
                description = "High-difficulty multi-concept problems on dielectric breakdown, conductor grounding, and field energy density.",
                subject = "Physics",
                materialType = "DPP Sheet",
                targetBatch = "All Batches",
                uploaderName = "Er. Prince Kumar",
                uploaderRole = "Senior Physics Faculty",
                fileSizeMb = 2.4f,
                tags = listOf("JEE Advanced", "Class 12", "Electrostatics", "High Yield"),
                uploadedAt = "Today, 10:00 AM",
                downloadCount = 142
            ),
            AdminStudyMaterial(
                id = "mat_2",
                title = "Coordination Compounds Complete Handwritten Revision Notes",
                description = "Crystal Field Splitting Diagrams, IUPAC naming cheat-sheet, Isomerism flowcharts, and 2018-2025 PYQ shortcuts.",
                subject = "Chemistry",
                materialType = "Handwritten Notes",
                targetBatch = "Lakshya JEE 2026 (11th)",
                uploaderName = "Dr. Shashi Shekhar",
                uploaderRole = "Inorganic HOD",
                fileSizeMb = 5.8f,
                tags = listOf("Inorganic", "Coordination", "NCERT Line-by-Line", "Notes"),
                uploadedAt = "Yesterday, 3:15 PM",
                downloadCount = 389
            ),
            AdminStudyMaterial(
                id = "mat_3",
                title = "Definite Integrals & Reduction Formulae 100 PYQ Master Bank",
                description = "Properties of Definite Integrals (King's Rule, Queen's Rule, Leibniz Rule) + 100 categorized solved questions with step derivations.",
                subject = "Mathematics",
                materialType = "Mock Question Paper",
                targetBatch = "Arjuna JEE 2025 (12th)",
                uploaderName = "Prof. Arvind Mathur",
                uploaderRole = "Maths Wizard",
                fileSizeMb = 4.2f,
                tags = listOf("Calculus", "Definite Integration", "PYQ", "100 Problems"),
                uploadedAt = "2 days ago",
                downloadCount = 512
            ),
            AdminStudyMaterial(
                id = "mat_4",
                title = "JEE Main 2026 Expected 300-Mark Full Syllabus Mock Paper 1",
                description = "Exact NTA CBT pattern test: 20 MCQs + 10 Numericals per subject with official negative marking scheme & answer keys.",
                subject = "Multi-Subject",
                materialType = "Mock Question Paper",
                targetBatch = "All Batches",
                uploaderName = "Upgrade Exam Cell",
                uploaderRole = "Admin Desk",
                fileSizeMb = 3.6f,
                tags = listOf("Full Mock", "300 Marks", "CBT Pattern", "NTA 2026"),
                uploadedAt = "3 days ago",
                downloadCount = 780
            ),
            AdminStudyMaterial(
                id = "mat_5",
                title = "Thermodynamics & Heat Transfer Crash Formula Handout",
                description = "Carnot efficiency formulas, Maxwell relations, indicator diagram shortcuts, and heat conduction boundary laws.",
                subject = "Physics",
                materialType = "Formula Handout",
                targetBatch = "Prayas Droppers",
                uploaderName = "Er. Prince Kumar",
                uploaderRole = "Senior Physics Faculty",
                fileSizeMb = 1.9f,
                tags = listOf("Physics", "Thermodynamics", "Formula Sheet"),
                uploadedAt = "4 days ago",
                downloadCount = 265
            ),
            AdminStudyMaterial(
                id = "mat_6",
                title = "Organic Reaction Mechanisms & Named Reagents NCERT Cheat Sheet",
                description = "Reimer-Tiemann, Kolbe, Aldol, Cannizzaro, Sandmeyer, and Gatterman reactions summarized in two high-density printable pages.",
                subject = "Chemistry",
                materialType = "NCERT Revision Sheet",
                targetBatch = "All Batches",
                uploaderName = "Academic Content Team",
                uploaderRole = "Faculty Lead",
                fileSizeMb = 2.1f,
                tags = listOf("Organic", "Reagents", "NCERT", "High Yield"),
                uploadedAt = "5 days ago",
                downloadCount = 630
            )
        )
    )
    val uploadedMaterials: StateFlow<List<AdminStudyMaterial>> = _uploadedMaterials.asStateFlow()

    private val _studentCohortList = MutableStateFlow(
        listOf(
            StudentCohortProfile(
                id = "std_101",
                name = "Aarav Sharma",
                email = "aarav.sharma.jee@gmail.com",
                batch = "Lakshya JEE 2026 (11th)",
                targetExam = "JEE 2026",
                targetCollege = "IIT Bombay (CSE)",
                studyHours = 94.5f,
                streakDays = 45,
                syllabusCompletionPct = 88,
                problemsSolved = 1420,
                mockTestAvgScore = 268,
                accuracyPct = 91,
                slotCompliancePct = 95,
                performanceStatus = "TOP_PERFORMER",
                avatarColor = 0xFF00C853,
                joinedDate = "01 Aug 2026",
                contactNumber = "+91 91234 56789",
                physicsPct = 92,
                chemistryPct = 94,
                mathsPct = 78
            ),
            StudentCohortProfile(
                id = "std_102",
                name = "Rohan Verma",
                email = "rohan.verma.iit@gmail.com",
                batch = "Arjuna JEE 2025 (12th)",
                targetExam = "JEE 2025",
                targetCollege = "IIT Delhi (MnC)",
                studyHours = 112.0f,
                streakDays = 52,
                syllabusCompletionPct = 94,
                problemsSolved = 1860,
                mockTestAvgScore = 282,
                accuracyPct = 94,
                slotCompliancePct = 98,
                performanceStatus = "TOP_PERFORMER",
                avatarColor = 0xFF6574FF,
                joinedDate = "15 Jul 2026",
                contactNumber = "+91 98221 44332",
                physicsPct = 96,
                chemistryPct = 91,
                mathsPct = 95
            ),
            StudentCohortProfile(
                id = "std_103",
                name = "Ananya Singh",
                email = "ananya.singh.study@gmail.com",
                batch = "Lakshya JEE 2026 (11th)",
                targetExam = "JEE 2026",
                targetCollege = "IIT Madras (Electrical)",
                studyHours = 86.0f,
                streakDays = 38,
                syllabusCompletionPct = 82,
                problemsSolved = 1240,
                mockTestAvgScore = 255,
                accuracyPct = 89,
                slotCompliancePct = 90,
                performanceStatus = "ON_TRACK",
                avatarColor = 0xFFE91E63,
                joinedDate = "10 Aug 2026",
                contactNumber = "+91 97112 33445",
                physicsPct = 84,
                chemistryPct = 88,
                mathsPct = 74
            ),
            StudentCohortProfile(
                id = "std_104",
                name = "Ayaan Sheikh",
                email = "ayaan.sheikh.dropper@gmail.com",
                batch = "Prayas Droppers",
                targetExam = "JEE 2025 (Dropper)",
                targetCollege = "IIT Kharagpur (Aerospace)",
                studyHours = 124.5f,
                streakDays = 49,
                syllabusCompletionPct = 91,
                problemsSolved = 2100,
                mockTestAvgScore = 274,
                accuracyPct = 92,
                slotCompliancePct = 94,
                performanceStatus = "TOP_PERFORMER",
                avatarColor = 0xFFFF9800,
                joinedDate = "01 Jun 2026",
                contactNumber = "+91 96554 11223",
                physicsPct = 94,
                chemistryPct = 89,
                mathsPct = 90
            ),
            StudentCohortProfile(
                id = "std_105",
                name = "Ishaan Patel",
                email = "ishaan.patel.jee@gmail.com",
                batch = "Lakshya JEE 2026 (11th)",
                targetExam = "JEE 2026",
                targetCollege = "IIT Roorkee",
                studyHours = 58.0f,
                streakDays = 14,
                syllabusCompletionPct = 65,
                problemsSolved = 780,
                mockTestAvgScore = 210,
                accuracyPct = 78,
                slotCompliancePct = 72,
                performanceStatus = "ON_TRACK",
                avatarColor = 0xFF00BCD4,
                joinedDate = "20 Aug 2026",
                contactNumber = "+91 95443 22110",
                physicsPct = 70,
                chemistryPct = 68,
                mathsPct = 57
            ),
            StudentCohortProfile(
                id = "std_106",
                name = "Sneha Gupta",
                email = "sneha.gupta.aspirant@gmail.com",
                batch = "Arjuna JEE 2025 (12th)",
                targetExam = "JEE 2025",
                targetCollege = "IIT BHU (Varanasi)",
                studyHours = 74.0f,
                streakDays = 29,
                syllabusCompletionPct = 76,
                problemsSolved = 1050,
                mockTestAvgScore = 238,
                accuracyPct = 85,
                slotCompliancePct = 86,
                performanceStatus = "ON_TRACK",
                avatarColor = 0xFF9C27B0,
                joinedDate = "05 Jul 2026",
                contactNumber = "+91 94332 11998",
                physicsPct = 78,
                chemistryPct = 82,
                mathsPct = 68
            ),
            StudentCohortProfile(
                id = "std_107",
                name = "Vikram Malhotra",
                email = "vikram.malhotra.prayas@gmail.com",
                batch = "Prayas Droppers",
                targetExam = "JEE 2025 (Dropper)",
                targetCollege = "NIT Trichy / IIT Guwahati",
                studyHours = 44.0f,
                streakDays = 7,
                syllabusCompletionPct = 49,
                problemsSolved = 520,
                mockTestAvgScore = 185,
                accuracyPct = 70,
                slotCompliancePct = 58,
                performanceStatus = "NEEDS_ATTENTION",
                avatarColor = 0xFFF44336,
                joinedDate = "15 Jun 2026",
                contactNumber = "+91 93221 88776",
                physicsPct = 52,
                chemistryPct = 55,
                mathsPct = 40
            ),
            StudentCohortProfile(
                id = "std_108",
                name = "Diya Sengupta",
                email = "diya.sengupta.prep@gmail.com",
                batch = "Lakshya JEE 2026 (11th)",
                targetExam = "JEE 2026",
                targetCollege = "IIT Hyderabad",
                studyHours = 70.5f,
                streakDays = 24,
                syllabusCompletionPct = 72,
                problemsSolved = 940,
                mockTestAvgScore = 226,
                accuracyPct = 83,
                slotCompliancePct = 84,
                performanceStatus = "ON_TRACK",
                avatarColor = 0xFF009688,
                joinedDate = "12 Aug 2026",
                contactNumber = "+91 92110 77665",
                physicsPct = 75,
                chemistryPct = 76,
                mathsPct = 65
            ),
            StudentCohortProfile(
                id = "std_109",
                name = "Kabir Mehta",
                email = "kabir.mehta.study@gmail.com",
                batch = "Arjuna JEE 2025 (12th)",
                targetExam = "JEE 2025",
                targetCollege = "BITS Pilani / NIT Surathkal",
                studyHours = 36.0f,
                streakDays = 4,
                syllabusCompletionPct = 41,
                problemsSolved = 410,
                mockTestAvgScore = 165,
                accuracyPct = 66,
                slotCompliancePct = 50,
                performanceStatus = "NEEDS_ATTENTION",
                avatarColor = 0xFFFF5722,
                joinedDate = "22 Jul 2026",
                contactNumber = "+91 91009 66554",
                physicsPct = 45,
                chemistryPct = 48,
                mathsPct = 30
            ),
            StudentCohortProfile(
                id = "std_110",
                name = "Tanvi Deshmukh",
                email = "tanvi.deshmukh.jee@gmail.com",
                batch = "Lakshya JEE 2026 (11th)",
                targetExam = "JEE 2026",
                targetCollege = "IIT Bombay (Electrical)",
                studyHours = 101.0f,
                streakDays = 42,
                syllabusCompletionPct = 87,
                problemsSolved = 1580,
                mockTestAvgScore = 264,
                accuracyPct = 90,
                slotCompliancePct = 93,
                performanceStatus = "TOP_PERFORMER",
                avatarColor = 0xFF3F51B5,
                joinedDate = "02 Aug 2026",
                contactNumber = "+91 90887 55443",
                physicsPct = 88,
                chemistryPct = 92,
                mathsPct = 81
            ),
            StudentCohortProfile(
                id = "std_111",
                name = "Aditya Joshi",
                email = "aditya.joshi.prayas@gmail.com",
                batch = "Prayas Droppers",
                targetExam = "JEE 2025 (Dropper)",
                targetCollege = "IIT Kanpur (Mechanical)",
                studyHours = 91.0f,
                streakDays = 36,
                syllabusCompletionPct = 81,
                problemsSolved = 1350,
                mockTestAvgScore = 249,
                accuracyPct = 88,
                slotCompliancePct = 88,
                performanceStatus = "ON_TRACK",
                avatarColor = 0xFF607D8B,
                joinedDate = "05 Jun 2026",
                contactNumber = "+91 98776 44332",
                physicsPct = 86,
                chemistryPct = 80,
                mathsPct = 77
            ),
            StudentCohortProfile(
                id = "std_112",
                name = "Riya Choudhary",
                email = "riya.choudhary.jee@gmail.com",
                batch = "Lakshya JEE 2026 (11th)",
                targetExam = "JEE 2026",
                targetCollege = "IIT Indore",
                studyHours = 22.0f,
                streakDays = 2,
                syllabusCompletionPct = 25,
                problemsSolved = 240,
                mockTestAvgScore = 145,
                accuracyPct = 62,
                slotCompliancePct = 40,
                performanceStatus = "NEW_ASPIRANT",
                avatarColor = 0xFF8BC34A,
                joinedDate = "28 Aug 2026",
                contactNumber = "+91 97665 33221",
                physicsPct = 30,
                chemistryPct = 35,
                mathsPct = 10
            )
        )
    )
    val studentCohortList: StateFlow<List<StudentCohortProfile>> = _studentCohortList.asStateFlow()

    fun uploadStudyMaterial(
        title: String,
        description: String,
        subject: String,
        materialType: String,
        targetBatch: String,
        fileSizeMb: Float = 2.5f,
        tags: List<String> = emptyList()
    ): Boolean {
        if (!authRepository.isCurrentUserAdmin()) {
            showSnackbar("Admin access required.")
            return false
        }
        if (!_adminPermissions.value.canUploadMaterial) {
            showSnackbar("Permission Denied: Material uploading is restricted by Admin Governance")
            return false
        }
        if (title.isBlank()) {
            showSnackbar("Please enter a title for the study material")
            return false
        }
        val adminUser = currentUser.value
        val uploader = adminUser?.fullName ?: "Prince Kumar"
        val role = adminUser?.role ?: "ADMIN"

        val newMaterial = AdminStudyMaterial(
            id = "mat_${System.currentTimeMillis()}",
            title = title.trim(),
            description = description.trim().ifBlank { "Official verified academic study material uploaded by faculty." },
            subject = subject.ifBlank { "Multi-Subject" },
            materialType = materialType.ifBlank { "DPP Sheet" },
            targetBatch = targetBatch.ifBlank { "All Batches" },
            uploaderName = uploader,
            uploaderRole = if (role == "ADMIN") "Senior Faculty / Admin" else "Faculty",
            fileSizeMb = fileSizeMb.coerceAtLeast(0.5f),
            tags = tags.ifEmpty { listOf(subject, materialType, "JEE 2026") },
            uploadedAt = "Just now",
            downloadCount = 0
        )

        _uploadedMaterials.update { listOf(newMaterial) + it }

        // Record Audit Log
        val newLog = AdminAuditLog(
            id = "log_${System.currentTimeMillis()}",
            adminName = "$uploader ($role)",
            action = "UPLOAD_MATERIAL",
            details = "Uploaded '${title.take(32)}' for $targetBatch ($subject • $materialType)",
            timestamp = "Just now"
        )
        _adminAuditLogs.update { listOf(newLog) + it }

        showSnackbar("📁 Material Published! Students can now access '$title'")
        return true
    }

    fun deleteStudyMaterial(id: String) {
        if (!authRepository.isCurrentUserAdmin()) {
            showSnackbar("Admin access required.")
            return
        }
        if (!_adminPermissions.value.canDeleteContent) {
            showSnackbar("Permission Denied: Deletions are restricted by Admin Governance")
            return
        }
        val target = _uploadedMaterials.value.firstOrNull { it.id == id }
        _uploadedMaterials.update { current -> current.filterNot { it.id == id } }

        val adminUser = currentUser.value
        val newLog = AdminAuditLog(
            id = "log_${System.currentTimeMillis()}",
            adminName = "${adminUser?.fullName ?: "Admin"} (${adminUser?.role ?: "ADMIN"})",
            action = "DELETE_MATERIAL",
            details = "Removed material: '${target?.title?.take(32) ?: id}'",
            timestamp = "Just now"
        )
        _adminAuditLogs.update { listOf(newLog) + it }
        showSnackbar("Study material removed from repository")
    }

    fun incrementMaterialDownload(id: String) {
        _uploadedMaterials.update { list ->
            list.map { if (it.id == id) it.copy(downloadCount = it.downloadCount + 1) else it }
        }
    }

    fun postAdminBroadcastAnnouncement(
        title: String,
        message: String,
        category: String = "URGENT",
        isPinned: Boolean = true,
        sendImmediatePush: Boolean = true,
        targetBatch: String = "All Batches"
    ) {
        if (!authRepository.isCurrentUserAdmin()) {
            showSnackbar("Admin access required.")
            return
        }
        if (!_adminPermissions.value.canBroadcastAnnouncements) {
            showSnackbar("Permission Denied: Announcement broadcast is restricted by Admin Governance")
            return
        }
        val adminUser = currentUser.value
        val author = adminUser?.fullName ?: "Admin Portal"

        postTeamAnnouncement(
            title = title,
            message = "$message\n[Target: $targetBatch]",
            category = category,
            authorName = author,
            authorRole = "Verified Admin Broadcast",
            isPinned = isPinned,
            isImportant = true
        )

        // Also record in audit log
        val newLog = AdminAuditLog(
            id = "log_${System.currentTimeMillis()}",
            adminName = author,
            action = "POST_ANNOUNCEMENT",
            details = "Broadcasted: '$title' to $targetBatch (Category: $category)",
            timestamp = "Just now"
        )
        _adminAuditLogs.update { listOf(newLog) + it }

        if (sendImmediatePush) {
            // Trigger heads-up emergency notification in notification center
            try {
                com.example.ui.components.triggerStudySlotReminderNotification(
                    context = getApplication(),
                    slotTitle = "📢 ADMIN BROADCAST: ${title.take(30)}",
                    slotBody = message.take(120),
                    notificationId = 3001
                )
            } catch (_: Exception) {
            }
        }
    }

    fun updateAdminPermissions(newPermissions: AdminPermissions) {
        if (!authRepository.isCurrentUserAdmin()) {
            showSnackbar("Admin access required.")
            return
        }
        if (!_adminPermissions.value.canManageRoles) {
            showSnackbar("Permission Denied: Master admin role required to adjust permissions")
            return
        }
        _adminPermissions.value = newPermissions

        val adminUser = currentUser.value
        val newLog = AdminAuditLog(
            id = "log_${System.currentTimeMillis()}",
            adminName = "${adminUser?.fullName ?: "Admin"} (${adminUser?.role ?: "ADMIN"})",
            action = "UPDATE_PERMISSIONS",
            details = "Admin governance permissions updated (Upload: ${newPermissions.canUploadMaterial}, Broadcast: ${newPermissions.canBroadcastAnnouncements})",
            timestamp = "Just now"
        )
        _adminAuditLogs.update { listOf(newLog) + it }
        showSnackbar("🛡️ Admin governance permissions updated successfully")
    }

    fun revokeAdminAccess() {
        authRepository.revokeAdminAuthorization()
        showSnackbar("Admin session locked.")
        setPage(JeePage.DASHBOARD)
    }

    fun exportCohortReportSummary(): String {
        if (!authRepository.isCurrentUserAdmin()) {
            return "Admin access required."
        }
        val cohort = _studentCohortList.value
        val total = cohort.size
        val avgHours = cohort.map { it.studyHours }.average()
        val avgStreak = cohort.map { it.streakDays }.average()
        val avgScore = cohort.map { it.mockTestAvgScore }.average()
        val topPerformers = cohort.count { it.performanceStatus == "TOP_PERFORMER" }

        val summary = buildString {
            appendLine("=== UPGRADE HUB: COHORT DIAGNOSTIC REPORT ===")
            appendLine("Generated At: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Calendar.getInstance().time)}")
            appendLine("Total Enrolled Aspirants: $total")
            appendLine("Top Performers (>260 avg): $topPerformers")
            appendLine("Cohort Average Focused Hours: ${"%.1f".format(avgHours)} hrs")
            appendLine("Cohort Average Daily Streak: ${"%.1f".format(avgStreak)} days")
            appendLine("Cohort Average CBT Mock Score: ${"%.0f".format(avgScore)} / 300")
            appendLine("--------------------------------------------------")
            appendLine("STUDENT ROSTER:")
            cohort.forEachIndexed { i, s ->
                appendLine("${i + 1}. ${s.name} (${s.email}) | ${s.batch} | Score: ${s.mockTestAvgScore} | Streak: ${s.streakDays}d | Hours: ${s.studyHours}h | Status: ${s.performanceStatus}")
            }
            appendLine("==================================================")
        }

        val adminUser = currentUser.value
        val newLog = AdminAuditLog(
            id = "log_${System.currentTimeMillis()}",
            adminName = "${adminUser?.fullName ?: "Admin"}",
            action = "EXPORT_DATA",
            details = "Exported cohort diagnostic roster ($total students)",
            timestamp = "Just now"
        )
        _adminAuditLogs.update { listOf(newLog) + it }
        showSnackbar("📊 Cohort summary report ready for export")
        return summary
    }

    // =========================================================================
    // Persistent Background Focus Timer, Streak Freeze & Ambient Tone
    // =========================================================================
    private val appPrefs by lazy {
        getApplication<Application>().getSharedPreferences("upgrade_hub_extended_prefs", android.content.Context.MODE_PRIVATE)
    }

    private val _focusPresetMinutes = MutableStateFlow(45)
    val focusPresetMinutes: StateFlow<Int> = _focusPresetMinutes.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(45 * 60)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _isFocusTimerRunning = MutableStateFlow(false)
    val isFocusTimerRunning: StateFlow<Boolean> = _isFocusTimerRunning.asStateFlow()

    private val _freezePassesAvailable = MutableStateFlow(appPrefs.getInt("freeze_passes_left", 2))
    val freezePassesAvailable: StateFlow<Int> = _freezePassesAvailable.asStateFlow()

    private val _isTodayFreezeActive = MutableStateFlow(appPrefs.getBoolean("today_freeze_active", false))
    val isTodayFreezeActive: StateFlow<Boolean> = _isTodayFreezeActive.asStateFlow()

    private val _isAmbientFocusToneEnabled = MutableStateFlow(false)
    val isAmbientFocusToneEnabled: StateFlow<Boolean> = _isAmbientFocusToneEnabled.asStateFlow()

    private var focusTimerJob: kotlinx.coroutines.Job? = null

    fun selectFocusPresetMinutes(mins: Int) {
        focusTimerJob?.cancel()
        _isFocusTimerRunning.value = false
        _focusPresetMinutes.value = mins
        _focusRemainingSeconds.value = mins * 60
    }

    fun toggleFocusTimer() {
        val nextState = !_isFocusTimerRunning.value
        _isFocusTimerRunning.value = nextState
        if (nextState) {
            focusTimerJob?.cancel()
            focusTimerJob = viewModelScope.launch {
                while (_isFocusTimerRunning.value && _focusRemainingSeconds.value > 0) {
                    kotlinx.coroutines.delay(1000L)
                    _focusRemainingSeconds.update { (it - 1).coerceAtLeast(0) }
                }
                if (_focusRemainingSeconds.value == 0 && _isFocusTimerRunning.value) {
                    _isFocusTimerRunning.value = false
                    val earnedHours = (_focusPresetMinutes.value / 60f).coerceAtLeast(0.5f)
                    completeNextStudySessionToday()
                    boostUserScore(points = 35, hours = earnedHours, problems = 8)
                    _focusRemainingSeconds.value = _focusPresetMinutes.value * 60
                    showSnackbar("🎯 ${_focusPresetMinutes.value}m focus block completed. This timer did not credit server XP.")
                }
            }
        } else {
            focusTimerJob?.cancel()
        }
    }

    fun resetFocusTimer() {
        focusTimerJob?.cancel()
        _isFocusTimerRunning.value = false
        _focusRemainingSeconds.value = _focusPresetMinutes.value * 60
    }

    fun completeAndLogFocusBlockNow() {
        focusTimerJob?.cancel()
        _isFocusTimerRunning.value = false
        val earnedHours = (_focusPresetMinutes.value / 60f).coerceAtLeast(0.5f)
        completeNextStudySessionToday()
        boostUserScore(points = 25, hours = earnedHours, problems = 5)
        _focusRemainingSeconds.value = _focusPresetMinutes.value * 60
    }

    fun equipStreakFreezePass() {
        if (!_isTodayFreezeActive.value && _freezePassesAvailable.value > 0) {
            val nextCount = _freezePassesAvailable.value - 1
            _freezePassesAvailable.value = nextCount
            _isTodayFreezeActive.value = true
            appPrefs.edit()
                .putInt("freeze_passes_left", nextCount)
                .putBoolean("today_freeze_active", true)
                .apply()
            showSnackbar("❄️ Streak Freeze Shield activated for today! ($nextCount left)")
        }
    }

    fun toggleAmbientFocusTone() {
        val next = !_isAmbientFocusToneEnabled.value
        _isAmbientFocusToneEnabled.value = next
        showSnackbar(
            if (next) "🎧 Binaural 40Hz Gamma Focus Ambience enabled" else "🔇 Focus Ambience muted"
        )
    }

    // =========================================================================
    // CBT Mock Test Attempt History Tracker
    // =========================================================================
    private val _cbtAttemptHistory = MutableStateFlow(
        listOf(
            CbtAttemptRecord(
                id = "att_seed_1",
                testTitle = "All-India Major Mock Test #1 (NTA Pattern)",
                examCategory = "JEE Main",
                score = 212,
                maxMarks = 300,
                accuracyPct = 86,
                timeTakenLabel = "148m",
                timestampLabel = "2 days ago • 99.1%ile Est."
            ),
            CbtAttemptRecord(
                id = "att_seed_2",
                testTitle = "NEET UG High-Yield NCERT Mock (PCB)",
                examCategory = "NEET (PCB)",
                score = 640,
                maxMarks = 720,
                accuracyPct = 91,
                timeTakenLabel = "162m",
                timestampLabel = "5 days ago • AIR ~3,200 Est."
            )
        )
    )
    val cbtAttemptHistory: StateFlow<List<CbtAttemptRecord>> = _cbtAttemptHistory.asStateFlow()

    fun recordCbtAttempt(
        testTitle: String,
        examCategory: String,
        score: Int,
        maxMarks: Int,
        accuracyPct: Int,
        timeTakenLabel: String
    ) {
        val newRec = CbtAttemptRecord(
            id = "att_${System.currentTimeMillis()}",
            testTitle = testTitle,
            examCategory = examCategory,
            score = score,
            maxMarks = maxMarks,
            accuracyPct = accuracyPct,
            timeTakenLabel = timeTakenLabel,
            timestampLabel = "Just now"
        )
        _cbtAttemptHistory.update { listOf(newRec) + it }
    }

    // =========================================================================
    // Prep Hub Vault Persistent Unlocks & Bookmarks
    // =========================================================================
    private val _unlockedVaultNoteIds = MutableStateFlow(
        appPrefs.getStringSet("unlocked_vault_notes", setOf("note_phy_01", "note_chem_01", "note_math_01"))?.toSet()
            ?: setOf("note_phy_01", "note_chem_01", "note_math_01")
    )
    val unlockedVaultNoteIds: StateFlow<Set<String>> = _unlockedVaultNoteIds.asStateFlow()

    private val _bookmarkedVaultIds = MutableStateFlow(
        appPrefs.getStringSet("bookmarked_vault_items", setOf("note_phy_01", "pyq_hub_01"))?.toSet()
            ?: setOf("note_phy_01", "pyq_hub_01")
    )
    val bookmarkedVaultIds: StateFlow<Set<String>> = _bookmarkedVaultIds.asStateFlow()

    fun unlockVaultNote(noteId: String) {
        val updated = _unlockedVaultNoteIds.value + noteId
        _unlockedVaultNoteIds.value = updated
        appPrefs.edit().putStringSet("unlocked_vault_notes", updated).apply()
    }

    fun toggleVaultBookmark(itemId: String) {
        val curr = _bookmarkedVaultIds.value
        val updated = if (itemId in curr) curr - itemId else curr + itemId
        _bookmarkedVaultIds.value = updated
        appPrefs.edit().putStringSet("bookmarked_vault_items", updated).apply()
        showSnackbar(if (itemId in updated) "Saved to Prep Hub Bookmarks" else "Removed from Bookmarks")
    }

    // =========================================================================
    // Memes Hub (Reddit Energy Originals, Prep Excuses, Daily Problems, Formula Memes & Infinite Scroll)
    // =========================================================================
    private val _infiniteMemesPage = MutableStateFlow(1)
    val infiniteMemesPage: StateFlow<Int> = _infiniteMemesPage.asStateFlow()

    private val _memesHubPosts = MutableStateFlow(
        listOf(
            // 01 — Execution Strategy
            MemeHubItem(
                id = "rv_orig_01",
                authorName = "@systematic_planner",
                authorBadge = "EXECUTION STRATEGY",
                timeAgo = "2 min ago",
                category = "Study Strategy 🎯",
                examTag = "TIME AUDIT",
                templateStyle = "ILLUSTRATED_ORIGINAL",
                topSetupText = "Timetable Perfectionism Trap",
                bottomPunchlineText = "Redesigning study schedules repeatedly yields zero percentile gains. Lock one chapter in the tracker and solve 25 PYQs today.",
                emojiBanner = "📅 🎯 📈",
                conceptTakeaway = "Execution Protocol: Commit to a strict 90-minute focused solving block before adjusting calendar settings.",
                subjectForFlashcard = "Physics",
                chapterForFlashcard = "Revision Strategy",
                likesCount = 2700,
                laughsCount = 184,
                isLiked = false,
                isBookmarked = true,
                comments = listOf(
                    "Aarav: Sticking to one daily slot routine boosted my accuracy from 62% to 84%.",
                    "Sneha: Eliminating planner perfectionism freed up 2 extra study hours daily.",
                    "Rohan: Direct PYQ execution builds real confidence over passive scheduling."
                ),
                handle = "@systematic_planner",
                avatarColorHex = 0xFF0066EE,
                badgeNumber = "01",
                illustrationKey = "FLOW_STATE",
                postType = "PREP_EXCUSE",
                usBhaiUsCount = 1420
            ),
            // 02 — Circadian Alignment
            MemeHubItem(
                id = "rv_orig_02",
                authorName = "@circadian_topper",
                authorBadge = "EXAM PHYSIOLOGY",
                timeAgo = "3:07 AM",
                category = "Exam Psychology 🧠",
                examTag = "SLEEP ALIGNMENT",
                templateStyle = "ILLUSTRATED_ORIGINAL",
                topSetupText = "Midnight Alertness vs Exam Shift Timing",
                bottomPunchlineText = "Studying till 3 AM impairs cognitive recall during morning 9:00 AM – 12:00 PM NTA exam shifts.",
                emojiBanner = "🌙 💡 ⏰",
                conceptTakeaway = "Peak Alertness Rule: Synchronize your deep focus blocks with your actual CBT test shift hours so your brain operates at peak velocity.",
                subjectForFlashcard = "Chemistry",
                chapterForFlashcard = "Organic Chemistry",
                likesCount = 4100,
                laughsCount = 263,
                isLiked = true,
                isBookmarked = false,
                comments = listOf(
                    "Karan: Transitioning my sleep schedule to wake up at 6:30 AM removed morning exam brain fog.",
                    "Ishita: Reaction mechanisms need active working memory, which peaks with consistent sleep."
                ),
                handle = "@circadian_topper",
                avatarColorHex = 0xFFCA8A04,
                badgeNumber = "02",
                illustrationKey = "LAMP_3AM",
                postType = "PREP_EXCUSE",
                usBhaiUsCount = 2890
            ),
            // 03 — Active Problem Solving
            MemeHubItem(
                id = "rv_orig_03",
                authorName = "@active_recall_lab",
                authorBadge = "CONCEPT MASTERY",
                timeAgo = "today",
                category = "Concept Insights 💡",
                examTag = "ACTIVE RETENTION",
                templateStyle = "ILLUSTRATED_ORIGINAL",
                topSetupText = "Passive Video Lectures vs Pen-and-Paper Solving",
                bottomPunchlineText = "Watching an 8-hour marathon lecture passively gives the illusion of competence; solving 30 PYQs independently solidifies it.",
                emojiBanner = "📚 ✍️ ⚡",
                conceptTakeaway = "Active Recall Principle: Pause instructional videos after every solved example and compute the final numerical step without assistance.",
                subjectForFlashcard = "Mathematics",
                chapterForFlashcard = "Calculus & Algebra Backlog",
                likesCount = 3300,
                laughsCount = 209,
                isLiked = false,
                isBookmarked = false,
                comments = listOf(
                    "Devansh: Solving 25 PYQs on paper taught me more than watching 4 separate comparison lectures.",
                    "Ananya: Writing step-by-step derivations is the only way formulas stay in long-term memory."
                ),
                handle = "@active_recall_lab",
                avatarColorHex = 0xFF0F766E,
                badgeNumber = "03",
                illustrationKey = "BOOK_PILE",
                postType = "DAILY_PROBLEM",
                usBhaiUsCount = 1950
            ),
            // 04 — Structured Breaks
            MemeHubItem(
                id = "rv_orig_04",
                authorName = "@focus_architect",
                authorBadge = "FOCUS DISCIPLINE",
                timeAgo = "1 hr ago",
                category = "Focus Studio ⏱️",
                examTag = "ATTENTION AUDIT",
                templateStyle = "ILLUSTRATED_ORIGINAL",
                topSetupText = "Structured Rest vs Digital Scrolling",
                bottomPunchlineText = "A scheduled 5-minute break with digital screen scrolling fragments concentration; opt for hydration, stretching, and quiet reflection.",
                emojiBanner = "⏱️ 🧘 🎯",
                conceptTakeaway = "Cognitive Reset Protocol: Keep your study desk free of unrelated mobile notifications during active problem-solving intervals.",
                subjectForFlashcard = "Physics",
                chapterForFlashcard = "Applied Mathematics & Quantum",
                likesCount = 5200,
                laughsCount = 418,
                isLiked = true,
                isBookmarked = true,
                comments = listOf(
                    "Vikram: 5-minute physical walk between sprint sets completely refreshed my focus.",
                    "Meera: Putting phone on airplane mode doubled my question-solving throughput."
                ),
                handle = "@focus_architect",
                avatarColorHex = 0xFF6D28D9,
                badgeNumber = "04",
                illustrationKey = "NAP_BOOKS",
                postType = "PREP_EXCUSE",
                usBhaiUsCount = 3640
            ),
            // 05 — Formula Boundary Conditions
            MemeHubItem(
                id = "rv_orig_05",
                authorName = "@formula_precision",
                authorBadge = "PHYSICS RIGOR",
                timeAgo = "during mock",
                category = "Formula Derivation 🧮",
                examTag = "BOUNDARY RULES",
                templateStyle = "ILLUSTRATED_ORIGINAL",
                topSetupText = "Formula Memorization vs Dimensional Applicability",
                bottomPunchlineText = "Memorizing formulas is insufficient without knowing their boundary limits: verify inertial reference frames and conservation criteria.",
                emojiBanner = "🧮 🔬 📐",
                conceptTakeaway = "Physics Rigor: Always cross-check limiting boundary values (m → 0 or v → c) to verify your derived expression before substitution.",
                subjectForFlashcard = "Physics",
                chapterForFlashcard = "Mechanics & Modern Physics",
                likesCount = 2900,
                laughsCount = 176,
                isLiked = false,
                isBookmarked = false,
                comments = listOf(
                    "Harsh: Writing down the valid domain for each formula saved me in Rotation questions.",
                    "Priya: Moment of Inertia parallel axis theorem only applies from the center of mass axis!"
                ),
                handle = "@formula_precision",
                avatarColorHex = 0xFF1E293B,
                badgeNumber = "05",
                illustrationKey = "FORMULA_BOARD",
                postType = "DAILY_PROBLEM",
                usBhaiUsCount = 2110
            ),
            // 06 — Negative Marking Control
            MemeHubItem(
                id = "rv_orig_06",
                authorName = "@scorecard_air",
                authorBadge = "SCORE AUDIT",
                timeAgo = "result day",
                category = "CBT Analytics 📈",
                examTag = "NEGATIVE CONTROL",
                templateStyle = "ILLUSTRATED_ORIGINAL",
                topSetupText = "Percentile Leap via Negative Marking Control",
                bottomPunchlineText = "A 20-mark leap in CBT mock scores occurs primarily by eliminating 5 preventable negative-marking blunders in the Mistake Vault.",
                emojiBanner = "📈 🎯 🏆",
                conceptTakeaway = "Accuracy Over Speed: Converting 5 unforced guesses into deliberate skips or verified calculations yields an instant net +25 score shift.",
                subjectForFlashcard = "Mathematics",
                chapterForFlashcard = "Mock Test Temperament",
                likesCount = 6800,
                laughsCount = 521,
                isLiked = true,
                isBookmarked = true,
                comments = listOf(
                    "Saurabh: Eliminating 4 blind guesses pushed my score from 145 to 165 instantly.",
                    "Rohan: Strict negative marking discipline was the single biggest factor in crossing 99%ile."
                ),
                handle = "@scorecard_air",
                avatarColorHex = 0xFF0284C7,
                badgeNumber = "06",
                illustrationKey = "TROPHY_WIN",
                postType = "ORIGINAL_FEED",
                usBhaiUsCount = 4300
            ),
            // 07 — NCERT Foundation
            MemeHubItem(
                id = "rv_orig_07",
                authorName = "@topper_methodology",
                authorBadge = "NCERT MASTERY",
                timeAgo = "just now",
                category = "NCERT Mastery 📖",
                examTag = "CHEMISTRY REVISION",
                templateStyle = "QUOTE_ORIGINAL",
                topSetupText = "Comprehensive Foundation Before Complex Problem Sets",
                bottomPunchlineText = "Top percentiles prioritize thorough line-by-line NCERT mastery and past 5-year PYQs before attempting advanced experimental mock sheets.",
                emojiBanner = "📖 🔬 🏅",
                conceptTakeaway = "Chemistry Foundation: In Inorganic and Physical chemistry, 85%+ of shift questions stem directly from NCERT tables and exemplar problems.",
                subjectForFlashcard = "Chemistry",
                chapterForFlashcard = "NCERT + PYQ Mastery",
                likesCount = 7400,
                laughsCount = 603,
                isLiked = true,
                isBookmarked = true,
                comments = listOf(
                    "Aditya: Every line in P-block and Coordination compounds holds potential +4 marks.",
                    "Neha: Organizing my Mistake Notebook by NCERT page numbers gave massive retention gains."
                ),
                handle = "@topper_methodology",
                avatarColorHex = 0xFFD97706,
                quoteCanvasText = "True mastery begins with flawless NCERT line accuracy, systematic PYQ drills, and an up-to-date Mistake Vault.",
                quoteCanvasTone = "CREAM",
                postType = "PREP_EXCUSE",
                usBhaiUsCount = 5120
            ),
            // 08 — @paper_review_expert (Screenshot 5 Card 1 — Rose Polka-Dot Quote Card)
            // 08 — Forensic Test Review
            MemeHubItem(
                id = "rv_orig_08",
                authorName = "@mock_diagnostic_lead",
                authorBadge = "ERROR AUDIT",
                timeAgo = "after mock",
                category = "Mistake Analysis 🔍",
                examTag = "POST-TEST PROTOCOL",
                templateStyle = "QUOTE_ORIGINAL",
                topSetupText = "Post-Mock Exam Review Protocol",
                bottomPunchlineText = "Never close a mock exam scorecard without immediately cataloging time-pressure lapses and calculation slips into your Mistake Notebook.",
                emojiBanner = "🔍 📊 📝",
                conceptTakeaway = "Diagnostic Discipline: Allocate 1.5 hours of forensic review for every 3-hour CBT mock completed.",
                subjectForFlashcard = "Physics",
                chapterForFlashcard = "CBT Mock Analysis",
                likesCount = 5900,
                laughsCount = 447,
                isLiked = false,
                isBookmarked = true,
                comments = listOf(
                    "Prabhat: Writing down why I made each mistake reduced repeat errors to near zero.",
                    "Kunal: Identifying time-pressure traps prevented careless errors on test day."
                ),
                handle = "@mock_diagnostic_lead",
                avatarColorHex = 0xFFE11D48,
                quoteCanvasText = "A test without thorough post-exam error analysis is lost preparation time. Analyze every wrong option.",
                quoteCanvasTone = "ROSE",
                postType = "PREP_EXCUSE",
                usBhaiUsCount = 4490
            ),
            // 09 — Graded Difficulty
            MemeHubItem(
                id = "rv_orig_09",
                authorName = "@pyq_mastery",
                authorBadge = "PROGRESSION",
                timeAgo = "Sunday",
                category = "Stepwise Practice 🪜",
                examTag = "GRADED DIFFICULTY",
                templateStyle = "QUOTE_ORIGINAL",
                topSetupText = "Graded Problem Progression",
                bottomPunchlineText = "Bridge the difficulty gap between textbook theory and JEE/NEET questions by advancing stepwise: Foundation → Standard PYQs → Advanced Multi-Concept.",
                emojiBanner = "🪜 💡 📐",
                conceptTakeaway = "Pedagogical Ladder: Ensure core concept fluency before tackling hybrid problems spanning multiple syllabus chapters.",
                subjectForFlashcard = "Mathematics",
                chapterForFlashcard = "PYQ Problem Solving",
                likesCount = 4600,
                laughsCount = 318,
                isLiked = false,
                isBookmarked = false,
                comments = listOf(
                    "Yash: Solving 10 easy questions first gave me the momentum to solve 5 tough multi-concept ones.",
                    "Tanvi: Stepwise progression prevented burnout in 3D Geometry and Vectors."
                ),
                handle = "@pyq_mastery",
                avatarColorHex = 0xFF0D9488,
                quoteCanvasText = "Master single-concept illustrations thoroughly before advancing to multi-concept hybrid numerical problems.",
                quoteCanvasTone = "MINT",
                postType = "DAILY_PROBLEM",
                usBhaiUsCount = 3180
            ),
            // 10 — High-Yield Mnemonics: Inorganic Exceptions
            MemeHubItem(
                id = "meme_01",
                authorName = "@inorganic_precision",
                authorBadge = "PERIODICITY",
                timeAgo = "18m ago",
                category = "High-Yield Mnemonics 🧪",
                examTag = "INORGANIC TRENDS",
                templateStyle = "DRAKE_COMPARE",
                topSetupText = "Standard Group Periodic Trends vs Inner-Shell Shielding Exceptions",
                bottomPunchlineText = "Lanthanoid contraction and poor shielding by 3d/4f electrons invert standard size and ionization energy sequences.",
                emojiBanner = "🧪 ⚡ 📊",
                conceptTakeaway = "High-Yield Fact (+4 Marks): Group 13 IE order is B > Tl > Ga > Al > In due to relativistic contraction and poor nuclear shielding.",
                subjectForFlashcard = "Chemistry",
                chapterForFlashcard = "P-Block & Periodicity",
                likesCount = 3420,
                laughsCount = 298,
                isLiked = false,
                isBookmarked = true,
                comments = listOf(
                    "Dev: This exact Group 13 Ionization Energy trend appeared in 2024 Shift 2!",
                    "Rhea: Excellent concise reminder for d- and f-block screening effects."
                ),
                handle = "@inorganic_precision",
                avatarColorHex = 0xFF7C3AED,
                quoteCanvasText = "Group 13 IE Order: B > Tl > Ga > Al > In — Poor d/f shielding dictates periodic exceptions.",
                quoteCanvasTone = "SKY",
                postType = "FORMULA_MEME",
                usBhaiUsCount = 1890
            ),
            // 11 — Thermodynamics Sign Trap
            MemeHubItem(
                id = "meme_03",
                authorName = "@thermo_rigor",
                authorBadge = "THERMODYNAMICS",
                timeAgo = "35m ago",
                category = "Formula Derivation 🧮",
                examTag = "SIGN CONVENTIONS",
                templateStyle = "DRAKE_COMPARE",
                topSetupText = "First Law of Thermodynamics Sign Conventions",
                bottomPunchlineText = "Physics defines ΔQ = ΔU + W (work done BY system), whereas IUPAC Chemistry defines ΔU = q + w (work done ON system).",
                emojiBanner = "⚖️ 📐 🔬",
                conceptTakeaway = "Examination Guard: One confused sign convention converts a +4 mark problem into a -1 negative score deduction.",
                subjectForFlashcard = "Physics",
                chapterForFlashcard = "Thermodynamics",
                likesCount = 4210,
                laughsCount = 312,
                isLiked = true,
                isBookmarked = true,
                comments = listOf(
                    "Arjun: I always write 'W_chem = - P ΔV' at the top of my scratch sheet before starting Chemistry.",
                    "Sonal: Remembering this distinction saved me 5 marks in the Thermodynamics section."
                ),
                handle = "@thermo_rigor",
                avatarColorHex = 0xFFEA580C,
                quoteCanvasText = "Physics: W = ∫P dV (Work BY system). Chemistry: w = -∫P_ext dV (Work ON system). Verify sign conventions first.",
                quoteCanvasTone = "CREAM",
                postType = "FORMULA_MEME",
                usBhaiUsCount = 2740
            )
        )
    )
    val memesHubPosts: StateFlow<List<MemeHubItem>> = _memesHubPosts.asStateFlow()

    // Pool of relatable Concept Insights, Exam Strategies & High-Yield Mnemonics for Infinite Scroll
    private val infiniteMemesTemplatePool = listOf(
        MemeHubItem(
            id = "inf_pool_1",
            authorName = "@active_solving_pro",
            authorBadge = "PRACTICE METHOD",
            timeAgo = "12m ago",
            category = "Study Strategy 🎯",
            examTag = "SELF DRILL",
            templateStyle = "QUOTE_ORIGINAL",
            topSetupText = "Instructor Demonstration vs Independent Solving",
            bottomPunchlineText = "Understanding an instructor's solution on screen is step zero; reproducing the solution with a pen on paper is true retention.",
            emojiBanner = "✍️ 💡 📈",
            conceptTakeaway = "Method: Pause instructional sessions before the final algebraic step and complete the calculation independently.",
            subjectForFlashcard = "Physics",
            chapterForFlashcard = "Active Problem Solving",
            likesCount = 3850,
            laughsCount = 244,
            comments = listOf("Sameer: Deriving each step on my own immediately clarified force balancing in Newton's Laws."),
            handle = "@active_solving_pro",
            avatarColorHex = 0xFF0284C7,
            quoteCanvasText = "Watching solutions builds familiarity. Independent derivation builds examination competence.",
            quoteCanvasTone = "SKY",
            postType = "DAILY_PROBLEM",
            usBhaiUsCount = 2940
        ),
        MemeHubItem(
            id = "inf_pool_2",
            authorName = "@notes_efficiency",
            authorBadge = "REVISION EFFICIENCY",
            timeAgo = "25m ago",
            category = "Revision Strategy 📝",
            examTag = "HIGH YIELD",
            templateStyle = "QUOTE_ORIGINAL",
            topSetupText = "Elaborate Aesthetics vs High-Yield Short Notes",
            bottomPunchlineText = "Multi-colored aesthetic notes consume precious problem-solving hours; focus on formula boundaries and error triggers.",
            emojiBanner = "📝 ⚡ 🎯",
            conceptTakeaway = "Short Notes Rule: Keep formula revision sheets condensed to a single page per chapter for 10-minute reviews.",
            subjectForFlashcard = "Chemistry",
            chapterForFlashcard = "Short Notes Strategy",
            likesCount = 4920,
            laughsCount = 389,
            comments = listOf("Anjali: Single-sheet formula summaries helped me revise all of Physical Chemistry before mocks."),
            handle = "@notes_efficiency",
            avatarColorHex = 0xFFE11D48,
            quoteCanvasText = "A single-page formula sheet actively revised 10 times beats a 60-page notebook read once.",
            quoteCanvasTone = "ROSE",
            postType = "PREP_EXCUSE",
            usBhaiUsCount = 3710
        ),
        MemeHubItem(
            id = "inf_pool_3",
            authorName = "@organic_mechanisms",
            authorBadge = "ORGANIC CHEMISTRY",
            timeAgo = "41m ago",
            category = "Organic Reaction Rules 🧪",
            examTag = "ALDEHYDES & KETONES",
            templateStyle = "ILLUSTRATED_ORIGINAL",
            topSetupText = "Aldol Condensation vs Cannizzaro Disproportionation",
            bottomPunchlineText = "Aldol requires α-hydrogens with dilute base; Cannizzaro requires substrates lacking α-hydrogens under concentrated alkali.",
            emojiBanner = "🧪 🔬 ⚡",
            conceptTakeaway = "Organic Retention: Aldol = α-H present (enolate intermediate). Cannizzaro = No α-H (hydride transfer redox).",
            subjectForFlashcard = "Chemistry",
            chapterForFlashcard = "Aldehydes & Ketones",
            likesCount = 5410,
            laughsCount = 412,
            comments = listOf("Rishabh: Key distinction: Formaldehyde and Benzaldehyde undergo Cannizzaro because they lack α-hydrogens."),
            handle = "@organic_mechanisms",
            avatarColorHex = 0xFF0D9488,
            badgeNumber = "10",
            illustrationKey = "FORMULA_BOARD",
            postType = "DAILY_PROBLEM",
            usBhaiUsCount = 4080
        ),
        MemeHubItem(
            id = "inf_pool_4",
            authorName = "@deep_work_focus",
            authorBadge = "FOCUS DISCIPLINE",
            timeAgo = "1h ago",
            category = "Focus Studio ⏱️",
            examTag = "UNBROKEN FOCUS",
            templateStyle = "ILLUSTRATED_ORIGINAL",
            topSetupText = "Cognitive Depth vs Fragmented Study Sessions",
            bottomPunchlineText = "A 3-hour uninterrupted focus block produces significantly deeper neural encoding than four fragmented 45-minute sessions.",
            emojiBanner = "⏱️ 🎯 🧠",
            conceptTakeaway = "CBT Training: Practice solving continuously for 180 minutes without checking non-academic notifications.",
            subjectForFlashcard = "Physics",
            chapterForFlashcard = "Deep Focus Discipline",
            likesCount = 6150,
            laughsCount = 530,
            comments = listOf("Aryan: Simulating full 3-hour mock test blocks completely eliminated test-day mental exhaustion."),
            handle = "@deep_work_focus",
            avatarColorHex = 0xFF6D28D9,
            badgeNumber = "11",
            illustrationKey = "NAP_BOOKS",
            postType = "PREP_EXCUSE",
            usBhaiUsCount = 4890
        ),
        MemeHubItem(
            id = "inf_pool_5",
            authorName = "@precision_audit",
            authorBadge = "ERROR ANALYSIS",
            timeAgo = "2h ago",
            category = "CBT Error Prevention 🔍",
            examTag = "QUESTION SCRUTINY",
            templateStyle = "QUOTE_ORIGINAL",
            topSetupText = "Negative Stem Verification ('Which of the following is INCORRECT')",
            bottomPunchlineText = "Under exam pressure, students frequently select the first correct statement they read when the prompt asked for the INCORRECT one.",
            emojiBanner = "🔍 ⚠️ 🎯",
            conceptTakeaway = "Verification Habit: Circle or underline the qualifying word ('INCORRECT', 'EXCEPT', 'NOT TRUE') before scanning options.",
            subjectForFlashcard = "Mathematics",
            chapterForFlashcard = "Definite Integration",
            likesCount = 5780,
            laughsCount = 465,
            comments = listOf("Kartik: Reading question stems twice stopped me from losing 5 marks on statement questions."),
            handle = "@precision_audit",
            avatarColorHex = 0xFFD97706,
            quoteCanvasText = "Underline qualifying negative keywords in questions before reading options to eliminate preventable -1 losses.",
            quoteCanvasTone = "CREAM",
            postType = "DAILY_PROBLEM",
            usBhaiUsCount = 4520
        ),
        MemeHubItem(
            id = "inf_pool_6",
            authorName = "@frictionless_start",
            authorBadge = "HABIT DISCIPLINE",
            timeAgo = "3h ago",
            category = "Time Management ⌛",
            examTag = "IMMEDIATE ACTION",
            templateStyle = "ILLUSTRATED_ORIGINAL",
            topSetupText = "Round Clock Syndrome vs Immediate Action",
            bottomPunchlineText = "Waiting for the next round hour or half-hour wastes valuable cognitive stamina. Begin the target problem set immediately.",
            emojiBanner = "⌛ 🚀 📈",
            conceptTakeaway = "Immediate Execution: Launch the timer right away without deferring to round clock intervals.",
            subjectForFlashcard = "Physics",
            chapterForFlashcard = "Time Management",
            likesCount = 7120,
            laughsCount = 615,
            comments = listOf("Nikhil: Starting immediately as soon as I sit down saved me at least an hour every day."),
            handle = "@frictionless_start",
            avatarColorHex = 0xFF0066EE,
            badgeNumber = "12",
            illustrationKey = "FLOW_STATE",
            postType = "PREP_EXCUSE",
            usBhaiUsCount = 5940
        ),
        MemeHubItem(
            id = "inf_pool_7",
            authorName = "@assertion_reason_guide",
            authorBadge = "LOGICAL DEDUCTION",
            timeAgo = "4h ago",
            category = "Assertion-Reason Mastery 📚",
            examTag = "NCERT LOGIC",
            templateStyle = "QUOTE_ORIGINAL",
            topSetupText = "Assertion-Reason Causal Verification Method",
            bottomPunchlineText = "Read: '[Assertion] BECAUSE [Reason]'. If the connecting clause establishes a valid causal mechanism, select Option (A).",
            emojiBanner = "📚 🔬 💡",
            conceptTakeaway = "Causal Test: Both statements may be factually correct, but Option (A) requires the Reason to provide the scientific cause.",
            subjectForFlashcard = "Biology",
            chapterForFlashcard = "NCERT Assertion-Reason",
            likesCount = 4480,
            laughsCount = 340,
            comments = listOf("Dr. Ishita: The 'BECAUSE' connector test works reliably in NEET & JEE Main biology/chemistry questions!"),
            handle = "@assertion_reason_guide",
            avatarColorHex = 0xFF0D9488,
            quoteCanvasText = "Evaluate Assertion + 'BECAUSE' + Reason. Scientific causation confirms Option (A).",
            quoteCanvasTone = "MINT",
            postType = "FORMULA_MEME",
            usBhaiUsCount = 3390
        ),
        MemeHubItem(
            id = "inf_pool_8",
            authorName = "@prioritized_revision",
            authorBadge = "PREPARATION PRIORITY",
            timeAgo = "5h ago",
            category = "Strategic Prioritization 📊",
            examTag = "HIGH IMPACT",
            templateStyle = "ILLUSTRATED_ORIGINAL",
            topSetupText = "High-Impact Problem Solving vs Low-Value Task Escapism",
            bottomPunchlineText = "Organizing stationary or reorganizing study folders often conceals resistance to difficult numerical solving. Engage the hardest topic first.",
            emojiBanner = "📊 🎯 🏆",
            conceptTakeaway = "First-Hour Rule: Tackle your most demanding weakness in the first study hour when willpower is highest.",
            subjectForFlashcard = "Chemistry",
            chapterForFlashcard = "Mock Revision",
            likesCount = 5340,
            laughsCount = 428,
            comments = listOf("Pooja: Doing 10 numerical problems first thing in the morning set a productive tone for the entire day."),
            handle = "@prioritized_revision",
            avatarColorHex = 0xFFCA8A04,
            badgeNumber = "13",
            illustrationKey = "BOOK_PILE",
            postType = "PREP_EXCUSE",
            usBhaiUsCount = 4150
        )
    )

    fun loadNextInfiniteMemesBatch() {
        val nextBatchNum = _infiniteMemesPage.value + 1
        _infiniteMemesPage.value = nextBatchNum
        val startIdx = ((nextBatchNum - 2) * 4) % infiniteMemesTemplatePool.size
        val generatedItems = (0 until 4).map { offset ->
            val template = infiniteMemesTemplatePool[(startIdx + offset) % infiniteMemesTemplatePool.size]
            val cycleNumber = ((nextBatchNum - 2) * 4 + offset) / infiniteMemesTemplatePool.size
            val uniqueId = "inf_${nextBatchNum}_${offset}_${System.currentTimeMillis()}"
            val badgeNumStr = String.format("%02d", 10 + ((nextBatchNum - 2) * 4 + offset) % 89)
            if (cycleNumber == 0) {
                template.copy(
                    id = uniqueId,
                    badgeNumber = if (template.templateStyle == "ILLUSTRATED_ORIGINAL") badgeNumStr else null
                )
            } else {
                template.copy(
                    id = uniqueId,
                    timeAgo = "${(offset + 1) * 7}m ago",
                    topSetupText = "${template.topSetupText} (Batch #${cycleNumber + 1})",
                    likesCount = template.likesCount + (cycleNumber * 140) + (offset * 35),
                    usBhaiUsCount = template.usBhaiUsCount + (cycleNumber * 95),
                    badgeNumber = if (template.templateStyle == "ILLUSTRATED_ORIGINAL") badgeNumStr else null
                )
            }
        }
        _memesHubPosts.update { current -> current + generatedItems }
    }

    fun toggleMemeUpvote(memeId: String) {
        _memesHubPosts.update { list ->
            list.map { meme ->
                if (meme.id == memeId) {
                    val nextLiked = !meme.isLiked
                    meme.copy(
                        isLiked = nextLiked,
                        likesCount = if (nextLiked) meme.likesCount + 1 else (meme.likesCount - 1).coerceAtLeast(0),
                        laughsCount = if (nextLiked) meme.laughsCount + 1 else meme.laughsCount
                    )
                } else meme
            }
        }
    }

    fun toggleUsBhaiUs(memeId: String) {
        _memesHubPosts.update { list ->
            list.map { meme ->
                if (meme.id == memeId) {
                    val nextUs = !meme.isUsBhaiUs
                    if (nextUs) {
                        showSnackbar("🤝 Resonance recorded! Many fellow aspirants share this experience.")
                    }
                    meme.copy(
                        isUsBhaiUs = nextUs,
                        usBhaiUsCount = if (nextUs) meme.usBhaiUsCount + 1 else (meme.usBhaiUsCount - 1).coerceAtLeast(0)
                    )
                } else meme
            }
        }
    }

    fun toggleMemeBookmark(memeId: String) {
        _memesHubPosts.update { list ->
            list.map { meme ->
                if (meme.id == memeId) {
                    val nextSaved = !meme.isBookmarked
                    showSnackbar(if (nextSaved) "🔖 Saved to your Memes Vault!" else "Removed from Memes Vault")
                    meme.copy(isBookmarked = nextSaved)
                } else meme
            }
        }
    }

    fun addMemeComment(memeId: String, commentText: String) {
        if (commentText.isBlank()) return
        val userName = currentUser.value?.fullName ?: "Prabhat"
        _memesHubPosts.update { list ->
            list.map { meme ->
                if (meme.id == memeId) {
                    meme.copy(comments = meme.comments + "$userName: ${commentText.trim()}")
                } else meme
            }
        }
        boostUserScore(points = 5)
        showSnackbar("💬 Comment posted to the local prep-feed preview. No server XP was credited.")
    }

    fun publishStudentProblemOrExcuse(
        handle: String,
        postType: String, // "PREP_EXCUSE", "DAILY_PROBLEM", "FORMULA_MEME"
        flairTag: String,
        headline: String,
        bodyText: String,
        quoteBoxText: String,
        cardVisualStyle: String, // "QUOTE_ORIGINAL", "ILLUSTRATED_ORIGINAL"
        illustrationKey: String,
        quoteTone: String,
        solutionOrTakeaway: String
    ) {
        if (headline.isBlank() || bodyText.isBlank()) {
            showSnackbar("Please enter both a Headline and your Daily Problem / Prep Excuse!")
            return
        }
        val cleanHandle = if (handle.trim().startsWith("@")) handle.trim() else "@${handle.trim().ifBlank { "aspirant_confession" }}"
        val categoryLabel = when (postType) {
            "PREP_EXCUSE" -> "Prep Excuse 🤡"
            "DAILY_PROBLEM" -> "Daily Problem 😭"
            "FORMULA_MEME" -> "Concept & Formula Meme"
            else -> "Mock & Topper Logic 🏆"
        }
        val avatarColor = when (quoteTone) {
            "ROSE" -> 0xFFE11D48
            "MINT" -> 0xFF0D9488
            "CREAM" -> 0xFFD97706
            else -> 0xFF0066EE
        }
        val newPost = MemeHubItem(
            id = "student_post_${System.currentTimeMillis()}",
            authorName = cleanHandle,
            authorBadge = flairTag.uppercase(),
            timeAgo = "just now",
            category = categoryLabel,
            examTag = flairTag.uppercase(),
            templateStyle = cardVisualStyle,
            topSetupText = headline.trim(),
            bottomPunchlineText = bodyText.trim(),
            emojiBanner = if (postType == "PREP_EXCUSE") "🤡 📅 😂" else "😭 📚 🔥",
            conceptTakeaway = solutionOrTakeaway.trim().ifBlank {
                if (postType == "PREP_EXCUSE") {
                    "Excuse Breaker: Start a 25-minute Pomodoro Sprint right now to break the inertia!"
                } else {
                    "Action Step: Break this chapter into Lecture → Notes → 15 PYQs in your Chapter Tracker today."
                }
            },
            subjectForFlashcard = "Physics",
            chapterForFlashcard = flairTag,
            likesCount = 1,
            laughsCount = 1,
            isLiked = true,
            isBookmarked = true,
            comments = emptyList(),
            handle = cleanHandle,
            avatarColorHex = avatarColor,
            badgeNumber = if (cardVisualStyle == "ILLUSTRATED_ORIGINAL") "NEW" else null,
            illustrationKey = if (cardVisualStyle == "ILLUSTRATED_ORIGINAL") illustrationKey else null,
            quoteCanvasText = quoteBoxText.trim().ifBlank { headline.trim() },
            quoteCanvasTone = quoteTone,
            postType = postType,
            usBhaiUsCount = 1,
            isUsBhaiUs = true
        )
        _memesHubPosts.update { listOf(newPost) + it }
        boostUserScore(points = 25)
        showSnackbar("🚀 Posted to the local prep-feed preview. No server XP was credited.")
    }

    fun publishCustomMeme(
        category: String,
        examTag: String,
        templateStyle: String,
        topSetupText: String,
        bottomPunchlineText: String,
        emojiBanner: String,
        conceptTakeaway: String,
        subjectForFlashcard: String
    ) {
        if (topSetupText.isBlank() || bottomPunchlineText.isBlank()) {
            showSnackbar("Please enter both Top Setup Text and Bottom Punchline!")
            return
        }
        val userName = currentUser.value?.fullName ?: "Prabhat"
        val cleanHandle = "@${userName.lowercase().replace(" ", "_")}"
        val newMeme = MemeHubItem(
            id = "meme_${System.currentTimeMillis()}",
            authorName = cleanHandle,
            authorBadge = "${_selectedExamCategory.value.uppercase()} • CREATOR",
            timeAgo = "just now",
            category = category,
            examTag = examTag,
            templateStyle = templateStyle,
            topSetupText = topSetupText.trim(),
            bottomPunchlineText = bottomPunchlineText.trim(),
            emojiBanner = emojiBanner.ifBlank { "🔥 😂 🎯" },
            conceptTakeaway = conceptTakeaway.trim().ifBlank {
                "Shared via UpgradeHub Memes Studio — Laugh for 2 mins, then crush your next 3-hour study block!"
            },
            subjectForFlashcard = subjectForFlashcard.ifBlank { "Physics" },
            chapterForFlashcard = examTag,
            likesCount = 1,
            laughsCount = 1,
            isLiked = true,
            isBookmarked = true,
            comments = emptyList(),
            handle = cleanHandle,
            avatarColorHex = 0xFF0066EE,
            quoteCanvasText = bottomPunchlineText.trim(),
            quoteCanvasTone = "CREAM",
            postType = "FORMULA_MEME",
            usBhaiUsCount = 1,
            isUsBhaiUs = true
        )
        _memesHubPosts.update { listOf(newMeme) + it }
        boostUserScore(points = 25)
        showSnackbar("🚀 Posted to the local Memes Hub preview. No server XP was credited.")
    }
}

data class MemeHubItem(
    val id: String,
    val authorName: String,
    val authorBadge: String,
    val timeAgo: String,
    val category: String,
    val examTag: String,
    val templateStyle: String, // "ILLUSTRATED_ORIGINAL", "QUOTE_ORIGINAL", "DRAKE_COMPARE", "EXPECTATION_VS_REALITY", "SPIDERMAN_POINTING", "EXPANDING_BRAIN", "TEACHER_QUOTE"
    val topSetupText: String,
    val bottomPunchlineText: String,
    val emojiBanner: String,
    val conceptTakeaway: String,
    val subjectForFlashcard: String,
    val chapterForFlashcard: String,
    val likesCount: Int,
    val laughsCount: Int,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val comments: List<String> = emptyList(),
    val handle: String = authorName,
    val avatarColorHex: Long = 0xFF0066EE,
    val badgeNumber: String? = null,
    val illustrationKey: String? = null, // "FLOW_STATE", "LAMP_3AM", "BOOK_PILE", "NAP_BOOKS", "FORMULA_BOARD", "TROPHY_WIN"
    val quoteCanvasText: String? = null,
    val quoteCanvasTone: String = "CREAM", // "CREAM", "ROSE", "MINT", "SKY"
    val postType: String = "ORIGINAL_FEED", // "ORIGINAL_FEED", "PREP_EXCUSE", "DAILY_PROBLEM", "FORMULA_MEME"
    val usBhaiUsCount: Int = 120,
    val isUsBhaiUs: Boolean = false
)

data class CbtAttemptRecord(
    val id: String,
    val testTitle: String,
    val examCategory: String,
    val score: Int,
    val maxMarks: Int,
    val accuracyPct: Int,
    val timeTakenLabel: String,
    val timestampLabel: String
)

data class MistakeNotebookEntry(
    val id: String,
    val questionTitle: String,
    val subject: String,
    val chapterOrSource: String,
    val errorType: String, // "Silly Mistake", "Concept Gap", "Formula Forgotten", "Time Pressure"
    val takeawayNote: String,
    val isResolved: Boolean = false,
    val createdAt: String = "Today"
)

data class TeamAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val category: String, // "URGENT", "EXAM ALERT", "BATCH UPDATE", "REWARD & CONTEST", "SYSTEM NOTICE"
    val authorName: String,
    val authorRole: String,
    val timestamp: String,
    val isPinned: Boolean = false,
    val isImportant: Boolean = false,
    val isRead: Boolean = false,
    val actionLabel: String? = null,
    val actionTargetPage: JeePage? = null,
    val acknowledgementCount: Int = 1,
    val isAcknowledgedByMe: Boolean = false
)


package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.TestResultEntity
import com.example.data.model.Category
import com.example.data.model.Flashcard
import com.example.data.model.Question
import com.example.data.repository.UsoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    STUDY,
    BOOKMARKS,
    ANALYTICS
}

enum class ActiveScreen {
    MAIN_TABS,
    MOCK_TEST,
    PRACTICE_QUIZ,
    TEST_RESULT
}

data class MockTestState(
    val category: Category?,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
    val markedForReview: Set<Int> = emptySet(),
    val totalSeconds: Int = 600, // 10 minutes default
    val secondsRemaining: Int = 600,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val timeSpentSeconds: Int = 0
)

data class PracticeQuizState(
    val category: Category?,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val correctCount: Int = 0,
    val isCompleted: Boolean = false
)

class UsoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsoRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = UsoRepository(db.userDao())
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeScreen = MutableStateFlow(ActiveScreen.MAIN_TABS)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // Test History & Bookmarks from Room
    val testResults: StateFlow<List<TestResultEntity>> = repository.allTestResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masteredCards: StateFlow<Set<String>> = repository.getMasteredCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val studentName: StateFlow<String> = repository.getStudentName()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Olympiad Scholar")

    val studentGrade: StateFlow<String> = repository.getStudentGrade()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Grade 8-10")

    // Mock Test State & Timer Job
    private val _mockTestState = MutableStateFlow(MockTestState(category = null))
    val mockTestState: StateFlow<MockTestState> = _mockTestState.asStateFlow()
    private var timerJob: Job? = null

    // Practice Quiz State
    private val _practiceState = MutableStateFlow(PracticeQuizState(category = null))
    val practiceState: StateFlow<PracticeQuizState> = _practiceState.asStateFlow()

    // Study Flashcards State
    private val _selectedStudyCategory = MutableStateFlow<Category?>(null)
    val selectedStudyCategory: StateFlow<Category?> = _selectedStudyCategory.asStateFlow()

    private val _flashcardIndex = MutableStateFlow(0)
    val flashcardIndex: StateFlow<Int> = _flashcardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _activeScreen.value = ActiveScreen.MAIN_TABS
    }

    fun navigateToHome() {
        _activeScreen.value = ActiveScreen.MAIN_TABS
        _currentTab.value = MainTab.HOME
    }

    // Start Mock Test
    fun startMockTest(category: Category? = null, questionCount: Int = 10, durationSeconds: Int = 600) {
        timerJob?.cancel()
        val questions = repository.getMockTestQuestions(category, questionCount)
        _mockTestState.value = MockTestState(
            category = category,
            questions = questions,
            currentIndex = 0,
            userAnswers = emptyMap(),
            markedForReview = emptySet(),
            totalSeconds = durationSeconds,
            secondsRemaining = durationSeconds,
            isRunning = true,
            isCompleted = false,
            score = 0,
            timeSpentSeconds = 0
        )
        _activeScreen.value = ActiveScreen.MOCK_TEST

        // Start Countdown Timer
        timerJob = viewModelScope.launch {
            while (_mockTestState.value.secondsRemaining > 0 && _mockTestState.value.isRunning) {
                delay(1000)
                _mockTestState.value = _mockTestState.value.copy(
                    secondsRemaining = _mockTestState.value.secondsRemaining - 1
                )
            }
            if (_mockTestState.value.secondsRemaining <= 0 && _mockTestState.value.isRunning) {
                submitMockTest()
            }
        }
    }

    fun selectMockAnswer(optionIndex: Int) {
        val current = _mockTestState.value
        val updatedAnswers = current.userAnswers.toMutableMap()
        updatedAnswers[current.currentIndex] = optionIndex
        _mockTestState.value = current.copy(userAnswers = updatedAnswers)
    }

    fun toggleMarkForReview() {
        val current = _mockTestState.value
        val set = current.markedForReview.toMutableSet()
        if (set.contains(current.currentIndex)) {
            set.remove(current.currentIndex)
        } else {
            set.add(current.currentIndex)
        }
        _mockTestState.value = current.copy(markedForReview = set)
    }

    fun goToMockQuestion(index: Int) {
        val questions = _mockTestState.value.questions
        if (index in questions.indices) {
            _mockTestState.value = _mockTestState.value.copy(currentIndex = index)
        }
    }

    fun nextMockQuestion() {
        val current = _mockTestState.value
        if (current.currentIndex < current.questions.size - 1) {
            _mockTestState.value = current.copy(currentIndex = current.currentIndex + 1)
        }
    }

    fun prevMockQuestion() {
        val current = _mockTestState.value
        if (current.currentIndex > 0) {
            _mockTestState.value = current.copy(currentIndex = current.currentIndex - 1)
        }
    }

    fun submitMockTest() {
        timerJob?.cancel()
        val current = _mockTestState.value
        var calculatedScore = 0
        current.questions.forEachIndexed { index, question ->
            val selected = current.userAnswers[index]
            if (selected == question.correctIndex) {
                calculatedScore++
            }
        }
        val spent = current.totalSeconds - current.secondsRemaining

        _mockTestState.value = current.copy(
            isRunning = false,
            isCompleted = true,
            score = calculatedScore,
            timeSpentSeconds = spent
        )
        _activeScreen.value = ActiveScreen.TEST_RESULT

        // Save result in Room
        viewModelScope.launch {
            val catName = current.category?.title ?: "Grand USO Olympiad Mock"
            repository.saveTestResult(
                categoryName = catName,
                score = calculatedScore,
                totalQuestions = current.questions.size,
                timeSpentSeconds = spent
            )
        }
    }

    // Practice Quiz Flow
    fun startPracticeQuiz(category: Category? = null) {
        val questions = if (category != null) {
            repository.getQuestionsByCategory(category).shuffled()
        } else {
            repository.getDailyChallengeQuestions()
        }
        _practiceState.value = PracticeQuizState(
            category = category,
            questions = questions,
            currentIndex = 0,
            selectedOptionIndex = null,
            isAnswerRevealed = false,
            correctCount = 0,
            isCompleted = false
        )
        _activeScreen.value = ActiveScreen.PRACTICE_QUIZ
    }

    fun selectPracticeOption(optionIndex: Int) {
        val state = _practiceState.value
        if (state.isAnswerRevealed) return

        val currentQ = state.questions[state.currentIndex]
        val isCorrect = optionIndex == currentQ.correctIndex
        val newCorrectCount = if (isCorrect) state.correctCount + 1 else state.correctCount

        _practiceState.value = state.copy(
            selectedOptionIndex = optionIndex,
            isAnswerRevealed = true,
            correctCount = newCorrectCount
        )
    }

    fun nextPracticeQuestion() {
        val state = _practiceState.value
        if (state.currentIndex < state.questions.size - 1) {
            _practiceState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                selectedOptionIndex = null,
                isAnswerRevealed = false
            )
        } else {
            _practiceState.value = state.copy(isCompleted = true)
        }
    }

    // Bookmark actions
    fun toggleBookmark(question: Question) {
        viewModelScope.launch {
            val isCurrentSaved = bookmarks.value.any { it.questionId == question.id }
            if (isCurrentSaved) {
                repository.removeBookmark(question.id)
            } else {
                repository.toggleBookmark(question)
            }
        }
    }

    fun removeBookmark(questionId: String) {
        viewModelScope.launch {
            repository.removeBookmark(questionId)
        }
    }

    // Study Hub & Flashcards
    fun setStudyCategory(category: Category?) {
        _selectedStudyCategory.value = category
        _flashcardIndex.value = 0
        _isCardFlipped.value = false
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun nextCard(totalCards: Int) {
        if (totalCards > 0) {
            _flashcardIndex.value = (_flashcardIndex.value + 1) % totalCards
            _isCardFlipped.value = false
        }
    }

    fun prevCard(totalCards: Int) {
        if (totalCards > 0) {
            _flashcardIndex.value = if (_flashcardIndex.value > 0) _flashcardIndex.value - 1 else totalCards - 1
            _isCardFlipped.value = false
        }
    }

    fun toggleFlashcardMastered(cardId: String) {
        viewModelScope.launch {
            val current = masteredCards.value.toMutableSet()
            if (current.contains(cardId)) {
                current.remove(cardId)
            } else {
                current.add(cardId)
            }
            repository.setMasteredCards(current)
        }
    }

    fun updateProfile(name: String, grade: String) {
        viewModelScope.launch {
            repository.setStudentName(name)
            repository.setStudentGrade(grade)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllTestResults()
        }
    }
}

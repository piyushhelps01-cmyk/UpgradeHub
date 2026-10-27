package com.example.data.repository

import com.example.data.local.BookmarkEntity
import com.example.data.local.StudyProgressEntity
import com.example.data.local.TestResultEntity
import com.example.data.local.UserDao
import com.example.data.model.Category
import com.example.data.model.Flashcard
import com.example.data.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UsoRepository(private val userDao: UserDao) {

    // Test Results
    val allTestResults: Flow<List<TestResultEntity>> = userDao.getAllTestResults()

    suspend fun saveTestResult(
        categoryName: String,
        score: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int
    ): Long {
        val percentage = if (totalQuestions > 0) (score.toFloat() / totalQuestions) * 100f else 0f
        val entity = TestResultEntity(
            categoryName = categoryName,
            score = score,
            totalQuestions = totalQuestions,
            percentage = percentage,
            timeSpentSeconds = timeSpentSeconds
        )
        return userDao.insertTestResult(entity)
    }

    suspend fun deleteTestResult(id: Long) = userDao.deleteTestResult(id)
    suspend fun clearAllTestResults() = userDao.clearAllTestResults()

    // Bookmarks
    val allBookmarks: Flow<List<BookmarkEntity>> = userDao.getAllBookmarks()

    fun isBookmarked(questionId: String): Flow<Boolean> = userDao.isBookmarked(questionId)

    suspend fun toggleBookmark(question: Question) {
        val isSaved = userDao.isBookmarked(question.id)
        // Check synchronously or insert
        val entity = BookmarkEntity(
            questionId = question.id,
            categoryName = question.category.name,
            questionText = question.question,
            optionA = question.options.getOrElse(0) { "" },
            optionB = question.options.getOrElse(1) { "" },
            optionC = question.options.getOrElse(2) { "" },
            optionD = question.options.getOrElse(3) { "" },
            correctIndex = question.correctIndex,
            explanation = question.explanation
        )
        userDao.insertBookmark(entity)
    }

    suspend fun removeBookmark(questionId: String) {
        userDao.deleteBookmark(questionId)
    }

    // Flashcards & Mastered progress
    fun getMasteredCards(): Flow<Set<String>> {
        return userDao.getProgress("mastered_cards").map { value ->
            if (value.isNullOrBlank()) emptySet()
            else value.split(",").filter { it.isNotBlank() }.toSet()
        }
    }

    suspend fun toggleCardMastered(cardId: String, currentlyMastered: Boolean) {
        val currentStr = userDao.getProgress("mastered_cards")
        // We'll update the comma separated string
    }

    suspend fun setMasteredCards(cards: Set<String>) {
        userDao.setProgress(
            StudyProgressEntity(
                key = "mastered_cards",
                value = cards.joinToString(",")
            )
        )
    }

    // Student Profile
    fun getStudentName(): Flow<String> = userDao.getProgress("student_name").map { it ?: "Olympiad Scholar" }
    suspend fun setStudentName(name: String) = userDao.setProgress(StudyProgressEntity("student_name", name))

    fun getStudentGrade(): Flow<String> = userDao.getProgress("student_grade").map { it ?: "Grade 8-10" }
    suspend fun setStudentGrade(grade: String) = userDao.setProgress(StudyProgressEntity("student_grade", grade))

    // Questions Queries
    fun getAllQuestions(): List<Question> = SampleData.questions

    fun getQuestionsByCategory(category: Category): List<Question> {
        return SampleData.questions.filter { it.category == category }
    }

    fun getMockTestQuestions(category: Category? = null, count: Int = 10): List<Question> {
        val pool = if (category != null) {
            SampleData.questions.filter { it.category == category }
        } else {
            SampleData.questions
        }
        return pool.shuffled().take(count)
    }

    fun getDailyChallengeQuestions(count: Int = 5): List<Question> {
        return SampleData.questions.shuffled().take(count)
    }

    fun getFlashcards(category: Category? = null): List<Flashcard> {
        return if (category != null) {
            SampleData.flashcards.filter { it.category == category }
        } else {
            SampleData.flashcards
        }
    }
}

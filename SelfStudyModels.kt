package com.example.data.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Core categories from SelfStudys (https://play.google.com/store/apps/details?id=selfstudys.com).
 * Includes NCERT Books & Solutions, JEE Main & Advanced, NEET UG, State Boards (Punjab, UP, Bihar, etc.),
 * Smart-Book Notes, Formula Cheat Sheets, and Full CBT Mock Tests.
 */
enum class SelfStudyCategory(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val tag: String
) {
    NCERT_PORTAL("ncert", "NCERT Books & Solutions", "Class 1-12 Textbooks, In-Text & Exercise Solutions", "NCERT"),
    JEE_PORTAL("jee", "JEE Main & Advanced", "Chapterwise PYQs (2018-2025), One-Page Notes & Mock Tests", "JEE"),
    NEET_PORTAL("neet", "NEET (UG) Hub", "Botany, Zoology, Physics & Chem PYQs, Diagrams & Mocks", "NEET"),
    STATE_BOARDS("state_boards", "State Boards & CBSE", "Punjab Board (PSEB), UP Board, CBSE Sample Papers", "BOARDS"),
    SMART_NOTES("smart_notes", "Smart Notes & Formulas", "Formula Sheets, Mind Maps & High-Yield Bullet Summaries", "NOTES"),
    MOCK_TESTS("mock_tests", "CBT Mock Test Series", "Live Timed Online Tests with Detailed Solutions & Analysis", "TESTS")
}

data class ChapterSection(
    val heading: String,
    val body: String,
    val keyFormulas: List<String> = emptyList(),
    val importantNotice: String? = null
)

data class ChapterExerciseSolution(
    val exerciseName: String, // e.g., "Exercise 1.1", "Miscellaneous", "Exemplar Problems"
    val questionNo: Int,
    val questionText: String,
    val solutionSteps: List<String>,
    val finalAnswer: String,
    val conceptTip: String? = null
)

data class FormulaItem(
    val title: String,
    val formulaText: String,
    val explanation: String,
    val units: String? = null
)

data class SelfStudyPyqQuestion(
    val id: String,
    val exam: String, // "JEE Main", "JEE Advanced", "NEET", "CBSE 12th"
    val year: String, // "2025", "2024", "2023", etc.
    val subject: String,
    val chapter: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int, // 0..3
    val stepByStepSolution: String,
    val difficulty: String = "Medium", // "Easy", "Medium", "Hard"
    val importantTag: String = "High Frequency"
)

data class SelfStudyChapter(
    val id: String,
    val bookId: String,
    val chapterNo: Int,
    val title: String,
    val subject: String,
    val classLevel: String, // "Class 11", "Class 12", "Class 10"
    val summary: String,
    val readingSections: List<ChapterSection>,
    val exerciseSolutions: List<ChapterExerciseSolution>,
    val exemplarSolutions: List<ChapterExerciseSolution>,
    val quickRevisionPoints: List<String>,
    val formulaSheet: List<FormulaItem>,
    val pyqs: List<SelfStudyPyqQuestion>,
    val estimatedReadTimeMinutes: Int = 25
)

data class SelfStudyBook(
    val id: String,
    val title: String,
    val subject: String,
    val classLevel: String,
    val medium: String = "English", // "English" or "Hindi"
    val edition: String = "Latest 2025-26 NCERT Edition",
    val author: String = "NCERT / SelfStudys Academic Team",
    val totalChapters: Int,
    val fileSize: String = "18.4 MB",
    val rating: Float = 4.9f,
    val chapters: List<SelfStudyChapter>
)

data class SelfStudyMockTest(
    val id: String,
    val title: String,
    val exam: String, // "JEE Main", "NEET", "Class 12 Physics"
    val subject: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val markingScheme: String = "+4 / -1",
    val questions: List<SelfStudyPyqQuestion>
)

data class SelfStudyTestSubmission(
    val testId: String,
    val score: Int,
    val maxScore: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val timeSpentSeconds: Int,
    val userAnswers: Map<Int, Int>, // question index -> chosen option index
    val timestamp: Long = System.currentTimeMillis()
)

data class SelfStudyStateBoardItem(
    val id: String,
    val boardName: String, // "Punjab State Board (PSEB)", "UP Board (UPMSP)", "CBSE Board", "Bihar Board (BSEB)"
    val classLevel: String,
    val subject: String,
    val title: String,
    val resourceType: String, // "Textbook", "Solutions Guide", "Sample Paper 2025", "Model Paper"
    val summary: String,
    val contentSections: List<ChapterSection>,
    val downloadSize: String = "12.5 MB"
)

package com.example.data.model

data class LeaderboardStudent(
    val rank: Int,
    val studentName: String,
    val avatarInitials: String,
    val avatarColor: Long,
    val batchName: String,
    val studyHours: Float,
    val problemsSolved: Int,
    val mockTestsTaken: Int,
    val streakDays: Int,
    val monthlyScore: Int,
    val weeklyScore: Int = (monthlyScore * 0.28).toInt(),
    val rankDelta: Int = 0, // +2, -1, 0
    val accuracyPercentage: Int = 85,
    val strongSubject: String = "Physics - Mechanics",
    val badges: List<String> = emptyList(),
    val cheersCount: Int = 12,
    val isCurrentUser: Boolean = false
) {
    val isTopThree: Boolean get() = rank in 1..3
}

data class HallOfFameWinner(
    val month: String,
    val rank: Int,
    val studentName: String,
    val avatarInitials: String,
    val avatarColor: Long,
    val batchName: String,
    val finalScore: Int,
    val tipQuote: String
)

enum class EventFilter(val label: String) {
    ALL("All Events"),
    LIVE("Live Now"),
    UPCOMING("Upcoming"),
    CONTESTS("Speed Contests"),
    MEGA_MOCKS("Mega Mocks")
}

data class JeeEvent(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val schedule: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val questionCount: Int,
    val participantsCount: Int,
    val rewardDescription: String = "Preview event · no rewards active",
    val syllabus: String,
    val isLive: Boolean = false,
    val isRegistered: Boolean = false,
    val difficulty: String = "JEE Main"
)

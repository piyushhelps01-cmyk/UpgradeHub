package com.example.data.repository

import com.example.data.model.EventFilter
import com.example.data.model.HallOfFameWinner
import com.example.data.model.JeeEvent
import com.example.data.model.LeaderboardStudent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LeaderboardEventsRepository {

    private val _currentMonth = MutableStateFlow("September 2026")
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    private val _students = MutableStateFlow<List<LeaderboardStudent>>(getDefaultLeaderboard())
    val students: StateFlow<List<LeaderboardStudent>> = _students.asStateFlow()

    private val _events = MutableStateFlow<List<JeeEvent>>(getDefaultEvents())
    val events: StateFlow<List<JeeEvent>> = _events.asStateFlow()

    private val _hallOfFameWinners = MutableStateFlow<List<HallOfFameWinner>>(getDefaultHallOfFameWinners())
    val hallOfFameWinners: StateFlow<List<HallOfFameWinner>> = _hallOfFameWinners.asStateFlow()

    fun sendHighFive(studentName: String) {
        _students.update { list ->
            list.map { student ->
                if (student.studentName == studentName) {
                    student.copy(cheersCount = student.cheersCount + 1)
                } else student
            }
        }
    }

    fun toggleEventRegistration(eventId: String) {
        _events.update { list ->
            list.map { event ->
                if (event.id == eventId) {
                    val newRegistered = !event.isRegistered
                    event.copy(
                        isRegistered = newRegistered,
                        participantsCount = if (newRegistered) event.participantsCount + 1 else (event.participantsCount - 1).coerceAtLeast(1)
                    )
                } else event
            }
        }
    }

    private fun getDefaultLeaderboard(): List<LeaderboardStudent> {
        return listOf(
            LeaderboardStudent(
                rank = 1,
                studentName = "Aryan Sharma",
                avatarInitials = "AS",
                avatarColor = 0xFF0071E3,
                batchName = "Lakshya JEE 2026",
                studyHours = 268.5f,
                problemsSolved = 1420,
                mockTestsTaken = 14,
                streakDays = 42,
                monthlyScore = 3890,
                weeklyScore = 1040,
                rankDelta = 0,
                accuracyPercentage = 94,
                strongSubject = "Physics - Mechanics & Thermo",
                badges = listOf("🔥 42d Streak", "🥇 Rank 1 Defending", "⚡ Speed Demon"),
                cheersCount = 48,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 2,
                studentName = "Ananya Verma",
                avatarInitials = "AV",
                avatarColor = 0xFF5856D6,
                batchName = "Arjuna JEE Main+Adv",
                studyHours = 254.0f,
                problemsSolved = 1380,
                mockTestsTaken = 12,
                streakDays = 39,
                monthlyScore = 3740,
                weeklyScore = 980,
                rankDelta = 1,
                accuracyPercentage = 91,
                strongSubject = "Mathematics - Calculus & Vectors",
                badges = listOf("🔥 39d Streak", "🎯 91% Accuracy", "🥈 Silver Contender"),
                cheersCount = 36,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 3,
                studentName = "Rohan Patel",
                avatarInitials = "RP",
                avatarColor = 0xFF00C853,
                batchName = "Prayas Dropper Batch",
                studyHours = 241.5f,
                problemsSolved = 1290,
                mockTestsTaken = 11,
                streakDays = 35,
                monthlyScore = 3580,
                weeklyScore = 890,
                rankDelta = -1,
                accuracyPercentage = 88,
                strongSubject = "Chemistry - Organic Reactions",
                badges = listOf("🔥 35d Streak", "🥉 Sample Top 3", "🧪 Chem Master"),
                cheersCount = 29,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 4,
                studentName = "Prince Kumar (You)",
                avatarInitials = "PK",
                avatarColor = 0xFFFF9500,
                batchName = "Lakshya JEE 2026",
                studyHours = 238.0f,
                problemsSolved = 1250,
                mockTestsTaken = 10,
                streakDays = 33,
                monthlyScore = 3520,
                weeklyScore = 920,
                rankDelta = 2, // Surged up 2 ranks!
                accuracyPercentage = 87,
                strongSubject = "Physics - Electrodynamics & Optics",
                badges = listOf("🔥 33d Streak", "🚀 Rapid Climber", "⚡ Sample score gap"),
                cheersCount = 24,
                isCurrentUser = true
            ),
            LeaderboardStudent(
                rank = 5,
                studentName = "Ishaan Gupta",
                avatarInitials = "IG",
                avatarColor = 0xFFFF2D55,
                batchName = "Lakshya JEE 2026",
                studyHours = 224.0f,
                problemsSolved = 1180,
                mockTestsTaken = 9,
                streakDays = 29,
                monthlyScore = 3390,
                weeklyScore = 810,
                rankDelta = 0,
                accuracyPercentage = 85,
                strongSubject = "Mathematics - Algebra & Complex",
                badges = listOf("🔥 29d Streak", "📐 Math Whiz"),
                cheersCount = 18,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 6,
                studentName = "Sneha Roy",
                avatarInitials = "SR",
                avatarColor = 0xFFAF52DE,
                batchName = "Arjuna JEE Batch",
                studyHours = 215.0f,
                problemsSolved = 1120,
                mockTestsTaken = 8,
                streakDays = 27,
                monthlyScore = 3240,
                weeklyScore = 770,
                rankDelta = 1,
                accuracyPercentage = 86,
                strongSubject = "Chemistry - Coordination & Kinetics",
                badges = listOf("🔥 27d Streak", "✨ Consistent Solvers"),
                cheersCount = 15,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 7,
                studentName = "Devendra Singh",
                avatarInitials = "DS",
                avatarColor = 0xFF34C759,
                batchName = "Prayas Dropper Batch",
                studyHours = 208.5f,
                problemsSolved = 1060,
                mockTestsTaken = 8,
                streakDays = 26,
                monthlyScore = 3150,
                weeklyScore = 740,
                rankDelta = -1,
                accuracyPercentage = 83,
                strongSubject = "Physics - Mechanics",
                badges = listOf("🔥 26d Streak", "⚙️ Deep Focus"),
                cheersCount = 14,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 8,
                studentName = "Kavya Nambiar",
                avatarInitials = "KN",
                avatarColor = 0xFF5AC8FA,
                batchName = "Lakshya JEE 2026",
                studyHours = 196.0f,
                problemsSolved = 990,
                mockTestsTaken = 7,
                streakDays = 24,
                monthlyScore = 2980,
                weeklyScore = 710,
                rankDelta = 0,
                accuracyPercentage = 82,
                strongSubject = "Chemistry - Physical Chem",
                badges = listOf("🔥 24d Streak"),
                cheersCount = 11,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 9,
                studentName = "Manish Tiwari",
                avatarInitials = "MT",
                avatarColor = 0xFFFF9500,
                batchName = "Prayas Dropper Batch",
                studyHours = 188.0f,
                problemsSolved = 940,
                mockTestsTaken = 6,
                streakDays = 21,
                monthlyScore = 2860,
                weeklyScore = 680,
                rankDelta = 2,
                accuracyPercentage = 81,
                strongSubject = "Mathematics - Trigonometry",
                badges = listOf("🔥 21d Streak"),
                cheersCount = 9,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 10,
                studentName = "Pooja Reddy",
                avatarInitials = "PR",
                avatarColor = 0xFF30B0C7,
                batchName = "Arjuna JEE Main+Adv",
                studyHours = 179.5f,
                problemsSolved = 890,
                mockTestsTaken = 6,
                streakDays = 19,
                monthlyScore = 2740,
                weeklyScore = 650,
                rankDelta = -2,
                accuracyPercentage = 80,
                strongSubject = "Physics - Modern Physics",
                badges = listOf("🔥 19d Streak"),
                cheersCount = 8,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 11,
                studentName = "Aditya Joshi",
                avatarInitials = "AJ",
                avatarColor = 0xFF6574FF,
                batchName = "Lakshya JEE 2026",
                studyHours = 172.0f,
                problemsSolved = 850,
                mockTestsTaken = 5,
                streakDays = 18,
                monthlyScore = 2620,
                weeklyScore = 620,
                rankDelta = 1,
                accuracyPercentage = 79,
                strongSubject = "Chemistry - Inorganic",
                badges = listOf("🔥 18d Streak"),
                cheersCount = 6,
                isCurrentUser = false
            ),
            LeaderboardStudent(
                rank = 12,
                studentName = "Tanvi Kulkarni",
                avatarInitials = "TK",
                avatarColor = 0xFFFF3B30,
                batchName = "Arjuna JEE Batch",
                studyHours = 165.0f,
                problemsSolved = 810,
                mockTestsTaken = 5,
                streakDays = 16,
                monthlyScore = 2510,
                weeklyScore = 590,
                rankDelta = 0,
                accuracyPercentage = 78,
                strongSubject = "Mathematics - Coordinate Geometry",
                badges = listOf("🔥 16d Streak"),
                cheersCount = 5,
                isCurrentUser = false
            )
        )
    }

    private fun getDefaultHallOfFameWinners(): List<HallOfFameWinner> {
        return listOf(
            HallOfFameWinner(
                month = "August 2026",
                rank = 1,
                studentName = "Kunal Rawat",
                avatarInitials = "KR",
                avatarColor = 0xFF0071E3,
                batchName = "Lakshya JEE 2026",
                finalScore = 4120,
                tipQuote = "\"I set aside time for daily practice and reviewed mistakes after Sunday mocks.\""
            ),
            HallOfFameWinner(
                month = "August 2026",
                rank = 2,
                studentName = "Priya Nanda",
                avatarInitials = "PN",
                avatarColor = 0xFF5856D6,
                batchName = "Arjuna JEE Main+Adv",
                finalScore = 3980,
                tipQuote = "\"Consistent short sessions helped me stay on track during exam prep.\""
            ),
            HallOfFameWinner(
                month = "August 2026",
                rank = 3,
                studentName = "Harsh Vardhan",
                avatarInitials = "HV",
                avatarColor = 0xFF00C853,
                batchName = "Prayas Dropper Batch",
                finalScore = 3840,
                tipQuote = "\"Reviewing mock-test gaps helped me decide which topic to revisit next.\""
            ),
            HallOfFameWinner(
                month = "July 2026",
                rank = 1,
                studentName = "Meera Sengupta",
                avatarInitials = "MS",
                avatarColor = 0xFFFF2D55,
                batchName = "Lakshya JEE 2026",
                finalScore = 4250,
                tipQuote = "\"I prioritized high-weightage chapters and reviewed every mock.\""
            ),
            HallOfFameWinner(
                month = "July 2026",
                rank = 2,
                studentName = "Ayush Saxena",
                avatarInitials = "AS",
                avatarColor = 0xFF34C759,
                batchName = "Prayas Dropper Batch",
                finalScore = 4090,
                tipQuote = "\"A steady routine gave me a clear target during exam preparation.\""
            ),
            HallOfFameWinner(
                month = "July 2026",
                rank = 3,
                studentName = "Divya Mehra",
                avatarInitials = "DM",
                avatarColor = 0xFFAF52DE,
                batchName = "Arjuna JEE Batch",
                finalScore = 3910,
                tipQuote = "\"Competing with my own weekly goals helped me stay consistent.\""
            )
        )
    }

    private fun getDefaultEvents(): List<JeeEvent> {
        return listOf(
            JeeEvent(
                id = "event_monthly_mega_test",
                title = "All India Monthly JEE Mock Championship",
                subtitle = "Monthly Mega Test • All 3 Subjects (PCM)",
                category = "Mega Mock",
                schedule = "Sunday 21 Sept, 9:00 AM - 12:00 PM",
                durationMinutes = 180,
                totalMarks = 300,
                questionCount = 75,
                participantsCount = 3840,
                rewardDescription = "Sample event only · no live reward or registration.",
                syllabus = "Full Class 11 & 12 Syllabus: Mechanics, Electrodynamics, Organic, Calculus, Vectors",
                isLive = true,
                isRegistered = true,
                difficulty = "JEE Main Level"
            ),
            JeeEvent(
                id = "event_speed_sprint_1",
                title = "100-Problem Speed Sprint Contest",
                subtitle = "High-Accuracy Rapid Solve • Boost Leaderboard Ranks",
                category = "Speed Contest",
                schedule = "Every Wednesday, 7:00 PM - 8:30 PM",
                durationMinutes = 90,
                totalMarks = 100,
                questionCount = 50,
                participantsCount = 1820,
                rewardDescription = "Preview event · XP is not awarded here.",
                syllabus = "High-Weightage Chapters: Rotational Motion, Thermo, Coordinate Geometry",
                isLive = false,
                isRegistered = true,
                difficulty = "Speed Blitz"
            ),
            JeeEvent(
                id = "event_ncert_recall_battle",
                title = "NCERT Chemistry Line-by-Line Recall Battle",
                subtitle = "Inorganic & Physical Chemistry NCERT Direct Facts",
                category = "Live Contest",
                schedule = "Friday 26 Sept, 8:00 PM - 9:00 PM",
                durationMinutes = 60,
                totalMarks = 120,
                questionCount = 30,
                participantsCount = 2410,
                rewardDescription = "Preview event · XP is not awarded here.",
                syllabus = "p-Block, Coordination Compounds, Chemical Kinetics, Solutions, Biomolecules",
                isLive = false,
                isRegistered = false,
                difficulty = "NCERT Pure"
            ),
            JeeEvent(
                id = "event_12hr_study_marathon",
                title = "12-Hour Focused Weekend Study Marathon",
                subtitle = "Community Live Study Room • Clock Verified Hours",
                category = "Marathon",
                schedule = "Saturday, 8:00 AM - 8:00 PM",
                durationMinutes = 720,
                totalMarks = 0,
                questionCount = 0,
                participantsCount = 4920,
                rewardDescription = "Sample event only · no cash reward is active.",
                syllabus = "Self-directed backlog completion, DPP solving, revision sessions",
                isLive = false,
                isRegistered = false,
                difficulty = "High Stamina"
            ),
            JeeEvent(
                id = "event_advanced_problem_duel",
                title = "JEE Advanced Multi-Correct Tournament",
                subtitle = "Deep Conceptual Problems • Negative Marking Challenge",
                category = "Mega Mock",
                schedule = "Next Sunday, 2:00 PM - 5:00 PM",
                durationMinutes = 180,
                totalMarks = 198,
                questionCount = 54,
                participantsCount = 1450,
                rewardDescription = "Preview event · XP is not awarded here.",
                syllabus = "Modern Physics, Complex Numbers, Chemical Bonding, Calculus Integration",
                isLive = false,
                isRegistered = false,
                difficulty = "JEE Advanced"
            )
        )
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import kotlinx.coroutines.delay

data class CampusPost(
    val id: Int,
    val authorName: String,
    val authorBadge: String,
    val avatarColor: Long,
    val timeAgo: String,
    val tag: String,
    val title: String,
    val body: String,
    val verifiedSolution: String? = null,
    val upvotes: Int,
    val commentsCount: Int,
    val isUpvoted: Boolean = false,
    val isBookmarked: Boolean = false,
    val comments: List<String> = emptyList()
)

data class StudyPodRoom(
    val id: Int,
    val roomName: String,
    val subject: String,
    val hostName: String,
    val activeMembers: Int,
    val maxMembers: Int,
    val currentSprintLabel: String,
    val accentColor: Long,
    val activePeersPreview: List<String> = emptyList(),
    val isJoined: Boolean = false
)

data class SpeedBattleQuestion(
    val id: Int,
    val subject: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val quickFormula: String
)

data class PeerMentorProfile(
    val id: Int,
    val name: String,
    val institute: String,
    val rankBadge: String,
    val specialty: String,
    val rating: String,
    val sessionsHelped: Int,
    val playbookSchedule: List<String>,
    val goldenAdvice: String,
    val isConnected: Boolean = false
)

@Composable
fun CampusHubScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    onNavigate: (JeePage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val isFocusTimerRunning by viewModel.isFocusTimerRunning.collectAsState()
    val focusRemainingSeconds by viewModel.focusRemainingSeconds.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf("All") }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var activeCommentPostId by remember { mutableStateOf<Int?>(null) }
    var commentDraft by remember { mutableStateOf("") }
    var activeLivePodRoom by remember { mutableStateOf<StudyPodRoom?>(null) }
    var selectedMentorPlaybook by remember { mutableStateOf<PeerMentorProfile?>(null) }
    var podChatInput by remember { mutableStateOf("") }

    val facultyMaterials by viewModel.uploadedMaterials.collectAsState()
    var materialSubjectFilter by remember { mutableStateOf("All") }
    val filteredFacultyMaterials = remember(facultyMaterials, materialSubjectFilter) {
        if (materialSubjectFilter == "All") facultyMaterials
        else facultyMaterials.filter { it.subject.equals(materialSubjectFilter, ignoreCase = true) }
    }

    // 1v1 Live Speed Quiz Battle State
    var isBattleActive by remember { mutableStateOf(false) }
    var battleSubjectFilter by remember { mutableStateOf("All") }
    var battleQuestionIndex by remember { mutableIntStateOf(0) }
    var battleUserScore by remember { mutableIntStateOf(0) }
    var battleBotScore by remember { mutableIntStateOf(0) }
    var battleSelectedOption by remember { mutableStateOf<Int?>(null) }
    var battleSecondsLeft by remember { mutableIntStateOf(15) }
    var battleFinished by remember { mutableStateOf(false) }

    val battleQuestionsPool = remember {
        listOf(
            SpeedBattleQuestion(
                id = 1,
                subject = "Physics",
                question = "In an adiabatic process for an ideal gas (γ = 1.4), if pressure P ∝ T^c, then the value of c is:",
                options = listOf("5 / 2", "7 / 2", "5 / 7", "7 / 5"),
                correctIndex = 1,
                quickFormula = "P^(1-γ) T^γ = const ⇒ P ∝ T^(γ / (γ - 1)) = T^(1.4 / 0.4) = T^(7/2)"
            ),
            SpeedBattleQuestion(
                id = 2,
                subject = "Chemistry",
                question = "Which species among the following has the highest bond order and is diamagnetic?",
                options = listOf("O₂⁺", "CN⁻", "NO", "O₂⁻"),
                correctIndex = 1,
                quickFormula = "CN⁻ has 14 electrons (isoelectronic with N₂ & CO) → Bond Order = 3.0 & Diamagnetic"
            ),
            SpeedBattleQuestion(
                id = 3,
                subject = "Maths",
                question = "If A is a 3×3 non-singular matrix such that |A| = 4, then the value of |adj(adj A)| is:",
                options = listOf("16", "64", "256", "1024"),
                correctIndex = 2,
                quickFormula = "|adj(adj A)| = |A|^((n-1)²) = 4^((3-1)²) = 4⁴ = 256"
            ),
            SpeedBattleQuestion(
                id = 4,
                subject = "Physics",
                question = "Ratio of de-Broglie wavelength of a proton and an α-particle accelerated through the same potential difference V is:",
                options = listOf("2√2 : 1", "√2 : 1", "4 : 1", "2 : 1"),
                correctIndex = 0,
                quickFormula = "λ = h / √(2mqV) ⇒ λ_p / λ_α = √(m_α q_α / m_p q_p) = √(4 × 2) = 2√2 : 1"
            ),
            SpeedBattleQuestion(
                id = 5,
                subject = "Chemistry",
                question = "Which compound gives a positive Iodoform test (CHI₃ yellow precipitate) with I₂ / NaOH?",
                options = listOf("Pentan-3-one", "Benzophenone", "Ethanal (Acetaldehyde)", "Methanol"),
                correctIndex = 2,
                quickFormula = "Requires CH₃–C(=O)– or CH₃–CH(OH)– group. Ethanal (CH₃CHO) is the only aldehyde giving positive haloform!"
            )
        )
    }

    val activeBattleQuestions = remember(battleSubjectFilter) {
        if (battleSubjectFilter == "All") battleQuestionsPool
        else battleQuestionsPool.filter { it.subject.equals(battleSubjectFilter, ignoreCase = true) }.ifEmpty { battleQuestionsPool }
    }

    LaunchedEffect(isBattleActive, battleQuestionIndex, battleFinished) {
        if (isBattleActive && !battleFinished) {
            battleSecondsLeft = 15
            while (battleSecondsLeft > 0 && battleSelectedOption == null) {
                delay(1000)
                battleSecondsLeft--
            }
            if (battleSelectedOption == null && isBattleActive && !battleFinished) {
                // Time up on this question: opponent scores +4
                battleBotScore += 4
                battleSelectedOption = -1
            }
        }
    }

    val podChatMessages = remember {
        mutableStateListOf(
            "Aarav (99.6%ile): Current 50m Sprint Goal → Complete 25 Rotational Mechanics PYQs!",
            "Ananya (AIIMS Target): Revising Chemical Bonding hybridization & MOT bond order.",
            "Rohan (AIR 412): Deep focus mode ON. Drop your sprint goal below!"
        )
    }

    val posts = remember {
        mutableStateListOf(
            CampusPost(
                id = 1,
                authorName = "Aarav Sharma",
                authorBadge = "IITB Target • 99.6%ile",
                avatarColor = 0xFF0070F3,
                timeAgo = "12m ago",
                tag = "Physics",
                title = "Fastest shortcut for Moment of Inertia of a truncated cone (frustum)?",
                body = "Whenever a variable radius integration comes up in Rotational Mechanics, setting up limits takes 4+ minutes. What's the cleanest direct ratio formula?",
                verifiedSolution = "Verified Topper Trick: Use I = (3/10)M · (R₂⁵ - R₁⁵) / (R₂³ - R₁³). Quick check: as R₁ → 0 it becomes (3/10)MR₂² (solid cone), and as R₁ → R₂ it becomes (1/2)MR² (cylinder)!",
                upvotes = 84,
                commentsCount = 3,
                comments = listOf(
                    "Rohan (AIR 184): Checking the limiting case R₁ → 0 and R₁ → R₂ eliminates 3 options in 5 seconds in CBT!",
                    "Sneha: Saved this straight to my Rotational Flashcard deck.",
                    "Karan: Works for Center of Mass of frustum too: h/4 · (R₁² + 2R₁R₂ + 3R₂²) / (R₁² + R₁R₂ + R₂²)."
                )
            ),
            CampusPost(
                id = 2,
                authorName = "Ananya Verma",
                authorBadge = "99.4%ile • 21d Streak",
                avatarColor = 0xFF10B981,
                timeAgo = "45m ago",
                tag = "Chemistry",
                title = "All 12 Inorganic Orders asked repeatedly in 2024–2025 Shift Papers",
                body = "Compiled every single exception from Periodicity, Chemical Bonding & Coordination Compounds: IE₁ & IE₂ of 2nd period, bond angles of NH₃/PH₃/AsH₃, and thermal stability of Group 2 carbonates.",
                verifiedSolution = "Key Takeaway: 2nd Period IE₁: Li < B < Be < C < O < N < F < Ne. For IE₂: Li < Be < B < C < N < O < F (O⁺ has stable 2p³ half-filled shell so IE₂ of O > N!).",
                upvotes = 142,
                commentsCount = 2,
                isBookmarked = true,
                comments = listOf(
                    "Vikram: IE₂ of O > N and Na >> Mg came in 3 shifts last January!",
                    "Meera: Super clean summary, added to my morning revision stack."
                )
            ),
            CampusPost(
                id = 3,
                authorName = "Devansh Patel",
                authorBadge = "Maths 99.8%ile",
                avatarColor = 0xFF8B5CF6,
                timeAgo = "2h ago",
                tag = "Maths",
                title = "How to solve 80% of Definite Integration PYQs in under 90 seconds",
                body = "Whenever limits are symmetric like [0, π/2] or [a, b], always test King's Property f(a+b-x) first, then Queen's Property f(2a-x). If the integrand has sin x and cos x powers, apply Walli's formula directly.",
                verifiedSolution = "Golden Formula: ∫_a^b f(x) / [f(x) + f(a+b-x)] dx = (b - a) / 2. Saves 3 full minutes in CBT!",
                upvotes = 119,
                commentsCount = 2,
                comments = listOf(
                    "Priya: Used this exact property in today's Full Syllabus Mock Test!",
                    "Harsh: Pair this with Leibniz rule for variable limit derivatives and Calculus is sorted."
                )
            )
        )
    }

    val studyRooms = remember {
        mutableStateListOf(
            StudyPodRoom(
                id = 1,
                roomName = "50/10 Pomodoro • Rotational & Electrostatics Sprint",
                subject = "Physics & Calculus",
                hostName = "Aarav (99.6%ile)",
                activeMembers = 18,
                maxMembers = 25,
                currentSprintLabel = "Silent Deep Focus • Live Timer Synced",
                accentColor = 0xFF0070F3,
                activePeersPreview = listOf(
                    "🔥 Aarav • 4h 20m today • Solving 2024 Shift PYQs",
                    "⚡ Sneha • 3h 45m today • Capacitors & Gauss Law",
                    "🎯 You • Synced with Pod Pomodoro Timer"
                ),
                isJoined = true
            ),
            StudyPodRoom(
                id = 2,
                roomName = "NCERT Line-by-Line Inorganic & Organic Mastery Pod",
                subject = "Chemistry & Biology",
                hostName = "Dr. Ishita & Ananya",
                activeMembers = 24,
                maxMembers = 30,
                currentSprintLabel = "45m Active Recall • 120 Reactions Target",
                accentColor = 0xFF10B981,
                activePeersPreview = listOf(
                    "🔥 Ananya • 5h 10m today • Coordination Compounds",
                    "⚡ Kabir • 3h 15m today • GOC Acidity Orders"
                ),
                isJoined = false
            ),
            StudyPodRoom(
                id = 3,
                roomName = "Late Night 75-Question Full Mock & Error Analysis Room",
                subject = "All-India CBT Arena",
                hostName = "Rohan (IITB CSE)",
                activeMembers = 21,
                maxMembers = 25,
                currentSprintLabel = "3-Hour Mock Simulation • Zero Distractions",
                accentColor = 0xFF8B5CF6,
                activePeersPreview = listOf(
                    "🔥 Rohan • 6h 00m today • 3D Geometry & Vectors",
                    "⚡ Devansh • 4h 50m today • Mistake Notebook Drill"
                ),
                isJoined = false
            )
        )
    }

    val mentors = remember {
        mutableStateListOf(
            PeerMentorProfile(
                id = 1,
                name = "Rohan Kulkarni",
                institute = "IIT Bombay • CSE",
                rankBadge = "JEE Adv AIR 184",
                specialty = "Physics Problem Solving, 3D Calculus & 3-Hour CBT Temperament",
                rating = "4.9 ★",
                sessionsHelped = 340,
                playbookSchedule = listOf(
                    "Round 1 (First 45 mins in CBT): Finish entire Chemistry + 10 easiest Physics formula questions to lock 90+ marks early.",
                    "Round 2 (Next 75 mins): Solve moderate Physics & high-weightage Maths (Vectors, 3D, Matrices, Definite Integration).",
                    "Mistake Rule: Never spend >3.5 minutes on an ego question in Round 1. Mark for Review and move immediately!"
                ),
                goldenAdvice = "Solving 30 quality shift PYQs with a timer every night beats watching 6 hours of passive theory lectures.",
                isConnected = true
            ),
            PeerMentorProfile(
                id = 2,
                name = "Dr. Ishita Nair",
                institute = "AIIMS New Delhi",
                rankBadge = "AIR 92 • 99.9%ile",
                specialty = "NCERT Line-by-Line Retention, Organic Mechanisms & Zero-Negative Marking",
                rating = "5.0 ★",
                sessionsHelped = 285,
                playbookSchedule = listOf(
                    "Morning 45m Active Recall: Revise 1 Inorganic exception sheet + 15 Organic named reactions before touching your phone.",
                    "NCERT Highlighter Code: Yellow = Direct definition, Pink = Exception/Graph trap, Green = PYQ repeated line.",
                    "Error Vault: Every wrong mock question must go into your Mistake Notebook with a 1-line fix."
                ),
                goldenAdvice = "In Chemistry & Biology, 92% of questions come directly from NCERT tables, graphs, and in-text exceptions.",
                isConnected = false
            ),
            PeerMentorProfile(
                id = 3,
                name = "Aditya Verma",
                institute = "NIT Trichy • CSE",
                rankBadge = "99.84%ile • Maths 100%ile",
                specialty = "Coordinate Geometry, Calculus Shortcuts & 60-Day Score Jump Strategy",
                rating = "4.9 ★",
                sessionsHelped = 210,
                playbookSchedule = listOf(
                    "80/20 Priority Chapters: Master 3D Geometry, Vectors, Matrices, Sequence & Series, and Definite Calculus first (44+ marks).",
                    "Option Elimination: Use boundary values (x = 0, 1, π/4), symmetry, and dimensional checks on tough integrals.",
                    "Daily Target: 25 timed Maths PYQs every afternoon between 2 PM – 5 PM to match exam biological clock."
                ),
                goldenAdvice = "Don't just read Maths solutions—re-solve every wrong PYQ from scratch without looking at the hint 24 hours later.",
                isConnected = false
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("campus_hub_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. CLEAN, HIGH-SIGNAL TOP 1% EDTECH CAMPUS HEADER (Zero Clutter)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campus_hub_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E3A8A),
                                    Color(0xFF0070F3)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "63 PEERS STUDYING LIVE",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF6EE7B7)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "TOP 1% ARENA",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                                    modifier = Modifier
                                        .clickable { onNavigate(JeePage.CONSUMER_UTILITY) }
                                        .testTag("campus_open_student_os_pill")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Student OS",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(
                                            AnnotatedString("Join our live Pomodoro Study Pod & 1v1 Quiz Arena on UpgradeHub: https://jeeprephub.co.in/campus")
                                        )
                                        viewModel.showSnackbar("Study Pod invite link copied!")
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share Campus Invite",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Campus Peer Arena & Focus Pods",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Verified topper doubt solutions, YPT-style real-time Pomodoro focus pods, 60-second 1v1 speed quiz duels & IIT/AIIMS playbooks.",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 2. 4 LASER-FOCUSED, INTERACTIVE CAMPUS TABS
        item {
            val tabs = listOf(
                Triple(0, "Doubt Wall", Icons.Default.Forum),
                Triple(1, "Study Pods", Icons.Default.Groups),
                Triple(2, "1v1 Quiz Duel", Icons.Default.Bolt),
                Triple(3, "Topper Mentors", Icons.Default.School),
                Triple(4, "Faculty Uploads", Icons.Default.Description)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { (idx, title, icon) ->
                    val isSelected = selectedTab == idx
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .clickable { selectedTab = idx }
                            .testTag("campus_tab_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. TAB CONTENT
        when (selectedTab) {
            0 -> {
                // TAB 0: PEER DOUBT SOLVER & TOPPER SHORTCUT WALL
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search doubts, formulas, PYQ tricks...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { showCreatePostDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("campus_new_post_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ask Doubt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                item {
                    val tags = listOf("All", "Physics", "Chemistry", "Maths", "Topper Notes")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { tag ->
                            FilterChip(
                                selected = selectedTagFilter == tag,
                                onClick = { selectedTagFilter = tag },
                                label = { Text(tag, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0070F3),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                val filteredPosts = posts.filter { post ->
                    (selectedTagFilter == "All" || post.tag.equals(selectedTagFilter, ignoreCase = true)) &&
                        (searchQuery.isBlank() ||
                            post.title.contains(searchQuery, ignoreCase = true) ||
                            post.body.contains(searchQuery, ignoreCase = true) ||
                            (post.verifiedSolution?.contains(searchQuery, ignoreCase = true) == true))
                }

                items(filteredPosts, key = { it.id }) { post ->
                    CampusPostCard(
                        post = post,
                        isCommentDrawerOpen = activeCommentPostId == post.id,
                        commentDraft = if (activeCommentPostId == post.id) commentDraft else "",
                        onCommentDraftChange = { commentDraft = it },
                        onToggleUpvote = {
                            val idx = posts.indexOfFirst { it.id == post.id }
                            if (idx != -1) {
                                val current = posts[idx]
                                val nowUpvoted = !current.isUpvoted
                                posts[idx] = current.copy(
                                    isUpvoted = nowUpvoted,
                                    upvotes = if (nowUpvoted) current.upvotes + 1 else current.upvotes - 1
                                )
                            }
                        },
                        onToggleBookmark = {
                            val idx = posts.indexOfFirst { it.id == post.id }
                            if (idx != -1) {
                                val current = posts[idx]
                                posts[idx] = current.copy(isBookmarked = !current.isBookmarked)
                                viewModel.showSnackbar(
                                    if (!current.isBookmarked) "Saved to Revision Bookmarks" else "Removed from Bookmarks"
                                )
                            }
                        },
                        onToggleComments = {
                            activeCommentPostId = if (activeCommentPostId == post.id) null else post.id
                            commentDraft = ""
                        },
                        onSubmitComment = {
                            if (commentDraft.isNotBlank()) {
                                val idx = posts.indexOfFirst { it.id == post.id }
                                if (idx != -1) {
                                    val current = posts[idx]
                                    posts[idx] = current.copy(
                                        commentsCount = current.commentsCount + 1,
                                        comments = current.comments + "You: ${commentDraft.trim()}"
                                    )
                                    commentDraft = ""
                                    viewModel.boostUserScore(points = 10)
                                    viewModel.showSnackbar("Solution reply posted in the local preview. No server XP was credited.")
                                }
                            }
                        }
                    )
                }
            }

            1 -> {
                // TAB 1: YPT-STYLE LIVE POMODORO STUDY PODS
                item {
                    val fMins = focusRemainingSeconds / 60
                    val fSecs = focusRemainingSeconds % 60
                    val fTimeStr = String.format("%02d:%02d", fMins, fSecs)

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SYNCED POMODORO FOCUS TIMER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = fTimeStr,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isFocusTimerRunning) "Studying live with your joined Pod" else "Start timer to log live Pod hours",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.toggleFocusTimer() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFocusTimerRunning) Color(0xFFF59E0B) else Color(0xFF10B981)
                                    )
                                ) {
                                    Text(
                                        text = if (isFocusTimerRunning) "Pause" else "Start 50m",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showCreateRoomDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Host Pod", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                items(studyRooms, key = { it.id }) { room ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(room.accentColor).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = room.subject,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(room.accentColor),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = room.currentSprintLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(room.roomName, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text(
                                "Host: ${room.hostName} • ${room.activeMembers}/${room.maxMembers} studying live",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (room.activePeersPreview.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        room.activePeersPreview.forEach { peerLine ->
                                            Text(
                                                text = peerLine,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { (room.activeMembers.toFloat() / room.maxMembers.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(room.accentColor),
                                trackColor = Color(room.accentColor).copy(alpha = 0.15f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val idx = studyRooms.indexOfFirst { it.id == room.id }
                                        if (idx != -1 && !studyRooms[idx].isJoined) {
                                            studyRooms[idx] = studyRooms[idx].copy(
                                                isJoined = true,
                                                activeMembers = studyRooms[idx].activeMembers + 1
                                            )
                                        }
                                        activeLivePodRoom = studyRooms.firstOrNull { it.id == room.id } ?: room
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Pod Desk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val idx = studyRooms.indexOfFirst { it.id == room.id }
                                        if (idx != -1) {
                                            val current = studyRooms[idx]
                                            val joining = !current.isJoined
                                            val updated = current.copy(
                                                isJoined = joining,
                                                activeMembers = if (joining) current.activeMembers + 1 else current.activeMembers - 1
                                            )
                                            studyRooms[idx] = updated
                                            if (joining) {
                                                activeLivePodRoom = updated
                                            }
                                            viewModel.showSnackbar(
                                                if (joining) "Joined '${current.roomName}'!" else "Left '${current.roomName}'."
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (room.isJoined) MaterialTheme.colorScheme.surfaceVariant else Color(room.accentColor),
                                        contentColor = if (room.isJoined) MaterialTheme.colorScheme.onSurface else Color.White
                                    )
                                ) {
                                    Icon(
                                        if (room.isJoined) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (room.isJoined) "Synced ✓" else "Join & Sync",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: LIVE 60-SECOND 1v1 SPEED QUIZ BATTLE (Addictive Play Store Top-1% Feature)
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, Color(0xFF0070F3).copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("campus_speed_quiz_battle_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0070F3).copy(alpha = 0.14f)
                                    ) {
                                        Icon(
                                            Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color(0xFF0070F3),
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "1v1 RAPID FORMULA & PYQ DUEL",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0070F3)
                                        )
                                        Text(
                                            text = "Beat AIR #184 Challenger Bot in 15s/Question (+4 / -1)",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "DEMO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!isBattleActive) {
                                // Subject Filter + Launch Duel Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("All", "Physics", "Chemistry", "Maths").forEach { subj ->
                                        val selected = battleSubjectFilter == subj
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (selected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { battleSubjectFilter = subj }
                                        ) {
                                            Text(
                                                text = subj,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        battleQuestionIndex = 0
                                        battleUserScore = 0
                                        battleBotScore = 0
                                        battleSelectedOption = null
                                        battleFinished = false
                                        isBattleActive = true
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_speed_battle_btn")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Start Live 1v1 Speed Battle Now", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                }
                            } else if (battleFinished) {
                                // Battle Result Summary
                                val userWon = battleUserScore >= battleBotScore
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (userWon) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFF59E0B).copy(alpha = 0.12f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (userWon) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (userWon) "🏆 VICTORY! YOU OUTSCORED THE TOPPER BOT!" else "⚡ CLOSE MATCH! KEEP DRILLING FORMULAS!",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (userWon) Color(0xFF059669) else Color(0xFFD97706)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("YOU", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "$battleUserScore pts",
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF0070F3)
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("ROHAN (99.6%ile)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "$battleBotScore pts",
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF8B5CF6)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    battleQuestionIndex = 0
                                                    battleUserScore = 0
                                                    battleBotScore = 0
                                                    battleSelectedOption = null
                                                    battleFinished = false
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3))
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Rematch", fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = { isBattleActive = false },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("Done")
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Active Live Battle Question
                                val currentQ = activeBattleQuestions.getOrNull(battleQuestionIndex) ?: activeBattleQuestions.first()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0070F3).copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "You: $battleUserScore pts",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0070F3),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (battleSecondsLeft <= 5) Color(0xFFEF4444).copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "⏱ ${battleSecondsLeft}s • Q${battleQuestionIndex + 1}/${activeBattleQuestions.size}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (battleSecondsLeft <= 5) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF8B5CF6).copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "Rohan: $battleBotScore pts",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF8B5CF6),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "[${currentQ.subject}] ${currentQ.question}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                currentQ.options.forEachIndexed { idx, optText ->
                                    val isAnswered = battleSelectedOption != null
                                    val isCorrect = idx == currentQ.correctIndex
                                    val isChosen = battleSelectedOption == idx

                                    val bg = when {
                                        !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        isCorrect -> Color(0xFF10B981).copy(alpha = 0.18f)
                                        isChosen -> Color(0xFFEF4444).copy(alpha = 0.18f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                    }
                                    val borderCol = when {
                                        !isAnswered -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        isCorrect -> Color(0xFF10B981)
                                        isChosen -> Color(0xFFEF4444)
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = bg,
                                        border = BorderStroke(1.dp, borderCol),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable(enabled = !isAnswered) {
                                                battleSelectedOption = idx
                                                if (idx == currentQ.correctIndex) {
                                                    battleUserScore += 4
                                                } else {
                                                    battleUserScore -= 1
                                                }
                                                // Challenger bot scores +4 on 3 out of 5 questions
                                                if (battleQuestionIndex % 2 == 0) {
                                                    battleBotScore += 4
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${('A' + idx)}.",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0070F3)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = optText,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = battleSelectedOption != null) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF0070F3).copy(alpha = 0.08f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "💡 Shortcut: ${currentQ.quickFormula}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0070F3),
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                if (battleQuestionIndex < activeBattleQuestions.size - 1) {
                                                    battleQuestionIndex++
                                                    battleSelectedOption = null
                                                } else {
                                                    battleFinished = true
                                                    viewModel.boostUserScore(points = 25, problems = activeBattleQuestions.size)
                                                    viewModel.showSnackbar("🏆 1v1 speed-battle preview complete. No server XP was credited.")
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (battleQuestionIndex < activeBattleQuestions.size - 1) "Next Duel Question →" else "See Final Duel Result",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Direct Shortcut to Full 3-Hour NTA CBT Arena & Leaderboard
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate(JeePage.TESTS) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Full NTA CBT Mocks", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onNavigate(JeePage.LEADERBOARD) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All-India Rankers", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            3 -> {
                // TAB 3: VERIFIED IIT / AIIMS TOPPER MENTORS & PLAYBOOKS
                item {
                    Text("Verified IIT / AIIMS / NIT Topper Mentors", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(
                        "Tap 'View Playbook' to read each ranker's exact CBT round strategy & daily revision routine",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(mentors, key = { it.id }) { mentor ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0070F3).copy(alpha = 0.16f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = mentor.name.take(2).uppercase(),
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF0070F3)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(mentor.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF0070F3),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            "${mentor.institute} • ${mentor.rankBadge}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = mentor.rating,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Specialty: ${mentor.specialty}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "“${mentor.goldenAdvice}”",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedMentorPlaybook = mentor },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View Playbook", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        selectedTab = 0
                                        showCreatePostDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ask Mentor", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // TAB 4: FACULTY & ADMIN STUDY MATERIAL REPOSITORY
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0070F3).copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Faculty Materials & DPP Sheets", fontSize = 13.5.sp, fontWeight = FontWeight.Black)
                                        Text("Official problem banks, derivations & notes from teachers", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "Physics", "Chemistry", "Mathematics", "Multi-Subject").forEach { sub ->
                                    FilterChip(
                                        selected = materialSubjectFilter == sub,
                                        onClick = { materialSubjectFilter = sub },
                                        label = { Text(sub, fontSize = 9.5.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                items(filteredFacultyMaterials, key = { it.id }) { mat ->
                    val subjectColor = when (mat.subject) {
                        "Physics" -> Color(0xFF0070F3)
                        "Chemistry" -> Color(0xFF10B981)
                        "Mathematics" -> Color(0xFF8B5CF6)
                        else -> Color(0xFFF59E0B)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = subjectColor.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "${mat.subject} • ${mat.materialType}",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = subjectColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = mat.targetBatch,
                                            fontSize = 8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = mat.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = mat.description, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "By ${mat.uploaderName} • ${mat.fileSizeMb} MB",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        viewModel.incrementMaterialDownload(mat.id)
                                        viewModel.showSnackbar("📥 Downloaded '${mat.title}'! Added to offline study vault.")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download (${mat.downloadCount})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Mentor Strategy Playbook Dialog
    selectedMentorPlaybook?.let { mentor ->
        AlertDialog(
            onDismissRequest = { selectedMentorPlaybook = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Column {
                    Text(
                        text = "${mentor.name}'s Topper Playbook",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${mentor.institute} • ${mentor.rankBadge}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    mentor.playbookSchedule.forEach { step ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedMentorPlaybook = null },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3))
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Create Doubt/Note Dialog
    if (showCreatePostDialog) {
        var postTitle by remember { mutableStateOf("") }
        var postBody by remember { mutableStateOf("") }
        var postTag by remember { mutableStateOf("Physics") }
        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            title = { Text("Ask a Doubt or Share Topper Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Physics", "Chemistry", "Maths", "Topper Notes").forEach { t ->
                            val sel = postTag == t
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (sel) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { postTag = t }
                            ) {
                                Text(
                                    text = t,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Question / Topic Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = postBody,
                        onValueChange = { postBody = it },
                        label = { Text("Explain your doubt or formula shortcut") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank()) {
                            posts.add(
                                0,
                                CampusPost(
                                    id = posts.size + 10,
                                    authorName = viewModel.currentUser.value?.fullName ?: "You",
                                    authorBadge = "Active Aspirant",
                                    avatarColor = 0xFF0070F3,
                                    timeAgo = "Just now",
                                    tag = postTag,
                                    title = postTitle.trim(),
                                    body = postBody.trim().ifBlank { "Posted to Doubt Wall." },
                                    verifiedSolution = null,
                                    upvotes = 1,
                                    commentsCount = 0,
                                    isUpvoted = true
                                )
                            )
                            showCreatePostDialog = false
                            viewModel.boostUserScore(points = 10)
                            viewModel.showSnackbar("Posted to the local Campus Doubt Wall preview. No server XP was credited.")
                        }
                    }
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Study Room Dialog
    if (showCreateRoomDialog) {
        var roomTitle by remember { mutableStateOf("") }
        var roomSubject by remember { mutableStateOf("Physics & Maths PYQs") }
        AlertDialog(
            onDismissRequest = { showCreateRoomDialog = false },
            title = { Text("Host a Live Pomodoro Pod", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roomTitle,
                        onValueChange = { roomTitle = it },
                        label = { Text("Pod Name") },
                        placeholder = { Text("e.g., 50/10 Pomodoro • Calculus Sprint") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = roomSubject,
                        onValueChange = { roomSubject = it },
                        label = { Text("Subject / Goal") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (roomTitle.isNotBlank()) {
                            studyRooms.add(
                                0,
                                StudyPodRoom(
                                    id = studyRooms.size + 10,
                                    roomName = roomTitle.trim(),
                                    subject = roomSubject.trim(),
                                    hostName = "You",
                                    activeMembers = 1,
                                    maxMembers = 25,
                                    currentSprintLabel = "50/10 Pomodoro • Live Now",
                                    accentColor = 0xFF10B981,
                                    activePeersPreview = listOf("🎯 You (Host) • Sprint started just now"),
                                    isJoined = true
                                )
                            )
                            showCreateRoomDialog = false
                            viewModel.showSnackbar("Live Study Pod launched!")
                        }
                    }
                ) {
                    Text("Launch Pod")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Interactive Live Study Pod Room Dialog (Group Timer + Active Peers + Sprint Goal Chat)
    activeLivePodRoom?.let { pod ->
        val pMins = focusRemainingSeconds / 60
        val pSecs = focusRemainingSeconds % 60
        val pTimeStr = String.format("%02d:%02d", pMins, pSecs)

        AlertDialog(
            onDismissRequest = { activeLivePodRoom = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "🟢 LIVE POD • ${pod.activeMembers} STUDYING NOW",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = pod.subject,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(pod.accentColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = pod.roomName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "GROUP SPRINT TIMER",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = pTimeStr,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = { viewModel.toggleFocusTimer() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFocusTimerRunning) Color(0xFFF59E0B) else Color(0xFF10B981)
                                )
                            ) {
                                Text(
                                    text = if (isFocusTimerRunning) "Pause Sync" else "Start Sprint",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "ACTIVE POD MEMBERS & SPRINT GOALS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        podChatMessages.takeLast(4).forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = podChatInput,
                        onValueChange = { podChatInput = it },
                        placeholder = { Text("Share your 50m sprint goal with the pod...", fontSize = 11.5.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (podChatInput.isNotBlank()) {
                            podChatMessages.add("You: ${podChatInput.trim()}")
                            podChatInput = ""
                            viewModel.boostUserScore(points = 10)
                            viewModel.showSnackbar("🎯 Sprint goal shared in the local pod preview. No server XP was credited.")
                        } else {
                            if (!isFocusTimerRunning) viewModel.toggleFocusTimer()
                            activeLivePodRoom = null
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3))
                ) {
                    Text(
                        text = if (podChatInput.isNotBlank()) "Post Sprint Goal" else "Keep Studying",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { activeLivePodRoom = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun CampusPostCard(
    post: CampusPost,
    isCommentDrawerOpen: Boolean,
    commentDraft: String,
    onCommentDraftChange: (String) -> Unit,
    onToggleUpvote: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleComments: () -> Unit,
    onSubmitComment: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(post.avatarColor),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = post.authorName.take(2).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(post.authorName, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
                        Text(
                            "${post.authorBadge} • ${post.timeAgo}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0070F3).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = post.tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(post.title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                post.body,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            post.verifiedSolution?.let { sol ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "VERIFIED TOPPER SOLUTION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF059669)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sol,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (post.isUpvoted) Color(0xFF0070F3).copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.clickable { onToggleUpvote() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (post.isUpvoted) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                contentDescription = "Upvote",
                                tint = if (post.isUpvoted) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "${post.upvotes}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (post.isUpvoted) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier.clickable { onToggleComments() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                "${post.commentsCount} Replies",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        if (post.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (post.isBookmarked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isCommentDrawerOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    post.comments.forEach { comment ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = comment,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(10.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = onCommentDraftChange,
                            placeholder = { Text("Write a solution or reply...", fontSize = 11.5.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onSubmitComment,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF0070F3))
                                .size(36.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Reply",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

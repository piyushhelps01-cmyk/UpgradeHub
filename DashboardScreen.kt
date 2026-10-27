package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.core.content.ContextCompat
import com.example.ui.components.StudySlotAlarmConfig
import com.example.work.StudySlotWorkScheduler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailySlot
import com.example.ui.components.BatchAutoPlannerDialog
import com.example.ui.components.BatchUrlImportSyncCard
import com.example.ui.components.DailyHabitStreakCard
import com.example.ui.components.DailyMotivationalQuoteCard
import com.example.ui.components.DashboardChapterProgressListCard
import com.example.ui.components.DashboardPdfExportImportCard
import com.example.ui.components.ScheduledStudySlotsEmergencyDrillSection
import com.example.ui.components.StudySlotAlarmManager
import com.example.ui.components.StudySlotTimeCustomizerDialog
import com.example.ui.components.WeeklyStudyHoursRechartsCard
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val version by viewModel.version.collectAsState()
    val allChapters by viewModel.activeChapters.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val habitStreakData by viewModel.habitStreakData.collectAsState()
    val sevenDayTrendData by viewModel.sevenDayTrendData.collectAsState()
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val batchCurriculums by viewModel.batchCurriculums.collectAsState()
    val parallelBatchIds by viewModel.parallelBatchIds.collectAsState()
    val parallelBatches by viewModel.parallelBatches.collectAsState()
    val batchTargetDays by viewModel.batchTargetDays.collectAsState()
    val batchDailyHours by viewModel.batchDailyHours.collectAsState()
    val batchUrlInput by viewModel.batchUrlInput.collectAsState()
    val isAutoPlanning by viewModel.isAutoPlanning.collectAsState()
    val lastAutoPlanResult by viewModel.lastAutoPlanResult.collectAsState()
    val isPdfExporting by viewModel.isPdfExporting.collectAsState()
    val lastExportedPdf by viewModel.lastExportedPdf.collectAsState()
    val currentDailyGoals by viewModel.currentDailyGoals.collectAsState()
    val progression by viewModel.curriculumProgression.collectAsState()
    val dailyQuote by viewModel.dailyQuote.collectAsState()
    val isQuoteBookmarked by viewModel.isQuoteBookmarked.collectAsState()
    val isQuoteRefreshing by viewModel.isQuoteRefreshing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isAutoPlannerDialogOpen by remember { mutableStateOf(false) }
    // Hidden-by-default expandable bottom sheet state for Batch Sync URL input
    var isBatchSyncSheetExpanded by rememberSaveable { mutableStateOf(false) }

    val studentName = currentUser?.fullName?.ifBlank { "Ayaan" } ?: "Ayaan"
    val dayIndex = (uiState.selectedDay - 1).coerceAtLeast(0)

    BackHandler(enabled = isBatchSyncSheetExpanded) {
        isBatchSyncSheetExpanded = false
    }

    val quickSavePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.savePdfToLocalUri(uri)
        }
    }

    val completedTodayCount = currentDailyGoals.count { it.isCompleted }
    val totalTodayCount = currentDailyGoals.size.coerceAtLeast(4)
    val todayCompletionRatio = (completedTodayCount.toFloat() / totalTodayCount.toFloat()).coerceIn(0f, 1f)

    // Top-level Scaffold layout on the main screen:
    // - Dedicated Top Header Card: Motivational Quote of the Day + compact student status bar
    // - Center Priority Content: Daily Schedule (4 Time-Block Study Slots + Daily Goals checklist)
    // - Hidden-by-Default Expandable Bottom Sheet: Batch Sync (PW, Unacademy, KGS URL input)
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BatchSyncExpandableBottomSheet(
                isExpanded = isBatchSyncSheetExpanded,
                onToggleExpanded = { isBatchSyncSheetExpanded = !isBatchSyncSheetExpanded },
                onCollapse = { isBatchSyncSheetExpanded = false },
                urlInput = batchUrlInput,
                selectedBatch = selectedBatch,
                parallelBatches = parallelBatches,
                targetDays = batchTargetDays,
                dailyHours = batchDailyHours,
                isPlanning = isAutoPlanning,
                onUrlChange = { viewModel.setBatchUrlInput(it) },
                onSyncUrl = { url ->
                    if (url.isBlank()) {
                        isAutoPlannerDialogOpen = true
                    } else {
                        viewModel.parseAndApplyBatchUrl(url)
                    }
                },
                onOpenFullPlannerDialog = { isAutoPlannerDialogOpen = true }
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // =========================================================================
                // 1. DEDICATED TOP HEADER CARD: Motivational Quote of the Day + Sprint Strip
                // =========================================================================
                item {
                    DedicatedMotivationalTopHeaderCard(
                        studentName = studentName,
                        selectedDay = uiState.selectedDay,
                        totalDays = progression.totalDays,
                        batchTitle = selectedBatch.title,
                        completedTodayCount = completedTodayCount,
                        totalTodayCount = totalTodayCount,
                        todayCompletionRatio = todayCompletionRatio,
                        averageHoursPerDay = sevenDayTrendData.consistencySummary.averageHoursPerDay,
                        dailyQuote = dailyQuote,
                        isQuoteBookmarked = isQuoteBookmarked,
                        isQuoteRefreshing = isQuoteRefreshing,
                        onRefreshQuote = { viewModel.fetchNextQuote() },
                        onBookmarkQuote = { viewModel.toggleBookmarkQuote() },
                        onCopyQuote = { viewModel.copyQuoteToClipboard(context) },
                        onOpenFocusTimer = { viewModel.setPage(JeePage.STREAK) },
                        onOpenAiMentor = { viewModel.setPage(JeePage.AI) },
                        onOpenBatchSyncSheet = { isBatchSyncSheetExpanded = true },
                        onOpenPlanner = { viewModel.setPage(JeePage.DAILY_PLANNER) }
                    )
                }

                // =========================================================================
                // 2. CENTER PRIORITY: DAILY SCHEDULE (Time-Block Slots + Daily Goals)
                // =========================================================================
                item {
                    DailyScheduleCenterPriorityCard(
                        selectedDay = uiState.selectedDay,
                        totalDays = progression.totalDays,
                        dayIndex = dayIndex,
                        slots = uiState.todayCommandSlots,
                        dailyGoals = currentDailyGoals,
                        version = version,
                        viewModel = viewModel,
                        onSelectDay = { newDay -> viewModel.selectDay(newDay) },
                        onQuickExportPdf = {
                            val safeName = selectedBatch.title.replace(Regex("[^a-zA-Z0-9]"), "_").take(12)
                            quickSavePdfLauncher.launch("UpgradeHub_${safeName}_Day${uiState.selectedDay}.pdf")
                        },
                        onOpenBatchSyncSheet = { isBatchSyncSheetExpanded = true },
                        onOpenFullPlanner = { viewModel.setPage(JeePage.DAILY_PLANNER) }
                    )
                }

                // =========================================================================
                // 2B. CENTRAL STUDY INTELLIGENCE HUB (Apple-Style Connected Pipeline & Recommendations)
                // =========================================================================
                item {
                    com.example.ui.components.DashboardStudyIntelligenceCommandCard(
                        viewModel = viewModel,
                        onNavigate = { page -> viewModel.setPage(page) }
                    )
                }

                // =========================================================================
                // 3. QUICK COMMAND SEARCH & CORE STUDY MODULE DOCK
                // =========================================================================
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dashboard_search_bar"),
                            placeholder = {
                                Text(
                                    text = "Search chapters, formulas, 2025 PYQs, mock tests...",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0070F3),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            singleLine = true
                        )

                        if (searchQuery.isNotBlank()) {
                            val quickMatches = listOf(
                                Triple("Classification of Elements & Periodicity Notes + Masterclass", "Prep Hub Vault • Chemistry", JeePage.BOOKMARKS),
                                Triple("Electrostatics, Gauss Law & Capacitor Formula PDF", "Prep Hub Vault • Physics", JeePage.BOOKMARKS),
                                Triple("2025 Shift-Wise PYQ Bank & Solutions", "Prep Hub Vault • PYQs", JeePage.BOOKMARKS),
                                Triple("Marks vs Percentile & NIT/IIT Rank Predictor", "Prep Hub Vault • Predictor", JeePage.BOOKMARKS),
                                Triple("Student Life OS: Tasks, Finance, Habits & Wellness", "Consumer Utilities • Eisenhower Matrix", JeePage.CONSUMER_UTILITY),
                                Triple("Memes Hub: Formula Memes & JEE Humour", "Study Break & Community Memes", JeePage.MEMES_HUB),
                                Triple("Full Syllabus & Unit CBT Mock Tests", "NTA Test Engine", JeePage.TESTS),
                                Triple("Master Chapter Tracker & 9-Step Chain", "Syllabus Tracker", JeePage.CHAPTERS),
                                Triple("Pomodoro Deep Focus Timer & Streak Freeze", "Streak & Focus Studio", JeePage.STREAK)
                            ).filter {
                                it.first.contains(searchQuery, ignoreCase = true) ||
                                    it.second.contains(searchQuery, ignoreCase = true)
                            }.ifEmpty {
                                listOf(
                                    Triple("Search \"$searchQuery\" in Prep Hub Vault Notes & PYQs", "Prep Hub Vault Resource Library", JeePage.BOOKMARKS),
                                    Triple("Ask AI Mentor about \"$searchQuery\"", "Step-by-step AI Doubt Solver", JeePage.AI)
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    quickMatches.forEach { (title, sub, targetPage) ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchQuery = ""
                                                    viewModel.setPage(targetPage)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        sub,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF0070F3),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0070F3),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Feature Dock Ribbon (Ordered by Daily Study Importance)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WorkspaceRibbonChip(
                                "Daily Plan",
                                "${batchTargetDays}d Schedule",
                                Icons.Default.DateRange,
                                Color(0xFF0070F3)
                            ) {
                                viewModel.setPage(JeePage.DAILY_PLANNER)
                            }
                            WorkspaceRibbonChip(
                                "Batch Sync",
                                "PW • Unacad • KGS",
                                Icons.Default.CloudSync,
                                Color(0xFF6366F1)
                            ) {
                                isBatchSyncSheetExpanded = true
                            }
                            WorkspaceRibbonChip(
                                "Chapters",
                                "9-Step Chain",
                                Icons.Default.CheckCircle,
                                Color(0xFFEC4899)
                            ) {
                                viewModel.setPage(JeePage.CHAPTERS)
                            }
                            WorkspaceRibbonChip(
                                "NCERT",
                                "Line-by-Line",
                                Icons.AutoMirrored.Filled.MenuBook,
                                Color(0xFFF59E0B)
                            ) {
                                viewModel.setPage(JeePage.NCERT)
                            }
                            WorkspaceRibbonChip(
                                "CBT Mocks",
                                "NTA Engine",
                                Icons.Default.Description,
                                Color(0xFF10B981)
                            ) {
                                viewModel.setPage(JeePage.TESTS)
                            }
                            WorkspaceRibbonChip(
                                "Practice",
                                "DPP & Cards",
                                Icons.Default.FitnessCenter,
                                Color(0xFF8B5CF6)
                            ) {
                                viewModel.setPage(JeePage.PRACTICE)
                            }
                            WorkspaceRibbonChip(
                                "Prep Vault",
                                "Notes & PYQs",
                                Icons.Default.Layers,
                                Color(0xFF0EA5E9)
                            ) {
                                viewModel.setPage(JeePage.BOOKMARKS)
                            }
                            WorkspaceRibbonChip(
                                "Memes Hub",
                                "Formula Memes",
                                Icons.Default.EmojiEmotions,
                                Color(0xFFF59E0B)
                            ) {
                                viewModel.setPage(JeePage.MEMES_HUB)
                            }
                            WorkspaceRibbonChip(
                                "Student OS",
                                "Tasks • Subs • Calm",
                                Icons.Default.AutoAwesome,
                                Color(0xFF10B981)
                            ) {
                                viewModel.setPage(JeePage.CONSUMER_UTILITY)
                            }
                        }
                    }
                }

                // =========================================================================
                // 3B. HIGH-PERFORMANCE STUDENT OS (Tasks, Finances, Habits, Wellness)
                // =========================================================================
                item {
                    StudentLifeOsDashboardCard(
                        viewModel = viewModel,
                        onOpenUtilityHub = { viewModel.setPage(JeePage.CONSUMER_UTILITY) }
                    )
                }

                // =========================================================================
                // 4. SUBJECT MASTERY SNAPSHOT (PHYSICS, CHEMISTRY, MATHS/BIOLOGY)
                // =========================================================================
                item {
                    val isNeetBatch = selectedBatch.examType.contains("NEET", ignoreCase = true) ||
                        allChapters.any { it.subject.contains("Bio", ignoreCase = true) }
                    val thirdSubjectLabel = if (isNeetBatch) "Biology" else "Maths"

                    fun calcSubjectStats(subjPrefix: String, fallbackPct: Int): Pair<Int, String> {
                        val matching = allChapters.filter { it.subject.contains(subjPrefix, ignoreCase = true) }
                        if (matching.isEmpty()) return fallbackPct to "Core Syllabus"
                        val totalSteps = (matching.size * 10).coerceAtLeast(1)
                        val doneSteps = matching.sumOf { viewModel.getChapterChain(it.id).completedCount() }
                        val computedPct = ((doneSteps.toFloat() / totalSteps.toFloat()) * 100f).toInt().coerceIn(12, 100)
                        val startedOrDoneChaps = matching.count { viewModel.getChapterChain(it.id).completedCount() >= 3 }
                        return computedPct to "$startedOrDoneChaps/${matching.size} Ch"
                    }

                    val (physPct, physSub) = remember(allChapters, version) { calcSubjectStats("Phys", 72) }
                    val (chemPct, chemSub) = remember(allChapters, version) { calcSubjectStats("Chem", 64) }
                    val (thirdPct, thirdSub) = remember(allChapters, version, thirdSubjectLabel) {
                        calcSubjectStats(if (isNeetBatch) "Bio" else "Math", 60)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubjectMasteryCompactCard(
                                subject = "Physics",
                                pct = physPct,
                                sub = physSub,
                                icon = Icons.Default.Bolt,
                                accent = Color(0xFF0070F3),
                                onClick = {
                                    viewModel.setSubjectFilter("Physics")
                                    viewModel.setPage(JeePage.CHAPTERS)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SubjectMasteryCompactCard(
                                subject = "Chemistry",
                                pct = chemPct,
                                sub = chemSub,
                                icon = Icons.Default.Science,
                                accent = Color(0xFF10B981),
                                onClick = {
                                    viewModel.setSubjectFilter("Chemistry")
                                    viewModel.setPage(JeePage.NCERT)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            SubjectMasteryCompactCard(
                                subject = thirdSubjectLabel,
                                pct = thirdPct,
                                sub = thirdSub,
                                icon = Icons.Default.Functions,
                                accent = Color(0xFF8B5CF6),
                                onClick = {
                                    viewModel.setSubjectFilter(thirdSubjectLabel)
                                    viewModel.setPage(JeePage.CHAPTERS)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Clean CTA to full Chapter Tracker without duplicating 88 chapter rows on Dashboard
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setPage(JeePage.CHAPTERS) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF0070F3),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open Master Chapter Tracker (${allChapters.size} Chapters)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // 5. CONSISTENCY & ANALYTICS SNAPSHOT (Clean Hub Linking to Dedicated Screens)
                // =========================================================================
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9500),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Consistency & Performance Hub",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFF9500).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${habitStreakData.currentStreak.coerceAtLeast(12)} DAYS STREAK",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Daily Pace", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${String.format(Locale.ENGLISH, "%.1f", sevenDayTrendData.consistencySummary.averageHoursPerDay)}h / day",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0070F3)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Today's Slots", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$completedTodayCount / $totalTodayCount Done",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Target Goal", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "AIR < 500",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF8B5CF6)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Direct links to specialized screens instead of duplicating them on Dashboard
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setPage(JeePage.STREAK) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("🔥 Streak Studio", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.setPage(JeePage.PROGRESS) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("📈 Full Analytics", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.setPage(JeePage.SETTINGS) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("⚙️ PDF / Backup", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // 7. RESOURCE VAULT, CBT MOCK TEST ARENA & CAMPUS LEADERBOARD
                // =========================================================================
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.38f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setPage(JeePage.BOOKMARKS) }
                            .testTag("jee_prep_hub_vault_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0070F3).copy(alpha = 0.12f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Layers,
                                                contentDescription = null,
                                                tint = Color(0xFF0070F3),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Prep Hub Vault",
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(5.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "jeeprephub.co.in",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF059669),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Short Notes, Formula PDFs, 2025 Shift PYQs & Rank Predictor",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("📄 PDF Notes", "24 Chapters", Color(0xFF0070F3)),
                                    Triple("🔥 Shift PYQs", "2019–2025", Color(0xFFF59E0B)),
                                    Triple("▶ Lectures", "One-Shot", Color(0xFFEF4444)),
                                    Triple("🎯 Rank Calc", "NIT / IIT", Color(0xFF10B981))
                                ).forEach { (title, sub, tint) ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = tint.copy(alpha = 0.09f),
                                        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setPage(JeePage.BOOKMARKS) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = title,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = tint,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = sub,
                                                fontSize = 8.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Card: CBT Mock Test Arena
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setPage(JeePage.TESTS) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0070F3).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "NTA CBT ARENA",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0070F3),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Full Syllabus Mock",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "75 Qs • +4/-1 Marking",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0070F3),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Start Mock →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Right Card: Campus Hub & Live Study Pods Arena
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.45f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setPage(JeePage.CAMPUS_HUB) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "STUDY PODS & DOUBTS",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF8B5CF6),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Campus Hub Arena",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Focus Pods, Doubts & Cutoffs",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Enter Hub →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // Subtle backdrop scrim when the Batch Sync Bottom Sheet is expanded
            AnimatedVisibility(
                visible = isBatchSyncSheetExpanded,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isBatchSyncSheetExpanded = false
                        }
                )
            }
        }
    }

    BatchAutoPlannerDialog(
        isOpen = isAutoPlannerDialogOpen,
        onDismiss = { isAutoPlannerDialogOpen = false },
        batches = batchCurriculums,
        selectedBatch = selectedBatch,
        parallelBatchIds = parallelBatchIds,
        targetDays = batchTargetDays,
        dailyWorkingHours = batchDailyHours,
        urlInput = batchUrlInput,
        isPlanning = isAutoPlanning,
        lastResult = lastAutoPlanResult,
        onSelectBatch = { viewModel.selectBatch(it) },
        onToggleParallelBatch = { viewModel.toggleParallelBatch(it) },
        onSetTargetDays = { viewModel.setBatchTargetDays(it) },
        onSetDailyHours = { viewModel.setBatchDailyHours(it) },
        onSetUrlInput = { viewModel.setBatchUrlInput(it) },
        onSyncUrl = { viewModel.parseAndApplyBatchUrl(it) },
        onGeneratePlan = { viewModel.executeAutoPlan() }
    )
}

/**
 * 1. DEDICATED TOP HEADER CARD:
 * Houses the 'Motivational Quote of the Day' right at the top of the main screen,
 * paired with a compact aspirant command status bar (Greeting, Sprint Day, Progress Ring & Quick Actions).
 */
@Composable
private fun DedicatedMotivationalTopHeaderCard(
    studentName: String,
    selectedDay: Int,
    totalDays: Int,
    batchTitle: String,
    completedTodayCount: Int,
    totalTodayCount: Int,
    todayCompletionRatio: Float,
    averageHoursPerDay: Float,
    dailyQuote: com.example.data.model.MotivationalQuote,
    isQuoteBookmarked: Boolean,
    isQuoteRefreshing: Boolean,
    onRefreshQuote: () -> Unit,
    onBookmarkQuote: () -> Unit,
    onCopyQuote: () -> Unit,
    onOpenFocusTimer: () -> Unit,
    onOpenAiMentor: () -> Unit,
    onOpenBatchSyncSheet: () -> Unit,
    onOpenPlanner: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("motivational_quote_top_header_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Compact Student Command Strip Hero Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_mentor_hero_card")
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
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "SPRINT DAY $selectedDay/$totalDays",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF7DD3FC),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "${String.format(Locale.ENGLISH, "%.1f", averageHoursPerDay)}h/d PACE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF6EE7B7),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Hi, $studentName 👋",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = batchTitle,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.clickable { onOpenFocusTimer() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFF0070F3),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Focus Timer",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0070F3),
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { onOpenAiMentor() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Mentor",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.32f),
                                border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.55f)),
                                modifier = Modifier.clickable { onOpenBatchSyncSheet() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Batch Sync",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Circular Today's Goal Progress Ring
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                        modifier = Modifier.clickable { onOpenPlanner() }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { todayCompletionRatio.coerceAtLeast(0.25f) },
                                    strokeWidth = 5.dp,
                                    color = Color(0xFF38BDF8),
                                    trackColor = Color.White.copy(alpha = 0.16f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$completedTodayCount/$totalTodayCount",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Goals",
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Motivational Quote Card directly underneath Hero Header
        DailyMotivationalQuoteCard(
            quote = dailyQuote,
            sprintDay = selectedDay,
            isBookmarked = isQuoteBookmarked,
            isRefreshing = isQuoteRefreshing,
            onRefreshClick = onRefreshQuote,
            onBookmarkToggle = onBookmarkQuote,
            onCopyClick = onCopyQuote
        )
    }
}

/**
 * 2. CENTRAL PRIORITY: DAILY SCHEDULE CARD
 * Prioritized at the center of the main screen, providing direct day switching,
 * Today's 4 Time-Block Study Slots, and Today's Daily Goals checklist.
 */
@Composable
private fun DailyScheduleCenterPriorityCard(
    selectedDay: Int,
    totalDays: Int,
    dayIndex: Int,
    slots: List<DailySlot>,
    dailyGoals: List<com.example.data.local.DailyGoalEntity>,
    version: Int,
    viewModel: JeeViewModel,
    onSelectDay: (Int) -> Unit,
    onQuickExportPdf: () -> Unit,
    onOpenBatchSyncSheet: () -> Unit,
    onOpenFullPlanner: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("daily_schedule_center_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Header: Title + Quick PDF Export + Full Planner Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Daily Schedule",
                                tint = Color(0xFF0070F3),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Daily Schedule",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PRIORITY FOCUS",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF059669),
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Day $selectedDay of $totalDays • 4 Time-Block Slots & Daily Targets",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clickable { onQuickExportPdf() }
                            .testTag("today_slots_quick_pdf_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Export PDF",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PDF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    Text(
                        text = "Full Plan →",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3),
                        modifier = Modifier.clickable { onOpenFullPlanner() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Day Selector Bar inside the Central Daily Schedule
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable(enabled = selectedDay > 1) {
                            onSelectDay((selectedDay - 1).coerceAtLeast(1))
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Prev",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val startDay = (selectedDay - 1).coerceIn(1, (totalDays - 3).coerceAtLeast(1))
                        (startDay..(startDay + 3).coerceAtMost(totalDays)).forEach { d ->
                            val isCurrent = d == selectedDay
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) Color(0xFF0070F3) else Color.Transparent,
                                modifier = Modifier.clickable { onSelectDay(d) }
                            ) {
                                Text(
                                    text = "D$d",
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isCurrent) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable(enabled = selectedDay < totalDays) {
                            onSelectDay((selectedDay + 1).coerceAtMost(totalDays))
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Next",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section A: Scheduled Study Slots & Emergency Alarm Drill Manual Verification Console
            ScheduledStudySlotsEmergencyDrillSection(
                dayIndex = dayIndex,
                slots = slots,
                version = version,
                viewModel = viewModel
            )

            // Section B: Daily Goals Quick-Checklist (Synced with Room DB)
            val goals = dailyGoals.take(4)
            if (goals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY GOALS CHECKLIST (TAP TO COMPLETE)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Sync Batch URL ↑",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6366F1),
                        modifier = Modifier.clickable { onOpenBatchSyncSheet() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { goal ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (goal.isCompleted)
                                        Color(0xFF10B981).copy(alpha = 0.10f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (goal.isCompleted) Color(0xFF10B981).copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.toggleDailyGoal(goal.id, goal.isCompleted) }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (goal.isCompleted) Color(0xFF10B981).copy(alpha = 0.2f)
                                                else Color(0xFF0070F3).copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "${goal.category} • ${goal.targetHours.toInt()}h",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (goal.isCompleted) Color(0xFF059669) else Color(0xFF0070F3),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Icon(
                                                imageVector = if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (goal.isCompleted) Color(0xFF10B981) else Color(0xFF0070F3),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = goal.title,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section C: Missed Slot Auto-Rescheduler (Backlog Recovery Engine)
            val lastRescheduled by viewModel.lastRescheduledSummary.collectAsState()
            val missedSlotsToday = remember(version, selectedDay) {
                viewModel.getMissedSlotsCountForDay(selectedDay)
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.10f),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.38f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_missed_slot_rescheduler_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (missedSlotsToday > 0)
                                "Backlog Recovery • $missedSlotsToday Slot(s) Pending Today"
                            else
                                "All 4 Study Slots On Track Today! 🎯",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = lastRescheduled ?: "Auto-distribute unfinished lectures/DPPs across next 3 days",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF59E0B),
                        modifier = Modifier
                            .clickable {
                                viewModel.rescheduleMissedSlotsToUpcomingDays(fromDay = selectedDay, spreadDays = 3)
                            }
                            .testTag("dashboard_reschedule_missed_slots_btn")
                    ) {
                        Text(
                            text = "Reschedule +3d",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. HIDDEN-BY-DEFAULT EXPANDABLE BOTTOM SHEET FOR 'BATCH SYNC' INPUT FIELD:
 * Anchored in the Scaffold's bottomBar. Collapsed by default so the main screen prioritizes
 * the Motivational Quote top header and Daily Schedule center. Expands smoothly on tap.
 */
@Composable
private fun BatchSyncExpandableBottomSheet(
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onCollapse: () -> Unit,
    urlInput: String,
    selectedBatch: com.example.data.model.BatchCurriculum,
    parallelBatches: List<com.example.data.model.BatchCurriculum>,
    targetDays: Int,
    dailyHours: Float,
    isPlanning: Boolean,
    onUrlChange: (String) -> Unit,
    onSyncUrl: (String) -> Unit,
    onOpenFullPlannerDialog: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.32f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("batch_sync_expandable_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Drag Handle & Collapsed Peek Header Bar (Always visible at bottom of Scaffold)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("batch_sync_bottom_sheet_trigger"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pill Drag Handle
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.14f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Batch Sync",
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Batch Sync (PW • Unacademy • KGS)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF6366F1).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = selectedBatch.platform,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF6366F1),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isExpanded)
                                    "Paste batch URL below to auto-sync lectures & schedule"
                                else
                                    "Tap to expand URL input • Active: ${selectedBatch.title.take(28)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExpanded)
                            MaterialTheme.colorScheme.surfaceVariant
                        else
                            Color(0xFF0070F3),
                        modifier = Modifier
                            .clickable { onToggleExpanded() }
                            .testTag("batch_sync_sheet_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpanded) "Hide" else "Sync URL",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isExpanded) MaterialTheme.colorScheme.onSurface else Color.White
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isExpanded) "Collapse Batch Sync" else "Expand Batch Sync",
                                tint = if (isExpanded) MaterialTheme.colorScheme.onSurface else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Bottom Sheet Body (Hidden by default!)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(220)) + fadeIn(animationSpec = tween(180)),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(140))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                        .testTag("batch_sync_sheet_expanded_body")
                ) {
                    BatchUrlImportSyncCard(
                        urlInput = urlInput,
                        selectedBatch = selectedBatch,
                        parallelBatches = parallelBatches,
                        targetDays = targetDays,
                        dailyHours = dailyHours,
                        isPlanning = isPlanning,
                        onUrlChange = onUrlChange,
                        onSyncUrl = { url ->
                            onSyncUrl(url)
                            if (url.isNotBlank()) {
                                onCollapse()
                            }
                        },
                        onOpenFullPlannerDialog = {
                            onCollapse()
                            onOpenFullPlannerDialog()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceRibbonChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accent.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accent,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SubjectMasteryCompactCard(
    subject: String,
    pct: Int,
    sub: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = subject,
                            tint = accent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    text = "$pct%",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = subject,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = sub,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// =========================================================================================
// GITHUB CONTRIBUTION-STYLE STUDY ACTIVITY HEATMAP (30, 45, 60, 75, 90, 120, 150 DAYS PLAN)
// =========================================================================================
@Composable
private fun StudyContributionHeatmapCard(
    selectedPlanDays: Int,
    currentSprintDay: Int,
    selectedDay: Int,
    version: Int,
    getDayHours: (Int) -> Float,
    onSelectPlanHorizon: (Int) -> Unit,
    onSelectDay: (Int) -> Unit,
    onOpenDayInPlanner: (Int) -> Unit,
    onQuickLogDayHours: (Int, Float) -> Unit
) {
    val planOptions = listOf(30, 45, 60, 75, 90, 120, 150)
    var activeHorizon by remember(selectedPlanDays) {
        mutableStateOf(
            if (selectedPlanDays in planOptions) selectedPlanDays else 90
        )
    }
    var inspectedDay by remember(selectedDay, activeHorizon) {
        mutableStateOf(selectedDay.coerceIn(1, activeHorizon))
    }
    var colorThemeIndex by remember { mutableStateOf(0) } // 0 = GitHub Emerald, 1 = Electric Blue, 2 = Solar Fire

    val dayHoursMap = remember(activeHorizon, version, currentSprintDay) {
        (1..activeHorizon).associateWith { d -> getDayHours(d) }
    }

    val totalLoggedHours = remember(dayHoursMap) {
        dayHoursMap.values.sum()
    }
    val activeStudyDaysCount = remember(dayHoursMap) {
        dayHoursMap.values.count { it > 0f }
    }
    val targetMetDaysCount = remember(dayHoursMap) {
        dayHoursMap.values.count { it >= 10f }
    }
    val longestStreak = remember(dayHoursMap, activeHorizon) {
        var best = 0
        var curr = 0
        for (d in 1..activeHorizon) {
            if ((dayHoursMap[d] ?: 0f) > 0f) {
                curr++
                if (curr > best) best = curr
            } else {
                curr = 0
            }
        }
        best
    }

    fun intensityLevel(hours: Float): Int = when {
        hours <= 0f -> 0
        hours < 5f -> 1
        hours < 9f -> 2
        hours < 12.5f -> 3
        else -> 4
    }

    val emptyCellColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
    val inspectedBorderColor = MaterialTheme.colorScheme.onSurface

    fun colorForIntensity(level: Int): Color {
        return when (colorThemeIndex) {
            1 -> when (level) { // Electric Blue
                0 -> emptyCellColor
                1 -> Color(0xFF93C5FD)
                2 -> Color(0xFF3B82F6)
                3 -> Color(0xFF1D4ED8)
                else -> Color(0xFF0070F3)
            }
            2 -> when (level) { // Solar Fire
                0 -> emptyCellColor
                1 -> Color(0xFFFDE68A)
                2 -> Color(0xFFF59E0B)
                3 -> Color(0xFFEA580C)
                else -> Color(0xFFDC2626)
            }
            else -> when (level) { // GitHub Emerald
                0 -> emptyCellColor
                1 -> Color(0xFF6EE7B7)
                2 -> Color(0xFF10B981)
                3 -> Color(0xFF059669)
                else -> Color(0xFF047857)
            }
        }
    }

    val accentColor = when (colorThemeIndex) {
        1 -> Color(0xFF0070F3)
        2 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val totalWeeks = (activeHorizon + 6) / 7
    val inspectedHours = dayHoursMap[inspectedDay] ?: 0f
    val inspectedLevel = intensityLevel(inspectedHours)
    val inspectedLevelLabel = when (inspectedLevel) {
        0 -> "Rest / Unlogged (0h)"
        1 -> "Light Warmup (1–5h)"
        2 -> "Steady Sprint (5–9h)"
        3 -> "Daily Target Met (9–12h)"
        else -> "Peak Ranker Grind (12.5h+ 🔥)"
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("study_contribution_heatmap_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Row: Title + Palette Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Study Activity Heatmap",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${activeHorizon}-DAY PLAN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "GitHub-style contribution graph across 30, 45, 60, 75, 90, 120 & 150-day plans",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Theme Palette Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clickable { colorThemeIndex = (colorThemeIndex + 1) % 3 }
                        .testTag("heatmap_palette_toggle")
                ) {
                    Text(
                        text = when (colorThemeIndex) {
                            1 -> "🔵 Blue"
                            2 -> "🟠 Fire"
                            else -> "🟢 Green"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            // 2. Plan Horizon Selector Pills (30, 45, 60, 75, 90, 120, 150 Days)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                planOptions.forEach { days ->
                    val isSelected = activeHorizon == days
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable {
                                activeHorizon = days
                                onSelectPlanHorizon(days)
                            }
                            .testTag("heatmap_plan_${days}d")
                    ) {
                        Text(
                            text = "${days}d Plan",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 3. 4-Metric Summary Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeatmapMetricChip(
                    label = "Active Days",
                    value = "$activeStudyDaysCount/$activeHorizon",
                    accent = accentColor,
                    modifier = Modifier.weight(1f)
                )
                HeatmapMetricChip(
                    label = "Total Hours",
                    value = String.format(Locale.US, "%.0fh", totalLoggedHours),
                    accent = Color(0xFF0070F3),
                    modifier = Modifier.weight(1f)
                )
                HeatmapMetricChip(
                    label = "Peak Streak",
                    value = "${longestStreak}d 🔥",
                    accent = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                HeatmapMetricChip(
                    label = "10h+ Days",
                    value = "$targetMetDaysCount d",
                    accent = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. GitHub Contribution Matrix (7 Rows = Days of Week × N Columns = Weeks)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left Day-of-Week Labels (7 rows)
                        Column(
                            modifier = Modifier.padding(top = 18.dp, end = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val rowLabels = listOf("D1", "D2", "D3", "D4", "D5", "D6", "D7")
                            rowLabels.forEach { lbl ->
                                Box(
                                    modifier = Modifier.height(16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = lbl,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Horizontally Scrollable Week Columns
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (weekIdx in 0 until totalWeeks) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Week Header Label (W1, W2, ...)
                                    Text(
                                        text = "W${weekIdx + 1}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    for (dayRow in 0 until 7) {
                                        val dayNumber = weekIdx * 7 + dayRow + 1
                                        if (dayNumber <= activeHorizon) {
                                            val hrs = dayHoursMap[dayNumber] ?: 0f
                                            val lvl = intensityLevel(hrs)
                                            val isInspected = dayNumber == inspectedDay
                                            val isTodaySprint = dayNumber == currentSprintDay
                                            val cellColor = colorForIntensity(lvl)

                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(cellColor)
                                                    .clickable {
                                                        inspectedDay = dayNumber
                                                        onSelectDay(dayNumber)
                                                    }
                                                    .testTag("heatmap_cell_day_$dayNumber"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isInspected || isTodaySprint) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color.Transparent,
                                                        border = BorderStroke(
                                                            1.6.dp,
                                                            if (isInspected) inspectedBorderColor else Color(0xFFFBBF24)
                                                        ),
                                                        modifier = Modifier.fillMaxSize()
                                                    ) {}
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contribution Legend Row (Less [0][1][2][3][4] More)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap any square to inspect or log study hours",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Less",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            (0..4).forEach { lvl ->
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colorForIntensity(lvl))
                                )
                            }
                            Text(
                                text = "More",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. Interactive Selected Day Inspector & 1-Tap Hours Logger
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Day $inspectedDay of $activeHorizon • ${String.format(Locale.US, "%.1fh", inspectedHours)} Logged",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = inspectedLevelLabel,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor,
                            modifier = Modifier
                                .clickable { onOpenDayInPlanner(inspectedDay) }
                                .testTag("heatmap_open_day_planner_btn")
                        ) {
                            Text(
                                text = "Open Day $inspectedDay →",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Quick 1-Tap Study Hours Update Pills for Inspected Day
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "4h Light" to 4f,
                            "8h Steady" to 8f,
                            "12h Target" to 12f,
                            "15h Peak 🔥" to 15f
                        ).forEach { (label, hrsVal) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onQuickLogDayHours(inspectedDay, hrsVal)
                                    }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapMetricChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StudentLifeOsDashboardCard(
    viewModel: JeeViewModel,
    onOpenUtilityHub: () -> Unit
) {
    val tasks by viewModel.utilityTasks.collectAsState()
    val totalSpent by viewModel.utilityTotalSpent.collectAsState()
    val habits by viewModel.utilityHabits.collectAsState()
    val latestWellness by viewModel.utilityLatestWellness.collectAsState()

    val pendingTasks = tasks.count { !it.isCompleted }
    val urgentQ1Tasks = tasks.count { !it.isCompleted && it.quadrant == "DO_FIRST" }
    val completedHabitsToday = habits.count { it.isCompletedToday }
    val totalHabits = habits.size
    val waterMl = latestWellness?.waterIntakeMl ?: 2000

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_student_life_os_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981)
                    ) {
                        Text(
                            text = "NEW",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "High-Performance Student OS",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onOpenUtilityHub() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Launch Studio →",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Unifying competitive exam prep with Eisenhower tasks, student budget, routines & mindfulness",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Mini Status Metric Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Productivity
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0070F3).copy(alpha = 0.08f),
                    border = BorderStroke(0.5.dp, Color(0xFF0070F3).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenUtilityHub() }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚡ Tasks", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0070F3))
                        Text(
                            text = "$urgentQ1Tasks Urgent",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("$pendingTasks pending", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Finance
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.08f),
                    border = BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenUtilityHub() }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💳 Finance", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text(
                            text = "₹${(totalSpent ?: 0.0).toInt()}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Ledger balance", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Atomic Habits
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF6D00).copy(alpha = 0.08f),
                    border = BorderStroke(0.5.dp, Color(0xFFFF6D00).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenUtilityHub() }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥 Habits", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6D00))
                        Text(
                            text = "$completedHabitsToday/$totalHabits Done",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Streaks active", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Wellness
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.08f),
                    border = BorderStroke(0.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenUtilityHub() }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧘 Wellness", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                        Text(
                            text = "${waterMl}ml 💧",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Low burnout", fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}


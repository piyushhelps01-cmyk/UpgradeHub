package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SelfStudyMockTest
import com.example.ui.components.MarksToRankPredictorCard
import com.example.ui.viewmodel.ActiveMockTestState
import com.example.ui.viewmodel.CbtAttemptRecord
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import com.example.ui.viewmodel.MistakeNotebookEntry
import kotlinx.coroutines.delay

private enum class CbtArenaWorkspace(val label: String) {
    OFFICIAL_MOCKS("Official Mocks"),
    CUSTOM_BUILDER("Custom Builder"),
    MISTAKE_VAULT("Mistake Vault"),
    ANALYTICS_AIR("Scorecards & AIR")
}

@Composable
fun CbtTestScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val activeState by viewModel.activeMockTestState.collectAsState()
    val mistakeEntries by viewModel.mistakeNotebookEntries.collectAsState()
    val attemptHistory by viewModel.cbtAttemptHistory.collectAsState()
    val availableTests = viewModel.selfStudyRepository.mockTests

    if (activeState.activeTest == null) {
        TestListCatalogView(
            viewModel = viewModel,
            tests = availableTests,
            attemptHistory = attemptHistory,
            mistakeEntries = mistakeEntries,
            onSelectTest = { test -> viewModel.startMockTest(test) },
            onAddMistake = { title, subject, source, errorType, note ->
                viewModel.addMistakeEntry(title, subject, source, errorType, note)
            },
            onToggleMistakeResolved = { id -> viewModel.toggleMistakeResolved(id) },
            onDeleteMistake = { id -> viewModel.deleteMistakeEntry(id) },
            onBack = { viewModel.setPage(JeePage.DASHBOARD) },
            modifier = modifier
        )
    } else if (activeState.isSubmitted && activeState.finalSubmission != null) {
        TestSubmissionResultView(
            activeState = activeState,
            onLogMistake = { title, subject, source, errorType, note ->
                viewModel.addMistakeEntry(title, subject, source, errorType, note)
                viewModel.showSnackbar("Saved to Mistake Vault ✓")
            },
            onExit = { viewModel.exitMockTest() },
            onRetake = {
                val test = activeState.activeTest
                if (test != null) viewModel.startMockTest(test)
            },
            modifier = modifier
        )
    } else {
        LiveCbtExamEngineView(
            activeState = activeState,
            onSelectAnswer = { qIdx, optIdx -> viewModel.selectMockTestAnswer(qIdx, optIdx) },
            onClearAnswer = { qIdx -> viewModel.clearMockTestAnswer(qIdx) },
            onToggleReview = { qIdx -> viewModel.toggleMockTestMarkForReview(qIdx) },
            onJumpToQuestion = { idx -> viewModel.jumpToMockTestQuestion(idx) },
            onSubmit = {
                viewModel.submitMockTest()
                val submitted = viewModel.activeMockTestState.value
                val t = submitted.activeTest
                val res = submitted.finalSubmission
                if (t != null && res != null) {
                    val acc = if (res.correctCount + res.incorrectCount > 0) {
                        (res.correctCount * 100) / (res.correctCount + res.incorrectCount)
                    } else 0
                    viewModel.recordCbtAttempt(
                        testTitle = t.title,
                        examCategory = t.exam,
                        score = res.score,
                        maxMarks = res.maxScore,
                        accuracyPct = acc,
                        timeTakenLabel = "${(t.durationMinutes / 2).coerceAtLeast(10)}m"
                    )
                }
            },
            onExit = { viewModel.exitMockTest() },
            modifier = modifier
        )
    }
}

@Composable
private fun TestListCatalogView(
    viewModel: JeeViewModel,
    tests: List<SelfStudyMockTest>,
    attemptHistory: List<CbtAttemptRecord>,
    mistakeEntries: List<MistakeNotebookEntry>,
    onSelectTest: (SelfStudyMockTest) -> Unit,
    onAddMistake: (String, String, String, String, String) -> Unit,
    onToggleMistakeResolved: (String) -> Unit,
    onDeleteMistake: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeWorkspace by remember { mutableStateOf(CbtArenaWorkspace.OFFICIAL_MOCKS) }
    var selectedExamTab by remember { mutableIntStateOf(0) } // 0: All, 1: JEE Main, 2: NEET, 3: Unit Drills

    // Custom Drill Builder States
    var customTestSubject by remember { mutableStateOf("Physics") }
    var customQuestionCount by remember { mutableIntStateOf(10) }
    var customMinutesPerQ by remember { mutableIntStateOf(2) }
    var customDifficultyFocus by remember { mutableStateOf("Mixed NTA Level") }

    val unresolvedMistakesCount = mistakeEntries.count { !it.isResolved }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("cbt_test_catalog"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Clean Command Header
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "NTA CBT ENGINE • +4 / -1",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${tests.size} Official Mocks Ready",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "All-India CBT Test & Diagnostic Arena",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Real NTA palette simulation with subject section switching, per-question timer & 1-tap Mistake Vault.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4-Workspace Segmented Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CbtArenaWorkspace.entries.forEach { ws ->
                                val isSelected = activeWorkspace == ws
                                val badgeSuffix = when (ws) {
                                    CbtArenaWorkspace.MISTAKE_VAULT -> if (unresolvedMistakesCount > 0) " ($unresolvedMistakesCount)" else ""
                                    CbtArenaWorkspace.ANALYTICS_AIR -> if (attemptHistory.isNotEmpty()) " (${attemptHistory.size})" else ""
                                    else -> ""
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.1f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.clickable { activeWorkspace = ws }
                                ) {
                                    Text(
                                        text = "${ws.label}$badgeSuffix",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        when (activeWorkspace) {
            CbtArenaWorkspace.OFFICIAL_MOCKS -> {
                // Exam filter tabs
                item {
                    TabRow(
                        selectedTabIndex = selectedExamTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        contentColor = Color(0xFF0070F3),
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                    ) {
                        listOf("All Mocks", "JEE Main", "NEET (PCB)", "Unit Drills").forEachIndexed { idx, title ->
                            Tab(
                                selected = selectedExamTab == idx,
                                onClick = { selectedExamTab = idx },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedExamTab == idx) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                }
                            )
                        }
                    }
                }

                val filteredTests = tests.filter { test ->
                    when (selectedExamTab) {
                        1 -> test.exam.contains("JEE", ignoreCase = true)
                        2 -> test.exam.contains("NEET", ignoreCase = true)
                        3 -> test.questions.size <= 15
                        else -> true
                    }
                }

                items(filteredTests, key = { it.id }) { test ->
                    TestCatalogCard(
                        test = test,
                        lastAttempt = attemptHistory.firstOrNull { it.testTitle == test.title },
                        onStart = { onSelectTest(test) }
                    )
                }
            }

            CbtArenaWorkspace.CUSTOM_BUILDER -> {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_mini_test_generator_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CUSTOM NTA DRILL GENERATOR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF8B5CF6),
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "Build a Personalized Timed CBT",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Target your weak subject with custom question volume and strict per-question pacing.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Step 1: Subject Selection
                            Text(
                                text = "1. Select Target Subject",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Physics", "Chemistry", "Maths", "Biology").forEach { subj ->
                                    val selected = customTestSubject == subj
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { customTestSubject = subj }
                                    ) {
                                        Text(
                                            text = subj,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }

                            // Step 2: Difficulty Level
                            Text(
                                text = "2. Select Difficulty Profile",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Mixed NTA Level", "High-Yield PYQ", "Rank Booster").forEach { diff ->
                                    val selected = customDifficultyFocus == diff
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) Color(0xFF0070F3).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, if (selected) Color(0xFF0070F3) else Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { customDifficultyFocus = diff }
                                    ) {
                                        Text(
                                            text = diff,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Step 3: Question Count & Pace
                            Text(
                                text = "3. Question Count & Time Limit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 15).forEach { qCount ->
                                    val selected = customQuestionCount == qCount
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, if (selected) Color(0xFF10B981) else Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { customQuestionCount = qCount }
                                    ) {
                                        Text(
                                            text = "$qCount Questions",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 9.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1 to "Sprint (1m/Q)", 2 to "Standard (2m/Q)", 3 to "Deep (3m/Q)").forEach { (mins, label) ->
                                    val selected = customMinutesPerQ == mins
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) Color(0xFFF59E0B).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        border = BorderStroke(1.dp, if (selected) Color(0xFFF59E0B) else Color.Transparent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { customMinutesPerQ = mins }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val allQuestions = (tests.flatMap { it.questions } + viewModel.selfStudyRepository.allBooks.flatMap { it.chapters.flatMap { ch -> ch.pyqs } }).distinctBy { it.questionText }
                                    val matching = allQuestions.filter {
                                        customTestSubject == "All" || it.subject.contains(customTestSubject, ignoreCase = true)
                                    }.ifEmpty { allQuestions }
                                    
                                    val pool = if (matching.size < customQuestionCount) {
                                        (matching + allQuestions).distinctBy { it.questionText }
                                    } else matching
                                    
                                    val selectedQs = pool.shuffled().take(customQuestionCount).ifEmpty { allQuestions.take(5) }
                                    val totalDuration = (selectedQs.size * customMinutesPerQ).coerceAtLeast(5)
                                    val customMock = SelfStudyMockTest(
                                        id = "custom_${System.currentTimeMillis()}",
                                        title = "$customTestSubject • $customDifficultyFocus (${selectedQs.size} Questions)",
                                        exam = if (customTestSubject.contains("Bio", ignoreCase = true)) "NEET (PCB)" else "JEE Main",
                                        subject = customTestSubject,
                                        durationMinutes = totalDuration,
                                        totalMarks = selectedQs.size * 4,
                                        markingScheme = "+4 Correct, -1 Wrong • ${customMinutesPerQ}m/Q Pacing",
                                        questions = selectedQs
                                    )
                                    onSelectTest(customMock)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Launch Custom $customTestSubject CBT (${customQuestionCount * customMinutesPerQ} Mins)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            CbtArenaWorkspace.MISTAKE_VAULT -> {
                item {
                    com.example.ui.components.SmartAutoMistakeAndTestAnalysisSection(
                        viewModel = viewModel
                    )
                }
                item {
                    MistakeNotebookSectionCard(
                        entries = mistakeEntries,
                        onAddMistake = onAddMistake,
                        onToggleResolved = onToggleMistakeResolved,
                        onDeleteMistake = onDeleteMistake
                    )
                }
            }

            CbtArenaWorkspace.ANALYTICS_AIR -> {
                item {
                    com.example.ui.components.SmartAutoMistakeAndTestAnalysisSection(
                        viewModel = viewModel
                    )
                }
                item {
                    MarksToRankPredictorCard(
                        isNeetDefault = selectedExamTab == 2
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cbt_attempt_history_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "PAST CBT SCORECARDS & ACCURACY TREND",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0070F3),
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "${attemptHistory.size} Completed CBT Sessions",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (attemptHistory.isEmpty()) {
                                Text(
                                    text = "Complete your first Mock Test to unlock accuracy trends and time analytics.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                attemptHistory.forEach { rec ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = rec.testTitle,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${rec.examCategory} • Accuracy: ${rec.accuracyPct}% • Time: ${rec.timeTakenLabel} • ${rec.timestampLabel}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${rec.score}/${rec.maxMarks}",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF059669),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestCatalogCard(
    test: SelfStudyMockTest,
    lastAttempt: CbtAttemptRecord?,
    onStart: () -> Unit
) {
    val subjectsInTest = remember(test.questions) {
        test.questions.map { it.subject }.distinct()
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = test.exam.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0070F3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (lastAttempt != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Last Score: ${lastAttempt.score}/${lastAttempt.maxMarks}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${test.durationMinutes} mins",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = test.title,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${subjectsInTest.joinToString(" • ").ifEmpty { test.subject }} • ${test.questions.size} Qs • Max Marks: ${test.totalMarks}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = test.markingScheme,
                    fontSize = 11.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lastAttempt != null) "Retake CBT" else "Start CBT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveCbtExamEngineView(
    activeState: ActiveMockTestState,
    onSelectAnswer: (Int, Int) -> Unit,
    onClearAnswer: (Int) -> Unit,
    onToggleReview: (Int) -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val test = activeState.activeTest ?: return
    val currentIdx = activeState.currentQuestionIndex
    val currentQuestion = test.questions.getOrNull(currentIdx) ?: return

    var remainingSeconds by remember { mutableIntStateOf(test.durationMinutes * 60) }
    var questionElapsedSeconds by remember(currentIdx) { mutableIntStateOf(0) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }
    var showPaletteDrawer by remember { mutableStateOf(false) }

    val subjectSections = remember(test.questions) {
        test.questions.map { it.subject }.distinct()
    }

    // Countdown Timer + Per-Question Stopwatch
    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        onSubmit()
    }

    LaunchedEffect(currentIdx) {
        questionElapsedSeconds = 0
        while (true) {
            delay(1000)
            questionElapsedSeconds++
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val qMins = questionElapsedSeconds / 60
    val qSecs = questionElapsedSeconds % 60
    val formattedQTime = String.format("%02d:%02d", qMins, qSecs)

    val selectedOption = activeState.userAnswers[currentIdx]
    val isMarkedForReview = activeState.markedForReview.contains(currentIdx)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("cbt_live_exam")
    ) {
        // CBT Top Bar
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = test.exam,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "Question ${currentIdx + 1} of ${test.questions.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // Timer Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (remainingSeconds < 300) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (remainingSeconds < 300) Color(0xFFEF4444) else Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (remainingSeconds < 300) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedTime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingSeconds < 300) Color(0xFFEF4444) else Color.White
                            )
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = { showSubmitConfirmation = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Submit", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subject Section Jump Bar + Palette Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjectSections.forEach { subj ->
                            val isCurrentSubj = currentQuestion.subject.equals(subj, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrentSubj) Color(0xFF38BDF8) else Color(0xFF1E293B),
                                modifier = Modifier.clickable {
                                    val firstIndex = test.questions.indexOfFirst { it.subject.equals(subj, ignoreCase = true) }
                                    if (firstIndex >= 0) onJumpToQuestion(firstIndex)
                                }
                            ) {
                                Text(
                                    text = subj,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentSubj) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = { showPaletteDrawer = !showPaletteDrawer },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showPaletteDrawer) "Hide Palette" else "Palette (${activeState.userAnswers.size}/${test.questions.size})",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Expandable Question Palette Grid
        AnimatedVisibility(visible = showPaletteDrawer) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PaletteLegendItem(Color(0xFF10B981), "Answered (${activeState.userAnswers.size})")
                        PaletteLegendItem(Color(0xFF8B5CF6), "Review (${activeState.markedForReview.size})")
                        PaletteLegendItem(Color.Gray, "Left (${test.questions.size - activeState.userAnswers.size})")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(test.questions.size) { idx ->
                            val isAnswered = activeState.userAnswers.containsKey(idx)
                            val isReview = activeState.markedForReview.contains(idx)
                            val isCurrent = idx == currentIdx

                            val bg = when {
                                isCurrent -> Color(0xFF0070F3)
                                isReview -> Color(0xFF8B5CF6)
                                isAnswered -> Color(0xFF10B981)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .border(
                                        1.dp,
                                        if (isCurrent) Color.White else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onJumpToQuestion(idx)
                                        showPaletteDrawer = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnswered || isReview || isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Question Details & Options
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Question Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0070F3).copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Q${currentIdx + 1} • ${currentQuestion.importantTag}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0070F3),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                // Per-Question Time Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (questionElapsedSeconds > 120) Color(0xFFF59E0B).copy(alpha = 0.16f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "⏱ $formattedQTime on Q",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (questionElapsedSeconds > 120) Color(0xFFD97706)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            IconButton(onClick = { onToggleReview(currentIdx) }) {
                                Icon(
                                    imageVector = if (isMarkedForReview) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Mark for review",
                                    tint = if (isMarkedForReview) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${currentQuestion.subject} • ${currentQuestion.chapter}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Options List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select one answer (+4, -1):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedOption != null) {
                        Text(
                            text = "Clear Response",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.clickable { onClearAnswer(currentIdx) }
                        )
                    }
                }
            }

            itemsIndexed(currentQuestion.options) { optIdx, optionText ->
                val isSelected = selectedOption == optIdx
                val optLabel = when (optIdx) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    else -> "D"
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF0070F3).copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAnswer(currentIdx, optIdx) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = optLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = optionText,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Authentic NTA Bottom Control Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentIdx > 0) onJumpToQuestion(currentIdx - 1) },
                    enabled = currentIdx > 0,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prev", fontSize = 12.sp)
                }

                TextButton(
                    onClick = {
                        onToggleReview(currentIdx)
                        if (currentIdx < test.questions.size - 1) {
                            onJumpToQuestion(currentIdx + 1)
                        }
                    }
                ) {
                    Text(
                        text = if (isMarkedForReview) "Unmark" else "Review & Next",
                        color = Color(0xFF8B5CF6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        if (currentIdx < test.questions.size - 1) {
                            onJumpToQuestion(currentIdx + 1)
                        } else {
                            showSubmitConfirmation = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (currentIdx < test.questions.size - 1) "Save & Next" else "Finish CBT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmation) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            title = {
                Text(
                    text = "Submit Official CBT?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Review your attempt summary before locking your final responses:")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Attempted: ${activeState.userAnswers.size} / ${test.questions.size}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text("• Marked for Review: ${activeState.markedForReview.size}", color = Color(0xFF8B5CF6), fontWeight = FontWeight.SemiBold)
                            Text("• Unattempted: ${test.questions.size - activeState.userAnswers.size}", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmation = false
                        onSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Submit & View Scorecard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmation = false }) {
                    Text("Resume Exam")
                }
            }
        )
    }
}

@Composable
private fun PaletteLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(10.dp)
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TestSubmissionResultView(
    activeState: ActiveMockTestState,
    onLogMistake: (String, String, String, String, String) -> Unit,
    onExit: () -> Unit,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    val submission = activeState.finalSubmission ?: return
    val test = activeState.activeTest ?: return
    val pct = if (submission.maxScore > 0) (submission.score.coerceAtLeast(0) * 100) / submission.maxScore else 0

    // Calculate Subject-Wise Breakdown
    val subjectBreakdown = remember(test, submission) {
        test.questions.mapIndexed { idx, q -> idx to q }
            .groupBy { it.second.subject }
            .map { (subj, indexedQs) ->
                var c = 0
                var w = 0
                indexedQs.forEach { (idx, q) ->
                    val ans = submission.userAnswers[idx]
                    if (ans != null) {
                        if (ans == q.correctOptionIndex) c++ else w++
                    }
                }
                val sc = c * 4 - w
                val mx = indexedQs.size * 4
                Triple(subj, "$sc/$mx", "✓$c  ✗$w")
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("cbt_test_result"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Top Score Header
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Official CBT Diagnostic Scorecard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = test.title,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${submission.score} / ${submission.maxScore}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = if (submission.score >= 0) Color(0xFF38BDF8) else Color(0xFFEF4444)
                        )

                        Text(
                            text = "Net Score Percentage: $pct%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CbtResultMetricPill("Correct (+4)", "${submission.correctCount}", Color(0xFF10B981))
                            CbtResultMetricPill("Incorrect (-1)", "${submission.incorrectCount}", Color(0xFFEF4444))
                            CbtResultMetricPill("Skipped (0)", "${submission.unattemptedCount}", Color(0xFF94A3B8))
                        }

                        if (subjectBreakdown.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subjectBreakdown.forEach { (subj, scoreStr, countStr) ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(subj, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                            Text(scoreStr, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                                            Text(countStr, fontSize = 10.sp, color = Color(0xFFCBD5E1))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retake Test")
                }

                Button(
                    onClick = onExit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back to Test Arena")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Question-by-Question Review with Step-by-Step Solutions
        item {
            Text(
                text = "Question-by-Question Solutions & 1-Tap Error Logging",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        itemsIndexed(test.questions) { idx, question ->
            val userAns = submission.userAnswers[idx]
            val isCorrect = userAns == question.correctOptionIndex
            val isAttempted = userAns != null

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (!isAttempted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    else if (isCorrect) Color(0xFF10B981).copy(alpha = 0.5f)
                    else Color(0xFFEF4444).copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Q${idx + 1} • ${question.subject} (${question.chapter})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0070F3)
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isAttempted) Color.Gray.copy(alpha = 0.15f)
                            else if (isCorrect) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (!isAttempted) "SKIPPED (0)" else if (isCorrect) "+4 CORRECT" else "-1 WRONG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (!isAttempted) Color.Gray else if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = question.questionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "✓ Correct Option: ${(question.correctOptionIndex + 1)} — ${question.options[question.correctOptionIndex]}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )

                    if (userAns != null && userAns != question.correctOptionIndex) {
                        Text(
                            text = "✗ Your Option: ${(userAns + 1)} — ${question.options[userAns]}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Step-by-Step Solution:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = question.stepByStepSolution,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1-Tap Save to Mistake Vault:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Silly Mistake" to Color(0xFFF59E0B),
                            "Concept Gap" to Color(0xFFEF4444),
                            "Formula Forgotten" to Color(0xFF8B5CF6)
                        ).forEach { (tagLabel, tagColor) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = tagColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, tagColor.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onLogMistake(
                                            question.questionText,
                                            question.subject,
                                            "${test.title} • Q${idx + 1}",
                                            tagLabel,
                                            question.stepByStepSolution
                                        )
                                    }
                            ) {
                                Text(
                                    text = "+ $tagLabel",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = tagColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
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
private fun MistakeNotebookSectionCard(
    entries: List<MistakeNotebookEntry>,
    onAddMistake: (String, String, String, String, String) -> Unit,
    onToggleResolved: (String) -> Unit,
    onDeleteMistake: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddForm by remember { mutableStateOf(false) }
    var newQuestion by remember { mutableStateOf("") }
    var newTakeaway by remember { mutableStateOf("") }
    var newSubject by remember { mutableStateOf("Physics") }
    var newErrorType by remember { mutableStateOf("Concept Gap") }

    val filteredEntries = remember(entries, selectedFilter) {
        if (selectedFilter == "All") entries
        else entries.filter { it.errorType.equals(selectedFilter, ignoreCase = true) }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.38f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mistake_notebook_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MISTAKE NOTEBOOK & ERROR LOG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "CBT & PYQ Error Vault (${entries.count { !it.isResolved }} Active)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Never repeat an exam error. Tag & master your weak spots.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEF4444),
                    modifier = Modifier
                        .clickable { showAddForm = !showAddForm }
                        .testTag("mistake_notebook_add_toggle_btn")
                ) {
                    Text(
                        text = if (showAddForm) "Close" else "+ Log Error",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showAddForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Physics", "Chemistry", "Maths", "Biology").forEach { subj ->
                            val isSelected = newSubject == subj
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { newSubject = subj }
                            ) {
                                Text(
                                    text = subj,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newQuestion,
                        onValueChange = { newQuestion = it },
                        placeholder = { Text("Question / Concept where mistake happened...", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTakeaway,
                        onValueChange = { newTakeaway = it },
                        placeholder = { Text("Key formula / takeaway to avoid repeating...", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Silly Mistake", "Concept Gap", "Formula Forgotten").forEach { errTag ->
                            val isSelected = newErrorType == errTag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFEF4444) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { newErrorType = errTag }
                            ) {
                                Text(
                                    text = errTag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            if (newQuestion.isNotBlank()) {
                                onAddMistake(
                                    newQuestion,
                                    newSubject,
                                    "Manual CBT / PYQ Log",
                                    newErrorType,
                                    newTakeaway.ifBlank { "Revise core formula and sign convention before marking." }
                                )
                                newQuestion = ""
                                newTakeaway = ""
                                showAddForm = false
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save to Mistake Vault", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Concept Gap", "Silly Mistake", "Formula Forgotten").forEach { filterName ->
                    val selected = selectedFilter == filterName
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { selectedFilter = filterName }
                    ) {
                        Text(
                            text = filterName,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredEntries.forEach { entry ->
                    val tagColor = when (entry.errorType) {
                        "Silly Mistake" -> Color(0xFFF59E0B)
                        "Formula Forgotten" -> Color(0xFF8B5CF6)
                        else -> Color(0xFFEF4444)
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (entry.isResolved)
                            Color(0xFF10B981).copy(alpha = 0.08f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                        border = BorderStroke(
                            1.dp,
                            if (entry.isResolved) Color(0xFF10B981).copy(alpha = 0.35f)
                            else tagColor.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = tagColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = entry.errorType.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = tagColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${entry.subject} • ${entry.chapterOrSource}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (entry.isResolved) Color(0xFF10B981) else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                        modifier = Modifier.clickable { onToggleResolved(entry.id) }
                                    ) {
                                        Text(
                                            text = if (entry.isResolved) "Mastered ✓" else "Mark Fixed",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.isResolved) Color.White else Color(0xFF059669),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteMistake(entry.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete mistake",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = entry.questionTitle,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 Fix: ${entry.takeawayNote}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CbtResultMetricPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

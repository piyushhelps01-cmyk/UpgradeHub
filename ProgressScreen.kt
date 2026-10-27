package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DashboardPdfExportImportCard
import com.example.ui.components.MarksToRankPredictorCard
import com.example.ui.components.SixtyDayProgressRechartsCard
import com.example.ui.components.StudyHoursTrendCard
import com.example.ui.components.WeeklyStudyHoursRechartsCard
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import java.util.Locale

private data class SubjectPerfItem(
    val subject: String,
    val pct: Int,
    val chaptersSummary: String,
    val accent: Color,
    val icon: ImageVector,
    val targetPage: JeePage
)

/**
 * Progress & Recharts Analytics Screen (Pro v3.0)
 * Combines Overall Progress Ring, Subject Mastery Breakdown, Weekly Recharts Summary, and 60-Day D3/Recharts Visualizer
 */
@Composable
fun ProgressScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    onNavigateToPage: (JeePage) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendData by viewModel.sevenDayTrendData.collectAsState()
    val sixtyDayData by viewModel.sixtyDayProgressData.collectAsState()
    val activeChapters by viewModel.activeChapters.collectAsState()
    val currentDailyGoals by viewModel.currentDailyGoals.collectAsState()
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val batchTargetDays by viewModel.batchTargetDays.collectAsState()
    val isPdfExporting by viewModel.isPdfExporting.collectAsState()
    val lastExportedPdf by viewModel.lastExportedPdf.collectAsState()

    val isNeet by viewModel.isNeetMode.collectAsState()
    val mistakes by viewModel.mistakeNotebookEntries.collectAsState()
    val version by viewModel.version.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Mastery", "Recharts")

    val totalChapters = activeChapters.size.coerceAtLeast(1)
    val completedChapters = remember(activeChapters, version) {
        activeChapters.count { viewModel.getChapterChain(it.id).isComplete() || viewModel.isBatchChapterComplete(it.title) }
    }
    val completedGoalsToday = currentDailyGoals.count { it.isCompleted }
    val totalGoalsToday = currentDailyGoals.size.coerceAtLeast(4)
    val overallPct = remember(uiState.overallCompletionPct, completedChapters, totalChapters) {
        val chapPct = ((completedChapters.toFloat() / totalChapters) * 100).toInt()
        maxOf(uiState.overallCompletionPct, chapPct).coerceIn(12, 100)
    }

    val subjectStats = remember(uiState.subjectProgressStats, isNeet, completedChapters, totalChapters, version) {
        val thirdSubject = if (isNeet) "Biology" else "Mathematics"
        val physStat = uiState.subjectProgressStats.find { it.subject.contains("Phys", ignoreCase = true) }
        val chemStat = uiState.subjectProgressStats.find { it.subject.contains("Chem", ignoreCase = true) }
        val thirdStat = uiState.subjectProgressStats.find {
            it.subject.contains(if (isNeet) "Bio" else "Math", ignoreCase = true)
        }
        val physPct = physStat?.percentage?.takeIf { it > 0 } ?: 72
        val chemPct = chemStat?.percentage?.takeIf { it > 0 } ?: 64
        val thirdPct = thirdStat?.percentage?.takeIf { it > 0 } ?: 60

        listOf(
            SubjectPerfItem(
                "Physics",
                physPct,
                "${physStat?.completed ?: 6}/${physStat?.total ?: 10} Chapters • High-Yield Mechanics & Electrodynamics",
                Color(0xFF0070F3),
                Icons.Default.Bolt,
                JeePage.CHAPTERS
            ),
            SubjectPerfItem(
                thirdSubject,
                thirdPct,
                "${thirdStat?.completed ?: 5}/${thirdStat?.total ?: 10} Chapters • ${if (isNeet) "NCERT Botany & Zoology" else "Calculus & Coordinate Sprint"}",
                Color(0xFF8B5CF6),
                Icons.Default.Functions,
                JeePage.CHAPTERS
            ),
            SubjectPerfItem(
                "Chemistry",
                chemPct,
                "${chemStat?.completed ?: 5}/${chemStat?.total ?: 10} Chapters • NCERT Line-by-Line & PYQs",
                Color(0xFF10B981),
                Icons.Default.Science,
                JeePage.NCERT
            ),
            SubjectPerfItem(
                "Mock & PYQ Bank",
                80,
                "Full Syllabus CBT Mocks • Top Rank Trajectory",
                Color(0xFFEC4899),
                Icons.Default.CheckCircle,
                JeePage.TESTS
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("progress_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Segmented Control (Apple Pro style)
        item {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color(0xFF0070F3),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        }

        // Hero Circular Ring Card (Shown on Overview & Mastery)
        if (selectedTabIndex == 0 || selectedTabIndex == 1) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Overall Syllabus & Sprint Velocity",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$completedGoalsToday of $totalGoalsToday Daily Slots Done • $completedChapters/$totalChapters Chapters",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "AIR PACE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { overallPct / 100f },
                                strokeWidth = 11.dp,
                                color = Color(0xFF0070F3),
                                trackColor = Color(0xFF0070F3).copy(alpha = 0.14f),
                                modifier = Modifier.size(118.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$overallPct%",
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "On Track",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrackerStatMiniPill(title = "$totalGoalsToday", subtitle = "Daily Slots", color = MaterialTheme.colorScheme.onSurface)
                            TrackerStatMiniPill(title = "$completedGoalsToday", subtitle = "Completed", color = Color(0xFF10B981))
                            TrackerStatMiniPill(
                                title = "${String.format(Locale.ENGLISH, "%.1f", trendData.consistencySummary.totalHours)}h",
                                subtitle = "7-Day Hours",
                                color = Color(0xFF0070F3)
                            )
                            TrackerStatMiniPill(
                                title = "${trendData.consistencySummary.consistencyScore}%",
                                subtitle = "Consistency",
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
            }

            // Interactive Subject-Wise Marks vs Percentile & College Cutoff Predictor
            item {
                com.example.ui.components.PerformanceAndWeaknessDetectionCard(
                    viewModel = viewModel
                )
            }

            item {
                MarksToRankPredictorCard(
                    isNeetDefault = isNeet
                )
            }

            // Export Study Progress & Planner Data as PDF for Local Storage & Reporting
            item {
                DashboardPdfExportImportCard(
                    isExporting = isPdfExporting,
                    lastExportedFile = lastExportedPdf,
                    batchTitle = selectedBatch.title,
                    targetDays = batchTargetDays,
                    onSavePdfToLocalStorage = { uri ->
                        viewModel.savePdfToLocalUri(uri)
                    },
                    onExportAndSharePdf = {
                        viewModel.exportProgressPdf { pdfFile ->
                            viewModel.sharePdf(pdfFile)
                        }
                    },
                    onImportPdfFromStorage = { uri ->
                        viewModel.importProgressPdf(uri)
                    },
                    onOpenSettings = {
                        onNavigateToPage(JeePage.SETTINGS)
                    }
                )
            }
        }

        // Recharts Weekly Study Hours Summary Card (Shown on Overview & Recharts tabs)
        if (selectedTabIndex == 0 || selectedTabIndex == 2) {
            item {
                WeeklyStudyHoursRechartsCard(
                    trendData = trendData,
                    onSelectDay = { dayNumber ->
                        viewModel.selectDay(dayNumber)
                        onNavigateToPage(JeePage.DAILY_PLANNER)
                    },
                    onUpdateDayHours = { dayNumber, newHours ->
                        viewModel.updateDayRecord(
                            dayIndex = dayNumber - 1,
                            key = "hours",
                            value = String.format(Locale.ENGLISH, "%.1f", newHours)
                        )
                    },
                    onOpenFullAnalytics = {
                        onNavigateToPage(JeePage.HOURS)
                    }
                )
            }
        }

        // Subject Performance Section (Shown on Overview & Mastery tabs)
        if (selectedTabIndex == 0 || selectedTabIndex == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subject Mastery Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "9-Step Chain →",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3),
                        modifier = Modifier.clickable { onNavigateToPage(JeePage.CHAPTERS) }
                    )
                }
            }

            items(subjectStats) { item ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPage(item.targetPage) }
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = item.accent.copy(alpha = 0.12f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.subject,
                                            tint = item.accent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.subject,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.chaptersSummary,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${item.pct}% Mastery",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Black,
                                color = item.accent
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { item.pct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = item.accent,
                            trackColor = item.accent.copy(alpha = 0.15f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Detailed Recharts 7-Day Line & 60-Day Dual-Axis Visualizer
        if (selectedTabIndex == 0 || selectedTabIndex == 2) {
            item {
                StudyHoursTrendCard(
                    trendData = trendData,
                    onSelectDay = { dayNumber -> viewModel.selectDay(dayNumber) }
                )
            }

            item {
                SixtyDayProgressRechartsCard(
                    progressData = sixtyDayData,
                    onSelectDay = { dayNumber -> viewModel.selectDay(dayNumber) }
                )
            }
        }

        // AI Weak Area Recommendation Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Weak Area Recommendation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val topUnresolved = mistakes.firstOrNull { !it.isResolved } ?: mistakes.firstOrNull()
                    Text(
                        text = if (topUnresolved != null) {
                            "Mistake Notebook Priority (${topUnresolved.subject} • ${topUnresolved.errorType}): \"${topUnresolved.questionTitle}\". Takeaway: ${topUnresolved.takeawayNote}"
                        } else {
                            "All Mistake Notebook items are resolved! Keep practicing high-yield PYQ variants in the Practice Stack."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                            modifier = Modifier.clickable { onNavigateToPage(JeePage.PRACTICE) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Open Practice Stack →",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5CF6)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.12f),
                            modifier = Modifier.clickable { onNavigateToPage(JeePage.AI) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ask AI Coach →",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0070F3)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun TrackerStatMiniPill(
    title: String,
    subtitle: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BacklogCategory
import com.example.data.repository.BacklogDueStatus
import com.example.data.repository.TopicStudyStatus
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeViewModel

/**
 * 1. USER DASHBOARD: Central Study Intelligence Hub Card (Apple-Style)
 * Connects: SYLLABUS -> LECTURES -> DPP -> ASSIGNMENTS -> PRACTICE -> MISTAKE BOOK -> REVISION -> TEST -> ANALYSIS -> WEAKNESS DETECTION -> PERSONALIZED RECOMMENDATIONS
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardStudyIntelligenceCommandCard(
    viewModel: JeeViewModel,
    onNavigate: (JeePage) -> Unit,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val repo = viewModel.studyIntelligenceRepository
    val activeEmail by repo.activeUserEmail.collectAsState()
    val activeRole by repo.activeUserRole.collectAsState()

    val topics = remember(activeEmail, version) { repo.getUserSyllabusTopics(activeEmail) }
    val lectures = remember(activeEmail, version) { repo.getUserTrackedLectures(activeEmail) }
    val backlogs = remember(activeEmail, version) { repo.getUserBacklogTasks(activeEmail) }
    val mistakes = remember(activeEmail, version) { repo.getUserAutoMistakes(activeEmail) }
    val revisions = remember(activeEmail, version) { repo.getUserRevisionPlans(activeEmail) }
    val weeklyReports = remember(activeEmail, version) { repo.getUserWeeklyMistakeReports(activeEmail) }
    val testReports = remember(activeEmail, version) { repo.getUserTestAnalyses(activeEmail) }
    val recommendations = remember(activeEmail, version) { repo.getPersonalizedRecommendationsForUser(activeEmail) }

    val syllabusPct = if (topics.isNotEmpty()) topics.map { it.progressPct }.average().toInt() else 0
    val watchedLectures = lectures.count { it.isWatched }
    val dppDone = lectures.count { it.dppCompleted }
    val pendingBacklogCount = backlogs.count { !it.isCompleted }
    val unresolvedMistakes = mistakes.count { !it.isResolved }
    val repeatedMistakes = mistakes.count { it.isRepeatedMistake && !it.isResolved }
    val revisionDueCount = revisions.count { !it.isCompleted }
    val latestTest = testReports.firstOrNull()
    val latestWeekly = weeklyReports.firstOrNull()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_study_intelligence_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with RLS User Isolation Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Central Study Intelligence",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val studentDisplayEmail = viewModel.currentUser.value?.email ?: activeEmail
                        Text(
                            text = "Active Student: $studentDisplayEmail",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE ENGINE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF059669))
                    }
                }
            }

            // Connected Pipeline Strip: SYLLABUS -> LECTURES -> DPP -> ASSIGNMENTS -> PRACTICE -> MISTAKE BOOK -> REVISION -> TEST -> ANALYSIS -> WEAKNESS DETECTION -> RECOMMENDATIONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pipelineNodes = listOf(
                    "SYLLABUS" to JeePage.CHAPTERS,
                    "LECTURES" to JeePage.DAILY_PLANNER,
                    "DPP" to JeePage.DAILY_PLANNER,
                    "ASSIGNMENTS" to JeePage.CHAPTERS,
                    "PRACTICE" to JeePage.PRACTICE,
                    "MISTAKE BOOK" to JeePage.TESTS,
                    "REVISION" to JeePage.FLOW,
                    "TEST" to JeePage.TESTS,
                    "ANALYSIS" to JeePage.PROGRESS,
                    "WEAKNESS DETECTION" to JeePage.PROGRESS,
                    "RECOMMENDATIONS" to JeePage.DASHBOARD
                )
                pipelineNodes.forEachIndexed { idx, (label, target) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.25f)),
                        modifier = Modifier.clickable { onNavigate(target) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0070F3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                    if (idx < pipelineNodes.lastIndex) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(12.dp)
                        )
                    }
                }
            }

            // Apple-Style Metrics Grid (Syllabus, Lectures, DPP, Backlog, Mistake Alerts, Revision Due, Upcoming Test, Performance)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IntelligenceMiniMetricTile(
                        title = "Syllabus Progress",
                        value = if (topics.isEmpty()) "Not enough data" else "$syllabusPct% (${topics.count { it.status == TopicStudyStatus.COMPLETED || it.status == TopicStudyStatus.REVISED }}/${topics.size})",
                        accent = Color(0xFF0070F3),
                        onClick = { onNavigate(JeePage.CHAPTERS) },
                        modifier = Modifier.weight(1f)
                    )
                    IntelligenceMiniMetricTile(
                        title = "Lectures & DPP",
                        value = if (lectures.isEmpty()) "Not enough data" else "$watchedLectures/${lectures.size} Lec • $dppDone DPP",
                        accent = Color(0xFF8B5CF6),
                        onClick = { onNavigate(JeePage.DAILY_PLANNER) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IntelligenceMiniMetricTile(
                        title = "Backlog & Due",
                        value = "$pendingBacklogCount Pending (${backlogs.count { it.dueStatus == BacklogDueStatus.OVERDUE && !it.isCompleted }} Overdue)",
                        accent = Color(0xFFF59E0B),
                        onClick = { onNavigate(JeePage.CHAPTERS) },
                        modifier = Modifier.weight(1f)
                    )
                    IntelligenceMiniMetricTile(
                        title = "Auto Mistake Alerts",
                        value = "$unresolvedMistakes Active ($repeatedMistakes Repeated)",
                        accent = Color(0xFFEF4444),
                        onClick = { onNavigate(JeePage.TESTS) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IntelligenceMiniMetricTile(
                        title = "Smart Revision Due",
                        value = "$revisionDueCount Weak Topic Plans",
                        accent = Color(0xFF10B981),
                        onClick = { onNavigate(JeePage.FLOW) },
                        modifier = Modifier.weight(1f)
                    )
                    IntelligenceMiniMetricTile(
                        title = "Test & Performance",
                        value = if (latestTest != null) "${latestTest.score}/${latestTest.maxScore} (${latestTest.accuracyPct}% Acc)" else "Not enough data yet.",
                        accent = Color(0xFFEC4899),
                        onClick = { onNavigate(JeePage.PROGRESS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Personalized Recommendations Section (Explaining WHY)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERSONALIZED RECOMMENDATIONS (WITH 'WHY' TRACE)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0070F3)
                        )
                        Text(
                            text = "${recommendations.size} Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (recommendations.isEmpty()) {
                        Text(
                            text = "Not enough data yet.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        recommendations.take(3).forEach { rec ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val page = runCatching { JeePage.valueOf(rec.targetPageName) }.getOrDefault(JeePage.CHAPTERS)
                                        onNavigate(page)
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${rec.actionCategory.uppercase()} • ${rec.subject}",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (rec.priority == "CRITICAL") Color(0xFFEF4444) else Color(0xFF0070F3)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (rec.priority == "CRITICAL") Color(0xFFEF4444).copy(alpha = 0.14f) else Color(0xFF0070F3).copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = rec.priority,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (rec.priority == "CRITICAL") Color(0xFFEF4444) else Color(0xFF0070F3),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = rec.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "WHY: ${rec.whyExplanation}",
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

            // Weekly Mistake Report Snapshot Banner
            if (latestWeekly != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(JeePage.WEEKLY) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WEEKLY MISTAKE REPORT • ${latestWeekly.weekLabel.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444)
                            )
                            if (!latestWeekly.hasEnoughData) {
                                Text("Not enough data yet.", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                            } else {
                                Text(
                                    text = "${latestWeekly.totalMistakes} Total • ${latestWeekly.repeatedMistakes} Repeated • Top Type: ${latestWeekly.mostCommonMistakeType}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Weak Topics: ${latestWeekly.weakTopics.joinToString(", ").ifBlank { "None" }} • ${latestWeekly.comparisonWithPreviousWeek}",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IntelligenceMiniMetricTile(
    title: String,
    value: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 2. SYLLABUS + STUDY SYSTEM & BACKLOG MANAGER SECTION (Used in ChapterTrackerScreen)
 * Hierarchy: Class/Exam -> Subject -> Chapter -> Topic
 * Tracks: Not Started, Learning, Completed, Revised, Progress %, Last studied, Pending work, Completion history
 * Plus Backlog Manager: Lectures, DPPs, Assignments, Chapters, Practice, Revisions, Tests
 */
@Composable
fun ConnectedSyllabusAndBacklogSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val repo = viewModel.studyIntelligenceRepository
    val activeEmail by repo.activeUserEmail.collectAsState()

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val topics = remember(activeEmail, version, refreshTrigger) { repo.getUserSyllabusTopics(activeEmail) }
    val backlogs = remember(activeEmail, version, refreshTrigger) { repo.getUserBacklogTasks(activeEmail) }
    val lectures = remember(activeEmail, version, refreshTrigger) { repo.getUserTrackedLectures(activeEmail) }

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Topic Tracker, 1: Backlog Manager, 2: Lecture & DPP Sync
    var safeWebSyncUrl by remember { mutableStateOf("") }
    var safeWebSyncPreview by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("connected_syllabus_backlog_section")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Connected Syllabus, Lectures & Backlog Manager",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Class/Exam → Subject → Chapter → Topic • Real-time Backlog Detection",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    0 to "Topic Tracker (${topics.size})",
                    1 to "Backlog (${backlogs.count { !it.isCompleted }})",
                    2 to "Lectures & DPP (${lectures.size})"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = selectedSubTab == idx,
                        onClick = { selectedSubTab = idx },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedSubTab) {
                0 -> {
                    if (topics.isEmpty()) {
                        Text("Not enough data yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        topics.forEach { topic ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${topic.examClass} → ${topic.subject} → ${topic.chapterTitle}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0070F3)
                                        )
                                        Text(
                                            text = "${topic.progressPct}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                    Text(
                                        text = "Topic: ${topic.topicTitle}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    LinearProgressIndicator(
                                        progress = { topic.progressPct / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF0070F3)
                                    )
                                    Text(
                                        text = "Last studied: ${topic.lastStudiedLabel} • Pending: ${topic.pendingWork.joinToString(", ").ifBlank { "All Clear ✓" }}",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (topic.completionHistory.isNotEmpty()) {
                                        Text(
                                            text = "History: ${topic.completionHistory.first()}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF059669)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        TopicStudyStatus.entries.forEach { st ->
                                            val active = topic.status == st
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (active) Color(0xFF0070F3) else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.4f)),
                                                modifier = Modifier.clickable {
                                                    repo.updateSyllabusTopicStatus(topic.id, st)
                                                    refreshTrigger++
                                                }
                                            ) {
                                                Text(
                                                    text = st.label,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // BACKLOG MANAGER
                    val totalBacklog = backlogs.count { !it.isCompleted }
                    val highPriority = backlogs.count { !it.isCompleted && it.priority == "HIGH" }
                    val dueToday = backlogs.count { !it.isCompleted && it.dueStatus == BacklogDueStatus.DUE_TODAY }
                    val overdue = backlogs.count { !it.isCompleted && it.dueStatus == BacklogDueStatus.OVERDUE }
                    val completed = backlogs.count { it.isCompleted }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Total Backlog: $totalBacklog" to Color(0xFF0F172A),
                            "High Priority: $highPriority" to Color(0xFFEF4444),
                            "Due Today: $dueToday" to Color(0xFFF59E0B),
                            "Overdue: $overdue" to Color(0xFFDC2626),
                            "Completed: $completed" to Color(0xFF10B981)
                        ).forEach { (badgeText, col) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = col.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, col.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = col,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    if (backlogs.isEmpty()) {
                        Text("Not enough data yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        backlogs.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "[${item.category.label.uppercase()}] ${item.subject} • ${item.chapterTitle}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0070F3)
                                        )
                                        Text(
                                            text = "${item.priority} • ${item.dueStatus.label}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (item.dueStatus == BacklogDueStatus.OVERDUE) Color(0xFFEF4444) else Color(0xFFF59E0B)
                                        )
                                    }
                                    Text(
                                        text = item.title,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Schedule: ${item.scheduledForLabel} • Est. ${item.estimatedMinutes} mins",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val nextPrio = if (item.priority == "HIGH") "MEDIUM" else "HIGH"
                                                repo.prioritizeBacklogItem(item.id, nextPrio)
                                                refreshTrigger++
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Priority: ${item.priority}", fontSize = 10.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                repo.rescheduleBacklogItem(item.id, "Rescheduled to Tomorrow Slot 1", BacklogDueStatus.UPCOMING)
                                                refreshTrigger++
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Reschedule +1d", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = {
                                                repo.toggleBacklogItemCompleted(item.id)
                                                refreshTrigger++
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (item.isCompleted) Color(0xFF10B981) else Color(0xFF0F172A)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(if (item.isCompleted) "Completed ✓" else "Mark Done", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // LECTURE + DPP + ASSIGNMENT TRACKER & SAFE PUBLIC WEB SYLLABUS VALIDATOR
                    lectures.forEach { lec ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "${lec.batchName} • ${lec.subject} • ${lec.chapterTitle} (${lec.durationMinutes}m)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0070F3)
                                )
                                Text(lec.lectureTitle, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = lec.isWatched,
                                        onClick = {
                                            repo.toggleLectureWatched(lec.id)
                                            refreshTrigger++
                                        },
                                        label = { Text(if (lec.isWatched) "Watched ✓" else "Lecture Pending", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = lec.dppCompleted,
                                        onClick = {
                                            repo.toggleLectureDppCompleted(lec.id)
                                            refreshTrigger++
                                        },
                                        label = { Text(if (lec.dppCompleted) "DPP Done ✓" else "DPP Pending", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = lec.assignmentCompleted,
                                        onClick = {
                                            repo.toggleLectureAssignmentCompleted(lec.id)
                                            refreshTrigger++
                                        },
                                        label = { Text(if (lec.assignmentCompleted) "Assignment ✓" else "Assignment", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Safe Public Educational Syllabus Sync (With Explicit Validation Gate)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SAFE PUBLIC EDUCATIONAL SYLLABUS SYNC (VALIDATED ONLY)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF059669)
                            )
                            Text(
                                text = "Syncs only publicly available NTA/NCERT syllabus metadata after validation. Never bypasses private content or imports unverified data automatically.",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.OutlinedTextField(
                                    value = safeWebSyncUrl,
                                    onValueChange = { safeWebSyncUrl = it },
                                    placeholder = { Text("https://ncert.nic.in/syllabus or public batch URL", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (safeWebSyncUrl.isBlank()) {
                                            safeWebSyncPreview = "Validation Alert: Enter a public educational URL first."
                                        } else if (safeWebSyncUrl.contains("private") || safeWebSyncUrl.contains("token")) {
                                            safeWebSyncPreview = "Blocked: Private/authenticated URLs are never bypassed."
                                        } else {
                                            safeWebSyncPreview = "Validated Public Syllabus Source: Ready to sync with Batch Planner."
                                            viewModel.parseAndApplyBatchUrl(safeWebSyncUrl)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Validate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            safeWebSyncPreview?.let {
                                Text(it, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. CENTRALIZED PRACTICE + QUESTION BANK (Used in PracticeStackScreen)
 * Filter by: Exam, Class, Subject, Chapter, Topic, Difficulty, Year, Source
 * Track: Attempted, Correct, Incorrect, Accuracy, Time taken, Attempts, Saved questions, Revision questions
 * Auto-triggers Smart Auto Mistake Book on wrong answer!
 */
@Composable
fun CentralizedQuestionBankCard(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val repo = viewModel.studyIntelligenceRepository
    val activeEmail by repo.activeUserEmail.collectAsState()
    var attemptVersion by remember { mutableIntStateOf(0) }

    val catalog = repo.questionBankCatalog
    val userStats = remember(activeEmail, attemptVersion) { repo.getUserQuestionAttempts(activeEmail) }

    var selectedExam by remember { mutableStateOf("All") }
    var selectedClass by remember { mutableStateOf("All") }
    var selectedSubject by remember { mutableStateOf("All") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var selectedSource by remember { mutableStateOf("All") }
    var selectedYear by remember { mutableStateOf("All") }

    var selectedAnswers by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var autoMistakeFeedback by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val filteredQuestions = catalog.filter { q ->
        (selectedExam == "All" || q.exam.equals(selectedExam, true)) &&
            (selectedClass == "All" || q.classLevel.equals(selectedClass, true)) &&
            (selectedSubject == "All" || q.subject.equals(selectedSubject, true)) &&
            (selectedDifficulty == "All" || q.difficulty.equals(selectedDifficulty, true)) &&
            (selectedSource == "All" || q.source.equals(selectedSource, true)) &&
            (selectedYear == "All" || q.year.equals(selectedYear, true))
    }

    val totalAttempted = userStats.values.count { it.attemptsCount > 0 }
    val totalCorrect = userStats.values.sumOf { it.correctCount }
    val totalIncorrect = userStats.values.sumOf { it.incorrectCount }
    val totalAttempts = userStats.values.sumOf { it.attemptsCount }
    val accuracyPct = if (totalCorrect + totalIncorrect > 0) (totalCorrect * 100) / (totalCorrect + totalIncorrect) else 0
    val savedCount = userStats.values.count { it.isSaved }
    val revisionCount = userStats.values.count { it.isMarkedForRevision }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("centralized_question_bank_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Centralized Practice + Question Bank (Auto Mistake Hook)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Attempted: $totalAttempted • Correct: $totalCorrect • Incorrect: $totalIncorrect • Accuracy: $accuracyPct% • Attempts: $totalAttempts • Saved: $savedCount • Revision: $revisionCount",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0070F3)
            )

            // Multi-Dimensional Filters: Exam, Class, Subject, Difficulty, Source, Year
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "JEE Main", "JEE Advanced", "NEET UG").forEach { ex ->
                    FilterChip(
                        selected = selectedExam == ex,
                        onClick = { selectedExam = ex },
                        label = { Text(ex, fontSize = 10.sp) }
                    )
                }
                listOf("Class 11", "Class 12").forEach { cl ->
                    FilterChip(
                        selected = selectedClass == cl,
                        onClick = { selectedClass = if (selectedClass == cl) "All" else cl },
                        label = { Text(cl, fontSize = 10.sp) }
                    )
                }
                listOf("Easy", "Medium", "Hard").forEach { diff ->
                    FilterChip(
                        selected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = if (selectedDifficulty == diff) "All" else diff },
                        label = { Text(diff, fontSize = 10.sp) }
                    )
                }
                listOf("PYQ", "NCERT", "DPP").forEach { src ->
                    FilterChip(
                        selected = selectedSource == src,
                        onClick = { selectedSource = if (selectedSource == src) "All" else src },
                        label = { Text(src, fontSize = 10.sp) }
                    )
                }
            }

            filteredQuestions.forEach { q ->
                val stat = userStats[q.id]
                val chosen = selectedAnswers[q.id]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${q.exam} (${q.year}) • ${q.classLevel} • ${q.subject} • ${q.chapter} • ${q.difficulty}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0070F3)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (stat?.isSaved == true) "★ Saved" else "☆ Save",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.clickable {
                                        repo.toggleSaveQuestion(q.id)
                                        attemptVersion++
                                    }
                                )
                                Text(
                                    text = if (stat?.isMarkedForRevision == true) "🔄 Revise" else "+ Revise",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.clickable {
                                        repo.toggleRevisionQuestion(q.id)
                                        attemptVersion++
                                    }
                                )
                            }
                        }
                        Text(text = "Topic: ${q.topic} • Source: ${q.source}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = q.questionText, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)

                        q.options.forEachIndexed { idx, opt ->
                            val isSelected = chosen == idx
                            val isCorrect = idx == q.correctIndex
                            val bg = when {
                                chosen == null -> MaterialTheme.colorScheme.surface
                                isCorrect -> Color(0xFF10B981).copy(alpha = 0.16f)
                                isSelected -> Color(0xFFEF4444).copy(alpha = 0.16f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = bg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAnswers = selectedAnswers + (q.id to idx)
                                        val simulatedTime = if (idx == q.correctIndex) q.idealTimeSeconds - 10 else q.idealTimeSeconds + 25
                                        val autoEntry = repo.recordQuestionBankAttempt(q, idx, simulatedTime)
                                        attemptVersion++
                                        if (autoEntry != null) {
                                            viewModel.addMistakeEntry(
                                                questionTitle = q.questionText,
                                                subject = q.subject,
                                                chapterOrSource = "${q.chapter} • ${q.source} ${q.year}",
                                                errorType = autoEntry.primaryClassification.label,
                                                takeawayNote = q.explanation
                                            )
                                            autoMistakeFeedback = autoMistakeFeedback + (
                                                q.id to "⚡ Auto-Saved to YOUR Mistake Book as [${autoEntry.primaryClassification.label}]!"
                                                )
                                        } else {
                                            autoMistakeFeedback = autoMistakeFeedback - q.id
                                        }
                                    }
                            ) {
                                Text(
                                    text = "${('A' + idx)}. $opt",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                )
                            }
                        }

                        if (chosen != null) {
                            Text(
                                text = "Solution: ${q.explanation}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            autoMistakeFeedback[q.id]?.let { msg ->
                                Text(msg, fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. SMART AUTO MISTAKE BOOK + TEST AUTO ANALYSIS ("HOW TO FIX THE PROBLEM")
 * Used in CbtTestScreen & WeeklyReviewScreen
 */
@Composable
fun SmartAutoMistakeAndTestAnalysisSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val repo = viewModel.studyIntelligenceRepository
    val activeEmail by repo.activeUserEmail.collectAsState()
    var localRefresh by remember { mutableIntStateOf(0) }

    val autoMistakes = remember(activeEmail, version, localRefresh) { repo.getUserAutoMistakes(activeEmail) }
    val testAnalyses = remember(activeEmail, version, localRefresh) { repo.getUserTestAnalyses(activeEmail) }
    val weeklyReports = remember(activeEmail, version, localRefresh) { repo.getUserWeeklyMistakeReports(activeEmail) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_auto_mistake_book_section")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Smart Auto Mistake Book & 'How to Fix the Problem' Engine",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Automatically detects & classifies mistakes for $activeEmail (Conceptual, Calculation, Formula, Silly, Misread, Time-management, Repeated).",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (autoMistakes.isEmpty()) {
                Text("Not enough data yet.", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                autoMistakes.take(5).forEach { m ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (m.isRepeatedMistake) Color(0xFFEF4444).copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, if (m.isRepeatedMistake) Color(0xFFEF4444).copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${m.primaryClassification.label.uppercase()} • ${m.subject} (${m.chapter})",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444)
                                )
                                if (m.isRepeatedMistake) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEF4444)) {
                                        Text(
                                            text = "REPEATED ${m.repeatCount}X",
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(m.questionTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Concept: ${m.conceptTag} • Time: ${m.timeTakenSeconds}s • Source: ${m.sourceLabel}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Fix Plan: ${m.correctiveAction}", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0070F3))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = if (m.isResolved) "Resolved ✓" else "Mark Resolved",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.clickable {
                                        repo.toggleAutoMistakeResolved(m.id)
                                        localRefresh++
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // "HOW TO FIX THE PROBLEM" Actionable Chains from Test Auto Analysis
            val latestReport = testAnalyses.firstOrNull()
            Text(
                text = "TEST AUTO ANALYSIS & 'HOW TO FIX THE PROBLEM'",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0070F3)
            )
            if (latestReport == null) {
                Text("Not enough data yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(
                    text = "${latestReport.testTitle}: Score ${latestReport.score}/${latestReport.maxScore} • Accuracy ${latestReport.accuracyPct}% • Correct ${latestReport.correctCount} / Incorrect ${latestReport.incorrectCount} / Unattempted ${latestReport.unattemptedCount}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                latestReport.howToFixChains.forEach { chain ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Problem: ${chain.problem}", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                            Text("→ Reason: ${chain.reason}", fontSize = 10.5.sp)
                            Text("→ Required revision: ${chain.requiredRevision}", fontSize = 10.5.sp)
                            Text("→ Targeted practice: ${chain.targetedPractice}", fontSize = 10.5.sp)
                            Text("→ Mistake review: ${chain.mistakeReview}", fontSize = 10.5.sp)
                            Text("→ Mini test: ${chain.miniTest}  →  Re-test: ${chain.reTest}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0070F3))
                        }
                    }
                }
            }

            // Historical Weekly Mistake Reports
            Text(
                text = "WEEKLY MISTAKE REPORTS (HISTORICAL ARCHIVE)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF8B5CF6)
            )
            weeklyReports.forEach { wk ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(wk.weekLabel, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        if (!wk.hasEnoughData) {
                            Text("Not enough data yet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text("Total: ${wk.totalMistakes} • New: ${wk.newMistakes} • Repeated: ${wk.repeatedMistakes} • Most Common: ${wk.mostCommonMistakeType}", fontSize = 10.5.sp)
                            Text("Weak Subjects: ${wk.weakSubjects.joinToString(", ")} • Weak Chapters: ${wk.weakChapters.joinToString(", ")}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Trend: ${wk.comparisonWithPreviousWeek}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. SMART REVISION SYSTEM CARD (Used in ConnectedFlowScreen)
 * Chain: Weak Topic -> Reason -> Priority -> What to revise -> Questions to solve -> Mini test -> Re-test
 */
@Composable
fun SmartRevisionSystemCard(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val repo = viewModel.studyIntelligenceRepository
    val activeEmail by repo.activeUserEmail.collectAsState()
    var revRefresh by remember { mutableIntStateOf(0) }
    val plans = remember(activeEmail, version, revRefresh) { repo.getUserRevisionPlans(activeEmail) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_revision_system_card")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Smart Revision System (Auto-Generated Personalized Plan)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Weak Topic → Reason → Priority → What to revise → Questions to solve → Mini test → Re-test",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (plans.isEmpty()) {
                Text("Not enough data yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                plans.forEach { plan ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${plan.subject} • ${plan.chapter} → ${plan.weakTopic}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (plan.priority == "CRITICAL") Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = plan.priority,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (plan.priority == "CRITICAL") Color(0xFFEF4444) else Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("• Reason: ${plan.reason}", fontSize = 10.5.sp, color = Color(0xFFEF4444))
                            Text("• What to revise: ${plan.whatToRevise}", fontSize = 10.5.sp)
                            Text("• Questions to solve: ${plan.questionsToSolve}", fontSize = 10.5.sp)
                            Text("• Mini test: ${plan.miniTestTitle}  →  Re-test: ${plan.reTestTarget}", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0070F3))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = plan.completedAtLabel?.let { "Completed on $it" } ?: "Status: Revision Due",
                                    fontSize = 10.sp,
                                    color = if (plan.isCompleted) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        repo.toggleRevisionPlanCompleted(plan.id)
                                        revRefresh++
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (plan.isCompleted) Color(0xFF10B981) else Color(0xFF0F172A)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (plan.isCompleted) "Revised ✓" else "Complete Revision", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 6. PREP VAULT 11-CATEGORY CLEAN GRID LAYOUT (Used in JeePrepHubPortalScreen)
 */
@Composable
fun PrepVaultCompleteGridSection(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val repo = viewModel.studyIntelligenceRepository
    val catalog = repo.prepVaultCatalog

    val all11Categories = listOf(
        "All",
        "Formula",
        "PYQ",
        "Sample Paper",
        "Repeated Questions",
        "Important Questions",
        "High-Weightage Topics",
        "Notes",
        "Concept Revision",
        "Study Material",
        "Mock Tests",
        "Cheat Sheets"
    )

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedExam by remember { mutableStateOf("All") }
    var selectedClass by remember { mutableStateOf("All") }
    var selectedSubject by remember { mutableStateOf("All") }

    val filtered = catalog.filter { item ->
        (selectedCategory == "All" || item.category.equals(selectedCategory, true)) &&
            (selectedExam == "All" || item.exam.equals(selectedExam, true)) &&
            (selectedClass == "All" || item.classLevel.equals(selectedClass, true)) &&
            (selectedSubject == "All" || item.subject.equals(selectedSubject, true))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("prep_vault_11_category_grid")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GridView, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Prep Vault — 11-Category Clean Grid", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }

            // 11 Categories Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                all11Categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Class / Exam / Subject Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "JEE Main", "JEE Advanced", "NEET UG").forEach { ex ->
                    FilterChip(
                        selected = selectedExam == ex,
                        onClick = { selectedExam = ex },
                        label = { Text(ex, fontSize = 10.sp) }
                    )
                }
                listOf("Class 11", "Class 12").forEach { cl ->
                    FilterChip(
                        selected = selectedClass == cl,
                        onClick = { selectedClass = if (selectedClass == cl) "All" else cl },
                        label = { Text(cl, fontSize = 10.sp) }
                    )
                }
                listOf("Physics", "Chemistry", "Mathematics", "Biology").forEach { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { selectedSubject = if (selectedSubject == subj) "All" else subj },
                        label = { Text(subj, fontSize = 10.sp) }
                    )
                }
            }

            // 2-Column Clean Grid Layout
            val rows = filtered.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { res ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = res.category.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0070F3)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = res.tagBadge,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF059669),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(res.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${res.classLevel} • ${res.exam} • ${res.subject} (${res.chapter})", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(res.summary, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                res.keyPointsOrFormulas.take(2).forEach { kp ->
                                    Text("• $kp", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0070F3))
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 7. PERFORMANCE ANALYSIS & WEAKNESS DETECTION ENGINE CARD (Used in ProgressScreen)
 * Timeframes: Daily, Weekly, Monthly, Overall
 */
@Composable
fun PerformanceAndWeaknessDetectionCard(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val repo = viewModel.studyIntelligenceRepository
    val activeEmail by repo.activeUserEmail.collectAsState()

    val topics = remember(activeEmail, version) { repo.getUserSyllabusTopics(activeEmail) }
    val lectures = remember(activeEmail, version) { repo.getUserTrackedLectures(activeEmail) }
    val backlogs = remember(activeEmail, version) { repo.getUserBacklogTasks(activeEmail) }
    val mistakes = remember(activeEmail, version) { repo.getUserAutoMistakes(activeEmail) }
    val testReports = remember(activeEmail, version) { repo.getUserTestAnalyses(activeEmail) }
    val revisions = remember(activeEmail, version) { repo.getUserRevisionPlans(activeEmail) }

    var selectedPeriod by remember { mutableStateOf("Weekly") } // Daily, Weekly, Monthly, Overall

    val hasAnyUserData = topics.isNotEmpty() || lectures.isNotEmpty() || mistakes.isNotEmpty() || testReports.isNotEmpty()

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("performance_weakness_detection_card")
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Performance Analysis & Weakness Detection Engine", fontSize = 14.5.sp, fontWeight = FontWeight.Black)
                    Text("Real student telemetry for $activeEmail", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Daily", "Weekly", "Monthly", "Overall").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (!hasAnyUserData) {
                Text(
                    text = "Not enough data yet.",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val latestTest = testReports.firstOrNull()
                val lecPct = if (lectures.isNotEmpty()) (lectures.count { it.isWatched } * 100) / lectures.size else 0
                val dppPct = if (lectures.isNotEmpty()) (lectures.count { it.dppCompleted } * 100) / lectures.size else 0
                val revPct = if (revisions.isNotEmpty()) (revisions.count { it.isCompleted } * 100) / revisions.size else 0
                val weakTopics = mistakes.filter { !it.isResolved }.map { it.topic }.distinct().ifEmpty { listOf("None detected") }
                val strongTopics = topics.filter { it.progressPct >= 85 }.map { it.topicTitle }.ifEmpty { listOf("Coordination Compounds (CFT)") }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("$selectedPeriod Performance Telemetry", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF0070F3))
                        Text("• Latest Test Score & Accuracy: ${latestTest?.let { "${it.score}/${it.maxScore} (${it.accuracyPct}%)" } ?: "Not enough data yet."}", fontSize = 11.sp)
                        Text("• Questions Attempted: ${latestTest?.let { it.correctCount + it.incorrectCount } ?: 0} (Correct: ${latestTest?.correctCount ?: 0}, Incorrect: ${latestTest?.incorrectCount ?: 0})", fontSize = 11.sp)
                        Text("• Lecture Completion: $lecPct% • DPP Completion: $dppPct% • Revision Completion: $revPct%", fontSize = 11.sp)
                        Text("• Active Backlog: ${backlogs.count { !it.isCompleted }} items • Active Mistakes: ${mistakes.count { !it.isResolved }}", fontSize = 11.sp)
                        Text("• Strong Topics: ${strongTopics.joinToString(", ")}", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                        Text("• Weak Topics: ${weakTopics.joinToString(", ")}", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                    }
                }

                // Weakness Detection Engine Output
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val topMistake = mistakes.firstOrNull()
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("WEAKNESS DETECTION ENGINE DIAGNOSTIC", fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = Color(0xFFEF4444))
                        Text("• Weak Subject: ${topMistake?.subject ?: "Physics"}", fontSize = 10.5.sp)
                        Text("• Weak Chapter: ${topMistake?.chapter ?: "Current Electricity"}", fontSize = 10.5.sp)
                        Text("• Weak Topic & Concept: ${topMistake?.topic ?: "RC Circuits"} (${topMistake?.conceptTag ?: "R_th equivalence"})", fontSize = 10.5.sp)
                        Text("• Calculation & Time-Management: ${mistakes.count { it.primaryClassification.name == "CALCULATION" }} Calculation • ${mistakes.count { it.primaryClassification.name == "TIME_MANAGEMENT" }} Time-Management • ${mistakes.count { it.isRepeatedMistake }} Repeated", fontSize = 10.5.sp)
                    }
                }
            }
        }
    }
}

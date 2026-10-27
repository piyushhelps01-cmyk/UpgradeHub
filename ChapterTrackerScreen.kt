package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterItem
import com.example.data.repository.JeeData
import com.example.ui.components.ChapterListOverallProgressSummary
import com.example.ui.components.ChapterTaskProgressComponent
import com.example.ui.components.GlassmorphicContainer
import com.example.ui.components.NcertSyllabusPlannerWorkflowCard
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterTrackerScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val allChapters by viewModel.activeChapters.collectAsState()
    val ncertChapters by viewModel.ncertChapters.collectAsState()
    val availableSubjects by viewModel.activeSubjects.collectAsState()
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val parallelBatches by viewModel.parallelBatches.collectAsState()
    val batchTargetDays by viewModel.batchTargetDays.collectAsState()
    val isNeet by viewModel.isNeetMode.collectAsState()
    var selectedPriorityFilter by remember { mutableStateOf("All") } // "All", "HIGH", "BACKLOG", "COMPLETED"

    val filteredChapters = allChapters.filter { chap ->
        val matchesSubject = (uiState.selectedSubjectFilter == "All" || chap.subject == uiState.selectedSubjectFilter)
        val matchesSearch = uiState.chapterSearchQuery.isEmpty() ||
                chap.title.contains(uiState.chapterSearchQuery, ignoreCase = true) ||
                chap.questionFocus.contains(uiState.chapterSearchQuery, ignoreCase = true) ||
                chap.subject.contains(uiState.chapterSearchQuery, ignoreCase = true)
        val chain = viewModel.getChapterChain(chap.id)
        val matchesPriority = when (selectedPriorityFilter) {
            "HIGH" -> chap.priority.equals("HIGH", ignoreCase = true)
            "BACKLOG" -> !chain.isComplete()
            "COMPLETED" -> chain.isComplete()
            else -> true
        }
        matchesSubject && matchesSearch && matchesPriority
    }

    val completedCount = allChapters.count { viewModel.getChapterChain(it.id).isComplete() }
    val progressPct = if (allChapters.isNotEmpty()) (completedCount * 100) / allChapters.size else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header & Rule Card
        item {
            GlassmorphicContainer(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                backgroundColor = MaterialTheme.colorScheme.surface,
                rimHighlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                rimShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
                glowBorder = Color(0xFF0071E3).copy(alpha = 0.15f),
                elevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isNeet) "NEET Chapter Tracker" else "Master Chapter Tracker",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isNeet) Color(0xFFE8F5E9) else Color(0xFF0071E3).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = selectedBatch.title,
                                    color = if (isNeet) Color(0xFF2E7D32) else Color(0xFF0071E3),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$completedCount of ${allChapters.size} Chapters Complete ($progressPct%)",
                                fontSize = 13.sp,
                                color = Color(0xFF0071E3),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isNeet) Color(0xFFC8E6C9) else Color(0xFFC4EED0)
                        ) {
                            Text(
                                text = "${allChapters.size} Chapters",
                                color = if (isNeet) Color(0xFF1B5E20) else Color(0xFF00210E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Material 3 Overall Chapter & Remaining Tasks Progress Tracker
                    ChapterListOverallProgressSummary(
                        chapters = filteredChapters.ifEmpty { allChapters },
                        getChain = { chapId -> viewModel.getChapterChain(chapId) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Very Important Rule Callout
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.38f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isNeet) {
                                    "NEET CRUCIAL: Do not count a lecture as completed chapter until the full mastery chain is completed: Lectures → Detailed Notes → DPP MCQs → NCERT Line-by-Line → Prarambh → Prabal → PYQ (2018-2025) → Revision → Test"
                                } else {
                                    "RULE: Do not count a lecture as a completed chapter. Mark COMPLETE only after the full chain: Lectures → Notes → DPP → 12th NCERT → Prarambh → Prabal → Prakshit → PYQ → Revision → Test"
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // NCERT -> Syllabus -> Planner Pipeline inside Chapter Tracker
        item {
            NcertSyllabusPlannerWorkflowCard(
                ncertMasteredCount = ncertChapters.count { it.isCompleted },
                totalChaptersCount = allChapters.size.coerceAtLeast(1),
                syllabusCompletedCount = completedCount,
                activePlannerDays = batchTargetDays,
                parallelBatchesCount = parallelBatches.size.coerceAtLeast(1),
                onNavigate = { page -> viewModel.setPage(page) },
                onSyncWorkflowToPlanner = {
                    viewModel.syncAllPendingNcertToSyllabusAndPlanner()
                }
            )
        }

        // Connected Study/Syllabus Topic Tracker (Class/Exam -> Subject -> Chapter -> Topic) + Backlog Manager + Lecture/DPP Sync
        item {
            com.example.ui.components.ConnectedSyllabusAndBacklogSection(
                viewModel = viewModel
            )
        }

        // Search Bar & Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.chapterSearchQuery,
                    onValueChange = { viewModel.setChapterSearchQuery(it) },
                    placeholder = {
                        Text(if (isNeet) "Search Botany, Zoology, Physics, Chemistry chapters..." else "Search by chapter, topic, or question focus...")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.chapterSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setChapterSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Filter horizontal chips
                val subjects = availableSubjects
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { s ->
                        FilterChip(
                            selected = (uiState.selectedSubjectFilter == s),
                            onClick = { viewModel.setSubjectFilter(s) },
                            label = { Text(s, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isNeet) Color(0xFF00897B) else Color(0xFF6574FF),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Weightage & Backlog Filter + Bulk Milestone Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "All" to "All Chapters",
                        "HIGH" to "🔥 High Weightage",
                        "BACKLOG" to "⏳ Pending Backlog",
                        "COMPLETED" to "✓ Completed"
                    ).forEach { (key, label) ->
                        val isSel = selectedPriorityFilter == key
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { selectedPriorityFilter = key }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 1-Tap Bulk Milestone Update Strip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⚡ BULK MILESTONE ACTIONS (${filteredChapters.size} shown chapters):",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0070F3)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "lecture" to "✓ All Lectures",
                                "notes" to "✓ All Notes",
                                "ncert" to "✓ All NCERT",
                                "pyq" to "✓ All PYQs"
                            ).forEach { (key, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.14f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        Color(0xFF10B981).copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.bulkMarkSubjectMilestone(
                                            chapterIds = filteredChapters.map { it.id },
                                            milestoneKey = key,
                                            markCompleted = true
                                        )
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chapter Items
        items(filteredChapters, key = { it.id }) { chapter ->
            ChapterItemCard(
                chapter = chapter,
                viewModel = viewModel,
                version = version
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterItemCard(
    chapter: ChapterItem,
    viewModel: JeeViewModel,
    version: Int
) {
    val chain = viewModel.getChapterChain(chapter.id)
    val isAllComplete = chain.isComplete()
    val completedCount = chain.completedCount()

    GlassmorphicContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = if (isAllComplete) Color(0xFF063624).copy(alpha = 0.75f) else MaterialTheme.colorScheme.surface,
        rimHighlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        rimShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        glowBorder = if (isAllComplete) Color(0xFF10B981).copy(alpha = 0.45f) else Color(0xFF0071E3).copy(alpha = 0.12f),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: Subject, priority tag, status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            chapter.subject.contains("Biology", ignoreCase = true) || chapter.subject.contains("Bio", ignoreCase = true) -> Color(0xFFC8E6C9)
                            chapter.subject.contains("Physics", ignoreCase = true) -> Color(0xFFEADDFF)
                            chapter.subject.contains("Maths", ignoreCase = true) -> Color(0xFFC2E7FF)
                            else -> Color(0xFFC4EED0)
                        }
                    ) {
                        Text(
                            text = chapter.subject,
                            color = when {
                                chapter.subject.contains("Biology", ignoreCase = true) || chapter.subject.contains("Bio", ignoreCase = true) -> Color(0xFF1B5E20)
                                chapter.subject.contains("Physics", ignoreCase = true) -> Color(0xFF21005D)
                                chapter.subject.contains("Maths", ignoreCase = true) -> Color(0xFF001D35)
                                else -> Color(0xFF00210E)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (chapter.priority == "HIGH") Color(0xFFFFD8E4) else Color(0xFFFFDEA5)
                    ) {
                        Text(
                            text = "${chapter.priority} PRIORITY",
                            color = if (chapter.priority == "HIGH") Color(0xFFB3261E) else Color(0xFF7A4500),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAllComplete) Color(0xFF00875A) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val remainingCount = chain.remainingCount()
                    Text(
                        text = if (isAllComplete) "COMPLETED ✓" else "$remainingCount Tasks Left ($completedCount/10)",
                        color = if (isAllComplete) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chapter Title
            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Lectures & Question Focus
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${chapter.lectures} lectures • ${chapter.durationHours} hrs",
                    fontSize = 12.sp,
                    color = Color(0xFF5266CF),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Question Focus: ${chapter.questionFocus}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            // AI Prerequisite Dependency & Exam Weightage Strip
            val prereqInfo = when {
                chapter.subject.contains("Physics", ignoreCase = true) ->
                    "Prerequisite: Vectors & Calculus • Est. Weightage: ${if (chapter.priority == "HIGH") "8–12 Marks (2–3 Qs)" else "4 Marks (1 Q)"}"
                chapter.subject.contains("Chem", ignoreCase = true) ->
                    "Prerequisite: Mole Concept & Bonding • Est. Weightage: ${if (chapter.priority == "HIGH") "8–12 Marks (2–3 Qs)" else "4 Marks (1 Q)"}"
                chapter.subject.contains("Bio", ignoreCase = true) ->
                    "Prerequisite: NCERT Diagrams & Terminology • Est. Weightage: ${if (chapter.priority == "HIGH") "16–24 Marks (4–6 Qs)" else "8 Marks (2 Qs)"}"
                else ->
                    "Prerequisite: Functions & Algebraic Identities • Est. Weightage: ${if (chapter.priority == "HIGH") "8–12 Marks (2–3 Qs)" else "4 Marks (1 Q)"}"
            }
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0070F3).copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔗 $prereqInfo",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0070F3),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Material 3 Per-Chapter Visual Progress Tracking Component (Completed vs Remaining Tasks)
            ChapterTaskProgressComponent(
                chapterId = chapter.id,
                chain = chain,
                onQuickCompleteTask = { taskKey ->
                    viewModel.toggleChapterCheck(chapter.id, taskKey, false)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 10 Milestones Chain Ticks
            Text(
                text = "Workflow Chain (Tap to toggle):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            val milestones = listOf(
                "lecture" to "Lecture",
                "notes" to "Notes",
                "dpp" to "DPP",
                "ncert" to "12th NCERT",
                "prarambh" to "Prarambh",
                "prabal" to "Prabal",
                "prakshit" to "Prakshit",
                "pyq" to "PYQ",
                "revision" to "Revision",
                "test" to "Test"
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                milestones.forEach { (key, name) ->
                    val checked = when (key) {
                        "lecture" -> chain.lecture
                        "notes" -> chain.notes
                        "dpp" -> chain.dpp
                        "ncert" -> chain.ncert
                        "prarambh" -> chain.prarambh
                        "prabal" -> chain.prabal
                        "prakshit" -> chain.prakshit
                        "pyq" -> chain.pyq
                        "revision" -> chain.revision
                        "test" -> chain.test
                        else -> false
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (checked) Color(0xFFC4EED0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (checked) Color(0xFF00875A) else Color.Transparent
                        ),
                        modifier = Modifier.clickable {
                            viewModel.toggleChapterCheck(chapter.id, key, checked)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (checked) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00875A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                                color = if (checked) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions: Queue in Practice Stack & Mark All / Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isAllComplete) {
                    Text(
                        text = "+ Queue in Practice Stack",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3),
                        modifier = Modifier
                            .clickable {
                                viewModel.addToPracticeQueue(
                                    chapter = chapter,
                                    priority = "HIGH",
                                    focusArea = "PYQs & Question Practice",
                                    targetMinutes = 90,
                                    sessionSlot = "Next Session",
                                    notes = "Queued from Chapter Tracker"
                                )
                            }
                            .testTag("quick_queue_chapter_${chapter.id}")
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = if (isAllComplete) "Clear Chain" else "Mark Full Chain Complete",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0071E3),
                    modifier = Modifier.clickable {
                        viewModel.markFullChapterChain(chapter.id, !isAllComplete)
                    }
                )
            }
        }
    }
}

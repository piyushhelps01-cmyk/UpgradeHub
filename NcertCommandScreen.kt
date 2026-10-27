package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.NcertChapterEntity
import com.example.data.repository.JeeData
import com.example.ui.components.NcertSyllabusPlannerWorkflowCard
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel

/**
 * NCERT Master Checklist: Room Database backed checklist tracker for NCERT chapters.
 * Tracks 7 distinct learning milestones per chapter, personal high-yield exception notes,
 * and overall syllabus mastery.
 */
@Composable
fun NcertCommandScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val rawNcertList by viewModel.ncertChapters.collectAsStateWithLifecycle()
    val activeChapters by viewModel.activeChapters.collectAsStateWithLifecycle()
    val isNeet by viewModel.isNeetMode.collectAsStateWithLifecycle()
    val batchTargetDays by viewModel.batchTargetDays.collectAsStateWithLifecycle()
    val parallelBatches by viewModel.parallelBatches.collectAsStateWithLifecycle()
    val version by viewModel.version.collectAsStateWithLifecycle()

    // Filter rawNcertList to only chapters that belong to the active curriculum
    val activeChapterIds = remember(activeChapters) { activeChapters.map { it.id }.toSet() }
    val ncertList: List<NcertChapterEntity> = remember(rawNcertList, activeChapters) {
        val matching = rawNcertList.filter { it.chapterId in activeChapterIds }
        if (matching.isNotEmpty()) {
            matching
        } else {
            activeChapters.map {
                NcertChapterEntity(
                    chapterId = it.id,
                    chapterTitle = it.title,
                    subject = it.subject,
                    priority = it.priority
                )
            }
        }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    // Aggregate statistics from Room
    val totalChapters = ncertList.size
    val masteredChapters = ncertList.count { it.isCompleted }
    val totalMilestones = totalChapters * 7
    val completedMilestones = ncertList.sumOf { it.completedStepsCount }
    val overallMilestonePct = if (totalMilestones > 0) (completedMilestones * 100) / totalMilestones else 0

    // Filtered list
    val filteredList = ncertList.filter { chapter ->
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Biology" -> chapter.subject.contains("Bio", ignoreCase = true)
            "12 Chemistry" -> chapter.subject == "12 Chemistry"
            "11 Chemistry" -> chapter.subject == "11 Chemistry"
            "Chemistry" -> chapter.subject.contains("Chem", ignoreCase = true)
            "Physics" -> chapter.subject.contains("Physics", ignoreCase = true)
            "Maths" -> chapter.subject.contains("Maths", ignoreCase = true)
            "Mastered" -> chapter.isCompleted
            "Pending" -> !chapter.isCompleted
            "High Priority" -> chapter.priority == "HIGH"
            else -> true
        }

        val matchesSearch = searchQuery.isBlank() ||
                chapter.chapterTitle.contains(searchQuery, ignoreCase = true) ||
                chapter.subject.contains(searchQuery, ignoreCase = true) ||
                chapter.notes.contains(searchQuery, ignoreCase = true)

        matchesFilter && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Hero Stats Card backed by Room
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ncert_hero_stats_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                                color = Color(0xFF00875A).copy(alpha = 0.15f)
                            ) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color(0xFF00875A),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NCERT Master Checklist",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Persisted securely in Room Database",
                                    fontSize = 11.sp,
                                    color = Color(0xFF00875A),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (masteredChapters > 0) Color(0xFFC4EED0) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (masteredChapters > 0) Color(0xFF00875A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$masteredChapters/$totalChapters Mastered",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (masteredChapters > 0) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar across all milestones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Milestones Progress: $completedMilestones / $totalMilestones steps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$overallMilestonePct%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00875A)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { overallMilestonePct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF00875A),
                        trackColor = Color(0xFF00875A).copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic Subject Summary Badges (Adapts for NEET vs JEE / Boards)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isNeet) {
                            val bioCount = ncertList.filter { it.subject.contains("Bio", ignoreCase = true) || it.subject.contains("Botany", ignoreCase = true) || it.subject.contains("Zoology", ignoreCase = true) }.count { it.isCompleted }
                            val bioTotal = ncertList.count { it.subject.contains("Bio", ignoreCase = true) || it.subject.contains("Botany", ignoreCase = true) || it.subject.contains("Zoology", ignoreCase = true) }.coerceAtLeast(1)
                            val chemCount = ncertList.filter { it.subject.contains("Chem", ignoreCase = true) }.count { it.isCompleted }
                            val chemTotal = ncertList.count { it.subject.contains("Chem", ignoreCase = true) }
                            val phyCount = ncertList.filter { it.subject.contains("Physics", ignoreCase = true) }.count { it.isCompleted }
                            val phyTotal = ncertList.count { it.subject.contains("Physics", ignoreCase = true) }

                            SubjectSummaryChip(
                                title = "Biology",
                                count = "$bioCount/$bioTotal",
                                color = Color(0xFF00875A),
                                modifier = Modifier.weight(1f)
                            )
                            SubjectSummaryChip(
                                title = "Chemistry",
                                count = "$chemCount/$chemTotal",
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                            SubjectSummaryChip(
                                title = "Physics",
                                count = "$phyCount/$phyTotal",
                                color = Color(0xFF5266CF),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            val chem12Count = ncertList.filter { it.subject == "12 Chemistry" }.count { it.isCompleted }
                            val chem12Total = ncertList.count { it.subject == "12 Chemistry" }

                            val chem11Count = ncertList.filter { it.subject == "11 Chemistry" }.count { it.isCompleted }
                            val chem11Total = ncertList.count { it.subject == "11 Chemistry" }

                            val phyCount = ncertList.filter { it.subject.contains("Physics") }.count { it.isCompleted }
                            val phyTotal = ncertList.count { it.subject.contains("Physics") }

                            SubjectSummaryChip(
                                title = "12 Chem",
                                count = "$chem12Count/$chem12Total",
                                color = Color(0xFF00875A),
                                modifier = Modifier.weight(1f)
                            )
                            SubjectSummaryChip(
                                title = "11 Chem",
                                count = "$chem11Count/$chem11Total",
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                            SubjectSummaryChip(
                                title = "Physics",
                                count = "$phyCount/$phyTotal",
                                color = Color(0xFF5266CF),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons (Backup, Snapshot, Reset)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.exportNcertProgressJson { json ->
                                    clipboardManager.setText(AnnotatedString(json))
                                    viewModel.showSnackbar("NCERT progress snapshot JSON copied to clipboard!")
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_ncert_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { showImportDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("import_ncert_button")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // NCERT -> Syllabus -> Planner Workflow Pipeline Card
        item {
            val syllabusDoneCount = remember(activeChapters, version) {
                activeChapters.count { ch ->
                    val chain = viewModel.getChapterChain(ch.id)
                    chain.ncert || chain.lecture
                }
            }
            NcertSyllabusPlannerWorkflowCard(
                ncertMasteredCount = masteredChapters,
                totalChaptersCount = totalChapters.coerceAtLeast(1),
                syllabusCompletedCount = syllabusDoneCount,
                activePlannerDays = batchTargetDays,
                parallelBatchesCount = parallelBatches.size.coerceAtLeast(1),
                onNavigate = { page ->
                    when (page) {
                        com.example.ui.viewmodel.JeePage.CHAPTERS -> onNavigate("chapters")
                        com.example.ui.viewmodel.JeePage.DAILY_PLANNER -> onNavigate("daily_planner")
                        else -> {}
                    }
                },
                onSyncWorkflowToPlanner = {
                    viewModel.syncAllPendingNcertToSyllabusAndPlanner()
                }
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ncert_search_input"),
                placeholder = { Text("Search NCERT chapters or notes...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Filter Chips
        item {
            val filterOptions = if (isNeet) {
                listOf(
                    "All",
                    "Biology",
                    "Physics",
                    "Chemistry",
                    "High Priority",
                    "Pending",
                    "Mastered"
                )
            } else {
                listOf(
                    "All",
                    "12 Chemistry",
                    "11 Chemistry",
                    "Physics",
                    "Maths",
                    "High Priority",
                    "Pending",
                    "Mastered"
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = (selectedFilter == filter)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00875A),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("ncert_filter_${filter.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        // Checklist List of NCERT Chapters
        if (filteredList.isEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No chapters matching '$searchQuery'",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.chapterId }) { chapter ->
                NcertChapterCard(
                    chapter = chapter,
                    onToggleStep = { stepKey ->
                        viewModel.toggleNcertStep(chapter.chapterId, stepKey)
                    },
                    onMarkAll = { completed ->
                        viewModel.markAllNcertSteps(chapter.chapterId, completed)
                    },
                    onPushToSyllabusAndPlanner = {
                        viewModel.pushNcertChapterToSyllabusAndPlanner(
                            chapterId = chapter.chapterId,
                            chapterTitle = chapter.chapterTitle,
                            subject = chapter.subject
                        )
                    },
                    onSaveNotes = { notes ->
                        viewModel.updateNcertNotes(chapter.chapterId, notes)
                    },
                    onConvertToFlashcard = { exceptionText ->
                        val cleanAnswer = exceptionText.ifBlank {
                            "High-yield NCERT exception, table order & in-text checkpoint for ${chapter.chapterTitle}"
                        }
                        viewModel.addFlashcard(
                            subject = chapter.subject,
                            chapter = chapter.chapterTitle,
                            question = "NCERT Line-by-Line Exception: ${chapter.chapterTitle}",
                            answer = cleanAnswer,
                            difficulty = "NCERT Exception"
                        )
                        viewModel.showSnackbar("🗂️ Sent '${chapter.chapterTitle}' exception to Flashcard Deck!")
                    },
                    onSaveToMistakeLog = { exceptionText ->
                        val noteBody = exceptionText.ifBlank {
                            "Revise NCERT in-text examples & exception tables for ${chapter.chapterTitle}"
                        }
                        viewModel.addMistakeEntry(
                            questionTitle = "NCERT Exception Trap: ${chapter.chapterTitle}",
                            subject = chapter.subject,
                            chapterOrSource = "NCERT Checklist • ${chapter.chapterTitle}",
                            errorType = "Concept Gap",
                            takeawayNote = noteBody
                        )
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset NCERT Progress in Room?") },
            text = { Text("This will reset all 7 checklist milestones and saved notes across all NCERT chapters to their initial unchecked state.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetNcertProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import NCERT Snapshot") },
            text = {
                Column {
                    Text("Paste your exported NCERT JSON array below:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("[{\"chapterId\": 0, \"theoryRead\": true, ...}]") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotEmpty()) {
                            viewModel.importNcertProgressJson(importJsonText) { success ->
                                if (success) showImportDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00875A))
                ) {
                    Text("Import to Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Individual Card displaying an NCERT Chapter, its 7-stage completion checklist,
 * progress bar, and expandable notes drawer for recording exceptions and formulas.
 */
@Composable
fun NcertChapterCard(
    chapter: NcertChapterEntity,
    onToggleStep: (String) -> Unit,
    onMarkAll: (Boolean) -> Unit,
    onPushToSyllabusAndPlanner: () -> Unit = {},
    onSaveNotes: (String) -> Unit,
    onConvertToFlashcard: (String) -> Unit = {},
    onSaveToMistakeLog: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expandedNotes by remember { mutableStateOf(false) }
    var notesText by remember(chapter.notes) { mutableStateOf(chapter.notes) }

    val subjectColor = when {
        chapter.subject.contains("Chemistry") -> Color(0xFF00875A)
        chapter.subject.contains("Physics") -> Color(0xFF5266CF)
        else -> Color(0xFF7B1FA2)
    }

    val isAllDone = chapter.isCompleted

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ncert_card_${chapter.chapterId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Subject, Priority & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = subjectColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = chapter.subject,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = subjectColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (chapter.priority == "HIGH") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFF8F00).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "HIGH YIELD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }

                // Completion Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAllDone) Color(0xFFC4EED0) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isAllDone) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00875A),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = if (isAllDone) "7/7 Mastered ✓" else "${chapter.completedStepsCount}/7 Done (${chapter.completionPercentage}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllDone) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter Title
            Text(
                text = chapter.chapterTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Indicator
            LinearProgressIndicator(
                progress = { chapter.completedStepsCount / 7f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAllDone) Color(0xFF00875A) else subjectColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7 Checklist Items
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NcertCheckItem(
                    title = "Line-by-Line Reading",
                    subtitle = "Thorough reading of theory, marginal callouts & chapter summary",
                    checked = chapter.theoryRead,
                    icon = Icons.Default.Book,
                    accentColor = subjectColor,
                    testTag = "step_read_${chapter.chapterId}",
                    onToggle = { onToggleStep("read") }
                )

                NcertCheckItem(
                    title = "In-Text Examples",
                    subtitle = "Solved in-text illustrations and checkpoints without looking",
                    checked = chapter.inTextExamples,
                    icon = Icons.Default.Edit,
                    accentColor = subjectColor,
                    testTag = "step_examples_${chapter.chapterId}",
                    onToggle = { onToggleStep("examples") }
                )

                NcertCheckItem(
                    title = "Back Exercises & Numericals",
                    subtitle = "Subjective, numerical & conceptual end-of-chapter questions",
                    checked = chapter.backExercises,
                    icon = Icons.Default.Description,
                    accentColor = subjectColor,
                    testTag = "step_back_${chapter.chapterId}",
                    onToggle = { onToggleStep("backexercises") }
                )

                NcertCheckItem(
                    title = "High-Yield Tables & Lines Marked",
                    subtitle = "Inorganic trends, exception tables & organic named reactions",
                    checked = chapter.markedLines,
                    icon = Icons.Default.Highlight,
                    accentColor = Color(0xFFFF8F00),
                    testTag = "step_marked_${chapter.chapterId}",
                    onToggle = { onToggleStep("marked") }
                )

                NcertCheckItem(
                    title = "NCERT Exemplar Problems",
                    subtitle = "Analytical MCQs, assertions & reasoning from Exemplar book",
                    checked = chapter.exemplarDone,
                    icon = Icons.Default.Psychology,
                    accentColor = subjectColor,
                    testTag = "step_exemplar_${chapter.chapterId}",
                    onToggle = { onToggleStep("exemplar") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NcertCheckMiniChip(
                        title = "Revision 1 (Active)",
                        checked = chapter.revision1,
                        modifier = Modifier.weight(1f),
                        testTag = "step_rev1_${chapter.chapterId}",
                        onToggle = { onToggleStep("revision1") }
                    )
                    NcertCheckMiniChip(
                        title = "Revision 2 (Polish)",
                        checked = chapter.revision2,
                        modifier = Modifier.weight(1f),
                        testTag = "step_rev2_${chapter.chapterId}",
                        onToggle = { onToggleStep("revision2") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Quick Toggle All + Expand Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onMarkAll(!isAllDone) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("mark_all_${chapter.chapterId}")
                ) {
                    Icon(
                        if (isAllDone) Icons.Default.RestartAlt else Icons.Default.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isAllDone) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF00875A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAllDone) "Reset Steps" else "Mark All 7 Done",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllDone) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF00875A)
                    )
                }

                TextButton(
                    onClick = onPushToSyllabusAndPlanner,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("push_syllabus_planner_${chapter.chapterId}")
                ) {
                    Text(
                        text = "→ Syllabus & Planner",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00ACC1)
                    )
                }

                TextButton(
                    onClick = { expandedNotes = !expandedNotes },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("toggle_notes_${chapter.chapterId}")
                ) {
                    Icon(
                        if (expandedNotes) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (chapter.notes.isNotBlank()) "Notes (${chapter.notes.length} ch)" else "Add NCERT Notes",
                        fontSize = 12.sp,
                        fontWeight = if (chapter.notes.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                        color = if (chapter.notes.isNotBlank()) Color(0xFF00875A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable Notes & Exceptions Editor
            AnimatedVisibility(
                visible = expandedNotes,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NCERT Exception Notes & Highlights",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (notesText != chapter.notes) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFF8F00).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Unsaved",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .testTag("ncert_notes_input_${chapter.chapterId}"),
                        placeholder = {
                            Text(
                                "Record tricky exception lines, table page numbers, catalyst conditions, or memory aids...",
                                fontSize = 11.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onConvertToFlashcard(notesText) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("🗂️ To Flashcard", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onSaveToMistakeLog(notesText) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("+ Mistake Log", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                onSaveNotes(notesText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00875A)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("save_notes_button_${chapter.chapterId}")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Room", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual checklist row for an NCERT milestone.
 */
@Composable
fun NcertCheckItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (checked) Color(0xFFC4EED0).copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (checked) Color(0xFF00875A).copy(alpha = 0.4f) else Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF00875A),
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                        color = if (checked) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (checked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF00875A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Compact chip for revision checkpoints.
 */
@Composable
fun NcertCheckMiniChip(
    title: String,
    checked: Boolean,
    testTag: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (checked) Color(0xFFC4EED0).copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (checked) Color(0xFF00875A).copy(alpha = 0.4f) else Color.Transparent
        ),
        modifier = modifier
            .clickable { onToggle() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (checked) Color(0xFF00875A) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                color = if (checked) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SubjectSummaryChip(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

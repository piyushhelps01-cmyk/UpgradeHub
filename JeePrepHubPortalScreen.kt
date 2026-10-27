package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel

enum class PrepHubSection(val title: String, val icon: ImageVector) {
    GRID_VAULT("Prep Vault Grid", Icons.Default.GridView),
    NOTES_PDFS("Formula & Notes", Icons.AutoMirrored.Filled.MenuBook),
    PYQ_BANK("Shift PYQs", Icons.Default.Quiz),
    CHEATSHEETS("Trap Cheatsheets", Icons.Default.Bolt),
    MASTERCLASSES("One-Shot Videos", Icons.Default.OndemandVideo)
}

data class HubStudyNotePdf(
    val id: String,
    val title: String,
    val subject: String,
    val category: String,
    val classLevel: String,
    val pages: Int,
    val weightageTag: String,
    val keyFormulas: List<String>,
    val detailedSummary: List<String>,
    val topperTip: String
)

data class HubPyqItem(
    val id: String,
    val subject: String,
    val chapter: String,
    val examShift: String,
    val difficulty: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val hint: String,
    val explanation: String,
    val keyConceptFormula: String
)

data class RapidTrapCheatsheet(
    val id: String,
    val subject: String,
    val tag: String,
    val title: String,
    val promptQuestion: String,
    val goldenRuleOrOrder: String,
    val examTrapNote: String
)

data class HubMasterclassVideo(
    val id: String,
    val title: String,
    val subject: String,
    val chapter: String,
    val duration: String,
    val instructor: String,
    val viewsTag: String,
    val youtubeUrl: String,
    val timestamps: List<Pair<String, String>>,
    val lectureTakeaways: List<String>
)

@Composable
fun JeePrepHubPortalScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedSection by remember { mutableStateOf(PrepHubSection.NOTES_PDFS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }

    val bookmarkedIdsSet by viewModel.bookmarkedVaultIds.collectAsState()
    val bookmarkedIds = bookmarkedIdsSet.toList()

    var activeReaderNote by remember { mutableStateOf<HubStudyNotePdf?>(null) }
    var activeVideoPlayer by remember { mutableStateOf<HubMasterclassVideo?>(null) }

    val notesCatalog = remember { getJeePrepHubNotesCatalog() }
    val pyqCatalog = remember { getJeePrepHubPyqCatalog() }
    val cheatsheetsCatalog = remember { getRapidTrapCheatsheets() }
    val videoCatalog = remember { getJeePrepHubMasterclasses() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("jee_prep_hub_portal_screen")
    ) {
        // Top Header with Clean 4-Tab Navigation
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.3f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
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
                                    text = "High-Yield Revision Vault",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "100% UNLOCKED",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Instant Formula Sheets, Active Recall Reader, Shift PYQs & Exam Trap Cheatsheets",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedSection.ordinal,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    contentColor = Color(0xFF0070F3),
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    PrepHubSection.entries.forEach { section ->
                        val selected = selectedSection == section
                        Tab(
                            selected = selected,
                            onClick = { selectedSection = section },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = section.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = if (selected) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = section.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.testTag("prep_hub_tab_${section.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Main Content Area
        AnimatedContent(
            targetState = selectedSection,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "PrepHubSectionTransition",
            modifier = Modifier.weight(1f)
        ) { section ->
            when (section) {
                PrepHubSection.GRID_VAULT -> {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            com.example.ui.components.PrepVaultCompleteGridSection(
                                viewModel = viewModel
                            )
                        }
                    }
                }

                PrepHubSection.NOTES_PDFS -> {
                    NotesAndPdfsSection(
                        notes = notesCatalog,
                        bookmarkedIds = bookmarkedIds,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedSubject = selectedSubjectFilter,
                        onSubjectChange = { selectedSubjectFilter = it },
                        onOpenNote = { note -> activeReaderNote = note },
                        onToggleBookmark = { id -> viewModel.toggleVaultBookmark(id) },
                        onQuickAddFlashcards = { note ->
                            viewModel.addFlashcard(
                                subject = note.subject,
                                chapter = note.title,
                                question = "High-Yield Formulas: ${note.title}",
                                answer = note.keyFormulas.joinToString("\n• ", prefix = "• "),
                                difficulty = "High-Yield"
                            )
                            viewModel.showSnackbar("🗂️ Added ${note.keyFormulas.size} formulas from '${note.title}' to Flashcards!")
                        }
                    )
                }

                PrepHubSection.PYQ_BANK -> {
                    PyqBankSection(
                        pyqs = pyqCatalog,
                        bookmarkedIds = bookmarkedIds,
                        selectedSubject = selectedSubjectFilter,
                        onSubjectChange = { selectedSubjectFilter = it },
                        onToggleBookmark = { id -> viewModel.toggleVaultBookmark(id) },
                        onCorrectSolved = {
                            viewModel.boostUserScore(points = 15, problems = 1)
                        },
                        onLogMistake = { pyq ->
                            viewModel.addMistakeEntry(
                                questionTitle = pyq.question,
                                subject = pyq.subject,
                                chapterOrSource = "${pyq.examShift} • ${pyq.chapter}",
                                errorType = "Concept Gap",
                                takeawayNote = "${pyq.keyConceptFormula} — ${pyq.explanation}"
                            )
                            viewModel.showSnackbar("📌 Logged PYQ to your Mistake Vault!")
                        }
                    )
                }

                PrepHubSection.CHEATSHEETS -> {
                    RapidCheatsheetsSection(
                        cheatsheets = cheatsheetsCatalog,
                        selectedSubject = selectedSubjectFilter,
                        onSubjectChange = { selectedSubjectFilter = it },
                        onCopyTrap = { item ->
                            clipboardManager.setText(
                                AnnotatedString("${item.title}\nRule: ${item.goldenRuleOrOrder}\nTrap: ${item.examTrapNote}")
                            )
                            viewModel.showSnackbar("Copied cheatsheet rule to clipboard!")
                        }
                    )
                }

                PrepHubSection.MASTERCLASSES -> {
                    MasterclassesSection(
                        videos = videoCatalog,
                        selectedSubject = selectedSubjectFilter,
                        onSubjectChange = { selectedSubjectFilter = it },
                        onSelectVideo = { video -> activeVideoPlayer = video },
                        onOpenYoutubeExternal = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                viewModel.showSnackbar("Opening lecture notes in-app player")
                            }
                        }
                    )
                }
            }
        }
    }

    // Interactive Note / Formula Reader Modal with Active Recall (Blur/Reveal) Mode
    activeReaderNote?.let { note ->
        var isBlindRecallMode by remember { mutableStateOf(false) }
        val revealedFormulaIndices = remember(note.id) { mutableStateListOf<Int>() }

        AlertDialog(
            onDismissRequest = { activeReaderNote = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${note.subject} • ${note.classLevel} • ${note.weightageTag}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0070F3),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { activeReaderNote = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Active Recall Toggle Bar inside Reader
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isBlindRecallMode) Color(0xFF8B5CF6).copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isBlindRecallMode) Color(0xFF8B5CF6) else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isBlindRecallMode = !isBlindRecallMode
                                revealedFormulaIndices.clear()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isBlindRecallMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = if (isBlindRecallMode) Color(0xFF8B5CF6) else Color(0xFF0070F3),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBlindRecallMode) "Active Recall ON (Tap formulas to reveal)" else " Enable Active Recall Mode (Hide Formulas)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBlindRecallMode) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isBlindRecallMode) "ON" else "TRY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isBlindRecallMode) Color(0xFF8B5CF6) else Color(0xFF0070F3)
                            )
                        }
                    }
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(410.dp)
                ) {
                    item {
                        Text(
                            text = "HIGH-YIELD FORMULAS & EQUATIONS (${note.keyFormulas.size})",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0070F3),
                            letterSpacing = 0.6.sp
                        )
                    }
                    items(note.keyFormulas.size) { idx ->
                        val formula = note.keyFormulas[idx]
                        val isHidden = isBlindRecallMode && !revealedFormulaIndices.contains(idx)
                        val labelPart = formula.substringBefore(":").ifBlank { "Formula #${idx + 1}" }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isHidden) Color(0xFF8B5CF6).copy(alpha = 0.10f) else Color(0xFF0070F3).copy(alpha = 0.08f),
                            border = BorderStroke(
                                1.dp,
                                if (isHidden) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color(0xFF0070F3).copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isBlindRecallMode) {
                                        if (revealedFormulaIndices.contains(idx)) {
                                            revealedFormulaIndices.remove(idx)
                                        } else {
                                            revealedFormulaIndices.add(idx)
                                        }
                                    }
                                }
                        ) {
                            if (isHidden) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🧠 Recall: $labelPart ...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6)
                                    )
                                    Text(
                                        text = "Tap to Reveal",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF0070F3)
                                    )
                                }
                            } else {
                                Text(
                                    text = formula,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "KEY REVISION POINTS & DERIVATION SHORTCUTS",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF059669),
                            letterSpacing = 0.6.sp
                        )
                    }

                    items(note.detailedSummary) { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = point,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.38f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💡 NTA EXAM TRAP & TOPPER TIP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.topperTip,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fullText = buildString {
                            appendLine(note.title)
                            appendLine("Formulas: ${note.keyFormulas.joinToString(" | ")}")
                            appendLine("Notes: ${note.detailedSummary.joinToString("\n- ")}")
                        }
                        clipboardManager.setText(AnnotatedString(fullText))
                        viewModel.showSnackbar("Copied full study sheet to clipboard!")
                        activeReaderNote = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3))
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Sheet", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = {
                            viewModel.addFlashcard(
                                subject = note.subject,
                                chapter = note.title,
                                question = "Key Formula / Concept: ${note.title}",
                                answer = note.keyFormulas.joinToString("\n• ", prefix = "• "),
                                difficulty = "High-Yield"
                            )
                            viewModel.showSnackbar("🗂️ Added ${note.title} formulas to Flashcards!")
                        }
                    ) {
                        Text("+ To Flashcards", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    TextButton(onClick = { activeReaderNote = null }) {
                        Text("Done")
                    }
                }
            }
        )
    }

    // Masterclass In-App Study Player Dialog
    activeVideoPlayer?.let { video ->
        AlertDialog(
            onDismissRequest = { activeVideoPlayer = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "ONE-SHOT REVISION MASTERCLASS • ${video.duration}",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = video.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(380.dp)
                ) {
                    item {
                        Text(
                            text = "KEY LECTURE TIMESTAMPS",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0070F3)
                        )
                    }
                    items(video.timestamps) { (time, topic) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0070F3)
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = topic,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MASTERCLASS KEY TAKEAWAYS",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF059669)
                        )
                    }
                    items(video.lectureTakeaways) { takeaway ->
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = takeaway,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            viewModel.showSnackbar("Marked as revised in the local preview. No server XP was credited.")
                        }
                        viewModel.boostUserScore(points = 20, hours = 1.5f)
                        activeVideoPlayer = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch on YouTube", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeVideoPlayer = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun NotesAndPdfsSection(
    notes: List<HubStudyNotePdf>,
    bookmarkedIds: List<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    onOpenNote: (HubStudyNotePdf) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onQuickAddFlashcards: (HubStudyNotePdf) -> Unit
) {
    val subjects = listOf("All", "Physics", "Chemistry", "Mathematics", "Saved")
    val filteredNotes = notes.filter { note ->
        val matchesSubject = when (selectedSubject) {
            "All" -> true
            "Saved" -> bookmarkedIds.contains(note.id)
            else -> note.subject.equals(selectedSubject, ignoreCase = true)
        }
        val matchesQuery = searchQuery.isBlank() ||
            note.title.contains(searchQuery, ignoreCase = true) ||
            note.category.contains(searchQuery, ignoreCase = true) ||
            note.subject.contains(searchQuery, ignoreCase = true)
        matchesSubject && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Subject Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search chapter formulas, organic reactions, calculus...", fontSize = 12.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0070F3)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    val selected = selectedSubject == subj
                    FilterChip(
                        selected = selected,
                        onClick = { onSubjectChange(subj) },
                        label = { Text(subj, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0070F3),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Instant Unlocked Notes List
        items(filteredNotes, key = { it.id }) { note ->
            val isBookmarked = bookmarkedIds.contains(note.id)
            val accentColor = when (note.subject) {
                "Physics" -> Color(0xFF0070F3)
                "Chemistry" -> Color(0xFF10B981)
                else -> Color(0xFF8B5CF6)
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenNote(note) }
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
                                color = accentColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${note.subject} • ${note.classLevel}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = note.weightageTag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onToggleBookmark(note.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = note.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Formula Preview Strip right on the card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.07f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ ${note.keyFormulas.firstOrNull() ?: ""}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${note.keyFormulas.size} Formulas • ${note.detailedSummary.size} Key Points",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.12f),
                                modifier = Modifier.clickable { onQuickAddFlashcards(note) }
                            ) {
                                Text(
                                    text = "+ Flashcards",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF059669),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Open Sheet",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
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

@Composable
private fun PyqBankSection(
    pyqs: List<HubPyqItem>,
    bookmarkedIds: List<String>,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onCorrectSolved: () -> Unit,
    onLogMistake: (HubPyqItem) -> Unit
) {
    val subjects = listOf("All", "Physics", "Chemistry", "Mathematics")
    val filteredPyqs = pyqs.filter {
        selectedSubject == "All" || it.subject.equals(selectedSubject, ignoreCase = true)
    }

    val selectedAnswers = remember { mutableStateListOf<Pair<String, Int>>() }
    val shownHints = remember { mutableStateListOf<String>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { onSubjectChange(subj) },
                        label = { Text(subj, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0070F3),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        items(filteredPyqs, key = { it.id }) { pyq ->
            val userChoice = selectedAnswers.find { it.first == pyq.id }?.second
            val isBookmarked = bookmarkedIds.contains(pyq.id)
            val isHintVisible = shownHints.contains(pyq.id)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
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
                                color = Color(0xFF0070F3).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = pyq.examShift,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0070F3),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${pyq.subject} • ${pyq.difficulty}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (userChoice == null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                    modifier = Modifier.clickable {
                                        if (isHintVisible) shownHints.remove(pyq.id) else shownHints.add(pyq.id)
                                    }
                                ) {
                                    Text(
                                        text = if (isHintVisible) "Hide Hint" else "💡 Hint",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(
                                onClick = { onToggleBookmark(pyq.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark PYQ",
                                    tint = if (isBookmarked) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Chapter: ${pyq.chapter}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pyq.question,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    )

                    AnimatedVisibility(visible = isHintVisible && userChoice == null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.10f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "💡 Concept Hint: ${pyq.hint}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8B5CF6),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    pyq.options.forEachIndexed { idx, opt ->
                        val isSelected = userChoice == idx
                        val isCorrect = idx == pyq.correctIndex
                        val bgColor = when {
                            userChoice == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            isCorrect -> Color(0xFF10B981).copy(alpha = 0.16f)
                            isSelected -> Color(0xFFEF4444).copy(alpha = 0.16f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        }
                        val borderColor = when {
                            userChoice == null -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            isCorrect -> Color(0xFF10B981)
                            isSelected -> Color(0xFFEF4444)
                            else -> Color.Transparent
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = userChoice == null) {
                                    selectedAnswers.add(pyq.id to idx)
                                    if (idx == pyq.correctIndex) {
                                        onCorrectSolved()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0070F3).copy(alpha = 0.14f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ('A' + idx).toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0070F3)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = opt,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = userChoice != null) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Formula: ${pyq.keyConceptFormula}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0070F3)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = pyq.explanation,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                            modifier = Modifier.clickable { onLogMistake(pyq) }
                                        ) {
                                            Text(
                                                text = "+ Log to Mistake Vault",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFEF4444),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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

@Composable
private fun RapidCheatsheetsSection(
    cheatsheets: List<RapidTrapCheatsheet>,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    onCopyTrap: (RapidTrapCheatsheet) -> Unit
) {
    val subjects = listOf("All", "Chemistry", "Physics", "Mathematics")
    val filtered = cheatsheets.filter {
        selectedSubject == "All" || it.subject.equals(selectedSubject, ignoreCase = true)
    }
    val flippedIds = remember { mutableStateListOf<String>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { onSubjectChange(subj) },
                        label = { Text(subj, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF8B5CF6),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { item ->
            val isRevealed = flippedIds.contains(item.id)
            val accent = when (item.subject) {
                "Chemistry" -> Color(0xFF10B981)
                "Physics" -> Color(0xFF0070F3)
                else -> Color(0xFF8B5CF6)
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isRevealed) flippedIds.remove(item.id) else flippedIds.add(item.id)
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accent.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${item.subject} • ${item.tag}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        TextButton(
                            onClick = { onCopyTrap(item) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "❓ Recall Prompt: ${item.promptQuestion}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRevealed) accent.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isRevealed) {
                                Text(
                                    text = "👆 Tap Card to Reveal Exact Order / Formula & Exam Trap",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accent
                                )
                            } else {
                                Text(
                                    text = "✅ ${item.goldenRuleOrOrder}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚠️ Exam Trap: ${item.examTrapNote}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFD97706)
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
private fun MasterclassesSection(
    videos: List<HubMasterclassVideo>,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    onSelectVideo: (HubMasterclassVideo) -> Unit,
    onOpenYoutubeExternal: (String) -> Unit
) {
    val subjects = listOf("All", "Chemistry", "Physics", "Mathematics")
    val filteredVideos = videos.filter {
        selectedSubject == "All" || it.subject.equals(selectedSubject, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { onSubjectChange(subj) },
                        label = { Text(subj, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        items(filteredVideos, key = { it.id }) { video ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectVideo(video) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "ONE-SHOT MASTERCLASS • ${video.subject.uppercase()}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = video.duration,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = video.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${video.instructor} • ${video.viewsTag}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onSelectVideo(video) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Timestamps & Notes", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onOpenYoutubeExternal(video.youtubeUrl) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "YouTube", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun getJeePrepHubNotesCatalog(): List<HubStudyNotePdf> = listOf(
    HubStudyNotePdf(
        id = "note_chem_01",
        title = "Classification of Elements & Periodicity in Properties",
        subject = "Chemistry",
        category = "Short Notes & Mind Map",
        classLevel = "Class 11",
        pages = 18,
        weightageTag = "8–12 Marks Guaranteed",
        keyFormulas = listOf(
            "Slater's Effective Nuclear Charge: Z_eff = Z - σ",
            "2nd Period IE₁ Order: Li < B < Be < C < O < N < F < Ne",
            "Electron Gain Enthalpy (Halogens): Cl > F > Br > I (compact 2p repulsion in F)",
            "Mulliken Electronegativity: χ_M = (IE + EA) / 2 (in eV) = 2.8 × χ_Pauling"
        ),
        detailedSummary = listOf(
            "Atomic & Ionic Radii: Decreases across a period (left to right) due to increasing Z_eff; increases down a group as new shells are added.",
            "Isoelectronic Species: For O²⁻, F⁻, Na⁺, Mg²⁺, Al³⁺, ionic radius decreases strictly with increasing nuclear charge Z.",
            "Half-filled (np³) and fully-filled (ns²) subshells show extra stability: IE₁ of Be > B and IE₁ of N > O.",
            "Diagonal Relationship: Li resembles Mg, Be resembles Al, and B resembles Si due to similar charge-to-radius (polarizing power) ratios.",
            "Oxides Nature: Basic character increases down a group; acidic character increases across a period (Na₂O basic, Al₂O₃ & ZnO amphoteric, Cl₂O₇ strongly acidic)."
        ),
        topperTip = "NTA asks the 2nd Ionization Enthalpy (IE₂) order almost every year! After losing 1 electron, O⁺ attains stable 2p³ configuration, so IE₂ of O > N and IE₂ of Na >> Mg."
    ),
    HubStudyNotePdf(
        id = "note_phy_01",
        title = "Electrostatics, Gauss Law & Capacitors Formula Sheet",
        subject = "Physics",
        category = "Formula Sheet",
        classLevel = "Class 12",
        pages = 24,
        weightageTag = "12–16 Marks High-Yield",
        keyFormulas = listOf(
            "Dipole Field: E_axial = 2kp / r³  |  E_eq = kp / r³  |  Torque τ = p × E",
            "Gauss Flux: Φ = ∮ E · dA = q_in / ε₀  |  E_sheet = σ / (2ε₀)",
            "Spherical Shell Potential: V_in = V_surface = kQ / R  |  E_in = 0",
            "Dielectric Slab Capacitor: C = ε₀A / (d - t + t/K)",
            "Common Potential & Heat Loss: V_c = (C₁V₁ + C₂V₂)/(C₁+C₂)  |  ΔH = ½ [C₁C₂/(C₁+C₂)](V₁ - V₂)²"
        ),
        detailedSummary = listOf(
            "Electric field is always conservative (∮ E · dl = 0) and directed along steepest decrease of potential (E = -∇V).",
            "Self-energy of a uniformly charged solid non-conducting sphere of radius R is U = (3/5) kQ²/R, whereas for a hollow conducting shell it is U = (1/2) kQ²/R.",
            "When a dielectric K is inserted with Battery Connected (V = constant): C becomes KC, Q becomes KQ, U becomes KU, E remains V/d.",
            "When a dielectric K is inserted with Battery Disconnected (Q = constant): C becomes KC, V becomes V/K, E becomes E/K, U becomes U/K."
        ),
        topperTip = "Always check whether the battery remains connected or disconnected before applying U = ½CV² vs U = Q²/(2C)!"
    ),
    HubStudyNotePdf(
        id = "note_phy_02",
        title = "Modern Physics, Photoelectric Effect & Bohr Atomic Model",
        subject = "Physics",
        category = "Rapid Revision Sheet",
        classLevel = "Class 12",
        pages = 22,
        weightageTag = "12–16 Marks Every Shift",
        keyFormulas = listOf(
            "Einstein's Photoelectric Eq: K_max = e V₀ = hc/λ - φ₀ = h(ν - ν₀) (hc ≈ 1240 eV·nm)",
            "de Broglie Wavelength: λ = h / p = h / √(2mK) = 12.27 / √V Å (for electron)",
            "Bohr Radius & Speed: r_n = 0.529 × (n² / Z) Å  |  v_n = 2.18 × 10⁶ × (Z / n) m/s",
            "Total Energy: E_n = -13.6 × (Z² / n²) eV  |  K.E. = -E_n  |  P.E. = 2E_n"
        ),
        detailedSummary = listOf(
            "Stopping potential V₀ depends ONLY on frequency/wavelength of incident light and work function φ₀, NEVER on intensity.",
            "Photoelectric saturation current is directly proportional to intensity of incident radiation (number of photons per second).",
            "Number of spectral lines emitted when electrons jump from n₂ to n₁ in a hydrogen gas sample: N = (n₂ - n₁)(n₂ - n₁ + 1) / 2.",
            "Nuclear Radius: R = R₀ A^(1/3) where R₀ = 1.2 fm. Nuclear density (~2.3 × 10¹⁷ kg/m³) is independent of mass number A."
        ),
        topperTip = "For a single isolated hydrogen atom, maximum spectral lines from n to ground state is (n - 1), NOT n(n - 1)/2!"
    ),
    HubStudyNotePdf(
        id = "note_chem_02",
        title = "General Organic Chemistry (GOC), Named Reactions & Reagent Book",
        subject = "Chemistry",
        category = "Reaction Handbook",
        classLevel = "Class 11 & 12",
        pages = 36,
        weightageTag = "16–20 Marks Core",
        keyFormulas = listOf(
            "Carbocation Stability: Cyclopropylmethyl > Tropylium > Benzyl ≈ Allyl > 3° > 2° > 1°",
            "Acidity Order: -SO₃H > -COOH > Picric Acid > H₂CO₃ > Phenol > H₂O > ROH > HC≡CH",
            "Hückel's Aromaticity Rule: Planar, completely conjugated ring with (4n + 2) π electrons",
            "Aqueous Amine Basicity: 2° > 1° > 3° (Methyl - 213)  |  2° > 3° > 1° (Ethyl - 231)"
        ),
        detailedSummary = listOf(
            "SIR Effect (Steric Inhibition of Resonance): Bulky ortho-substituents twist -NO₂ or -COOH out of plane with the benzene ring.",
            "Ortho Effect: Ortho-substituted benzoic acids are stronger acids than benzoic acid regardless of +I/-I nature of ortho group.",
            "SN1 vs SN2: SN1 favors polar protic solvents, weak nucleophiles, 3° substrates (racemization + rearrangement); SN2 is concerted 1-step with 100% Walden inversion in polar aprotic solvents (DMSO, Acetone)."
        ),
        topperTip = "Remember amine basicity in water: 213 for Methyl (Me₂NH > MeNH₂ > Me₃N) and 231 for Ethyl (Et₂NH > Et₃N > EtNH₂)!"
    ),
    HubStudyNotePdf(
        id = "note_math_01",
        title = "Calculus Rank Booster: Limits, Continuity & Definite Integration",
        subject = "Mathematics",
        category = "Handwritten Formula PDF",
        classLevel = "Class 12",
        pages = 30,
        weightageTag = "16–20 Marks Guaranteed",
        keyFormulas = listOf(
            "King's Property: ∫_a^b f(x) dx = ∫_a^b f(a + b - x) dx",
            "Leibniz Rule: d/dx [∫_{g(x)}^{h(x)} f(t) dt] = f(h(x))·h'(x) - f(g(x))·g'(x)",
            "1^∞ Indeterminate Form: lim_{x→a} [f(x)]^{g(x)} = exp( lim_{x→a} g(x)·[f(x) - 1] )",
            "Walli's Formula: ∫_0^{π/2} sin^n(x) dx = [(n-1)/n] · [(n-3)/(n-2)] ..."
        ),
        detailedSummary = listOf(
            "Standard Maclaurin Expansions: sin x = x - x³/6 + x⁵/120; cos x = 1 - x²/2 + x⁴/24; ln(1+x) = x - x²/2 + x³/3.",
            "Queen's Property: ∫_0^{2a} f(x) dx = ∫_0^a [f(x) + f(2a - x)] dx. Use when f(2a - x) simplifies.",
            "Limit of a Sum: lim_{n→∞} (1/n) Σ_{r=1}^n f(r/n) = ∫_0^1 f(x) dx."
        ),
        topperTip = "Over 70% of JEE Main Definite Integration questions yield in 2 steps when you apply King's Property I + I = ∫_a^b [f(x) + f(a+b-x)] dx!"
    ),
    HubStudyNotePdf(
        id = "note_math_02",
        title = "3D Geometry, Vectors & Matrices Shortest Distance Sheet",
        subject = "Mathematics",
        category = "Formula & Shortcut Sheet",
        classLevel = "Class 12",
        pages = 20,
        weightageTag = "20–24 Marks Highest ROI",
        keyFormulas = listOf(
            "Shortest Distance (Skew Lines): d = |(a₂ - a₁) · (b₁ × b₂)| / |b₁ × b₂|",
            "Cayley-Hamilton Theorem: Every square matrix A satisfies det(A - λI) = 0",
            "Adjoint Identities: |adj(A)| = |A|^(n-1)  |  |adj(adj A)| = |A|^((n-1)²)  |  |kA| = k^n |A|",
            "Image of Point (x₁,y₁,z₁) in Plane ax+by+cz+d=0: (x-x₁)/a = (y-y₁)/b = (z-z₁)/c = -2(ax₁+by₁+cz₁+d)/(a²+b²+c²)"
        ),
        detailedSummary = listOf(
            "Vector Triple Product: a × (b × c) = (a · c)b - (a · b)c lies in the plane of b and c.",
            "For system AX = B: Unique solution if |A| ≠ 0; No solution if |A| = 0 and (adj A)B ≠ 0; Infinite solutions if |A| = 0 and (adj A)B = 0."
        ),
        topperTip = "3D Geometry + Matrices & Determinants together guarantee 5–6 questions (20–24 marks) in every single JEE Main paper!"
    )
)

private fun getJeePrepHubPyqCatalog(): List<HubPyqItem> = listOf(
    HubPyqItem(
        id = "pyq_hub_01",
        subject = "Chemistry",
        chapter = "Classification of Elements & Periodicity",
        examShift = "JEE Main 2025 Jan Shift 1",
        difficulty = "Moderate",
        question = "The correct order of first ionization enthalpy (Δ_i H₁) for the elements Li, Be, B, C, N, O is:",
        options = listOf(
            "Li < Be < B < C < N < O",
            "Li < B < Be < C < O < N",
            "Li < B < Be < C < N < O",
            "Be < Li < B < O < C < N"
        ),
        correctIndex = 1,
        hint = "Check fully-filled 2s² orbital stability in Be vs B, and half-filled 2p³ stability in N vs O.",
        explanation = "Across the 2nd period, ionization enthalpy generally increases due to increasing Z_eff. However, Be (2s² fully-filled penetration) has higher IE₁ than B (2s²2p¹), and N (2p³ half-filled symmetrical stability) has higher IE₁ than O (2p⁴). Thus: Li < B < Be < C < O < N.",
        keyConceptFormula = "2s² > 2p¹ (Penetration) & 2p³ > 2p⁴ (Half-filled exchange energy)"
    ),
    HubPyqItem(
        id = "pyq_hub_02",
        subject = "Physics",
        chapter = "Modern Physics & Dual Nature",
        examShift = "JEE Main 2024 Apr Shift 2",
        difficulty = "Easy",
        question = "A proton and an alpha particle are accelerated from rest through the same electrostatic potential difference V. The ratio of their de Broglie wavelengths (λ_p / λ_α) is:",
        options = listOf(
            "2√2 : 1",
            "√2 : 1",
            "4 : 1",
            "2 : 1"
        ),
        correctIndex = 0,
        hint = "Use λ = h / √(2mqV) with m_α = 4 m_p and q_α = 2 q_p.",
        explanation = "Since λ = h / √(2mqV) and V is identical for both: λ_p / λ_α = √[(m_α q_α) / (m_p q_p)]. Given m_α = 4 m_p and q_α = 2 q_p, the ratio is √(4 × 2) = √8 = 2√2 : 1.",
        keyConceptFormula = "λ = h / √(2 m q V)"
    ),
    HubPyqItem(
        id = "pyq_hub_03",
        subject = "Mathematics",
        chapter = "Definite Integration",
        examShift = "JEE Main 2025 Jan Shift 2",
        difficulty = "Moderate",
        question = "The value of the definite integral I = ∫_{0}^{π/2} [ √(sin x) / (√(sin x) + √(cos x)) ] dx is:",
        options = listOf(
            "π / 2",
            "π / 4",
            "π / 8",
            "0"
        ),
        correctIndex = 1,
        hint = "Apply King's Property x → (π/2 - x) and add the two integrals 2I.",
        explanation = "Applying King's Property ∫_0^a f(x)dx = ∫_0^a f(a-x)dx gives I = ∫_0^{π/2} [ √(cos x) / (√(cos x) + √(sin x)) ] dx. Adding the two equations: 2I = ∫_0^{π/2} 1 dx = π/2 => I = π/4.",
        keyConceptFormula = "∫_a^b f(x) / [f(x) + f(a+b-x)] dx = (b - a) / 2"
    ),
    HubPyqItem(
        id = "pyq_hub_04",
        subject = "Chemistry",
        chapter = "Coordination Compounds",
        examShift = "JEE Main 2024 Jan Shift 1",
        difficulty = "Moderate",
        question = "Which of the following octahedral complexes is inner-orbital (d²sp³) and diamagnetic in nature?",
        options = listOf(
            "[CoF₆]³⁻",
            "[Co(NH₃)₆]³⁺",
            "[Fe(H₂O)₆]²⁺",
            "[Ni(NH₃)₆]²⁺"
        ),
        correctIndex = 1,
        hint = "Look for Co³⁺ (3d⁶) paired with a strong field ligand like NH₃.",
        explanation = "In [Co(NH₃)₆]³⁺, Co³⁺ has a 3d⁶ configuration. NH₃ acts as a strong-field ligand with Co³⁺, pairing all 6 electrons in the t₂g orbitals (t₂g⁶ eg⁰). Thus it undergoes d²sp³ hybridization with 0 unpaired electrons (diamagnetic).",
        keyConceptFormula = "Co³⁺ (3d⁶) + Strong Field Ligand -> t₂g⁶ eg⁰ (d²sp³, μ = 0 BM)"
    )
)

private fun getRapidTrapCheatsheets(): List<RapidTrapCheatsheet> = listOf(
    RapidTrapCheatsheet(
        id = "trap_01",
        subject = "Chemistry",
        tag = "Inorganic Exception",
        title = "Bond Angle Order of Hydrides (Group 15 & 16)",
        promptQuestion = "What is the decreasing bond angle order of NH₃, PH₃, AsH₃, SbH₃ and H₂O vs H₂S?",
        goldenRuleOrOrder = "NH₃ (107°) > PH₃ (93.8°) > AsH₃ (91.8°) > SbH₃ (91.3°)  |  H₂O (104.5°) > H₂S (92°)",
        examTrapNote = "From 3rd period onwards (PH₃, H₂S), Drago's Rule applies: almost pure p-orbitals participate in bonding without sp³ hybridization, so bond angles drop close to 90°!"
    ),
    RapidTrapCheatsheet(
        id = "trap_02",
        subject = "Chemistry",
        tag = "Organic Reagent Map",
        title = "Aldol vs Cannizzaro vs Haloform Distinguishing Test",
        promptQuestion = "Which carbonyl compounds give Aldol, Cannizzaro, and Iodoform reactions?",
        goldenRuleOrOrder = "• Aldol: Requires ≥1 α-Hydrogen (dil. NaOH, Δ gives α,β-unsaturated carbonyl)\n• Cannizzaro: Zero α-Hydrogen (HCHO, PhCHO, Me₃C-CHO + conc. 50% NaOH → Disproportionation)\n• Haloform (I₂/NaOH): Requires CH₃-C(=O)- or CH₃-CH(OH)- group",
        examTrapNote = "Acetaldehyde (CH₃CHO) is the ONLY aldehyde that gives a positive Iodoform test (yellow ppt of CHI₃)!"
    ),
    RapidTrapCheatsheet(
        id = "trap_03",
        subject = "Physics",
        tag = "Electrostatics & Cavity Trap",
        title = "Electric Field Inside a Spherical Cavity in a Solid Sphere",
        promptQuestion = "Is the electric field inside a non-concentric spherical cavity inside a uniformly charged solid sphere zero or uniform?",
        goldenRuleOrOrder = "E_cavity = (ρ · a) / (3 ε₀) — NON-ZERO and UNIFORM everywhere inside the cavity!",
        examTrapNote = "Students confuse a hollow shell (where E_in = 0) with a cavity carved out of a solid sphere (where E is uniform and depends on center-to-center vector a)!"
    ),
    RapidTrapCheatsheet(
        id = "trap_04",
        subject = "Mathematics",
        tag = "Matrices & Determinants",
        title = "Determinant of Adjoint & Inverse Power Shortcuts",
        promptQuestion = "For an n×n matrix A with determinant |A|, what are |adj A|, |adj(adj A)|, and |kA|?",
        goldenRuleOrOrder = "• |kA| = kⁿ |A|\n• |adj A| = |A|^(n - 1)\n• |adj(adj A)| = |A|^[(n - 1)²]\n• adj(AB) = (adj B)(adj A)",
        examTrapNote = "Never forget the reversal law for adjoint and transpose: adj(AB) = adj(B)·adj(A), and |k·adj(A)| = kⁿ · |A|^(n-1)!"
    )
)

private fun getJeePrepHubMasterclasses(): List<HubMasterclassVideo> = listOf(
    HubMasterclassVideo(
        id = "vid_chem_01",
        title = "Classification of Elements & Periodicity Class 11 — Complete Masterclass",
        subject = "Chemistry",
        chapter = "Periodic Table & Periodicity",
        duration = "1h 48m",
        instructor = "Prep Hub Academic Team",
        viewsTag = "Featured One-Shot Revision",
        youtubeUrl = "https://www.youtube.com/results?search_query=JEE+Prep+Hub+Classification+of+Elements+Periodicity",
        timestamps = listOf(
            "00:00" to "Genesis of Periodic Classification & Modern Periodic Law",
            "22:15" to "Z_eff, Slater's Rule & Atomic/Ionic Radius Exceptions",
            "54:40" to "Ionization Enthalpy (IE₁ & IE₂) Topper Tricks",
            "01:18:10" to "Electron Gain Enthalpy (Cl vs F) & Electronegativity Scales",
            "01:35:00" to "2020–2025 JEE Main PYQ Rapid Fire Solve"
        ),
        lectureTakeaways = listOf(
            "Transition contraction (3d) causes Ga atomic radius (135 pm) to be slightly smaller than Al (143 pm).",
            "Lanthanoid contraction causes 4d and 5d series elements (Zr & Hf) to have almost identical atomic radii.",
            "In 2nd period, IE₁ order is Li < B < Be < C < O < N < F < Ne."
        )
    ),
    HubMasterclassVideo(
        id = "vid_phy_01",
        title = "Electrostatics & Gauss's Law One-Shot + Top 30 PYQs",
        subject = "Physics",
        chapter = "Electric Charges and Fields",
        duration = "2h 15m",
        instructor = "Prep Hub Physics Faculty",
        viewsTag = "High-Yield JEE Main & Adv",
        youtubeUrl = "https://www.youtube.com/results?search_query=JEE+Prep+Hub+Electrostatics+One+Shot",
        timestamps = listOf(
            "00:00" to "Coulomb's Law Vector Form & Equilibrium of Charges",
            "35:20" to "Electric Field of Ring, Rod, Disc & Hollow Hemisphere",
            "01:10:00" to "Electric Dipole Torque, Potential Energy & Osc. Time Period",
            "01:42:00" to "Gauss Law Cavity Problems & Solid Sphere Flux"
        ),
        lectureTakeaways = listOf(
            "Electric field inside a cavity in a uniformly charged solid sphere with center-to-center vector a is uniform: E = ρ a / (3ε₀).",
            "Time period of small angular oscillations of a dipole in uniform field E is T = 2π √(I / pE)."
        )
    ),
    HubMasterclassVideo(
        id = "vid_math_01",
        title = "Vectors & 3D Geometry — Guaranteed 16 Marks Marathon",
        subject = "Mathematics",
        chapter = "3D Geometry & Vectors",
        duration = "2h 05m",
        instructor = "Prep Hub Maths Faculty",
        viewsTag = "Most Watched Sprint Session",
        youtubeUrl = "https://www.youtube.com/results?search_query=JEE+Prep+Hub+3D+Geometry+Vectors",
        timestamps = listOf(
            "00:00" to "Dot & Cross Product Geometrical Meaning & Projection",
            "38:15" to "Scalar & Vector Triple Product Shortcuts",
            "01:12:40" to "Shortest Distance Between Skew Lines",
            "01:45:00" to "Foot of Perpendicular & Image of Point in a Line"
        ),
        lectureTakeaways = listOf(
            "Always parameterize a general point on the line as (x₁ + λa, y₁ + λb, z₁ + λc) and apply perpendicular dot product = 0.",
            "If three vectors are coplanar, their scalar triple product [a b c] = 0."
        )
    )
)

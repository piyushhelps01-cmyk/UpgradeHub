package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FlashcardDeckEntity
import com.example.data.repository.JeeData
import com.example.ui.components.PracticeStackQueueView
import com.example.ui.components.flashcards.CreateDeckDialog
import com.example.ui.components.flashcards.DeckCardManagerDialog
import com.example.ui.components.flashcards.FlashcardDeckCard
import com.example.ui.components.flashcards.FlashcardRevisionDialog
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel

enum class PracticeSection(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    QUEUE("Practice Queue", Icons.Default.Queue),
    QUESTION_BANK("Question Bank", Icons.Default.School),
    HISTORY("Practice History", Icons.Default.Timeline),
    FLASHCARDS("Formula Flashcards", Icons.Default.Layers),
    WORKLOAD("Workload & Milestones", Icons.Default.FitnessCenter)
}

@Composable
fun PracticeStackScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(PracticeSection.QUEUE) }
    var selectedSubjectFilter by remember { mutableStateOf("All") }

    val decks by viewModel.flashcardDecks.collectAsState()
    val practiceSubjects by viewModel.activePracticeSubjects.collectAsState()
    val mistakeEntries by viewModel.mistakeNotebookEntries.collectAsState()

    // Dialog States
    var showCreateDialog by remember { mutableStateOf(false) }
    var showMistakeQuizDialog by remember { mutableStateOf(false) }
    var showConstantsSheet by remember { mutableStateOf(false) }
    var revisionDeck by remember { mutableStateOf<FlashcardDeckEntity?>(null) }
    var manageDeck by remember { mutableStateOf<FlashcardDeckEntity?>(null) }
    var deckToDelete by remember { mutableStateOf<FlashcardDeckEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Navigation Tabs (Flashcards vs Workload)
        TabRow(
            selectedTabIndex = selectedSection.ordinal,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("practice_section_tab_row"),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            divider = {}
        ) {
            PracticeSection.entries.forEach { section ->
                val selected = selectedSection == section
                Tab(
                    selected = selected,
                    onClick = { selectedSection = section },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                section.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = section.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    },
                    modifier = Modifier.testTag("practice_tab_${section.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Action Utility Strip: "Quiz Me on My Mistakes" + "Constants & SI Units Sheet"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { showMistakeQuizDialog = true }
                    .testTag("quiz_mistakes_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎯 Quiz My Mistakes (${mistakeEntries.count { !it.isResolved }})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0070F3).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { showConstantsSheet = true }
                    .testTag("scientific_constants_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📐 Constants & Units",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0070F3)
                    )
                }
            }
        }

        AnimatedContent(
            targetState = selectedSection,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "PracticeSectionAnimation"
        ) { section ->
            when (section) {
                PracticeSection.QUEUE -> {
                    PracticeStackQueueView(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }

                PracticeSection.QUESTION_BANK -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            com.example.ui.components.CentralizedQuestionBankCard(viewModel = viewModel)
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }

                PracticeSection.FLASHCARDS -> {
                    FlashcardsSectionView(
                        decks = decks,
                        subjects = practiceSubjects,
                        selectedSubjectFilter = selectedSubjectFilter,
                        onSelectFilter = { selectedSubjectFilter = it },
                        onCreateDeckClick = { showCreateDialog = true },
                        onStartRevision = { deck -> revisionDeck = deck },
                        onManageCards = { deck -> manageDeck = deck },
                        onResetProgress = { deck -> viewModel.resetDeckProgress(deck.id, deck.title) },
                        onDeleteDeck = { deck -> deckToDelete = deck }
                    )
                }

                PracticeSection.HISTORY -> {
                    PracticeHistorySectionView(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }

                PracticeSection.WORKLOAD -> {
                    WorkloadSectionView(
                        uiState = uiState,
                        viewModel = viewModel,
                        onSwitchToFlashcards = { selectedSection = PracticeSection.FLASHCARDS }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showCreateDialog) {
        CreateDeckDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, subject, desc, colorHex ->
                viewModel.createFlashcardDeck(title, subject, desc, colorHex)
                showCreateDialog = false
            }
        )
    }

    revisionDeck?.let { deck ->
        FlashcardRevisionDialog(
            deck = deck,
            viewModel = viewModel,
            onDismiss = { revisionDeck = null }
        )
    }

    manageDeck?.let { deck ->
        DeckCardManagerDialog(
            deck = deck,
            viewModel = viewModel,
            onDismiss = { manageDeck = null }
        )
    }

    deckToDelete?.let { deck ->
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text("Delete \"${deck.title}\"?") },
            text = { Text("All flashcards in this deck will be permanently deleted. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFlashcardDeck(deck.id, deck.title)
                        deckToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Interactive "Quiz Me on My Mistakes" Active Recall Drill Dialog
    if (showMistakeQuizDialog) {
        val pendingMistakes = mistakeEntries.filter { !it.isResolved }.ifEmpty { mistakeEntries }
        var currentIdx by remember { mutableStateOf(0) }
        var showTakeawayAnswer by remember { mutableStateOf(false) }
        val currentMistake = pendingMistakes.getOrNull(currentIdx.coerceIn(0, (pendingMistakes.size - 1).coerceAtLeast(0)))

        AlertDialog(
            onDismissRequest = { showMistakeQuizDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎯 Mistake Notebook Active Drill", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    if (pendingMistakes.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${currentIdx + 1}/${pendingMistakes.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            },
            text = {
                if (currentMistake == null) {
                    Text("All mistakes resolved! Log tricky questions from CBT Mock Tests or NCERT to drill them here.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${currentMistake.subject} • ${currentMistake.errorType} • ${currentMistake.chapterOrSource}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0070F3),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Question / Trap:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentMistake.questionTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (showTakeawayAnswer) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTakeawayAnswer = !showTakeawayAnswer }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (showTakeawayAnswer) "💡 YOUR SAVED TAKEAWAY & FIX:" else "👆 Tap to Reveal Takeaway & Formula Fix",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (showTakeawayAnswer) Color(0xFF059669) else Color(0xFF0070F3)
                                )
                                if (showTakeawayAnswer) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentMistake.takeawayNote,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (currentMistake != null) {
                    Button(
                        onClick = {
                            if (!currentMistake.isResolved) {
                                viewModel.toggleMistakeResolved(currentMistake.id)
                            }
                            showTakeawayAnswer = false
                            if (currentIdx < pendingMistakes.size - 1) {
                                currentIdx++
                            } else {
                                showMistakeQuizDialog = false
                                viewModel.showSnackbar("🎉 Mistake drill completed. This local action did not credit server XP.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Mastered & Resolve", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (pendingMistakes.size > 1) {
                        TextButton(
                            onClick = {
                                showTakeawayAnswer = false
                                currentIdx = (currentIdx + 1) % pendingMistakes.size
                            }
                        ) {
                            Text("Next →")
                        }
                    }
                    TextButton(onClick = { showMistakeQuizDialog = false }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    // Quick Scientific Constants & SI Unit Conversions Reference Sheet
    if (showConstantsSheet) {
        val constantsList = listOf(
            "Speed of Light (c)" to "3.0 × 10⁸ m/s",
            "Planck's Constant (h)" to "6.626 × 10⁻³⁴ J·s (hc ≈ 1240 eV·nm)",
            "Avogadro's Number (Nₐ)" to "6.022 × 10²³ mol⁻¹",
            "Universal Gas Constant (R)" to "8.314 J/(mol·K) ≈ 0.0821 L·atm/(mol·K) ≈ 2 cal/(mol·K)",
            "Coulomb Constant (1/4πε₀)" to "9.0 × 10⁹ N·m²/C² (ε₀ = 8.854 × 10⁻¹² F/m)",
            "Gravitational Constant (G)" to "6.674 × 10⁻¹¹ N·m²/kg²",
            "Boltzmann Constant (k_B)" to "1.38 × 10⁻²³ J/K",
            "Faraday's Constant (F)" to "96,485 C/mol ≈ 96,500 C/mol",
            "Electron Volt & Atomic Mass" to "1 eV = 1.602 × 10⁻¹⁹ J • 1 u = 931.5 MeV/c²",
            "Bohr Radius (a₀) & Rydberg (R_H)" to "a₀ = 0.529 Å • 1/λ = R_H (1/n₁² - 1/n₂²), E₁ = -13.6 eV"
        )
        AlertDialog(
            onDismissRequest = { showConstantsSheet = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("📐 Exam Physical Constants & Shortcuts", fontSize = 16.sp, fontWeight = FontWeight.Black)
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(360.dp)
                ) {
                    items(constantsList) { (name, value) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0070F3))
                                Text(value, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showConstantsSheet = false }) {
                    Text("Got It")
                }
            }
        )
    }
}

/**
 * Digital Flashcard Module View in Practice Stack.
 */
@Composable
private fun FlashcardsSectionView(
    decks: List<com.example.data.model.FlashcardDeckUiModel>,
    subjects: List<String>,
    selectedSubjectFilter: String,
    onSelectFilter: (String) -> Unit,
    onCreateDeckClick: () -> Unit,
    onStartRevision: (FlashcardDeckEntity) -> Unit,
    onManageCards: (FlashcardDeckEntity) -> Unit,
    onResetProgress: (FlashcardDeckEntity) -> Unit,
    onDeleteDeck: (FlashcardDeckEntity) -> Unit
) {
    val filteredDecks = when (selectedSubjectFilter) {
        "All" -> decks
        else -> decks.filter { it.deck.subject.equals(selectedSubjectFilter, ignoreCase = true) }
    }

    val totalCards = decks.sumOf { it.cardCount }
    val totalMastered = decks.sumOf { it.masteredCount }
    val globalMasteryPct = if (totalCards > 0) ((totalMastered.toFloat() / totalCards) * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overview Summary Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
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
                            Icon(
                                Icons.Default.FlipCameraAndroid,
                                contentDescription = null,
                                tint = Color(0xFF6574FF),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Formula & Concept Decks",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onCreateDeckClick,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("create_custom_deck_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Deck", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Interactive digital flashcards for rapid formula retention, reaction mechanisms, and calculus rules. Flip cards to test your recall.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Strip
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${decks.size}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF6574FF)
                                )
                                Text("Decks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalCards",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Total Cards", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalMastered",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF00C853)
                                )
                                Text("Mastered", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$globalMasteryPct%",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = if (globalMasteryPct >= 80) Color(0xFF00C853) else Color(0xFFFFAB00)
                                )
                                Text("Recall Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Filter Pills Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(subjects) { subject ->
                    FilterChip(
                        selected = selectedSubjectFilter == subject,
                        onClick = { onSelectFilter(subject) },
                        label = { Text(subject, fontSize = 12.sp) },
                        modifier = Modifier.testTag("filter_chip_${subject.lowercase()}")
                    )
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "${selectedSubjectFilter.uppercase()} DECKS (${filteredDecks.size})",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (filteredDecks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No decks found in $selectedSubjectFilter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap \"New Deck\" above to create a custom formula deck.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredDecks, key = { it.deck.id }) { deckModel ->
                FlashcardDeckCard(
                    deckModel = deckModel,
                    onStartRevision = { onStartRevision(deckModel.deck) },
                    onManageCards = { onManageCards(deckModel.deck) },
                    onResetProgress = { onResetProgress(deckModel.deck) },
                    onDeleteDeck = { onDeleteDeck(deckModel.deck) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * Interactive Chapter Milestone Matrix & Practice Level Tracker.
 */
@Composable
private fun WorkloadSectionView(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    onSwitchToFlashcards: () -> Unit
) {
    val activeChapters by viewModel.activeChapters.collectAsState()
    val subjects by viewModel.activeSubjects.collectAsState()
    val version by viewModel.version.collectAsState()
    var selectedMatrixSubject by remember { mutableStateOf("All") }

    val filteredMatrixChapters = remember(activeChapters, selectedMatrixSubject, version) {
        if (selectedMatrixSubject == "All") activeChapters
        else activeChapters.filter { it.subject.equals(selectedMatrixSubject, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Summary Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
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
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color(0xFF0070F3),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Interactive Practice Milestone Matrix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF6574FF).copy(alpha = 0.14f),
                            modifier = Modifier.clickable { onSwitchToFlashcards() }
                        ) {
                            Text(
                                text = "Formula Decks →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6574FF),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap any milestone chip below to mark DPPs, Level 1–3 Modules, PYQs, and Chapter Tests complete in real time.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Overall Practice Level Summary Strip
        item {
            Text(
                text = "OVERALL PRACTICE LEVEL COMPLETION",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(uiState.practiceStats) { stat ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stat.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${stat.completedCount} / ${stat.totalCount} (${stat.percentage}%)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF0070F3),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF0070F3),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Subject Filter for Interactive Chapter Matrix
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CHAPTER-BY-CHAPTER PRACTICE CHECKLIST (${filteredMatrixChapters.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { subj ->
                        FilterChip(
                            selected = selectedMatrixSubject == subj,
                            onClick = { selectedMatrixSubject = subj },
                            label = { Text(subj, fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0070F3),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Interactive Chapter Cards where users can toggle DPP, Prarambh, Prabal, Prakshit, PYQ, Test directly
        items(filteredMatrixChapters, key = { it.id }) { chapter ->
            val chain = remember(chapter.id, version) { viewModel.getChapterChain(chapter.id) }
            val practiceSteps = listOf(
                Triple("dpp", "DPP", chain.dpp),
                Triple("prarambh", "L1 Prarambh", chain.prarambh),
                Triple("prabal", "L2 Prabal", chain.prabal),
                Triple("prakshit", "L3 Prakshit", chain.prakshit),
                Triple("pyq", "PYQ", chain.pyq),
                Triple("test", "Test", chain.test)
            )
            val donePracticeCount = practiceSteps.count { it.third }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chapter.subject.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0070F3)
                            )
                            Text(
                                text = chapter.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (donePracticeCount == practiceSteps.size) Color(0xFF10B981).copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$donePracticeCount/${practiceSteps.size} Solved",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (donePracticeCount == practiceSteps.size) Color(0xFF059669)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(practiceSteps) { (stepKey, stepLabel, isChecked) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChecked) Color(0xFF10B981).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChecked) Color(0xFF10B981) else Color.Transparent
                                ),
                                modifier = Modifier.clickable {
                                    viewModel.toggleChapterCheck(chapter.id, stepKey, isChecked)
                                }
                            ) {
                                Text(
                                    text = if (isChecked) "✓ $stepLabel" else stepLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChecked) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun PracticeHistorySectionView(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.practiceSessionsHistory.collectAsState()
    val totalSolved by viewModel.totalPracticeQuestionsSolved.collectAsState()
    val avgAccuracy by viewModel.averagePracticeAccuracy.collectAsState()
    val totalMinutes by viewModel.totalPracticeMinutes.collectAsState()
    val currentPlannerDays = viewModel.batchTargetDays.collectAsState().value

    var showLogDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Telemetry Banner Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_history_telemetry_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Practice Telemetry & History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Persisted in Room Database • Synced with $currentPlannerDays-Day Plan",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showLogDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("log_practice_session_button")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Solved", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$totalSolved Qs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Accuracy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${String.format("%.1f", avgAccuracy)}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF059669)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF6574FF).copy(alpha = 0.15f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Time Spent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "${totalMinutes}m",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF5266CF)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Practice Drill History (${history.size} Sessions)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "PLAN: ${currentPlannerDays}D",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No Practice Sessions Logged Yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Complete exercises from the Practice Queue or tap 'Log Session' above to record your drills.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(history, key = { it.id }) { session ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("practice_session_card_${session.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.sessionTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = session.subject,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = session.sessionType,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "Day ${session.dayNumber} • ${session.plannerDurationDays}D Plan",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deletePracticeSession(session.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete entry",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "${session.questionsCorrect}/${session.questionsAttempted} Correct",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (session.accuracyPercentage >= 75f) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "•  ${session.timeSpentMinutes} mins",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (session.accuracyPercentage >= 80f) Color(0xFF10B981).copy(alpha = 0.15f)
                                else if (session.accuracyPercentage >= 60f) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", session.accuracyPercentage)}% Accuracy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (session.accuracyPercentage >= 80f) Color(0xFF059669)
                                    else if (session.accuracyPercentage >= 60f) Color(0xFFD97706)
                                    else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (session.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Note: ${session.notes}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showLogDialog) {
        LogPracticeSessionDialog(
            onDismiss = { showLogDialog = false },
            onSave = { title, type, subject, attempted, correct, mins, notes ->
                viewModel.logCustomPracticeSession(
                    sessionTitle = title,
                    sessionType = type,
                    subject = subject,
                    questionsAttempted = attempted,
                    questionsCorrect = correct,
                    timeSpentMinutes = mins,
                    notes = notes
                )
                showLogDialog = false
            }
        )
    }
}

@Composable
fun LogPracticeSessionDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Int, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var sessionType by remember { mutableStateOf("PYQ Drill") }
    var attemptedText by remember { mutableStateOf("30") }
    var correctText by remember { mutableStateOf("25") }
    var minutesText by remember { mutableStateOf("60") }
    var notes by remember { mutableStateOf("") }

    val subjects = listOf("Physics", "Chemistry", "Mathematics", "Biology")
    val types = listOf("PYQ Drill", "DPP Solving", "NCERT Line-by-Line", "Formula Sprint", "Full Mock")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Practice Session", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session / Chapter Title") },
                    placeholder = { Text("e.g. Current Electricity PYQs") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    subjects.take(3).forEach { sub ->
                        FilterChip(
                            selected = subject == sub,
                            onClick = { subject = sub },
                            label = { Text(sub, fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.take(3).forEach { t ->
                        FilterChip(
                            selected = sessionType == t,
                            onClick = { sessionType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = attemptedText,
                        onValueChange = { attemptedText = it.filter { c -> c.isDigit() } },
                        label = { Text("Attempted") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = correctText,
                        onValueChange = { correctText = it.filter { c -> c.isDigit() } },
                        label = { Text("Correct") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Mins") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                androidx.compose.material3.OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Weak areas (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "$subject $sessionType" }
                    val attempted = attemptedText.toIntOrNull() ?: 30
                    val correct = (correctText.toIntOrNull() ?: 25).coerceAtMost(attempted)
                    val mins = minutesText.toIntOrNull() ?: 60
                    onSave(finalTitle, sessionType, subject, attempted, correct, mins, notes)
                }
            ) {
                Text("Save Drill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

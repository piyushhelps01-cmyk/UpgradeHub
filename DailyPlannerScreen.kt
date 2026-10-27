package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailySlot
import com.example.data.repository.JeeData
import com.example.ui.components.BatchAutoPlannerDialog
import com.example.ui.components.BatchCurriculumProgressBar
import com.example.ui.components.BatchUrlImportSyncCard
import com.example.ui.components.DailyGoalsComponent
import com.example.ui.components.DashboardPdfExportImportCard
import com.example.ui.components.GlassmorphicContainer
import com.example.ui.components.StudySlotAlarmManager
import com.example.ui.components.StudySlotTimeCustomizerDialog
import com.example.ui.viewmodel.JeePage
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import java.util.Calendar

@Composable
fun DailyPlannerScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val version by viewModel.version.collectAsState()
    val batchCurriculums by viewModel.batchCurriculums.collectAsState()
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val parallelBatchIds by viewModel.parallelBatchIds.collectAsState()
    val parallelBatches by viewModel.parallelBatches.collectAsState()
    val batchTargetDays by viewModel.batchTargetDays.collectAsState()
    val batchDailyHours by viewModel.batchDailyHours.collectAsState()
    val batchUrlInput by viewModel.batchUrlInput.collectAsState()
    val isAutoPlanning by viewModel.isAutoPlanning.collectAsState()
    val isNeet by viewModel.isNeetMode.collectAsState()
    val lastAutoPlanResult by viewModel.lastAutoPlanResult.collectAsState()
    val progression by viewModel.curriculumProgression.collectAsState()
    val isPdfExporting by viewModel.isPdfExporting.collectAsState()
    val lastExportedPdf by viewModel.lastExportedPdf.collectAsState()
    var isAutoPlannerDialogOpen by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    var isPrintView by remember { mutableStateOf(false) }

    val day = uiState.selectedDay
    val dayIndex = day - 1

    // Calendar calculation for current day date
    val cal = remember(day) {
        Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 1, 0, 0, 0)
            add(Calendar.DAY_OF_YEAR, dayIndex)
        }
    }
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val dateText = String.format("%02d %s 2026", cal.get(Calendar.DAY_OF_MONTH), months[cal.get(Calendar.MONTH)])
    val dayOfWeekName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
    val isFiveSlots = (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY ||
            cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
            cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
    val availableTarget = if (isFiveSlots) "15 Hours (5 × 3h slots)" else "12 Hours (4 × 3h slots)"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Day Selector Toolbar
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
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.prevDay() },
                            enabled = day > 1,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = Color(0xFF38BDF8))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "DAY $day OF ${progression.totalDays}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$dateText • $dayOfWeekName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = availableTarget,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextDay() },
                            enabled = day < progression.totalDays,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", tint = Color(0xFF38BDF8))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // View switch tabs: "Interactive Slots" vs "Printable Sheet / Handwritten Layout"
                    TabRow(
                        selectedTabIndex = if (isPrintView) 1 else 0,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = Color(0xFF0070F3),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = !isPrintView,
                            onClick = { isPrintView = false },
                            text = { Text("Interactive", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1) }
                        )
                        Tab(
                            selected = isPrintView,
                            onClick = { isPrintView = true },
                            text = { Text("Printable Sheet", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1) }
                        )
                    }
                }
            }
        }

        // Active Batch Sync Banner with 30-150 Day Presets & 2-3 Parallel Batches
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0070F3).copy(alpha = 0.10f),
                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (parallelBatches.size > 1) {
                                        "${parallelBatches.size} Parallel Batches Active"
                                    } else {
                                        selectedBatch.title
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (parallelBatches.size > 1) {
                                        parallelBatches.joinToString(" + ") { it.title.take(18) }
                                    } else {
                                        "Auto-Plan: $batchTargetDays Days Horizon (30–150d) • ${batchDailyHours.toInt()}h/day"
                                    },
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0070F3),
                            modifier = Modifier
                                .clickable { isAutoPlannerDialogOpen = true }
                                .testTag("planner_open_auto_dialog_btn")
                        ) {
                            Text(
                                text = "Sync / Parallel (2–3)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick 30-150 Day Horizon Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Horizon:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf(30, 45, 60, 75, 90, 120, 150).forEach { daysPreset ->
                            val isSelected = batchTargetDays == daysPreset
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable {
                                    viewModel.setBatchTargetDays(daysPreset)
                                }
                            ) {
                                Text(
                                    text = "${daysPreset}d",
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isPrintView) {
            // Interactive 3-Hour Focus Slots prioritized at the center of the Daily Planner
            item {
                Text(
                    text = "3-HOUR STUDY FOCUS SLOTS (DAILY SCHEDULE)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF0070F3)
                )
            }

            items(uiState.todayCommandSlots) { slot ->
                SlotCard(
                    dayIndex = dayIndex,
                    slot = slot,
                    viewModel = viewModel,
                    version = version
                )
            }
        } else {
            // Printable Handwritten Layout (Exact copy from PDF)
            item {
                PrintableExecutionSheetCard(
                    day = day,
                    dateText = dateText,
                    dayOfWeekName = dayOfWeekName,
                    slots = uiState.todayCommandSlots,
                    viewModel = viewModel,
                    dayIndex = dayIndex,
                    onCopyText = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        viewModel.showSnackbar("Printable execution layout copied to clipboard!")
                    }
                )
            }
        }

        item {
            DailyGoalsComponent(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Backlog Recovery & Missed Slot Auto-Rescheduler Engine
        item {
            val lastRescheduled by viewModel.lastRescheduledSummary.collectAsState()
            val missedSlotsCount = remember(version, day) {
                viewModel.getMissedSlotsCountForDay(day)
            }
            var selectedSpreadDays by remember { mutableStateOf(3) }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backlog_recovery_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BACKLOG RECOVERY & CATCH-UP ENGINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFFD97706)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (missedSlotsCount > 0)
                                    "$missedSlotsCount Unfinished Study Slot(s) on Day $day"
                                else
                                    "All Day $day Slots Completed • Zero Backlog!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = lastRescheduled ?: "Distribute missed lectures, DPPs & PYQs across upcoming days (+45m/day buffer)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(3 to "Next 3 Days", 5 to "Next 5 Days", 7 to "7-Day Buffer").forEach { (spread, label) ->
                            val isChosen = selectedSpreadDays == spread
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) Color(0xFFF59E0B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedSpreadDays = spread }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.rescheduleMissedSlotsToUpcomingDays(
                                fromDay = day,
                                spreadDays = selectedSpreadDays
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_reschedule_missed_slots_btn")
                    ) {
                        Text(
                            text = "Auto-Reschedule Missed Slots (+${selectedSpreadDays}d Catch-Up)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (!isPrintView) {
            // Teacher & Batch Daily Tracker (Saleem Sir, Sachin Sir, etc.)
            item {
                TeacherDailyTrackerCard(
                    dayIndex = dayIndex,
                    viewModel = viewModel,
                    version = version
                )
            }

            // Daily Audit: Actual Hours, Day Score, Weak Topics, Next Day Plan
            item {
                DayAuditCard(
                    dayIndex = dayIndex,
                    day = day,
                    viewModel = viewModel,
                    version = version
                )
            }
        }

        item {
            CurriculumProgressionCard(
                day = day,
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            BatchCurriculumProgressBar(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // External Batch URL Tracker & Auto-Sync (PW, Unacademy, KGS, etc.)
        item {
            BatchUrlImportSyncCard(
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
                    viewModel.setPage(JeePage.SETTINGS)
                }
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
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

@Composable
fun SlotCard(
    dayIndex: Int,
    slot: DailySlot,
    viewModel: JeeViewModel,
    version: Int
) {
    val context = LocalContext.current
    StudySlotAlarmManager.ensureInitialized(context)
    val slotAlarmConfigs by StudySlotAlarmManager.slotConfigs.collectAsState()
    val slotAlarm = slotAlarmConfigs.getOrElse(slot.slotIndex) {
        StudySlotAlarmManager.getSlotConfig(slot.slotIndex)
    }
    var showSlotTimeCustomizer by remember { mutableStateOf(false) }

    if (showSlotTimeCustomizer) {
        StudySlotTimeCustomizerDialog(
            slotIndex = slot.slotIndex,
            slotCustomTitle = slot.title,
            onDismiss = {
                val updated = StudySlotAlarmManager.getSlotConfig(slot.slotIndex)
                viewModel.showSnackbar("⏰ Slot ${slot.slotIndex + 1} set: ${updated.formattedTime} • ${updated.title} (Auto-rings 30s)")
                showSlotTimeCustomizer = false
            }
        )
    }

    val progress = viewModel.getSlotProgress(dayIndex, slot.slotIndex)

    GlassmorphicContainer(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.40f))
                    ) {
                        Text(
                            text = "SLOT ${slot.slotIndex + 1} (3h)",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slot.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Suggested Target: ${slot.suggestedTarget}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mandatory Study Slot Time & 30s Auto-Ring Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFF10B981).copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .clickable { showSlotTimeCustomizer = true }
                        .testTag("planner_slot_time_chip_${slot.slotIndex + 1}")
                ) {
                    Text(
                        text = "⏰ ${slotAlarm.formattedTime} • Auto 30s ✎",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 10 Checkpoints for this slot
            Text(
                text = "Milestone Checklist (Tick upon finishing):",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            val checks = listOf(
                "lecture" to "Lecture",
                "dpp" to "DPP",
                "module" to "Module",
                "prarambh" to "Prarambh",
                "prabal" to "Prabal",
                "prakshit" to "Prakshit",
                "pyq" to "PYQ",
                "ncert" to "12th NCERT",
                "revision" to "Revision",
                "test" to "Test"
            )

            // Flow of small tick chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                checks.chunked(3).forEach { rowChecks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowChecks.forEach { (key, label) ->
                            val isChecked = when (key) {
                                "lecture" -> progress.lecture
                                "dpp" -> progress.dpp
                                "module" -> progress.module
                                "prarambh" -> progress.prarambh
                                "prabal" -> progress.prabal
                                "prakshit" -> progress.prakshit
                                "pyq" -> progress.pyq
                                "ncert" -> progress.ncert
                                "revision" -> progress.revision
                                "test" -> progress.test
                                else -> false
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChecked) Color(0xFFC4EED0) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.toggleSlotCheck(dayIndex, slot.slotIndex, key, isChecked)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = if (isChecked) Color(0xFF00875A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isChecked) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        if (rowChecks.size < 3) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct actions: Set Slot Time & Topic + Immediate 30s Mock Drill Test Ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSlotTimeCustomizer = true }
                        .testTag("set_slot_time_btn_${slot.slotIndex + 1}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = Color(0xFF0070F3),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set Time & Topic",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            StudySlotAlarmManager.triggerSlotAlarmWithThirtySecMockDrill(
                                context = context,
                                slotIndex = slot.slotIndex,
                                slotTitle = slot.title,
                                scheduledTimeLabel = slotAlarm.formattedTime
                            )
                        }
                        .testTag("test_mock_drill_btn_${slot.slotIndex + 1}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🚨 Ring 30s Mock Drill",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherDailyTrackerCard(
    dayIndex: Int,
    viewModel: JeeViewModel,
    version: Int
) {
    val isNeet = viewModel.isNeetMode.collectAsState().value
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF6574FF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Teacher-Wise Daily Tracker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Track lecture numbers, DPP, NCERT, PYQ, and revision per teacher",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            val selectedBatch by viewModel.selectedBatch.collectAsState()
            val teachers = remember(selectedBatch, isNeet) {
                if (selectedBatch.faculty.isNotEmpty()) {
                    selectedBatch.faculty.map { f ->
                        val id = f.facultyName.lowercase().replace(Regex("[^a-z0-9]"), "_").take(15)
                        id to "${f.facultyName} (${f.subject})"
                    }
                } else if (isNeet) {
                    listOf(
                        "tarun" to "Tarun Sir (Botany)",
                        "vipin" to "Dr. Vipin Sharma (Zoology)",
                        "saleem" to "Saleem Sir (NEET Physics)",
                        "sudhanshu" to "Sudhanshu Sir (Phys Chem)",
                        "om" to "Om Pandey Sir (IOC)",
                        "pankaj" to "Pankaj Sir (OC)"
                    )
                } else {
                    listOf(
                        "saleem" to "Saleem Sir (Physics)",
                        "sachin" to "Sachin Sir (Maths)",
                        "anna" to "Anna Sir (11th Maths)",
                        "faisal" to "Faisal Sir (Phys Chem)",
                        "om" to "Om Pandey Sir (IOC)",
                        "pankaj" to "Pankaj Sir (OC)"
                    )
                }
            }

            teachers.forEach { (tId, tName) ->
                val lecNo = viewModel.getTeacherLecNo(dayIndex, tId)
                val dpp = viewModel.getTeacherCheck(dayIndex, tId, "dpp")
                val ncert = viewModel.getTeacherCheck(dayIndex, tId, "ncert")
                val pyq = viewModel.getTeacherCheck(dayIndex, tId, "pyq")
                val rev = viewModel.getTeacherCheck(dayIndex, tId, "rev")
                val test = viewModel.getTeacherCheck(dayIndex, tId, "test")

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = tName, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Lec #:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedTextField(
                                    value = lecNo,
                                    onValueChange = { viewModel.updateTeacherLecNo(dayIndex, tId, it) },
                                    modifier = Modifier.width(84.dp),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    placeholder = { Text("ex: 14", fontSize = 10.sp) },
                                    singleLine = true
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TeacherCheckChip("DPP", dpp) { viewModel.toggleTeacherCheck(dayIndex, tId, "dpp", dpp) }
                            TeacherCheckChip("NCERT", ncert) { viewModel.toggleTeacherCheck(dayIndex, tId, "ncert", ncert) }
                            TeacherCheckChip("PYQ", pyq) { viewModel.toggleTeacherCheck(dayIndex, tId, "pyq", pyq) }
                            TeacherCheckChip("REV", rev) { viewModel.toggleTeacherCheck(dayIndex, tId, "rev", rev) }
                            TeacherCheckChip("TEST", test) { viewModel.toggleTeacherCheck(dayIndex, tId, "test", test) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherCheckChip(label: String, checked: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (checked) Color(0xFFC4EED0) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (checked) Color(0xFF00875A) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (checked) "✓ $label" else label,
                fontSize = 10.sp,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                color = if (checked) Color(0xFF00210E) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DayAuditCard(
    dayIndex: Int,
    day: Int,
    viewModel: JeeViewModel,
    version: Int
) {
    val actualHoursStr = viewModel.getDayRecord(dayIndex, "hours")
    val scoreStr = viewModel.getDayRecord(dayIndex, "score")
    val weakTopics = viewModel.getDayRecord(dayIndex, "weakTopics")
    val notesPlan = viewModel.getDayRecord(dayIndex, "notesPlan")

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Color(0xFFB26A00),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Performance & Error Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = actualHoursStr,
                    onValueChange = { viewModel.updateDayRecord(dayIndex, "hours", it) },
                    label = { Text("Actual Hours (ex: 12h)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = scoreStr,
                    onValueChange = { viewModel.updateDayRecord(dayIndex, "score", it) },
                    label = { Text("Day Score (ex: 8.5/10)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = weakTopics,
                onValueChange = { viewModel.updateDayRecord(dayIndex, "weakTopics", it) },
                label = { Text("Weak Topic / Error & Mistake Log") },
                placeholder = { Text("e.g. Capacitor dielectric boundary conditions, King's rule integrals...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notesPlan,
                onValueChange = { viewModel.updateDayRecord(dayIndex, "notesPlan", it) },
                label = { Text("Notes & Tomorrow's Plan") },
                placeholder = { Text("Plan early morning 3h slot for Mathematics matrices...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val hours = actualHoursStr.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
                    val score = scoreStr.replace("[^0-9]".toRegex(), "").toIntOrNull()?.coerceIn(0, 10) ?: 0
                    viewModel.updateStudyDayAudit(day, hours, score, weakTopics, notesPlan)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6574FF))
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Day $day Audit to Room Database", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun PrintableExecutionSheetCard(
    day: Int,
    dateText: String,
    dayOfWeekName: String,
    slots: List<DailySlot>,
    viewModel: JeeViewModel,
    dayIndex: Int,
    onCopyText: (String) -> Unit
) {
    val score = viewModel.getDayRecord(dayIndex, "score").ifEmpty { "__" }
    val hours = viewModel.getDayRecord(dayIndex, "hours").ifEmpty { "__" }
    val weak = viewModel.getDayRecord(dayIndex, "weakTopics").ifEmpty { "None logged yet" }
    val notes = viewModel.getDayRecord(dayIndex, "notesPlan").ifEmpty { "Follow 3-hour focus blocks" }
    val isNeet = viewModel.isNeetMode.collectAsState().value

    val formattedPrintableText = buildString {
        appendLine("===================================================================================")
        appendLine("DAY [$day/60] | DATE: $dateText ($dayOfWeekName) | TARGET: ${slots.size * 3}h | ACTUAL: $hours h | SCORE: $score/10")
        appendLine("===================================================================================")
        appendLine()
        appendLine("TIME SLOTS TRACKER (Class 09:00-14:00 is NOT counted as self-study):")
        slots.forEachIndexed { i, s ->
            appendLine("[ ] SLOT ${i + 1} (03 Hours): ${s.title} — ${s.suggestedTarget}")
        }
        appendLine()
        appendLine("-----------------------------------------------------------------------------------")
        appendLine("TEACHER & BATCH DAILY CHECKLIST:")
        appendLine("-----------------------------------------------------------------------------------")
        val teachers = if (isNeet) {
            listOf(
                "tarun" to "Tarun Sir (Botany)",
                "vipin" to "Dr. Vipin Sharma (Zoology)",
                "saleem" to "Saleem Sir (NEET Physics)",
                "sudhanshu" to "Sudhanshu Sir (PC)",
                "om" to "Om Pandey Sir (IOC)",
                "pankaj" to "Pankaj Sir (OC)"
            )
        } else {
            listOf(
                "saleem" to "Saleem Sir (Physics)",
                "sachin" to "Sachin Sir (Maths)",
                "anna" to "Anna Sir (11th Maths)",
                "faisal" to "Faisal Sir (PC)",
                "om" to "Om Pandey Sir (IOC)",
                "pankaj" to "Pankaj Sir (OC)"
            )
        }
        teachers.forEach { (tId, tName) ->
            val lec = viewModel.getTeacherLecNo(dayIndex, tId).ifEmpty { "___" }
            val dpp = if (viewModel.getTeacherCheck(dayIndex, tId, "dpp")) "[x]" else "[ ]"
            val ncert = if (viewModel.getTeacherCheck(dayIndex, tId, "ncert")) "[x]" else "[ ]"
            val pyq = if (viewModel.getTeacherCheck(dayIndex, tId, "pyq")) "[x]" else "[ ]"
            val rev = if (viewModel.getTeacherCheck(dayIndex, tId, "rev")) "[x]" else "[ ]"
            appendLine("$dpp DPP | $ncert NCERT | $pyq PYQ | $rev REV | $tName (Lec: $lec)")
        }
        appendLine("-----------------------------------------------------------------------------------")
        appendLine("WEAK TOPIC / ERROR LOG:")
        appendLine(weak)
        appendLine("-----------------------------------------------------------------------------------")
        appendLine("NOTES & NEXT-DAY PLAN:")
        appendLine(notes)
        appendLine("===================================================================================")
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF6574FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Printable Daily Execution Sheet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onCopyText(formattedPrintableText) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6574FF))
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Sheet", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = formattedPrintableText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun CurriculumProgressionCard(
    day: Int,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val progression by viewModel.curriculumProgression.collectAsState()
    val isDayCompleted = progression.activeSchedule?.isCompleted == true ||
            (progression.allSchedules.find { it.dayNumber == day }?.isCompleted == true)

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                        color = Color(0xFF6574FF).copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Timeline,
                                contentDescription = null,
                                tint = Color(0xFF5266CF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${progression.totalDays}-DAY PROGRESSION TRACKER",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF0070F3)
                        )
                        Text(
                            text = "Day $day of ${progression.totalDays} • ${progression.completedDays} Completed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDayCompleted) Color(0xFF00875A).copy(alpha = 0.15f) else Color(0xFF0070F3).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isDayCompleted) Color(0xFF00875A) else Color(0xFF0070F3).copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        viewModel.toggleStudyDayCompleted(day, !isDayCompleted)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDayCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isDayCompleted) Color(0xFF00875A) else Color(0xFF0070F3),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDayCompleted) "Day Done ✓" else "Mark Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDayCompleted) Color(0xFF00875A) else Color(0xFF0070F3)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room-Synced Curriculum Progress",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${progression.progressPercentage.toInt()}% (${progression.completedDays}/${progression.totalDays})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0070F3)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (progression.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF6574FF),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3 metrics: Streak, Hours Logged, Pacing Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔥 Streak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${progression.currentStreak} Days",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⏱ Hours Logged", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", progression.totalStudyHoursLogged)}h / 720h",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎯 Pacing", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = progression.pacingStatus.split(" ").firstOrNull() ?: "On Track",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Milestone
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF6574FF).copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFF5266CF),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = progression.currentMilestone,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

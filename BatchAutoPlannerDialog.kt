package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AutoPlanResult
import com.example.data.model.BatchCurriculum
import kotlin.math.roundToInt

@Composable
fun BatchAutoPlannerDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    batches: List<BatchCurriculum>,
    selectedBatch: BatchCurriculum,
    targetDays: Int,
    dailyWorkingHours: Float,
    urlInput: String,
    isPlanning: Boolean,
    lastResult: AutoPlanResult?,
    onSelectBatch: (String) -> Unit,
    onSetTargetDays: (Int) -> Unit,
    onSetDailyHours: (Float) -> Unit,
    onSetUrlInput: (String) -> Unit,
    onSyncUrl: (String) -> Unit,
    onGeneratePlan: () -> Unit,
    parallelBatchIds: List<String> = listOf(selectedBatch.id),
    onToggleParallelBatch: (String) -> Unit = {}
) {
    if (!isOpen) return

    var activeTab by remember { mutableStateOf(0) } // 0: Batch & 30-150d Setup, 1: Chapter Lectures, 2: Active Plan Preview

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("batch_auto_planner_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0070F3).copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Multi-Exam & Parallel Batch Planner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "PW • Unacademy • KGS Sync | 30–150 Days | 2–3 Parallel Batches",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_auto_planner_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        "1. Batches & 30–150d",
                        "2. Chapters (${selectedBatch.chapters.size})",
                        "3. Active Plan"
                    )
                    tabs.forEachIndexed { index, label ->
                        val isSelected = activeTab == index
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = index }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Content Body based on tab
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        0 -> BatchSetupTab(
                            batches = batches,
                            selectedBatch = selectedBatch,
                            parallelBatchIds = parallelBatchIds,
                            targetDays = targetDays,
                            dailyWorkingHours = dailyWorkingHours,
                            urlInput = urlInput,
                            onSelectBatch = onSelectBatch,
                            onToggleParallelBatch = onToggleParallelBatch,
                            onSetTargetDays = onSetTargetDays,
                            onSetDailyHours = onSetDailyHours,
                            onSetUrlInput = onSetUrlInput,
                            onSyncUrl = onSyncUrl
                        )
                        1 -> ChapterLecturesTab(
                            batch = selectedBatch,
                            targetDays = targetDays
                        )
                        2 -> PlanPreviewTab(
                            lastResult = lastResult,
                            selectedBatch = selectedBatch,
                            targetDays = targetDays,
                            dailyWorkingHours = dailyWorkingHours
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            onGeneratePlan()
                            activeTab = 2
                        },
                        enabled = !isPlanning,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("generate_auto_plan_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3))
                    ) {
                        if (isPlanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Planning...", color = Color.White)
                        } else {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync & Generate $targetDays-Day Plan", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchSetupTab(
    batches: List<BatchCurriculum>,
    selectedBatch: BatchCurriculum,
    parallelBatchIds: List<String>,
    targetDays: Int,
    dailyWorkingHours: Float,
    urlInput: String,
    onSelectBatch: (String) -> Unit,
    onToggleParallelBatch: (String) -> Unit,
    onSetTargetDays: (Int) -> Unit,
    onSetDailyHours: (Float) -> Unit,
    onSetUrlInput: (String) -> Unit,
    onSyncUrl: (String) -> Unit
) {
    var selectedExamFilter by remember { mutableStateOf("All") }
    val examFilters = listOf("All", "JEE", "NEET", "Class 8–12", "SSC", "Group D")

    val filteredBatches = remember(batches, selectedExamFilter) {
        batches.filter { batch ->
            when (selectedExamFilter) {
                "JEE" -> batch.examType.contains("JEE", ignoreCase = true)
                "NEET" -> batch.examType.contains("NEET", ignoreCase = true)
                "Class 8–12" -> batch.classGrade.contains("Class", ignoreCase = true)
                "SSC" -> batch.examType.contains("SSC", ignoreCase = true)
                "Group D" -> batch.examType.contains("Group D", ignoreCase = true) || batch.examType.contains("Railway", ignoreCase = true)
                else -> true
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Batch URL or Link Sync (PW, Unacademy, KGS, etc.)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Batch URL Import & Sync (PW, Unacademy, KGS & More)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Paste any batch link from Physics Wallah (PW), Unacademy, KGS (Khan Global Studies), Allen, or YouTube playlist:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Platform Preset Pills for instant 1-tap URL testing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "PW Lakshya JEE" to "https://pw.live/batches/lakshya-jee-2026",
                            "Unacademy JEE/NEET" to "https://unacademy.com/batch/phoenix-jee-dropper-2026",
                            "KGS SSC CGL" to "https://khanglobalstudies.com/courses/ssc-cgl-chsl-2026",
                            "KGS Railway Group D" to "https://khanglobalstudies.com/courses/rrb-group-d-ntpc",
                            "PW Udaan Class 8-10" to "https://pw.live/batches/udaan-class-10-foundation"
                        ).forEach { (label, sampleUrl) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    onSetUrlInput(sampleUrl)
                                    onSyncUrl(sampleUrl)
                                }
                            ) {
                                Text(
                                    text = "⚡ $label",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = onSetUrlInput,
                            placeholder = { Text("https://pw.live/... or unacademy.com/... or khanglobalstudies.com/...", fontSize = 11.5.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("batch_url_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onSyncUrl(urlInput) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                            modifier = Modifier.testTag("sync_batch_url_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 2: 30–150 Day Planner Configuration
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF0070F3), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Planner Horizon (30–150 Days)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$targetDays DAYS PLAN",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = targetDays.coerceIn(30, 150).toFloat(),
                        onValueChange = { onSetTargetDays(it.roundToInt().coerceIn(30, 150)) },
                        valueRange = 30f..150f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF0070F3),
                            activeTrackColor = Color(0xFF0070F3)
                        )
                    )

                    // Quick 30–150 day selection chips (horizontally scrollable so all 7 presets fit cleanly)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(30, 45, 60, 75, 90, 120, 150).forEach { days ->
                            FilterChip(
                                selected = targetDays == days,
                                onClick = { onSetTargetDays(days) },
                                label = {
                                    Text(
                                        text = "${days}d",
                                        fontSize = 11.sp,
                                        fontWeight = if (targetDays == days) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0070F3),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Multi-Exam Filter + 2–3 Parallel Batches Selection
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLASS 8–12, JEE, NEET, SSC & GROUP D BATCHES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${parallelBatchIds.size}/3 Parallel Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Exam Filter Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    examFilters.forEach { filter ->
                        val isSel = selectedExamFilter == filter
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { selectedExamFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredBatches.forEach { batch ->
                        val isPrimary = batch.id == selectedBatch.id
                        val isParallel = parallelBatchIds.contains(batch.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isPrimary -> Color(0xFF0070F3).copy(alpha = 0.14f)
                                isParallel -> Color(0xFF10B981).copy(alpha = 0.10f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (isPrimary || isParallel) 1.5.dp else 1.dp,
                                color = when {
                                    isPrimary -> Color(0xFF0070F3)
                                    isParallel -> Color(0xFF10B981)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBatch(batch.id) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF0070F3).copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = batch.platform.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF38BDF8),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = batch.classGrade.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = batch.examType,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isPrimary) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Primary Batch",
                                            tint = Color(0xFF0070F3),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = batch.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${batch.chapters.size} Chapters • ${batch.totalLectures} Lectures (~${batch.totalHours.toInt()}h content)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isPrimary) "★ Primary Batch" else if (isParallel) "⚡ Active in Parallel Slot" else "Tap card to set Primary",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPrimary) Color(0xFF38BDF8) else if (isParallel) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isParallel) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, if (isParallel) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { onToggleParallelBatch(batch.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Layers,
                                                contentDescription = null,
                                                tint = if (isParallel) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isParallel) "Parallel ✓" else "+ Parallel (2–3)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isParallel) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
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

        // Section 4: Daily Target Working Hours
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Target Working Hours",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF9100).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${dailyWorkingHours.toInt()}h / DAY",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = dailyWorkingHours,
                        onValueChange = { onSetDailyHours(it.roundToInt().toFloat()) },
                        valueRange = 6f..16f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF9100),
                            activeTrackColor = Color(0xFFFF9100)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(8f, 10f, 12f, 14f, 16f).forEach { hrs ->
                            FilterChip(
                                selected = dailyWorkingHours == hrs,
                                onClick = { onSetDailyHours(hrs) },
                                label = { Text("${hrs.toInt()} hrs", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF9100),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterLecturesTab(
    batch: BatchCurriculum,
    targetDays: Int
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${batch.title} Syllabus",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "${batch.totalLectures} Lectures • $targetDays Days",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0070F3)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(batch.chapters) { chap ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF0070F3).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = chap.subject.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = chap.priority,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = chap.chapterName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${chap.totalLectures} Lec (~${chap.estimatedHours.toInt()}h)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanPreviewTab(
    lastResult: AutoPlanResult?,
    selectedBatch: BatchCurriculum,
    targetDays: Int,
    dailyWorkingHours: Float
) {
    if (lastResult == null) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = Color(0xFF0070F3),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Ready to Auto-Plan ($targetDays Days)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap 'Sync & Generate $targetDays-Day Plan' below to interleave your active parallel batches across $targetDays days at ${dailyWorkingHours.toInt()} hours/day.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${lastResult.targetDays}-Day Parallel Plan Generated!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${lastResult.totalGeneratedGoals} daily goals across ${lastResult.targetDays} days • ${lastResult.parallelBatchesCount} Parallel Batch(es) Active.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(lastResult.previewGoalsForDay1) { goal ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "SLOT ${goal.slotIndex + 1} • ${goal.category.uppercase()}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = goal.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (goal.notes.isNotBlank()) {
                                Text(
                                    text = goal.notes,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF9100).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${goal.targetHours}h",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

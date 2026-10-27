package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerticalAlignTop
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PracticeQueueEntity
import com.example.data.model.ChapterItem
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel

/**
 * State-managed 'Practice Stack' queue component where students can add pending chapters
 * to prioritize for their upcoming study sessions.
 */
@Composable
fun PracticeStackQueueView(
    viewModel: JeeViewModel,
    uiState: JeeUiState,
    modifier: Modifier = Modifier
) {
    val queueItems by viewModel.practiceQueue.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingItems = queueItems.filter { !it.isCompleted }
    val completedItems = queueItems.filter { it.isCompleted }

    val nextUpItem = pendingItems.firstOrNull()

    val totalPendingMinutes = pendingItems.sumOf { it.targetMinutes }
    val totalPendingHours = String.format("%.1f", totalPendingMinutes / 60.0)

    val filteredItems = when (selectedFilter) {
        "Pending" -> pendingItems
        "Completed" -> completedItems
        "Physics" -> queueItems.filter { it.subject.contains("Physics", ignoreCase = true) }
        "Chemistry" -> queueItems.filter { it.subject.contains("Chem", ignoreCase = true) }
        "Maths" -> queueItems.filter { it.subject.contains("Math", ignoreCase = true) }
        "Biology" -> queueItems.filter { it.subject.contains("Bio", ignoreCase = true) }
        else -> queueItems
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card: Up Next for Study Session
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_stack_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF6574FF).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = Color(0xFF6574FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Practice Stack Queue",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Prioritized Chapter Practice Flow",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_chapter_to_queue_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6574FF))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Queue Chapter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Next Up Spotlight
                    if (nextUpItem != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF1F3FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6574FF).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF6574FF)
                                        ) {
                                            Text(
                                                text = "NEXT UP #1",
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = nextUpItem.sessionSlot,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF3344AA)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (nextUpItem.priority) {
                                            "URGENT" -> Color(0xFFFFD8E4)
                                            "HIGH" -> Color(0xFFFFDEA5)
                                            else -> Color(0xFFC2E7FF)
                                        }
                                    ) {
                                        Text(
                                            text = "${nextUpItem.priority} PRIORITY",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp,
                                            color = when (nextUpItem.priority) {
                                                "URGENT" -> Color(0xFFB3261E)
                                                "HIGH" -> Color(0xFF7A4500)
                                                else -> Color(0xFF001D35)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = nextUpItem.chapterTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1B1F)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.White
                                        ) {
                                            Text(
                                                text = nextUpItem.subject,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF4A4458),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "🎯 ${nextUpItem.focusArea}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF4A4458)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFF6574FF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${nextUpItem.targetMinutes}m Target",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6574FF)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { viewModel.togglePracticeQueueItem(nextUpItem.id, false) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("hero_mark_complete_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mark Completed for Session", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        // Empty Next Up Banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "All Queued Practice Complete! 🎉",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF00C853)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Queue new pending chapters below to plan your next study sprint.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Queue Metrics Strip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${pendingItems.size}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF6574FF)
                                )
                                Text("In Queue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${totalPendingHours}h",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Study Load", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${completedItems.size}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF00C853)
                                )
                                Text("Practiced", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Filters and Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val filterOptions = listOf(
                        "All" to queueItems.size,
                        "Pending" to pendingItems.size,
                        "Completed" to completedItems.size,
                        "Physics" to queueItems.count { it.subject.contains("Physics", ignoreCase = true) },
                        "Chemistry" to queueItems.count { it.subject.contains("Chem", ignoreCase = true) },
                        "Maths" to queueItems.count { it.subject.contains("Math", ignoreCase = true) }
                    )

                    items(filterOptions) { (label, count) ->
                        FilterChip(
                            selected = selectedFilter == label,
                            onClick = { selectedFilter = label },
                            label = {
                                Text(
                                    text = "$label ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedFilter == label) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6574FF),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("queue_filter_$label")
                        )
                    }
                }

                if (completedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = { viewModel.clearCompletedPracticeQueue() },
                        modifier = Modifier.testTag("clear_completed_queue_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Done", fontSize = 11.sp)
                    }
                }
            }
        }

        // Empty state
        if (filteredItems.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6574FF).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Queue,
                                contentDescription = null,
                                tint = Color(0xFF6574FF),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Practice Stack is Empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add pending chapters from your syllabus to prioritize problem-solving, DPPs, or PYQs for your next session.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6574FF))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Chapter to Queue", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Queue Items List
        itemsIndexed(filteredItems, key = { _, item -> item.id }) { index, item ->
            PracticeQueueItemCard(
                item = item,
                index = index,
                isNextUp = index == 0 && !item.isCompleted,
                onToggleComplete = { viewModel.togglePracticeQueueItem(item.id, item.isCompleted) },
                onPrioritizeTop = { viewModel.prioritizePracticeQueueTop(item.id) },
                onMoveUp = { viewModel.movePracticeQueueItemUp(item.id) },
                onMoveDown = { viewModel.movePracticeQueueItemDown(item.id) },
                onRemove = { viewModel.removeFromPracticeQueue(item.id) }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showAddDialog) {
        AddChapterToQueueDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onConfirm = { chapter, priority, focus, minutes, slot, notes ->
                viewModel.addToPracticeQueue(chapter, priority, focus, minutes, slot, notes)
                showAddDialog = false
            }
        )
    }
}

/**
 * Interactive card representing an individual chapter queued in the Practice Stack.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PracticeQueueItemCard(
    item: PracticeQueueEntity,
    index: Int,
    isNextUp: Boolean,
    onToggleComplete: () -> Unit,
    onPrioritizeTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("queue_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (item.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else if (isNextUp) {
                Color(0xFFF6F8FF)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isNextUp) 2.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order Index, Subject, Priority Pill, and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isNextUp) Color(0xFF6574FF) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "#${index + 1}",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (isNextUp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            item.subject.contains("Physics", ignoreCase = true) -> Color(0xFFEADDFF)
                            item.subject.contains("Math", ignoreCase = true) -> Color(0xFFC2E7FF)
                            item.subject.contains("Chem", ignoreCase = true) -> Color(0xFFC4EED0)
                            else -> Color(0xFFE8DEF8)
                        }
                    ) {
                        Text(
                            text = item.subject,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                item.subject.contains("Physics", ignoreCase = true) -> Color(0xFF21005D)
                                item.subject.contains("Math", ignoreCase = true) -> Color(0xFF001D35)
                                item.subject.contains("Chem", ignoreCase = true) -> Color(0xFF00210E)
                                else -> Color(0xFF1D1B20)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (item.priority) {
                            "URGENT" -> Color(0xFFFFD8E4)
                            "HIGH" -> Color(0xFFFFDEA5)
                            else -> Color(0xFFE8DEF8)
                        }
                    ) {
                        Text(
                            text = item.priority,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            color = when (item.priority) {
                                "URGENT" -> Color(0xFFB3261E)
                                "HIGH" -> Color(0xFF7A4500)
                                else -> Color(0xFF49454F)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Control Icons: Prioritize Top, Up, Down, Remove
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!item.isCompleted) {
                        IconButton(
                            onClick = onPrioritizeTop,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("prioritize_top_${item.id}")
                        ) {
                            Icon(
                                Icons.Default.VerticalAlignTop,
                                contentDescription = "Prioritize to top",
                                tint = Color(0xFF6574FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = "Move Up",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = "Move Down",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("remove_queue_item_${item.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Remove from Queue",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Checkbox and Chapter Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.testTag("queue_checkbox_${item.id}"),
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF00C853),
                        checkmarkColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.chapterTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata Tags: Focus Area, Target Duration, Slot
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8DEF8)
                ) {
                    Text(
                        text = "🎯 ${item.focusArea}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF49454F),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEADDFF).copy(alpha = 0.6f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = Color(0xFF21005D)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${item.targetMinutes}m target",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFC2E7FF).copy(alpha = 0.6f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = Color(0xFF001D35)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = item.sessionSlot,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF001D35)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog for adding a pending chapter to the Practice Stack Queue.
 */
@Composable
fun AddChapterToQueueDialog(
    viewModel: JeeViewModel,
    onDismiss: () -> Unit,
    onConfirm: (chapter: ChapterItem, priority: String, focus: String, minutes: Int, slot: String, notes: String) -> Unit
) {
    val pendingChapters = remember { viewModel.getPendingChaptersForQueue() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("All") }
    var selectedChapter by remember { mutableStateOf(pendingChapters.firstOrNull()) }

    var selectedPriority by remember { mutableStateOf("HIGH") }
    var selectedFocus by remember { mutableStateOf("PYQs & Question Practice") }
    var selectedMinutes by remember { mutableIntStateOf(90) }
    var selectedSlot by remember { mutableStateOf("Next Session") }
    var notesInput by remember { mutableStateOf("") }

    val filteredChapters = pendingChapters.filter { chap ->
        val matchesSubject = when (selectedSubject) {
            "All" -> true
            "Physics" -> chap.subject.contains("Physics", ignoreCase = true)
            "Chemistry" -> chap.subject.contains("Chem", ignoreCase = true)
            "Maths" -> chap.subject.contains("Math", ignoreCase = true)
            "Biology" -> chap.subject.contains("Bio", ignoreCase = true)
            else -> chap.subject.equals(selectedSubject, ignoreCase = true)
        }
        val matchesSearch = searchQuery.isBlank() ||
                chap.title.contains(searchQuery, ignoreCase = true) ||
                chap.subject.contains(searchQuery, ignoreCase = true)

        matchesSubject && matchesSearch
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Queue, contentDescription = null, tint = Color(0xFF6574FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to Practice Stack", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Prioritize pending chapter for your upcoming study session.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Search Box
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search pending chapter...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Subject Filter Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val subjects = listOf("All", "Physics", "Chemistry", "Maths")
                        items(subjects) { subj ->
                            FilterChip(
                                selected = selectedSubject == subj,
                                onClick = { selectedSubject = subj },
                                label = { Text(subj, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6574FF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Chapter Selection List
                item {
                    Text(
                        text = "Select Pending Chapter (${filteredChapters.size}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(filteredChapters.take(20)) { chap ->
                    val isSelected = selectedChapter?.id == chap.id
                    val chain = viewModel.getChapterChain(chap.id)
                    val completedMilestones = chain.completedCount()

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFEADDFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6574FF)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChapter = chap }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = chap.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "${chap.subject} • $completedMilestones/10 Milestones Done",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF6574FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Focus Area Selection
                item {
                    Text(
                        text = "Focus Target for Session:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val focusOptions = listOf(
                        "PYQs & Question Practice",
                        "DPP & Module Exercises",
                        "Prabal & Prakshit Sets",
                        "NCERT Line-by-Line Read",
                        "Formula Sheet & Mistakes"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(focusOptions) { focus ->
                            FilterChip(
                                selected = selectedFocus == focus,
                                onClick = { selectedFocus = focus },
                                label = { Text(focus, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6574FF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Target Minutes Selection
                item {
                    Text(
                        text = "Target Duration:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val minuteOptions = listOf(45, 60, 90, 120, 180)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(minuteOptions) { mins ->
                            FilterChip(
                                selected = selectedMinutes == mins,
                                onClick = { selectedMinutes = mins },
                                label = { Text("${mins}m", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6574FF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Session Slot Selection
                item {
                    Text(
                        text = "Session Slot:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val slotOptions = listOf("Next Session", "Morning Slot", "Afternoon Slot", "Night Slot")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(slotOptions) { slot ->
                            FilterChip(
                                selected = selectedSlot == slot,
                                onClick = { selectedSlot = slot },
                                label = { Text(slot, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6574FF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Priority Selection
                item {
                    Text(
                        text = "Priority Level:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val priorityOptions = listOf("URGENT", "HIGH", "MEDIUM")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        priorityOptions.forEach { p ->
                            FilterChip(
                                selected = selectedPriority == p,
                                onClick = { selectedPriority = p },
                                label = { Text(p, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (p) {
                                        "URGENT" -> Color(0xFFB3261E)
                                        "HIGH" -> Color(0xFF7A4500)
                                        else -> Color(0xFF6574FF)
                                    },
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Notes input
                item {
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        placeholder = { Text("Specific exercise / goal notes...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chap = selectedChapter
                    if (chap != null) {
                        onConfirm(
                            chap,
                            selectedPriority,
                            selectedFocus,
                            selectedMinutes,
                            selectedSlot,
                            notesInput
                        )
                    }
                },
                enabled = selectedChapter != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6574FF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_add_queue_chapter_button")
            ) {
                Text("Add to Stack", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

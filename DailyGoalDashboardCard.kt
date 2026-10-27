package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyGoalEntity
import java.util.Calendar

enum class GoalFilter {
    ALL, PENDING, COMPLETED
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyGoalDashboardCard(
    day: Int,
    sprintDayCurrent: Int,
    goals: List<DailyGoalEntity>,
    onToggleGoal: (goalId: Long, currentVal: Boolean) -> Unit,
    onAddCustomGoal: (title: String, category: String, targetHours: Float, priority: String) -> Unit,
    onDeleteGoal: (goalId: Long) -> Unit,
    onSetAllCompleted: (isCompleted: Boolean) -> Unit,
    onResetGoals: () -> Unit,
    onSelectDay: (day: Int) -> Unit,
    modifier: Modifier = Modifier,
    targetDays: Int = 60
) {
    var selectedFilter by remember { mutableStateOf(GoalFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Date calculations
    val dayIndex = day - 1
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

    // Stats
    val totalCount = goals.size
    val completedCount = goals.count { it.isCompleted }
    val pendingCount = totalCount - completedCount
    val totalHours = goals.map { it.targetHours }.sum()
    val completedHours = goals.filter { it.isCompleted }.map { it.targetHours }.sum()
    val progressPct = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val isAllDone = totalCount > 0 && completedCount == totalCount

    val filteredGoals = when (selectedFilter) {
        GoalFilter.ALL -> goals
        GoalFilter.PENDING -> goals.filter { !it.isCompleted }
        GoalFilter.COMPLETED -> goals.filter { it.isCompleted }
    }

    GlassmorphicContainer(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        rimHighlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        rimShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        glowBorder = Color(0xFF0071E3).copy(alpha = 0.15f),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Day Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0071E3).copy(alpha = 0.10f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0071E3).copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = "DAY $day OF $targetDays",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color(0xFF0071E3),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    if (day != sprintDayCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onSelectDay(sprintDayCurrent) }
                        ) {
                            Text(
                                text = "Go to Today (Day $sprintDayCurrent)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0071E3),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (day > 1) onSelectDay(day - 1) },
                        enabled = day > 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = if (day > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { if (day < targetDays) onSelectDay(day + 1) },
                        enabled = day < targetDays,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = if (day < targetDays) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$dateText • $dayOfWeekName",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Visual Progress Ring Showcase Banner (Glassmorphic Sub-Panel)
            GlassmorphicContainer(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                rimHighlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                rimShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                elevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: The Animated Circular Progress Ring with subtle float & parallax
                        DailyProgressRing(
                            progressPct = progressPct,
                            completedCount = completedCount,
                            totalCount = totalCount,
                            completedHours = completedHours,
                            targetHours = totalHours,
                            size = 136.dp,
                            strokeWidth = 11.dp,
                            modifier = Modifier.testTag("daily_progress_ring")
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        // Right: Motivational Status & Quick Metrics
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (progressPct >= 1f && totalCount > 0)
                                    Color(0xFF34C759).copy(alpha = 0.15f)
                                else
                                    Color(0xFF0071E3).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (progressPct >= 1f && totalCount > 0)
                                        "🎯 100% COMPLETE"
                                    else if (progressPct >= 0.5f)
                                        "🔥 HALFWAY SPRINT"
                                    else if (progressPct > 0f)
                                        "⚡ IN PROGRESS"
                                    else
                                        "📋 READY TO CONQUER",
                                    color = if (progressPct >= 1f && totalCount > 0)
                                        Color(0xFF248A3D)
                                    else
                                        Color(0xFF0071E3),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (progressPct >= 1f && totalCount > 0)
                                    "Daily Goal Achieved!"
                                else if (progressPct >= 0.5f)
                                    "Strong Momentum"
                                else
                                    "Today's JEE Tasks",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (progressPct >= 1f && totalCount > 0)
                                    "All $totalCount targets cleared today. Great discipline!"
                                else
                                    "$pendingCount task${if (pendingCount != 1) "s" else ""} remaining (${String.format("%.0fh", totalHours - completedHours)} left)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons in the ring banner
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                    modifier = Modifier.clickable {
                                        onSetAllCompleted(progressPct < 1f)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (progressPct >= 1f) Icons.Default.Refresh else Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (progressPct >= 1f) "Reset All" else "All Done",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0071E3),
                                    modifier = Modifier.clickable { showAddDialog = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Goal",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+ Goal",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isAllDone) {
                        ConfettiAnimation(
                            triggerKey = isAllDone,
                            particleCount = 50,
                            durationMillis = 2800
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFilter == GoalFilter.ALL,
                        onClick = { selectedFilter = GoalFilter.ALL },
                        label = { Text("All ($totalCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0071E3).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF0071E3)
                        ),
                        border = null
                    )

                    FilterChip(
                        selected = selectedFilter == GoalFilter.PENDING,
                        onClick = { selectedFilter = GoalFilter.PENDING },
                        label = { Text("Pending ($pendingCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0071E3).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF0071E3)
                        ),
                        border = null
                    )

                    FilterChip(
                        selected = selectedFilter == GoalFilter.COMPLETED,
                        onClick = { selectedFilter = GoalFilter.COMPLETED },
                        label = { Text("Done ($completedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF34C759).copy(alpha = 0.18f),
                            selectedLabelColor = Color(0xFF248A3D)
                        ),
                        border = null
                    )
                }

                IconButton(
                    onClick = { onResetGoals() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Day Tasks",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Tasks List
            if (filteredGoals.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selectedFilter == GoalFilter.COMPLETED) Icons.Default.EditNote else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF0071E3),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedFilter == GoalFilter.COMPLETED)
                                "No completed tasks yet. Tick off your first task above!"
                            else
                                "All pending tasks completed for Day $day! Excellent work.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredGoals.forEach { goal ->
                        TaskItemRow(
                            goal = goal,
                            onToggle = { onToggleGoal(goal.id, goal.isCompleted) },
                            onDelete = { onDeleteGoal(goal.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Custom Goal Dialog
    if (showAddDialog) {
        AddCustomGoalDialog(
            day = day,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, hours, priority ->
                onAddCustomGoal(title, category, hours, priority)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaskItemRow(
    goal: DailyGoalEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (goal.category.lowercase()) {
        "physics" -> Color(0xFF0071E3)
        "chemistry", "chem" -> Color(0xFF34C759)
        "maths", "mathematics" -> Color(0xFF5856D6)
        "revision" -> Color(0xFFFF9500)
        "mock test", "test" -> Color(0xFFFF2D55)
        else -> Color(0xFFAF52DE)
    }

    // Innovative tactile Neumorphic card with soft inner/outer shadows
    NeumorphicCard(
        shape = RoundedCornerShape(16.dp),
        baseColor = if (goal.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        lightColor = Color.White,
        shadowColor = Color(0xFFD1D1D6).copy(alpha = 0.5f),
        isSunken = goal.isCompleted,
        onClick = { onToggle() },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${goal.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Neumorphic Checkbox
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (goal.isCompleted)
                            Brush.linearGradient(listOf(Color(0xFF34C759), Color(0xFF28A745)))
                        else
                            Brush.linearGradient(listOf(Color(0xFFF2F2F7), Color(0xFFE5E5EA)))
                    )
                    .border(
                        BorderStroke(
                            1.2.dp,
                            if (goal.isCompleted) Color(0xFF34C759) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onToggle() }
                    .testTag("goal_checkbox_${goal.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (goal.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task info
            Column(modifier = Modifier.weight(1f)) {
                // Category & Meta Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, categoryColor.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = goal.category.uppercase(),
                            color = categoryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (goal.slotIndex >= 0) {
                        Text(
                            text = "Slot ${goal.slotIndex + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0071E3).copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, Color(0xFF0071E3).copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = "CUSTOM",
                                color = Color(0xFF0071E3),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = "• ${String.format("%.1fh", goal.targetHours)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = goal.title,
                    fontSize = 13.5.sp,
                    fontWeight = if (goal.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (goal.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (goal.notes.isNotEmpty() && !goal.isCompleted) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = goal.notes,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Custom goal delete button
            if (goal.slotIndex == -1) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Goal",
                        tint = Color(0xFFFF3B30).copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCustomGoalDialog(
    day: Int,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, targetHours: Float, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Physics") }
    var targetHours by remember { mutableFloatStateOf(1.5f) }
    var priority by remember { mutableStateOf("HIGH") }

    val categories = listOf("Physics", "Chemistry", "Maths", "Revision", "Mock Test", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Goal for Day $day",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description") },
                    placeholder = { Text("e.g., Solve 25 Electrostatics PYQs") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_goal_input"),
                    singleLine = true
                )

                Text(
                    text = "Subject / Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == cat) Color(0xFF6574FF) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == cat) Color(0xFF6574FF) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Text(
                    text = "Estimated Time: ${String.format("%.1f", targetHours)} Hours",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1.0f to "1h", 1.5f to "1.5h", 2.0f to "2h", 3.0f to "3h").forEach { (h, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (targetHours == h) Color(0xFF6574FF) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { targetHours = h }
                        ) {
                            Text(
                                text = label,
                                color = if (targetHours == h) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), selectedCategory, targetHours, priority)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6574FF)),
                modifier = Modifier.testTag("submit_custom_goal_button")
            ) {
                Text("Add Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

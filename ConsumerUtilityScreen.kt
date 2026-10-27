package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AtomicHabitEntity
import com.example.data.local.ProductivityTaskEntity
import com.example.data.local.StudentExpenseEntity
import com.example.data.local.WellnessDailyLogEntity
import com.example.ui.viewmodel.JeeUiState
import com.example.ui.viewmodel.JeeViewModel
import kotlinx.coroutines.delay

enum class UtilityTab(val title: String, val subtitle: String) {
    PRODUCTIVITY("Productivity", "Eisenhower Matrix & Tasks"),
    FINANCE("Student Finance", "Budget, Fees & Subs"),
    HABITS("Atomic Habits", "Daily Routines & Streaks"),
    WELLNESS("Wellness & Mind", "Burnout & Calm Breath")
}

@Composable
fun ConsumerUtilityScreen(
    uiState: JeeUiState,
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(UtilityTab.PRODUCTIVITY) }

    // Dialog state
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }

    // Observables
    val tasks by viewModel.utilityTasks.collectAsState()
    val expenses by viewModel.utilityExpenses.collectAsState()
    val totalSpent by viewModel.utilityTotalSpent.collectAsState()
    val habits by viewModel.utilityHabits.collectAsState()
    val latestWellness by viewModel.utilityLatestWellness.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("consumer_utility_screen")
    ) {
        // Hero Header Banner
        UtilityHeroBanner(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // Sub-Tab Navigation Bar
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFF0070F3),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = Color(0xFF0070F3),
                    height = 3.dp
                )
            }
        ) {
            UtilityTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    modifier = Modifier.testTag("utility_tab_${tab.name.lowercase()}"),
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Active Tab Screen Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                UtilityTab.PRODUCTIVITY -> ProductivitySubScreen(
                    tasks = tasks,
                    onToggleTask = { id, done -> viewModel.toggleProductivityTask(id, done) },
                    onDeleteTask = { viewModel.deleteProductivityTask(it) },
                    onClearCompleted = { viewModel.clearCompletedTasks() },
                    onOpenAddDialog = { showAddTaskDialog = true }
                )
                UtilityTab.FINANCE -> StudentFinanceSubScreen(
                    expenses = expenses,
                    totalSpent = totalSpent ?: 0.0,
                    onDeleteExpense = { viewModel.deleteStudentExpense(it) },
                    onOpenAddDialog = { showAddExpenseDialog = true }
                )
                UtilityTab.HABITS -> AtomicHabitsSubScreen(
                    habits = habits,
                    onToggleHabit = { viewModel.toggleAtomicHabitToday(it) },
                    onDeleteHabit = { viewModel.deleteAtomicHabit(it) },
                    onOpenAddDialog = { showAddHabitDialog = true }
                )
                UtilityTab.WELLNESS -> WellnessSubScreen(
                    wellnessLog = latestWellness,
                    onAddWater = { viewModel.addWaterIntakeQuick(it) },
                    onLogWellness = { sleep, quality, water, stress, mindful, notes ->
                        viewModel.logStudentWellness(sleep, quality, water, stress, mindful, notes)
                    }
                )
            }
        }
    }

    // Modal Dialogs
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, quad, prio, poms, cat, due ->
                viewModel.addProductivityTask(title, quad, prio, poms, cat, due)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, amt, cat, isSub, cycle, notes ->
                viewModel.addStudentExpense(title, amt, cat, isSub, cycle, notes)
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { title, cat, days, reminder ->
                viewModel.addAtomicHabit(title, cat, days, reminder)
                showAddHabitDialog = false
            }
        )
    }
}

@Composable
private fun UtilityHeroBanner(
    selectedTab: UtilityTab,
    onTabSelected: (UtilityTab) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0070F3)
                    ) {
                        Text(
                            text = "STUDENT OS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "High-Performance Life Utilities",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Balancing exam excellence with productivity, finances, routines & wellness",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==========================================
// 1. PRODUCTIVITY SUB-SCREEN (EISENHOWER MATRIX)
// ==========================================

@Composable
private fun ProductivitySubScreen(
    tasks: List<ProductivityTaskEntity>,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (ProductivityTaskEntity) -> Unit,
    onClearCompleted: () -> Unit,
    onOpenAddDialog: () -> Unit
) {
    var selectedQuadrantFilter by remember { mutableStateOf("ALL") }

    val filteredTasks = remember(tasks, selectedQuadrantFilter) {
        if (selectedQuadrantFilter == "ALL") tasks
        else tasks.filter { it.quadrant == selectedQuadrantFilter }
    }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val completionPct = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Eisenhower Matrix Execution",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$completedCount of $totalCount priorities crushed ($completionPct%)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (completedCount > 0) {
                                OutlinedButton(
                                    onClick = onClearCompleted,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Clear Done", fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { completionPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF0070F3),
                            trackColor = Color(0xFF0070F3).copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // Quadrant Filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All Tasks",
                        "DO_FIRST" to "⚡ Q1: Urgent & Crucial",
                        "SCHEDULE" to "📅 Q2: Deep Concept Work",
                        "DELEGATE" to "👥 Q3: Delegate/Quick",
                        "ELIMINATE" to "🚫 Q4: Eliminate Time-Wasters"
                    )
                    filters.forEach { (key, label) ->
                        val isSelected = selectedQuadrantFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedQuadrantFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0070F3).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF0070F3)
                            )
                        )
                    }
                }
            }

            // Task Items
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Tasks in this Quadrant",
                        description = "Tap the + button to add a high-yield study task or daily priority."
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        onToggle = { onToggleTask(task.id, !task.isCompleted) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }

        // FAB to Add Task
        FloatingActionButton(
            onClick = onOpenAddDialog,
            containerColor = Color(0xFF0070F3),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_task")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Priority Task")
        }
    }
}

@Composable
private fun TaskItemCard(
    task: ProductivityTaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val quadrantBadgeColor = when (task.quadrant) {
        "DO_FIRST" -> Color(0xFFEF4444)
        "SCHEDULE" -> Color(0xFF0070F3)
        "DELEGATE" -> Color(0xFFF59E0B)
        else -> Color(0xFF6B7280)
    }

    val quadrantLabel = when (task.quadrant) {
        "DO_FIRST" -> "DO FIRST (Q1)"
        "SCHEDULE" -> "SCHEDULE (Q2)"
        "DELEGATE" -> "DELEGATE (Q3)"
        else -> "ELIMINATE (Q4)"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            else quadrantBadgeColor.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox / Complete Button
            Surface(
                shape = CircleShape,
                color = if (task.isCompleted) Color(0xFF10B981) else Color.Transparent,
                border = BorderStroke(
                    1.5.dp,
                    if (task.isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onToggle() }
            ) {
                if (task.isCompleted) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = quadrantBadgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = quadrantLabel,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = quadrantBadgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = task.priority,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0070F3),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${task.category}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${task.dueDate}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (task.estimatedPomodoros > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "🍅 ${task.estimatedPomodoros} Pomodoro sessions",
                            fontSize = 10.5.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// 2. STUDENT FINANCE & EXPENSE / SUBSCRIPTION LEDGER
// ==========================================

@Composable
private fun StudentFinanceSubScreen(
    expenses: List<StudentExpenseEntity>,
    totalSpent: Double,
    onDeleteExpense: (StudentExpenseEntity) -> Unit,
    onOpenAddDialog: () -> Unit
) {
    val monthlyBudget = 15000.0 // Standard student monthly budget in INR
    val budgetProgress = (totalSpent / monthlyBudget).coerceIn(0.0, 1.0).toFloat()
    val isOverBudget = totalSpent > monthlyBudget

    val subscriptions = remember(expenses) { expenses.filter { it.isSubscription } }
    val regularExpenses = remember(expenses) { expenses.filter { !it.isSubscription } }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val displayedExpenses = remember(expenses, selectedFilter) {
        when (selectedFilter) {
            "SUBS" -> subscriptions
            "ONETIME" -> regularExpenses
            else -> expenses
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Budget Burn-rate Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isOverBudget) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Student Allowance & Ledger",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Coaching fees, test series & living costs",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOverBudget) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isOverBudget) "OVER BUDGET" else "ON TRACK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOverBudget) Color(0xFFEF4444) else Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Total Spent",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${totalSpent.toInt()}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isOverBudget) Color(0xFFEF4444) else Color(0xFF0070F3)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Target Monthly Ceiling",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${monthlyBudget.toInt()}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { budgetProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isOverBudget) Color(0xFFEF4444) else Color(0xFF10B981),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Filters (All, Subscriptions, One-time)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All Expenses (${expenses.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "SUBS",
                        onClick = { selectedFilter = "SUBS" },
                        label = { Text("Subscriptions & Batches (${subscriptions.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "ONETIME",
                        onClick = { selectedFilter = "ONETIME" },
                        label = { Text("Books & Living (${regularExpenses.size})", fontSize = 11.sp) }
                    )
                }
            }

            // Expense Items
            if (displayedExpenses.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Expenses Logged",
                        description = "Keep track of test series, books, and tuition fees effortlessly."
                    )
                }
            } else {
                items(displayedExpenses, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        onDelete = { onDeleteExpense(expense) }
                    )
                }
            }
        }

        // FAB to Add Expense
        FloatingActionButton(
            onClick = onOpenAddDialog,
            containerColor = Color(0xFF10B981),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_expense")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense Entry")
        }
    }
}

@Composable
private fun ExpenseItemCard(
    expense: StudentExpenseEntity,
    onDelete: () -> Unit
) {
    val categoryColor = when (expense.category) {
        "TEST_SERIES" -> Color(0xFF0070F3)
        "COACHING" -> Color(0xFF8B5CF6)
        "BOOKS" -> Color(0xFFF59E0B)
        "FOOD_MESS" -> Color(0xFF10B981)
        else -> Color(0xFF6B7280)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = categoryColor.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "₹",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = categoryColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = expense.category.replace("_", " "),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (expense.isSubscription) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = expense.billingCycle,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5CF6),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = expense.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (expense.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = expense.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${expense.amount.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. ATOMIC HABITS & DAILY STUDENT RITUALS
// ==========================================

@Composable
private fun AtomicHabitsSubScreen(
    habits: List<AtomicHabitEntity>,
    onToggleHabit: (AtomicHabitEntity) -> Unit,
    onDeleteHabit: (AtomicHabitEntity) -> Unit,
    onOpenAddDialog: () -> Unit
) {
    val completedTodayCount = habits.count { it.isCompletedToday }
    val totalHabits = habits.size
    val habitPct = if (totalHabits > 0) (completedTodayCount * 100 / totalHabits) else 0

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Habit Streak Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6D00),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Atomic Habits & Daily Disciplines",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "$completedTodayCount of $totalHabits habits marked complete today ($habitPct%)",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF6D00).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "CONSISTENCY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF6D00),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { habitPct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFF6D00),
                            trackColor = Color(0xFFFF6D00).copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // Habits List
            if (habits.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Habits Created",
                        description = "Build compounding routines: morning recall, active testing, and mindful sleep."
                    )
                }
            } else {
                items(habits, key = { it.id }) { habit ->
                    HabitItemCard(
                        habit = habit,
                        onToggle = { onToggleHabit(habit) },
                        onDelete = { onDeleteHabit(habit) }
                    )
                }
            }
        }

        // FAB to Add Habit
        FloatingActionButton(
            onClick = onOpenAddDialog,
            containerColor = Color(0xFFFF6D00),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_habit")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Atomic Habit")
        }
    }
}

@Composable
private fun HabitItemCard(
    habit: AtomicHabitEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompletedToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (habit.isCompletedToday) Color(0xFF10B981).copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // One-tap completion button
            Surface(
                shape = CircleShape,
                color = if (habit.isCompletedToday) Color(0xFF10B981) else Color.Transparent,
                border = BorderStroke(
                    2.dp,
                    if (habit.isCompletedToday) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onToggle() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (habit.isCompletedToday) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = "TAP",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = habit.category.replace("_", " "),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⏰ ${habit.reminderTime}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = habit.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${habit.currentStreak} day streak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6D00)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• Best: ${habit.bestStreak}d",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// 4. WELLNESS, BURNOUT MONITOR & MINDFUL BREATHING
// ==========================================

@Composable
private fun WellnessSubScreen(
    wellnessLog: WellnessDailyLogEntity?,
    onAddWater: (Int) -> Unit,
    onLogWellness: (Float, Int, Int, Int, Int, String) -> Unit
) {
    var sleepHours by remember(wellnessLog) { mutableFloatStateOf(wellnessLog?.sleepHours ?: 7.5f) }
    var sleepQuality by remember(wellnessLog) { mutableIntStateOf(wellnessLog?.sleepQualityRating ?: 4) }
    var stressRating by remember(wellnessLog) { mutableIntStateOf(wellnessLog?.stressRating ?: 3) }
    var mindfulMinutes by remember(wellnessLog) { mutableIntStateOf(wellnessLog?.mindfulMinutes ?: 10) }
    var notes by remember(wellnessLog) { mutableStateOf(wellnessLog?.notes ?: "") }

    val currentWaterMl = wellnessLog?.waterIntakeMl ?: 2000
    val targetWaterMl = wellnessLog?.targetWaterMl ?: 3000
    val waterProgress = (currentWaterMl.toFloat() / targetWaterMl).coerceIn(0f, 1f)

    // Burnout Index calculation
    // High stress + low sleep = elevated burnout risk
    val burnoutScore = remember(sleepHours, stressRating) {
        val sleepPenalty = if (sleepHours < 6.0f) (6.0f - sleepHours) * 2 else 0f
        ((stressRating * 0.7f) + sleepPenalty).coerceIn(1f, 10f)
    }

    val burnoutStatus = when {
        burnoutScore <= 4.0f -> "LOW BURNOUT RISK (Peak Mental Readiness)"
        burnoutScore <= 7.0f -> "MODERATE FATIGUE (Active Rest Recommended)"
        else -> "HIGH BURNOUT ALERT (Immediate Mindful Pause Required)"
    }
    val burnoutColor = when {
        burnoutScore <= 4.0f -> Color(0xFF10B981)
        burnoutScore <= 7.0f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Burnout Assessment Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, burnoutColor.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Student Burnout Index",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = burnoutColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = String.format("%.1f / 10", burnoutScore),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = burnoutColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = burnoutStatus,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = burnoutColor
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (burnoutScore > 6.0f)
                            "Warning: Low sleep and high exam pressure degrade cognitive recall. Run the 4-7-8 Breathing Reset below before your next mock test!"
                        else
                            "Great neurological balance! Your sleep and focus levels support deep problem-solving speed.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Hydration Tracker Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Brain Hydration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currentWaterMl ml of $targetWaterMl ml target (${(waterProgress * 100).toInt()}%)",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "💧",
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { waterProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF0070F3),
                        trackColor = Color(0xFF0070F3).copy(alpha = 0.15f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAddWater(250) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+250 ml (Cup)", fontSize = 11.5.sp)
                        }
                        OutlinedButton(
                            onClick = { onAddWater(500) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+500 ml (Bottle)", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // Guided 4-7-8 Mindful Box Breathing Timer Card
        item {
            BoxBreathingTimerCard()
        }

        // Daily Check-in Sliders & Log
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Wellness & Recovery Check-in",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Log sleep hours and stress to track trends against mock exam performance",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sleep Hours Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sleep Duration", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(String.format("%.1f Hours", sleepHours), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0070F3))
                    }
                    Slider(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        valueRange = 4.0f..10.0f,
                        steps = 11
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stress Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Exam Stress / Anxiety Level", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("$stressRating / 10", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (stressRating > 6) Color(0xFFEF4444) else Color(0xFF10B981))
                    }
                    Slider(
                        value = stressRating.toFloat(),
                        onValueChange = { stressRating = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Mindset & Energy Reflection", fontSize = 12.sp) },
                        placeholder = { Text("e.g. Felt focused in Chem, need better sleep tonight", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onLogWellness(sleepHours, sleepQuality, currentWaterMl, stressRating, mindfulMinutes, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Wellness Check-in", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Interactive 4-7-8 Breathing Reset Component for Exam Anxiety Relief
 */
@Composable
private fun BoxBreathingTimerCard() {
    var isBreathingActive by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf("Ready") } // Inhale (4s), Hold (7s), Exhale (8s)
    var countdown by remember { mutableIntStateOf(4) }

    LaunchedEffect(isBreathingActive) {
        if (isBreathingActive) {
            while (isBreathingActive) {
                // Inhale 4s
                phase = "Inhale slowly through nose..."
                for (i in 4 downTo 1) {
                    countdown = i
                    delay(1000L)
                    if (!isBreathingActive) break
                }
                if (!isBreathingActive) break

                // Hold 7s
                phase = "Hold breath gently..."
                for (i in 7 downTo 1) {
                    countdown = i
                    delay(1000L)
                    if (!isBreathingActive) break
                }
                if (!isBreathingActive) break

                // Exhale 8s
                phase = "Exhale slowly through mouth..."
                for (i in 8 downTo 1) {
                    countdown = i
                    delay(1000L)
                    if (!isBreathingActive) break
                }
            }
        } else {
            phase = "Tap Start to Begin"
            countdown = 4
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val circleScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "4-7-8 Mindful Breath Reset",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Science-backed vagus nerve calming for test anxiety",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "CALM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Breathing Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .scale(if (isBreathingActive) circleScale else 1.0f)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                    .padding(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBreathingActive) "$countdown" else "🧘",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = phase,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { isBreathingActive = !isBreathingActive },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBreathingActive) Color(0xFFEF4444) else Color(0xFF8B5CF6)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBreathingActive) "Stop Breathing Session" else "Start 4-7-8 Breathing Reset",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// MODAL DIALOGS FOR ADDING ENTRIES
// ==========================================

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var quadrant by remember { mutableStateOf("DO_FIRST") }
    var priority by remember { mutableStateOf("P1") }
    var pomodoros by remember { mutableIntStateOf(2) }
    var category by remember { mutableStateOf("Physics") }
    var dueDate by remember { mutableStateOf("Today") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Priority Task (Eisenhower Matrix)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. Solve 20 Kinematics Advanced Problems") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Eisenhower Quadrant:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quads = listOf("DO_FIRST" to "Q1: Do First", "SCHEDULE" to "Q2: Schedule", "DELEGATE" to "Q3: Delegate", "ELIMINATE" to "Q4: Eliminate")
                    quads.forEach { (qKey, qLabel) ->
                        FilterChip(
                            selected = quadrant == qKey,
                            onClick = { quadrant = qKey },
                            label = { Text(qLabel, fontSize = 10.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Subject/Tag") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pomodoro Sessions: $pomodoros 🍅", fontSize = 11.5.sp)
                    Row {
                        IconButton(onClick = { if (pomodoros > 1) pomodoros-- }) { Text("-", fontSize = 18.sp) }
                        IconButton(onClick = { pomodoros++ }) { Text("+", fontSize = 18.sp) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title, quadrant, priority, pomodoros, category, dueDate)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Boolean, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("TEST_SERIES") }
    var isSub by remember { mutableStateOf(false) }
    var cycle by remember { mutableStateOf("ONE_TIME") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Student Expense / Subscription", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title") },
                    placeholder = { Text("e.g. Resonance Mock Test Series Batch") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (INR ₹)") },
                    placeholder = { Text("e.g. 2999") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val cats = listOf("TEST_SERIES", "COACHING", "BOOKS", "FOOD_MESS", "TECH_STATIONERY", "MISC")
                    cats.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Is Recurring Subscription?", fontSize = 11.5.sp)
                    FilterChip(
                        selected = isSub,
                        onClick = {
                            isSub = !isSub
                            cycle = if (isSub) "MONTHLY" else "ONE_TIME"
                        },
                        label = { Text(if (isSub) "Yes (Recurring)" else "One-Time") }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Validity Details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0.0) {
                        onConfirm(title, amt, category, isSub, cycle, notes)
                    }
                },
                enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("STUDY_FOCUS") }
    var targetDays by remember { mutableIntStateOf(7) }
    var reminderTime by remember { mutableStateOf("07:00 AM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Atomic Student Habit", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Title") },
                    placeholder = { Text("e.g. 15-Minute Nightly Organic Chemistry Review") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Routine Category:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val cats = listOf("MORNING_ROUTINE", "STUDY_FOCUS", "PHYSICAL_WELLNESS", "EVENING_WINDDOWN")
                    cats.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Daily Reminder Time") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(title, category, targetDays, reminderTime)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Create Habit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

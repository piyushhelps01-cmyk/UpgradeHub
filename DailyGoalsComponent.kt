package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.local.DailyGoalEntity
import com.example.ui.viewmodel.JeeViewModel

/**
 * 'Daily Goals' component that fetches current day tasks from the Room database
 * and allows marking them as complete/incomplete.
 *
 * Fully integrated with [DailyGoalDashboardCard] and [JeeViewModel] StateFlow observing Room DB changes.
 */
@Composable
fun DailyGoalsComponent(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val currentGoals by viewModel.currentDailyGoals.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    DailyGoalsComponent(
        day = uiState.selectedDay,
        sprintDayCurrent = uiState.sprintDayCurrent,
        goals = currentGoals,
        onToggleGoal = { goalId, currentVal ->
            viewModel.toggleDailyGoal(goalId, currentVal)
        },
        onAddCustomGoal = { title, category, targetHours, priority ->
            viewModel.addCustomDailyGoal(title, category, targetHours, priority)
        },
        onDeleteGoal = { goalId ->
            viewModel.deleteDailyGoal(goalId)
        },
        onSetAllCompleted = { isCompleted ->
            viewModel.setAllDailyGoalsCompleted(isCompleted)
        },
        onResetGoals = {
            viewModel.resetDailyGoalsForCurrentDay()
        },
        onSelectDay = { day ->
            viewModel.selectDay(day)
        },
        modifier = modifier
    )
}

/**
 * Convenience alias for [DailyGoalsComponent].
 */
@Composable
fun DailyGoals(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    DailyGoalsComponent(viewModel = viewModel, modifier = modifier)
}

/**
 * Stateless overload of [DailyGoalsComponent] delegating directly to [DailyGoalDashboardCard].
 */
@Composable
fun DailyGoalsComponent(
    day: Int,
    goals: List<DailyGoalEntity>,
    onToggleGoal: (goalId: Long, currentVal: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    sprintDayCurrent: Int = day,
    onAddCustomGoal: ((title: String, category: String, targetHours: Float, priority: String) -> Unit)? = null,
    onDeleteGoal: ((goalId: Long) -> Unit)? = null,
    onSetAllCompleted: ((isCompleted: Boolean) -> Unit)? = null,
    onResetGoals: (() -> Unit)? = null,
    onSelectDay: ((day: Int) -> Unit)? = null
) {
    DailyGoalDashboardCard(
        day = day,
        sprintDayCurrent = sprintDayCurrent,
        goals = goals,
        onToggleGoal = onToggleGoal,
        onAddCustomGoal = { title, category, hours, priority ->
            onAddCustomGoal?.invoke(title, category, hours, priority)
        },
        onDeleteGoal = { goalId ->
            onDeleteGoal?.invoke(goalId)
        },
        onSetAllCompleted = { isCompleted ->
            onSetAllCompleted?.invoke(isCompleted)
        },
        onResetGoals = {
            onResetGoals?.invoke()
        },
        onSelectDay = { targetDay ->
            onSelectDay?.invoke(targetDay)
        },
        modifier = modifier
    )
}

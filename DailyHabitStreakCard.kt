package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitDayIndicator
import com.example.data.model.HabitStreakData
import java.util.Locale

@Composable
fun DailyHabitStreakCard(
    streakData: HabitStreakData,
    onDayClick: (dayNumber: Int) -> Unit,
    onLogHoursClick: () -> Unit,
    onViewDailyGoalsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCompleteStudySessionClick: () -> Unit = onViewDailyGoalsClick
) {
    var showMilestonesDialog by remember { mutableStateOf(false) }
    var inspectDay by remember { mutableStateOf<HabitDayIndicator?>(null) }

    // Pulsing flame animation for active streak
    val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    val animatedSessionProgress by animateFloatAsState(
        targetValue = (streakData.todayProgressPct / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "sessionProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_streak_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (streakData.isTodayGoalMet) Color(0xFF10B981).copy(alpha = 0.45f)
            else Color(0xFFFF6D00).copy(alpha = 0.40f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Section: Flame Icon, Streak Count, Best Streak & Milestone Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Glowing Flame Badge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF6D00).copy(alpha = 0.16f),
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.5.dp, Color(0xFFFF9100).copy(alpha = 0.55f), CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak Flame",
                                tint = Color(0xFFFF6D00),
                                modifier = Modifier
                                    .size(28.dp)
                                    .scale(if (streakData.currentStreak > 0) flameScale else 1.0f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${streakData.currentStreak}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("streak_count_text")
                            )
                            Text(
                                text = "-Day Streak",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF6D00).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = streakData.streakRating,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFF9100),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DAILY STUDY STREAK • BEST: ${streakData.bestStreak} DAYS",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Info / Milestones button
                IconButton(
                    onClick = { showMilestonesDialog = true },
                    modifier = Modifier.testTag("streak_milestones_info_button")
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Milestone Tiers",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Today's Study Session Completion Status Banner with Material 3 Progress Indicators
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (streakData.isTodayGoalMet) {
                    Color(0xFF10B981).copy(alpha = 0.12f)
                } else {
                    Color(0xFFFF9100).copy(alpha = 0.12f)
                },
                border = BorderStroke(
                    1.dp,
                    if (streakData.isTodayGoalMet) Color(0xFF10B981).copy(alpha = 0.45f)
                    else Color(0xFFFF9100).copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                                CircularProgressIndicator(
                                    progress = { animatedSessionProgress.coerceAtLeast(0.12f) },
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.5.dp,
                                    color = if (streakData.isTodayGoalMet) Color(0xFF10B981) else Color(0xFFFF9100),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeCap = StrokeCap.Round
                                )
                                Icon(
                                    imageVector = if (streakData.isTodayGoalMet) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (streakData.isTodayGoalMet) Color(0xFF10B981) else Color(0xFFFF9100),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (streakData.isTodayGoalMet) {
                                        "Today's Streak Session Locked In! ✨"
                                    } else {
                                        "Complete Study Session to Extend Streak"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = if (streakData.isTodayGoalMet) Color(0xFF34D399) else Color(0xFFFBBF24)
                                )
                                Text(
                                    text = "${streakData.todayCompletedSessions}/${streakData.todayTotalSessions} sessions done • ${String.format(Locale.ENGLISH, "%.1f", streakData.todayCompletedHours)}h / ${streakData.todayTargetHours.toInt()}h logged today",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (streakData.isTodayGoalMet) Color(0xFF10B981) else Color(0xFFFF6D00)
                        ) {
                            Text(
                                text = "${streakData.todayProgressPct}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { animatedSessionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (streakData.isTodayGoalMet) Color(0xFF10B981) else Color(0xFFFF9100),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Indicator of Consecutive Days (Timeline / Streak Chain)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-DAY SESSION STREAK TIMELINE",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Next: ${streakData.nextMilestoneName} (${streakData.daysUntilNextMilestone}d left)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Consecutive Days Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                streakData.consecutiveDays.forEach { dayIndicator ->
                    HabitDayNode(
                        day = dayIndicator,
                        onClick = {
                            inspectDay = dayIndicator
                            onDayClick(dayIndicator.dayNumber)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Motivational Banner Quote
            Text(
                text = streakData.motivationalMessage,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Session Completion & Focus Timer Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLogHoursClick,
                    modifier = Modifier
                        .weight(0.95f)
                        .testTag("log_hours_shortcut_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Focus Studio", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = onCompleteStudySessionClick,
                    modifier = Modifier
                        .weight(1.35f)
                        .testTag("complete_session_streak_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (streakData.isTodayGoalMet) Color(0xFF10B981) else Color(0xFFFF6D00)
                    )
                ) {
                    Icon(
                        imageVector = if (streakData.isTodayGoalMet) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (streakData.isTodayGoalMet) "+ Log Extra Session" else "Complete Session (+Streak)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Milestones Info Dialog
    if (showMilestonesDialog) {
        AlertDialog(
            onDismissRequest = { showMilestonesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Habit Streak Milestones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Consistency beats intensity. Completing at least 2 daily study sessions or 6 focused hours keeps your daily streak active!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val tiers = listOf(
                        Triple("3 Days", "Spark 🔥", "Ignited consistent daily study routine"),
                        Triple("7 Days", "Iron Habit ⚡", "1 full week of unbroken goal attainment"),
                        Triple("14 Days", "Sprint Titan 🚀", "2 solid weeks of exam discipline"),
                        Triple("30 Days", "Unstoppable Master 👑", "1 full month of top-tier consistency"),
                        Triple("60 Days", "Grand JEE Ranker 🏆", "Complete 60-day sprint mastery")
                    )

                    tiers.forEach { (days, name, desc) ->
                        val unlocked = streakData.currentStreak >= days.takeWhile { it.isDigit() }.toInt()
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (unlocked) Color(0xFF10B981).copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (unlocked) Color(0xFF10B981).copy(alpha = 0.5f) else Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$days • $name",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (unlocked) Color(0xFF34D399) else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (unlocked) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showMilestonesDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    // Day Detail Alert
    inspectDay?.let { day ->
        AlertDialog(
            onDismissRequest = { inspectDay = null },
            title = {
                Text(
                    text = "Day ${day.dayNumber} (${day.dateLabel})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (day.isGoalMet) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFFF9100).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (day.isGoalMet) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (day.isGoalMet) Color(0xFF10B981) else Color(0xFFFF9100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (day.isGoalMet) "Session Goal Reached (Streak Active)" else "Session Goal In Progress",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (day.isGoalMet) Color(0xFF34D399) else Color(0xFFFBBF24)
                            )
                        }
                    }

                    Text(
                        text = "• Completed Hours: ${String.format(Locale.ENGLISH, "%.1f", day.completedHours)}h (Target: ${day.targetHours.toInt()}h)",
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Sessions Completed: ${day.completedGoalsCount} of ${day.totalGoalsCount} daily sessions",
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { inspectDay = null }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Individual visual day beacon in the streak chain.
 */
@Composable
private fun HabitDayNode(
    day: HabitDayIndicator,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = when {
            day.isToday -> Color(0xFF0070F3).copy(alpha = 0.16f)
            day.isGoalMet -> Color(0xFFFF6D00).copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            width = if (day.isToday) 1.5.dp else 1.dp,
            color = when {
                day.isToday -> Color(0xFF38BDF8)
                day.isGoalMet -> Color(0xFFFF9100).copy(alpha = 0.45f)
                else -> Color.Transparent
            }
        ),
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
            .testTag("habit_day_node_${day.dayNumber}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (day.isToday) "TODAY" else day.dayOfWeek.take(3).uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (day.isToday) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Text(
                text = "D${day.dayNumber}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Central beacon circle with icon
            Surface(
                shape = CircleShape,
                color = when {
                    day.isGoalMet -> Color(0xFFFF6D00)
                    day.isToday -> Color(0xFF0070F3)
                    day.isFuture -> Color.Transparent
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                border = if (day.isFuture) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        day.isGoalMet -> {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        day.isToday -> {
                            Text(
                                text = "${day.completedHours.toInt()}h",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        day.isFuture -> {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        else -> {
                            Text(
                                text = "${day.completedHours.toInt()}h",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = if (day.isFuture) "--" else "${String.format(Locale.ENGLISH, "%.1f", day.completedHours)}h",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (day.isGoalMet) Color(0xFFFF9100) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

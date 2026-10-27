package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChapterChain
import com.example.data.model.ChapterItem

/**
 * Overall summary progress header for Chapter Completion Lists using Material 3
 * CircularProgressIndicator and LinearProgressIndicator to show completed vs. remaining tasks.
 */
@Composable
fun ChapterListOverallProgressSummary(
    chapters: List<ChapterItem>,
    getChain: (Int) -> ChapterChain,
    modifier: Modifier = Modifier
) {
    val totalChapters = chapters.size
    val completedChapters = chapters.count { getChain(it.id).isComplete() }
    val inProgressChapters = chapters.count {
        val c = getChain(it.id)
        c.completedCount() > 0 && !c.isComplete()
    }
    val totalTasks = totalChapters * 10
    val completedTasks = chapters.sumOf { getChain(it.id).completedCount() }
    val remainingTasks = (totalTasks - completedTasks).coerceAtLeast(0)
    val taskFraction = if (totalTasks > 0) {
        (completedTasks.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedFraction by animateFloatAsState(
        targetValue = taskFraction,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "overallChapterTaskProgress"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("chapter_list_overall_progress")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
                        CircularProgressIndicator(
                            progress = { animatedFraction.coerceAtLeast(0.06f) },
                            modifier = Modifier.size(50.dp),
                            strokeWidth = 5.dp,
                            color = Color(0xFF0070F3),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "${(taskFraction * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Chapter Task Completion Tracker",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedTasks/$totalTasks tasks done • $remainingTasks tasks remaining",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "$completedChapters Mastered • $inProgressChapters In Progress • ${totalChapters - completedChapters - inProgressChapters} Not Started",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF0070F3),
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

/**
 * Per-chapter visual progress tracking component using Material 3 LinearProgressIndicator
 * and CircularProgressIndicator to clearly display completed vs. remaining tasks per chapter.
 */
@Composable
fun ChapterTaskProgressComponent(
    chapterId: Int,
    chain: ChapterChain,
    onQuickCompleteTask: ((taskKey: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalTasks = chain.totalTasksCount()
    val completedTasks = chain.completedCount()
    val remainingTasks = chain.remainingCount()
    val progressFraction = chain.progressFraction()
    val progressPct = (progressFraction * 100).toInt()
    val remainingNames = chain.remainingTaskNames()

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "chapterProgress_$chapterId"
    )

    val statusColor = when {
        remainingTasks == 0 -> Color(0xFF10B981) // Emerald - All tasks complete
        completedTasks >= 6 -> Color(0xFF0070F3) // Electric Blue - Over halfway
        completedTasks >= 1 -> Color(0xFFF59E0B) // Amber - Started
        else -> Color(0xFF64748B) // Slate - Not started
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = statusColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.30f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("chapter_progress_component_$chapterId")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                        CircularProgressIndicator(
                            progress = { animatedProgress.coerceAtLeast(0.05f) },
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.5.dp,
                            color = statusColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "$progressPct%",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "$completedTasks of $totalTasks Tasks Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (remainingTasks == 0) {
                                "All chapter milestones verified & complete"
                            } else {
                                "$remainingTasks task${if (remainingTasks == 1) "" else "s"} left to complete chapter"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Remaining Tasks Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                    modifier = Modifier.testTag("chapter_remaining_tasks_badge_$chapterId")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (remainingTasks == 0) Icons.Default.DoneAll else Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (remainingTasks == 0) "0 Remaining ✓" else "$remainingTasks Remaining",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            // Material 3 LinearProgressIndicator for Chapter Tasks
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("chapter_linear_progress_$chapterId"),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // Remaining Tasks Interactive Strip
            if (remainingNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remaining:",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    remainingNames.forEach { taskLabel ->
                        val taskKey = taskLabelToKey(taskLabel)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                            modifier = if (onQuickCompleteTask != null) {
                                Modifier.clickable { onQuickCompleteTask(taskKey) }
                            } else Modifier
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = taskLabel,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Chapter Completion Progress List Card for the Main Dashboard using Material 3
 * progress indicators to clearly show remaining tasks per chapter.
 */
@Composable
fun DashboardChapterProgressListCard(
    chapters: List<ChapterItem>,
    version: Int,
    getChain: (Int) -> ChapterChain,
    onToggleChapterTask: (chapterId: Int, taskKey: String, currentChecked: Boolean) -> Unit,
    onOpenFullChapterTracker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayChapters = chapters.take(4)
    val totalChapters = chapters.size
    val completedChapters = chapters.count { getChain(it.id).isComplete() }
    val totalRemainingTasks = chapters.sumOf { getChain(it.id).remainingCount() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_chapter_progress_list_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.14f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AssignmentLate,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Chapter Completion & Remaining Tasks",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedChapters/$totalChapters chapters done • $totalRemainingTasks total tasks remaining",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFF0070F3).copy(alpha = 0.14f),
                    modifier = Modifier.clickable { onOpenFullChapterTracker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All $totalChapters",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayChapters.forEach { chapter ->
                    val chain = getChain(chapter.id)
                    val completed = chain.completedCount()
                    val remaining = chain.remainingCount()
                    val fraction = chain.progressFraction()
                    val remainingList = chain.remainingTaskNames()
                    val nextTaskLabel = remainingList.firstOrNull()

                    val barColor = when {
                        remaining == 0 -> Color(0xFF10B981)
                        completed >= 6 -> Color(0xFF0070F3)
                        else -> Color(0xFFF59E0B)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                        border = BorderStroke(1.dp, barColor.copy(alpha = 0.28f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFullChapterTracker() }
                    ) {
                        Column(modifier = Modifier.padding(11.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(28.dp)) {
                                        CircularProgressIndicator(
                                            progress = { fraction.coerceAtLeast(0.06f) },
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp,
                                            color = barColor,
                                            trackColor = MaterialTheme.colorScheme.surface,
                                            strokeCap = StrokeCap.Round
                                        )
                                        Text(
                                            text = "${(fraction * 100).toInt()}%",
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${chapter.subject} • $completed/10 tasks done",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = barColor.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = if (remaining == 0) "Completed ✓" else "$remaining Tasks Left",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = barColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surface,
                                strokeCap = StrokeCap.Round
                            )

                            if (nextTaskLabel != null) {
                                Spacer(modifier = Modifier.height(7.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pending: ${remainingList.take(3).joinToString(" • ")}${if (remainingList.size > 3) " +${remainingList.size - 3}" else ""}",
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0070F3).copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable {
                                            val key = taskLabelToKey(nextTaskLabel)
                                            onToggleChapterTask(chapter.id, key, false)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Done $nextTaskLabel",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF38BDF8)
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

private fun taskLabelToKey(label: String): String = when (label) {
    "Lecture" -> "lecture"
    "Notes" -> "notes"
    "DPP" -> "dpp"
    "12th NCERT" -> "ncert"
    "Prarambh" -> "prarambh"
    "Prabal" -> "prabal"
    "Prakshit" -> "prakshit"
    "PYQ" -> "pyq"
    "Revision" -> "revision"
    "Test" -> "test"
    else -> "lecture"
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BatchChapterPlan
import com.example.data.model.BatchCompletionStats
import com.example.data.model.BatchCurriculum
import com.example.ui.viewmodel.JeeViewModel

@Composable
fun BatchCurriculumProgressBar(
    viewModel: JeeViewModel,
    modifier: Modifier = Modifier
) {
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val stats by viewModel.batchCompletionStats.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    var selectedSubjectFilter by remember { mutableStateOf("All") }

    val progressFraction = if (stats.totalChapters > 0) {
        (stats.completedChapters.toFloat() / stats.totalChapters).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "batch_progress_anim"
    )

    GlassmorphicContainer(
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_curriculum_progress_card"),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        rimHighlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        rimShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        glowBorder = Color(0xFF0071E3).copy(alpha = 0.15f),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // Header: Title and Milestone Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF6574FF).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = selectedBatch.examType.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF5266CF),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CURRICULUM PROGRESS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedBatch.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Percentage Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when {
                        stats.overallPercentage == 100 -> Color(0xFF00C853)
                        stats.overallPercentage >= 50 -> Color(0xFF6574FF)
                        else -> Color(0xFF5266CF)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (stats.overallPercentage == 100) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${stats.overallPercentage}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Animated Gradient Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val gradientBrush = Brush.horizontalGradient(
                    colors = if (stats.overallPercentage == 100) {
                        listOf(Color(0xFF00C853), Color(0xFF69F0AE))
                    } else {
                        listOf(Color(0xFF5266CF), Color(0xFF6574FF), Color(0xFF00E5FF))
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(gradientBrush)
                        .testTag("batch_progress_fill_bar")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row: Chapters Done, Lectures Done, Milestone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF6574FF),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${stats.completedChapters} of ${stats.totalChapters} Chapters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${stats.completedLectures} / ${stats.totalLectures} Lectures",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    val statusText = when {
                        stats.overallPercentage == 100 -> "Mastered"
                        stats.overallPercentage >= 75 -> "Final Sprint"
                        stats.overallPercentage >= 50 -> "Dominating"
                        stats.overallPercentage >= 25 -> "Building Momentum"
                        stats.overallPercentage > 0 -> "In Progress"
                        else -> "Not Started"
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (stats.overallPercentage == 100) Color(0xFF00C853) else Color(0xFF5266CF),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subject-wise Mini Progress Bars
            Text(
                text = "Subject Completion Breakdown",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.subjectProgress.forEach { subj ->
                    val subjColor = when (subj.subject.lowercase()) {
                        "physics" -> Color(0xFF5266CF)
                        "chemistry" -> Color(0xFF00B0FF)
                        "mathematics", "maths" -> Color(0xFF00C853)
                        "biology", "botany", "zoology" -> Color(0xFF2E7D32)
                        else -> Color(0xFFFF9100)
                    }

                    val subjProgress = if (subj.totalChapters > 0) {
                        (subj.completedChapters.toFloat() / subj.totalChapters).coerceIn(0f, 1f)
                    } else 0f

                    val animSubjProgress by animateFloatAsState(
                        targetValue = subjProgress,
                        animationSpec = tween(500),
                        label = "subj_progress_${subj.subject}"
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (subj.subject.lowercase()) {
                                "physics" -> Icons.Default.ElectricBolt
                                "chemistry" -> Icons.Default.Science
                                "mathematics", "maths" -> Icons.Default.Calculate
                                "biology", "botany", "zoology" -> Icons.Default.MenuBook
                                else -> Icons.Default.AutoAwesome
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = subjColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subj.subject,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(min = 70.dp, max = 96.dp)
                            )

                            // Progress Bar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animSubjProgress)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(subjColor)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${subj.completedChapters}/${subj.totalChapters} (${subj.percentage}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Expandable Toggle Button to View & Mark Chapters Complete
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF6574FF).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFF6574FF).copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .testTag("toggle_batch_chapters_expand")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF5266CF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExpanded) "Hide Chapters Checklist" else "Mark Chapters Complete (${stats.completedChapters}/${stats.totalChapters})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5266CF)
                        )
                    }

                    Text(
                        text = if (isExpanded) "Close" else "Tap to Check",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6574FF)
                    )
                }
            }

            // Expanded Chapter Checklist with instant checkboxes
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // Subject Filter Chips
                    val filterSubjects = listOf("All") + selectedBatch.chapters.map { it.subject }.distinct()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filterSubjects.forEach { subj ->
                            val isFilterSelected = (selectedSubjectFilter == subj)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isFilterSelected) Color(0xFF6574FF) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedSubjectFilter = subj }
                            ) {
                                Text(
                                    text = subj,
                                    fontSize = 11.sp,
                                    fontWeight = if (isFilterSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isFilterSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    val filteredChapters = selectedBatch.chapters.filter {
                        selectedSubjectFilter == "All" || it.subject.equals(selectedSubjectFilter, ignoreCase = true)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredChapters.forEachIndexed { index, chapter ->
                            val isCompleted = viewModel.isBatchChapterComplete(chapter.chapterName)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCompleted) {
                                    Color(0xFFE8F5E9).copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCompleted) Color(0xFFA5D6A7) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleBatchChapterComplete(chapter.chapterName) }
                                    .testTag("batch_chapter_item_$index")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isCompleted,
                                        onCheckedChange = {
                                            viewModel.toggleBatchChapterComplete(chapter.chapterName)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF00C853),
                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.size(24.dp).testTag("checkbox_${chapter.chapterName.hashCode()}")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chapter.chapterName,
                                            fontSize = 13.sp,
                                            fontWeight = if (isCompleted) FontWeight.Medium else FontWeight.SemiBold,
                                            color = if (isCompleted) {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = chapter.subject,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5266CF)
                                            )
                                            Text(
                                                text = "• ${chapter.totalLectures} L / ${chapter.estimatedHours}h",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (chapter.priority.isNotBlank()) {
                                                Text(
                                                    text = "• ${chapter.priority}",
                                                    fontSize = 10.sp,
                                                    color = if (chapter.priority.contains("High", ignoreCase = true)) Color(0xFFD32F2F) else Color(0xFF1976D2),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    if (isCompleted) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF2E7D32)
                                        ) {
                                            Text(
                                                text = "DONE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
